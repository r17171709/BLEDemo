package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.renyu.bledemo.R;
import com.renyu.bledemo.params.AddQRequestBean;
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
import com.renyu.blelibrary.utils.HexUtil;
import com.renyu.qrcodelibrary.ZBarQRScanActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by renyu on 2017/2/16.
 */

public class QActivity extends AppCompatActivity {

    BLEFramework bleFramework;

    @BindView(R.id.q_ble_state)
    TextView q_ble_state;
    @BindView(R.id.et_deviation)
    EditText et_deviation;
    @BindView(R.id.et_current)
    EditText et_current;
    @BindView(R.id.q_button_get_device_currentresult)
    TextView q_button_get_device_currentresult;
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
                    Toast.makeText(QActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                }
                q_ble_state.setText("BLE状态：连接成功");
            }
            else if (msg.what==BLEFramework.STATE_DISCONNECTED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(QActivity.this, "连接断开", Toast.LENGTH_SHORT).show();
                }
                q_ble_state.setText("BLE状态：连接断开");
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
                Toast.makeText(QActivity.this, "指令执行出错", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what==com.renyu.bledemo.params.CommonParams.GET_DEVICE_CURRENT_RESP) {
                int tempInt=Integer.parseInt(msg.obj.toString());
                double et_current_num= TextUtils.isEmpty(et_current.getText().toString())?
                        0.45:Double.parseDouble(et_current.getText().toString());
                double et_deviation_num= TextUtils.isEmpty(et_deviation.getText().toString())?
                        15:Double.parseDouble(et_deviation.getText().toString());

                double low=et_current_num*10*(100-et_deviation_num);
                double max=et_current_num*10*(100+et_deviation_num);
                if (tempInt>low && tempInt<max) {
                    q_button_get_device_currentresult.setText("Pass，电流值"+tempInt);
                    Toast.makeText(QActivity.this, "Read Current Raw value成功", Toast.LENGTH_SHORT).show();
                    save(-1);
                }
                else {
                    q_button_get_device_currentresult.setText("Fail，电流值"+tempInt);
                    Toast.makeText(QActivity.this, "Read Current Raw value失败", Toast.LENGTH_SHORT).show();
                    save(1);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q);
        ButterKnife.bind(this);

        bleFramework=BLEFramework.getBleFrameworkInstance(this,
                com.renyu.bledemo.params.CommonParams.UUID_SERVICE,
                com.renyu.bledemo.params.CommonParams.UUID_Characteristic,
                com.renyu.bledemo.params.CommonParams.UUID_DESCRIPTOR);
        bleFramework.setTimeSeconds(15000);
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
                    byte[] response= DataUtils.decodeResult(value);
                    message.what=response[0]&0xff;
                    if ((response[0]&0xff) == com.renyu.bledemo.params.CommonParams.GET_DEVICE_CURRENT_RESP) {
                        byte[] temp=new byte[2];
                        temp[0]=response[1];
                        temp[1]=response[2];
                        int tempInt=HexUtil.byte2ToInt(temp);
                        message.obj=tempInt;
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

        Intent intent=new Intent(QActivity.this, DeviceListActivity.class);
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
            ACache.get(this).put("sn", data.getStringExtra("sn"));
            bleFramework.startConn(bleDevice);
            progressDialog= ProgressDialog.show(this, "提示", "正在连接");
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

    @OnClick({R.id.q_button_scan, R.id.q_button_get_device_current, R.id.button_upload})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.q_button_scan:
                startActivityForResult(new Intent(QActivity.this, ZBarQRScanActivity.class), com.renyu.bledemo.params.CommonParams.QRCODESCAN);
                break;
            case R.id.q_button_get_device_current:
                DataUtils.getDeviceCurrentReq(bleFramework);
                break;
            case R.id.button_upload:
                if (ACache.get(QActivity.this).getAsString("sn")!=null &&
                        q_ble_state.getText().toString().equals("BLE状态：连接断开")) {
                    q_button_get_device_currentresult.setText("测试结果");
                    save(2);
                }
                break;
        }
    }

    private void save(int errorCode) {
        AddQRequestBean bean=new AddQRequestBean();
        bean.setSn(ACache.get(QActivity.this).getAsString("sn"));
        if (errorCode==2) {
            bean.setCurrent("BLE状态,无法获取");
            bean.setTestResult("Fail");
        }
        else if (errorCode==1) {
            bean.setCurrent("Fail");
            bean.setTestResult("Fail");
        }
        else if (errorCode==-1) {
            bean.setCurrent("Pass");
            bean.setTestResult("Pass");
        }
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bean.setTestDate(dateFormat.format(new Date()));
        if (ExcelUtils.writeExcelQ(Environment.getExternalStorageDirectory().getPath()+ File.separator+ com.renyu.bledemo.params.CommonParams.WRITEFILE_Q, bean)) {
            Toast.makeText(QActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
            ACache.get(this).clear();
            bleFramework.disConnect();
        }
        else {
            Toast.makeText(QActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACache.get(this).clear();
        bleFramework.disConnect();
    }
}
