package com.renyu.blelibrary.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.HexUtil;
import com.renyu.iitebletest.jniLibs.JNIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by renyu on 2017/1/12.
 */

public class BLEFramework {

    private static BLEFramework bleFramework;

    // 设备连接断开
    public static final int STATE_DISCONNECTED = 0;
    // 设备正在扫描
    public static final int STATE_SCANNING = 1;
    // 设备扫描结束
    public static final int STATE_SCANNED = 2;
    // 设备正在连接
    public static final int STATE_CONNECTING = 3;
    // 设备连接成功
    public static final int STATE_CONNECTED = 4;
    // 设备配置服务成功
    public static final int STATE_SERVICES_DISCOVERED = 5;
    // 当前设备状态
    private int connectionState=STATE_DISCONNECTED;

    // 搜索到的设备
    private ArrayList<BLEDevice> devices;
    // 临时搜索设备
    private HashMap<String, BLEDevice> tempsDevices;
    // 搜索所需时间
    private int timeSeconds=10000;
    // 搜索Handler
    private Handler handlerScan;

    // 数据发送队列
    private static RequestQueue requestQueue;

    private Context context;
    private BluetoothManager manager;
    private BluetoothAdapter adapter;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    private BluetoothGattCallback bleGattCallback;
    private BluetoothGatt gatt;

    private BLEConnectListener bleConnectListener;
    private BLEStateChangeListener bleStateChangeListener;

    public static BLEFramework getBleFrameworkInstance(Context context) {
        if (bleFramework==null) {
            synchronized (BLEFramework.class) {
                if (bleFramework==null) {
                    bleFramework=new BLEFramework(context.getApplicationContext());
                    requestQueue=RequestQueue.getQueueInstance(context.getApplicationContext(), bleFramework);
                }
            }
        }
        return bleFramework;
    }

