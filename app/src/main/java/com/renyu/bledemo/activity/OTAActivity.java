package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.renyu.bledemo.R;
import com.renyu.bledemo.utils.DataUtils;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.bean.OTABean;
import com.renyu.blelibrary.ble.BLEFramework;
import com.renyu.blelibrary.impl.BLEConnectListener;
import com.renyu.blelibrary.impl.BLEStateChangeListener;
import com.renyu.blelibrary.params.CommonParams;
import com.renyu.blelibrary.utils.BLEUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by renyu on 2017/2/19.
 */

public class OTAActivity extends AppCompatActivity {

    BLEFramework bleFramework;

    @BindView(R.id.tv_progress)
    TextView tv_progress;

    ProgressDialog progressDialog;

    Handler handlerConnState=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what== BLEFramework.STATE_SERVICES_DISCOVERED
                    || msg.what==BLEFramework.STATE_SERVICES_OTA_DISCOVERED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    if (msg.what== BLEFramework.STATE_SERVICES_DISCOVERED) {
                        Toast.makeText(OTAActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        DataUtils.enterOta(bleFramework);
                    }
                    else {
                        Toast.makeText(OTAActivity.this, "开始ota升级", Toast.LENGTH_SHORT).show();
                        bleFramework.startOTA();
                    }
                }
            }
            else if (msg.what==BLEFramework.STATE_DISCONNECTED) {
                if (progressDialog!=null) {
                    progressDialog.dismiss();
                    Toast.makeText(OTAActivity.this, "连接断开", Toast.LENGTH_SHORT).show();
                }
            }
            else if (msg.what== BLEFramework.STATE_SCANNED) {
                EventBus.getDefault().post(""+BLEFramework.STATE_SCANNED);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota);

        ButterKnife.bind(this);

        bleFramework=BLEFramework.getBleFrameworkInstance(this,
                com.renyu.bledemo.params.CommonParams.UUID_SERVICE,
                com.renyu.bledemo.params.CommonParams.UUID_Characteristic,
                com.renyu.bledemo.params.CommonParams.UUID_DESCRIPTOR);
        bleFramework.setTimeSeconds(5000);
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

        EventBus.getDefault().register(this);
    }

    @OnClick({R.id.button_startota, R.id.button_update})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_startota:
                openBlueTooth();
                break;
            case R.id.button_update:
                BLEFramework.isOTA=true;
                openBlueTooth();
                break;
        }
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

        Intent intent=new Intent(OTAActivity.this, DeviceListActivity.class);
        startActivityForResult(intent, com.renyu.bledemo.params.CommonParams.SCANDEVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CommonParams.RESULT_ENABLE_BT && resultCode==RESULT_OK) {
            getDevices();
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_OK) {
            BluetoothDevice bleDevice=data.getParcelableExtra("device");
            bleFramework.startConn(bleDevice);
            progressDialog= ProgressDialog.show(OTAActivity.this, "提示", "正在连接");
        }
        else if (requestCode==com.renyu.bledemo.params.CommonParams.SCANDEVICE && resultCode==RESULT_CANCELED) {
            bleFramework.cancelScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleFramework.disConnect();
        BLEFramework.isOTA=false;

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OTABean bean) {
        if (bean.getProcess()==-1) {
            tv_progress.setText("ota升级失败");
        }
        else if (bean.getProcess()==101) {
            tv_progress.setText("ota升级成功");
            BLEFramework.isOTA=false;
        }
        else {
            tv_progress.setText("ota正在升级，升级完成"+bean.getProcess()+"%");
        }
    }
}
