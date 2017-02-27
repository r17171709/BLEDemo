package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.utils.ImageUtils;
import com.renyu.bledemo.R;
import com.renyu.bledemo.params.AddBRequestBean;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by renyu on 2017/2/16.
 */

public class BActivity extends AppCompatActivity {

    BLEFramework bleFramework;

    @BindView(R.id.b_ble_state)
    TextView b_ble_state;
    @BindView(R.id.b_ble_rssi)
    TextView b_ble_rssi;
    @BindView(R.id.cb_led)
    CheckBox cb_led;
    @BindView(R.id.cb_buzzer)
    CheckBox cb_buzzer;
    @BindView(R.id.cb_rfid)
    CheckBox cb_rfid;
    @BindView(R.id.cb_current_sensor)
    CheckBox cb_current_sensor;
    @BindView(R.id.iv_qrcode)
    ImageView iv_qrcode;
    ProgressDialog progressDialog;

    Handler handlerConnState=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what== BLEFramework.STATE_SERVICES_DISCOVERED
                    || msg.what==BLEFramework.STATE_SERVICES_OTA_DISCOVERED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(BActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                }
                b_ble_state.setText("BLE状态：连接成功");
            }
            else if (msg.what==BLEFramework.STATE_DISCONNECTED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(BActivity.this, "连接断开", Toast.LENGTH_SHORT).show();
                }
                ACache.get(BActivity.this).clear();
                b_ble_rssi.setText("rssi(正常范围大于-40)：暂无");
                b_ble_state.setText("BLE状态：连接断开");
                cb_buzzer.setChecked(false);
                cb_current_sensor.setChecked(false);
                cb_led.setChecked(false);
                cb_rfid.setChecked(false);
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
                Toast.makeText(BActivity.this, "指令执行出错", Toast.LENGTH_SHORT).show();
            }
            else if (msg.what== com.renyu.bledemo.params.CommonParams.ERROR_RSSI) {
                Toast.makeText(BActivity.this, "rssi不符合测试判断依据", Toast.LENGTH_SHORT).show();
                uploadData(1);
            }
            else if (msg.what==com.renyu.bledemo.params.CommonParams.SET_SN_RESP) {
                if (msg.arg1==1) {
                    Toast.makeText(BActivity.this, "sn写入成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(BActivity.this, "sn写入失败", Toast.LENGTH_SHORT).show();
                    uploadData(2);
                }
            }
            else if (msg.what==com.renyu.bledemo.params.CommonParams.SET_MAGIC_RESP) {
                if (msg.arg1==1) {
                    Toast.makeText(BActivity.this, "magic写入成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(BActivity.this, "magic写入失败", Toast.LENGTH_SHORT).show();
                    uploadData(4);
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);
        ButterKnife.bind(this);

        bleFramework=BLEFramework.getBleFrameworkInstance(this,
                com.renyu.bledemo.params.CommonParams.UUID_SERVICE,
                com.renyu.bledemo.params.CommonParams.UUID_Characteristic,
                com.renyu.bledemo.params.CommonParams.UUID_DESCRIPTOR);
        bleFramework.setTimeSeconds(10000);
        bleFramework.setBleConnectListener(new BLEConnectListener() {
            @Override
            public void getAllScanDevice(ArrayList<BLEDevice> devices) {
                Log.d("BActivity", "devices.size():" + devices.size());
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
                    if ((response[0]&0xff) == com.renyu.bledemo.params.CommonParams.SET_SN_RESP) {
                        if ((int) response[2]==1) {
                            message.arg1=1;
                        }
                        else {
                            message.arg1=-1;
                        }
                    }
                    if ((response[0]&0xff) == com.renyu.bledemo.params.CommonParams.SET_MAGIC_RESP) {
                        if ((int) response[2]==1) {
                            message.arg1=1;
                        }
                        else {
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

        Intent intent=new Intent(BActivity.this, DeviceListActivity.class);
        startActivityForResult(intent, com.renyu.bledemo.params.CommonParams.SCANDEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CommonParams.RESULT_ENABLE_BT && resultCode==RESULT_OK) {
            getDevices();
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_OK) {
            iv_qrcode.setImageBitmap(null);
            BluetoothDevice bleDevice=data.getParcelableExtra("device");
            // 存储SN
            ACache.get(BActivity.this).put("sn", data.getStringExtra("sn"));
            // rssi
            ACache.get(BActivity.this).put("rssi", ""+data.getIntExtra("rssi", 0));
            b_ble_rssi.setText("rssi(正常范围大于-40)："+data.getIntExtra("rssi", 0));
            // rssi满足一定条件才能继续
            if (data.getIntExtra("rssi", 0) < com.renyu.bledemo.params.CommonParams.RSSIWRONG) {
                Message m=new Message();
                m.what=com.renyu.bledemo.params.CommonParams.ERROR_RSSI;
                handlerCallbackValue.sendMessage(m);
            }
            else {
                bleFramework.startConn(bleDevice);
                progressDialog= ProgressDialog.show(BActivity.this, "提示", "正在连接");
            }
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_CANCELED) {
            bleFramework.cancelScan();
        }
    }

    @OnClick({R.id.b_button_scanall, R.id.b_button_sn, R.id.b_button_upload, R.id.b_button_magic})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_button_scanall:
                openBlueTooth();
                break;
            case R.id.b_button_sn:
                if (!b_ble_state.getText().toString().equals("BLE状态：连接断开")) {
                    DataUtils.setSNReq(bleFramework, ACache.get(BActivity.this).getAsString("sn"));
                }
                else {
                    Toast.makeText(this, "BLE连接断开，暂时无法发送", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.b_button_magic:
                if (!b_ble_state.getText().toString().equals("BLE状态：连接断开")) {
                    DataUtils.setMagicReq(bleFramework, (byte) 0x66);
                }
                else {
                    Toast.makeText(this, "BLE连接断开，暂时无法发送", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.b_button_upload:
                if (ACache.get(BActivity.this).getAsString("sn")==null) {
                    Toast.makeText(this, "暂无sn信息，不能保存", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (b_ble_state.getText().toString().equals("BLE状态：连接断开")) {
                        if (b_ble_rssi.getText().toString().equals("rssi(正常范围大于-40)：暂无")) {
                            uploadData(3);
                        }
                        else {
                            uploadData(1);
                        }
                    }
                    else {
                        saveImage();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ACache.get(BActivity.this).clear();
        bleFramework.disConnect();
    }

    public void saveImage() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                File file=new File(Environment.getExternalStorageDirectory().getPath()+File.separator
                        + com.renyu.bledemo.params.CommonParams.BARCODEFILE);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                boolean isOK=ImageUtils.save(QRCodeEncoder.syncEncodeQRCode(ACache.get(BActivity.this).getAsString("sn"), 300),
                        file,
                        Bitmap.CompressFormat.JPEG);
                if (isOK) {
                    subscriber.onNext(file.getPath());
                }
                else {
                    subscriber.onNext("");
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if (o.toString().equals("")) {
                    Toast.makeText(BActivity.this, "生成图片失败", Toast.LENGTH_SHORT).show();
                }
                else {
                    iv_qrcode.setImageBitmap(BitmapFactory.decodeFile(o.toString()));
                    Toast.makeText(BActivity.this, "生成图片成功", Toast.LENGTH_SHORT).show();
                }
                uploadData(-1);
            }
        });
    }

    private void uploadData(int errorCode) {
        AddBRequestBean bean=new AddBRequestBean();
        bean.setSn(ACache.get(BActivity.this).getAsString("sn"));
        bean.setRssi(ACache.get(BActivity.this).getAsString("rssi"));
        if (errorCode==3) {
            bean.setBuzzer("设备无法连接，无法获取该值");
            bean.setCurrent_sensor("设备无法连接，无法获取该值");
            bean.setLed("设备无法连接，无法获取该值");
            bean.setRfid("设备无法连接，无法获取该值");
            bean.setSn_state("设备无法连接，无法获取该值");
            bean.setMagic("设备无法连接，无法获取该值");
            bean.setTestResult("Fail");
        }
        else if (errorCode==2) {
            bean.setBuzzer(cb_buzzer.isChecked()?"Pass":"Fail");
            bean.setCurrent_sensor(cb_current_sensor.isChecked()?"Pass":"Fail");
            bean.setLed(cb_led.isChecked()?"Pass":"Fail");
            bean.setRfid(cb_rfid.isChecked()?"Pass":"Fail");
            bean.setSn_state("SN无法写入");
            bean.setMagic("暂未测试");
            bean.setTestResult("Fail");
        }
        else if (errorCode==4) {
            bean.setBuzzer(cb_buzzer.isChecked()?"Pass":"Fail");
            bean.setCurrent_sensor(cb_current_sensor.isChecked()?"Pass":"Fail");
            bean.setLed(cb_led.isChecked()?"Pass":"Fail");
            bean.setRfid(cb_rfid.isChecked()?"Pass":"Fail");
            bean.setSn_state("暂未测试");
            bean.setMagic("MAGIC无法写入");
            bean.setTestResult("Fail");
        }
        else if (errorCode==1) {
            bean.setBuzzer("rssi不符合测试判断依据，无法获取该值");
            bean.setCurrent_sensor("rssi不符合测试判断依据，无法获取该值");
            bean.setLed("rssi不符合测试判断依据，无法获取该值");
            bean.setRfid("rssi不符合测试判断依据，无法获取该值");
            bean.setSn_state("rssi不符合测试判断依据，无法获取该值");
            bean.setMagic("rssi不符合测试判断依据，无法获取该值");
            bean.setTestResult("Fail");
        }
        else if (errorCode==-1) {
            bean.setBuzzer(cb_buzzer.isChecked()?"Pass":"Fail");
            bean.setCurrent_sensor(cb_current_sensor.isChecked()?"Pass":"Fail");
            bean.setLed(cb_led.isChecked()?"Pass":"Fail");
            bean.setRfid(cb_rfid.isChecked()?"Pass":"Fail");
            bean.setSn_state("SN正常写入");
            bean.setMagic("MAGIC正常写入");
            if (cb_buzzer.isChecked() &&
                    cb_current_sensor.isChecked() &&
                    cb_led.isChecked() &&
                    cb_rfid.isChecked()) {
                bean.setTestResult("Pass");
            }
            else {
                bean.setTestResult("Fail");
            }
        }
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bean.setTestDate(dateFormat.format(new Date()));
        if (ExcelUtils.writeExcelB(Environment.getExternalStorageDirectory().getPath()+ File.separator+ com.renyu.bledemo.params.CommonParams.WRITEFILE_B, bean)) {
            Toast.makeText(BActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
            ACache.get(BActivity.this).clear();
            bleFramework.disConnect();
            b_ble_rssi.setText("rssi(正常范围大于-40)：暂无");
            cb_buzzer.setChecked(false);
            cb_current_sensor.setChecked(false);
            cb_led.setChecked(false);
            cb_rfid.setChecked(false);
        }
        else {
            Toast.makeText(BActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }
}