    public BLEFramework(Context context) {
        this.context=context;
        manager= (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter= manager.getAdapter();
        devices=new ArrayList<>();
        tempsDevices=new HashMap<>();
        handlerScan=new Handler(Looper.getMainLooper());
        leScanCallback=new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (device!=null) {
                    if (!tempsDevices.containsKey(device.getAddress())) {
                        Log.d("onLeScan", device.getAddress());
                        Log.d("onLeScan", HexUtil.encodeHexStr(scanRecord));
                        BLEDevice device1=new BLEDevice();
                        device1.setDevice(device);
                        device1.setScanRecord(scanRecord);
                        tempsDevices.put(device.getAddress(), device1);
                        devices.add(device1);
                    }
                }
            }
        };
        bleGattCallback=new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        setConnectionState(STATE_CONNECTED);
                        // 开始搜索服务
                        gatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        setConnectionState(STATE_DISCONNECTED);
                        gatt.close();
                        BLEFramework.this.gatt=null;
                        devices.clear();
                        tempsDevices.clear();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BLEFramework.this.gatt=gatt;
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    if (gatt.getService(CommonParams.UUID_SERVICE)!=null) {
                        BluetoothGattCharacteristic characteristic = gatt.getService(CommonParams.UUID_SERVICE).getCharacteristic(CommonParams.UUID_Characteristic);
                        if (enableNotification(characteristic, gatt, CommonParams.UUID_DESCRIPTOR)) {
                            setConnectionState(STATE_SERVICES_DISCOVERED);
                            return;
                        }
                    }
                }
                disConnect();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status==BluetoothGatt.GATT_SUCCESS) {
                    requestQueue.release();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                Log.d("BLEService", "收到指令"+characteristic.getValue()[0]+" "+characteristic.getValue()[1]+" "+characteristic.getValue()[2]);
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }
        };
    }

    private void disConnect() {
        if (gatt!=null)
        gatt.disconnect();
    }

    private boolean enableNotification(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt, UUID uuid) {
        boolean success = gatt.setCharacteristicNotification(characteristic, true);
        if(!success) {
            return false;
        }
        if (characteristic.getDescriptors().size()>0) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
            if(descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                return true;
            }
        }
        return false;
    }

    public void setBleConnectListener(BLEConnectListener bleConnectListener) {
        this.bleConnectListener = bleConnectListener;
    }

    public void setBleStateChangeListener(BLEStateChangeListener bleStateChangeListener) {
        this.bleStateChangeListener = bleStateChangeListener;
    }

    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds=timeSeconds;
    }

    /**
     * 开始扫描
     * @return
     */
    public void startScan() {
        boolean success=adapter.startLeScan(leScanCallback);
        if (success) {
            // 开始搜索
            setConnectionState(STATE_SCANNING);
            handlerScan.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, timeSeconds);
        }
        else {
            setConnectionState(STATE_DISCONNECTED);
        }
    }

    /**
     * 结束扫描
     */
    public void stopScan() {
        adapter.stopLeScan(leScanCallback);
        handlerScan.removeCallbacksAndMessages(null);
        // 搜索完毕
        setConnectionState(STATE_SCANNED);
        if (bleConnectListener!=null) {
            bleConnectListener.getAllScanDevice(devices);
        }
    }

    /**
     * 开始连接
     * @param device
     */
    public void startConn(BluetoothDevice device) {
        // 开始连接
        setConnectionState(STATE_CONNECTING);
        device.connectGatt(context, false, bleGattCallback);
    }

    /**
     * 获取当前状态
     * @return
     */
    public int getConnectionState() {
        return connectionState;
    }

    /**
     * 设置当前状态
     * @param state
     */
    private void setConnectionState(int state) {
        connectionState=state;
        if (bleStateChangeListener!=null) {
            bleStateChangeListener.getCurrentState(state);
        }
    }

    /**
     * 获取所有扫描后的设备
     * @return
     */
    public ArrayList<BLEDevice> getAllScanDevice() {
        return devices;
    }

    /**
     * 发送数据
     * @param value
     */
    protected void writeCharacteristic(byte[] value) {
        writeCharacteristic(CommonParams.UUID_Characteristic, value);
    }

    /**
     * 发送数据
     * @param uuid
     * @param value
     */
    protected void writeCharacteristic(UUID uuid, byte[] value) {
        if (gatt!=null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(CommonParams.UUID_SERVICE).getCharacteristic(uuid);
            if (characteristic==null) {
                Log.d("BLEFramework", "writeCharacteristic中uuid不存在");
                return;
            }
            characteristic.setValue(value);
            if (!gatt.writeCharacteristic(characteristic)) {
                Log.d("BLEFramework", "writeCharacteristic失败");
            }
            else {
                Log.d("BLEFramework", "writeCharacteristic成功");
            }
        }
    }

    /**
     * 主动读数据
     * @param serviceUUID
     * @param CharacUUID
     */
    protected void readCharacteristic(UUID serviceUUID, UUID CharacUUID) {
        if (gatt!=null) {
            BluetoothGattCharacteristic characteristic = gatt.getService(serviceUUID).getCharacteristic(CharacUUID);
            if (characteristic==null) {
                Log.d("BLEFramework", "readCharacteristic中uuid不存在");
                return;
            }
            if (!gatt.readCharacteristic(characteristic)) {
                Log.d("BLEFramework", "readCharacteristic失败");
            }
            else {
                Log.d("BLEFramework", "readCharacteristic成功");
            }
        }
    }

    private void addCommand(byte command, byte[] info, int currentPackageSeq, int totalPackageNum) {
        JNIUtils jniUtils=new JNIUtils();
        int payloadLength=info.length;
        byte[] password=jniUtils.sendencode(info, payloadLength);
        byte[] sendValue=new byte[4+payloadLength+4];
        sendValue[0]= (byte) currentPackageSeq;
        sendValue[1]= (byte) totalPackageNum;
        sendValue[2]= command;
        sendValue[3]= (byte) payloadLength;
        for (int i=0;i<payloadLength;i++) {
            sendValue[4+i]=password[i];
        }
        for (int i=0;i<4;i++) {
            sendValue[4+payloadLength+i]=password[16+i];
        }
        requestQueue.add(sendValue);
    }

    /**
     * 添加命令
     * @param command
     * @param info
     */
    public void addCommand(byte command, byte[] info) {
        if (info.length>12) {
            byte[] info1=new byte[12];
            for (int i = 0; i < 12; i++) {
                info1[i]=info[i];
            }
            addCommand(command, info1, 1, 2);
            byte[] info2=new byte[info.length-12];
            for (int i=0;i<info.length-12;i++) {
                info2[i]=info[12+i];
            }
            addCommand(command, info2, 2, 2);
        }
        else {
            addCommand(command, info, 1, 1);
        }
    }
}
