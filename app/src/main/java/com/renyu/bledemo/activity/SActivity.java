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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.renyu.bledemo.R;
import com.renyu.bledemo.params.AddRequestBean;
import com.renyu.bledemo.utils.DataUtils;
import com.renyu.bledemo.utils.ExcelUtils;
import com.renyu.bledemo.utils.ReamlUtils;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.ble.BLEFramework;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEResponseListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.ACache;
import com.renyu.blelibrary.utils.BLEUtils;
import com.renyu.qrcodelibrary.ZBarQRScanActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SActivity extends AppCompatActivity {

    BLEFramework bleFramework;

    @BindView(R.id.ble_state)
    TextView ble_state;
    @BindView(R.id.edit_machineid)
    EditText edit_machineid;
    @BindView(R.id.search_deviceid_result)
    TextView search_deviceid_result;
    ProgressDialog progressDialog;

    String scanTarget;

    Handler handlerConnState=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==BLEFramework.STATE_SERVICES_DISCOVERED
                    || msg.what==BLEFramework.STATE_SERVICES_OTA_DISCOVERED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(SActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                }
                ble_state.setText("BLE状态：连接成功");
            }
            else if (msg.what==BLEFramework.STATE_DISCONNECTED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(SActivity.this, "连接断开", Toast.LENGTH_SHORT).show();
                }
                ACache.get(SActivity.this).clear();
                ble_state.setText("BLE状态：连接断开");
                edit_machineid.setText("");
                search_deviceid_result.setText("查询结果：暂无");
            }
            else if (msg.what== BLEFramework.STATE_SCANNED) {
                EventBus.getDefault().post(""+BLEFramework.STATE_SCANNED);
            }
        }
    };

    Handler handlerCallbackValue=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==com.renyu.bledemo.params.CommonParams.ERROR_RESP) {
                Toast.makeText(SActivity.this, "指令执行出错", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what==com.renyu.bledemo.params.CommonParams.SET_DEVICEID_RESP) {
                if (msg.arg1==1) {
                    Toast.makeText(SActivity.this, "deviceId写入成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SActivity.this, "deviceId写入失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s);
        ButterKnife.bind(this);

        bleFramework=BLEFramework.getBleFrameworkInstance(this,
                com.renyu.bledemo.params.CommonParams.UUID_SERVICE,
                com.renyu.bledemo.params.CommonParams.UUID_Characteristic,
                com.renyu.bledemo.params.CommonParams.UUID_DESCRIPTOR);
        bleFramework.setTimeSeconds(10000);
        bleFramework.setBleConnectListener(new BLEConnectListener() {
            @Override
            public void getAllScanDevice(ArrayList<BLEDevice> devices) {
                Log.d("SActivity", "devices.size():" + devices.size());
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
                handlerConnState.sendMessage(message);
            }
        });
        bleFramework.setBleResponseListener(new BLEResponseListener() {
            @Override
            public void getResponseValues(byte[] value) {
                int result=value[2]&0xff;
                Message message=new Message();
                if (result!=com.renyu.bledemo.params.CommonParams.ERROR_RESP) {
                    byte[] response=DataUtils.decodeResult(value);
                    message.what=response[0]&0xff;
                    if ((response[0]&0xff) == com.renyu.bledemo.params.CommonParams.SET_DEVICEID_RESP) {
                        if ((int) response[2]==1) {
                            ACache.get(SActivity.this).put("ble_check", "Pass");
                            message.arg1=1;
                        }
                        else {
                            ACache.get(SActivity.this).put("ble_check", "Fail");
                            message.arg1=-1;
                        }
                    }
                    handlerCallbackValue.sendMessage(message);
                }
                else {
                    Log.d("SActivity", "指令出错");
                    handlerCallbackValue.sendEmptyMessage(com.renyu.bledemo.params.CommonParams.ERROR_RESP);
                }
            }
        });
    }

    /**
     * 打开蓝牙开关
     */
    public void openBlueTooth(String scanTarget) {
        if (BLEUtils.checkBluetoothAvaliable(this)) {
            if (BLEUtils.checkBluetoothOpen(this)) {
                getDevices(scanTarget);
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

    private void getDevices(String scanTarget) {
        bleFramework.startScan();

        Intent intent=new Intent(SActivity.this, DeviceListActivity.class);
        if (scanTarget!=null) {
            intent.putExtra("scanTarget", scanTarget);
        }
        startActivityForResult(intent, com.renyu.bledemo.params.CommonParams.SCANDEVICE);

        this.scanTarget=null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CommonParams.RESULT_ENABLE_BT && resultCode==RESULT_OK) {
            getDevices(scanTarget);
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_OK) {
            BluetoothDevice bleDevice=data.getParcelableExtra("device");
            // 存储SN
            ACache.get(SActivity.this).put("sn", data.getStringExtra("sn"));
            bleFramework.startConn(bleDevice);
            progressDialog= ProgressDialog.show(SActivity.this, "提示", "正在连接");
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_CANCELED) {
            bleFramework.cancelScan();
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.QRCODESCAN && resultCode==RESULT_OK) {
            String result=data.getStringExtra("result");
            scanTarget=result;
            openBlueTooth(result);
        }
    }

    @OnClick({R.id.button_setdeviceid, R.id.button_start_search,
            R.id.read_from_excel, R.id.button_save_to_excel, R.id.button_qrcode_scan,
            R.id.search_deviceid})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_setdeviceid:
//                DataUtils.setSNReq(bleFramework, ACache.get(SActivity.this).getAsString("sn"));
//                DataUtils.setMagicReq(bleFramework, (byte) 0x66);
//                DataUtils.readMagicReq(bleFramework);
//                DataUtils.readSNReq(bleFramework);
//                DataUtils.enterOta(bleFramework);
//                bleFramework.startOTA();
                if (!ble_state.getText().toString().equals("BLE状态：连接断开")) {
                    DataUtils.setDeviceId(bleFramework, ACache.get(SActivity.this).getAsString("deviceId"));
                }
                else {
                    Toast.makeText(this, "BLE连接断开，暂时无法发送", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_start_search:
//                openBlueTooth(null);
                break;
            case R.id.button_save_to_excel:
                if (ACache.get(SActivity.this).getAsString("sn")==null ||
                        ACache.get(SActivity.this).getAsString("deviceId")==null ||
                        ACache.get(SActivity.this).getAsString("ble_check")==null) {
                    Toast.makeText(this, "暂无sn与deviceId信息，不能保存", Toast.LENGTH_SHORT).show();
                }
                else {
                    AddRequestBean bean=new AddRequestBean();
                    bean.setSn(ACache.get(SActivity.this).getAsString("sn"));
                    bean.setDeviceID(ACache.get(SActivity.this).getAsString("deviceId"));
                    bean.setTestResult(ACache.get(SActivity.this).getAsString("ble_check"));
                    SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    bean.setTestDate(dateFormat.format(new Date()));
                    if (ExcelUtils.writeSExcel(Environment.getExternalStorageDirectory().getPath()+ File.separator+ com.renyu.bledemo.params.CommonParams.WRITEFILE_S, bean)) {
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                        ACache.get(SActivity.this).clear();
                        bleFramework.disConnect();
                        edit_machineid.setText("");
                        search_deviceid_result.setText("查询结果：暂无");
                    }
                    else {
                        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.read_from_excel:
                progressDialog= ProgressDialog.show(SActivity.this, "提示", "正在导入");
                ArrayList<AddRequestBean> beans=ExcelUtils.readExcel(Environment.getExternalStorageDirectory().getPath()+ File.separator+ com.renyu.bledemo.params.CommonParams.READFILE_S);
                if (beans.size()==0) {
                    if (progressDialog!=null) {
                        progressDialog.dismiss();
                        progressDialog=null;
                    }
                    Toast.makeText(SActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                }
                else {
                    ReamlUtils.write(this, beans, new ReamlUtils.OnWriteResultListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(SActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                            if (progressDialog!=null) {
                                progressDialog.dismiss();
                                progressDialog=null;
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            Toast.makeText(SActivity.this, "导入失败", Toast.LENGTH_SHORT).show();
                            if (progressDialog!=null) {
                                progressDialog.dismiss();
                                progressDialog=null;
                            }
                        }
                    });
                }
                break;
            case R.id.button_qrcode_scan:
                if (ACache.get(SActivity.this).getAsString("deviceId") == null) {
                    Toast.makeText(this, "请确保deviceId存在", Toast.LENGTH_SHORT).show();
                }
                else {
                    startActivityForResult(new Intent(SActivity.this, ZBarQRScanActivity.class), com.renyu.bledemo.params.CommonParams.QRCODESCAN);
                }
                break;
            case R.id.search_deviceid:
                if (TextUtils.isEmpty(edit_machineid.getText().toString())) {
                    Log.d("SActivity", "请输入机器ID");
                }
                else {
                    ArrayList<AddRequestBean> temps=ReamlUtils.findOne(this, edit_machineid.getText().toString());
                    if (temps.size()==0) {
                        search_deviceid_result.setText("查询失败");
                    }
                    else {
                        search_deviceid_result.setText("查询结果："+temps.get(0).getDeviceID());
                        // 存储deviceId
                        ACache.get(SActivity.this).put("deviceId", temps.get(0).getDeviceID());
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACache.get(SActivity.this).clear();
        bleFramework.disConnect();
    }
}
