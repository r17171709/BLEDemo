package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.renyu.bledemo.R;
import com.renyu.bledemo.params.AddRequestBean;
import com.renyu.bledemo.utils.DataUtils;
import com.renyu.bledemo.utils.ExcelUtils;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.ble.BLEFramework;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEResponseListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.ACache;
import com.renyu.blelibrary.utils.BLEUtils;
import com.renyu.iitebletest.jniLibs.JNIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    BLEFramework bleFramework;
    JNIUtils jniUtils;

    ProgressDialog progressDialog;

    Handler handler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==com.renyu.blelibrary.ble.BLEFramework.STATE_SERVICES_DISCOVERED
                    || msg.what==com.renyu.blelibrary.ble.BLEFramework.STATE_SERVICES_OTA_DISCOVERED) {
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
            else if (msg.what==com.renyu.blelibrary.ble.BLEFramework.STATE_DISCONNECTED) {
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        jniUtils=new JNIUtils();

        bleFramework=BLEFramework.getBleFrameworkInstance(this,
                com.renyu.bledemo.params.CommonParams.UUID_SERVICE,
                com.renyu.bledemo.params.CommonParams.UUID_Characteristic,
                com.renyu.bledemo.params.CommonParams.UUID_DESCRIPTOR);
        bleFramework.setTimeSeconds(10000);
        bleFramework.setBleConnectListener(new BLEConnectListener() {
            @Override
            public void getAllScanDevice(ArrayList<BLEDevice> devices) {
                Log.d("MainActivity", "devices.size():" + devices.size());
                for (BLEDevice device : devices) {
                    byte[] scanRecord=device.getScanRecord();
                    int a=(int) scanRecord[5]&0xff;
                    int b=(int) scanRecord[6]&0xff;
                    if (a==0xaa && b==0xfe) {
                        EventBus.getDefault().post(device);
                    }
                }
            }
        });
        bleFramework.setBleStateChangeListener(new BLEStateChangeListener() {
            @Override
            public void getCurrentState(int currentState) {
                Message message=new Message();
                message.what=currentState;
                handler.sendMessage(message);
            }
        });
        bleFramework.setBleResponseListener(new BLEResponseListener() {
            @Override
            public void getResponseValues(byte[] value) {
                Log.d("BLEService", "收到指令"+value[0]+" "+value[1]+" "+value[2]);
                int result=(int) value[2]&0xff;
                if (result!=com.renyu.bledemo.params.CommonParams.ERROR_RESP) {
                    byte[] response=DataUtils.decodeResult(value);
                }
                else {
                    Log.d("MainActivity", "指令出错");
                }
            }
        });
    }

    /**
     * 打开蓝牙开关
     */
    public void openBlueTooth() {
        if (BLEUtils.checkBluetoothAvaliable(this)) {
            if (BLEUtils.checkBluetoothOpen(this)) {
                getDevices();
            }
            else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, CommonParams.RESULT_ENABLE_BT);
            }
        }
        else {
            Toast.makeText(this, "该设备不支持BLE功能，无法使用BLE功能", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDevices() {
        bleFramework.startScan();

        startActivityForResult(new Intent(MainActivity.this, DeviceListActivity.class), com.renyu.bledemo.params.CommonParams.SCANDEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CommonParams.RESULT_ENABLE_BT && resultCode==RESULT_OK) {
            getDevices();
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_OK) {
            BluetoothDevice bleDevice=data.getParcelableExtra("device");
            // 存储SN
            ACache.get(MainActivity.this).put("sn", data.getStringExtra("sn"));
            // rssi
            ACache.get(MainActivity.this).put("rssi", ""+data.getIntExtra("rssi", 0));
            bleFramework.startConn(bleDevice);
            progressDialog= ProgressDialog.show(MainActivity.this, "提示", "正在连接");
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_CANCELED) {
            bleFramework.cancelScan();
        }
    }

    @OnClick({R.id.button_record_packet_number, R.id.button_start_search, R.id.button_save_to_excel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_record_packet_number:
//                DataUtils.setSNReq(bleFramework, ACache.get(MainActivity.this).getAsString("sn"));
                DataUtils.setMagicReq(bleFramework, (byte) 0x66);
//                DataUtils.readMagicReq(bleFramework);
//                DataUtils.readSNReq(bleFramework);
//                DataUtils.enterOta(bleFramework);
//                bleFramework.startOTA();
                break;
            case R.id.button_start_search:
                openBlueTooth();
                break;
            case R.id.button_save_to_excel:
                List<AddRequestBean> beanList=new ArrayList<>();
                for (int i=0;i<10;i++) {
                    AddRequestBean bean=new AddRequestBean();
                    bean.setSn("123");
                    bean.setTestResult("OK");
                    bean.setTestDate("2017.1.1");
                    beanList.add(bean);
                }
                ExcelUtils.writeExcel(Environment.getExternalStorageDirectory().getPath(), beanList);
                break;
        }
    }
}
