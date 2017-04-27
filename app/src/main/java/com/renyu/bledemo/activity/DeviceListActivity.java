package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.renyu.bledemo.R;
import com.renyu.bledemo.adapter.DeviceListAdapter;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.blelibrary.ble.BLEFramework;
import com.renyu.iitebletest.jniLibs.JNIUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by renyu on 2017/2/9.
 */

public class DeviceListActivity extends AppCompatActivity {

    @BindView(R.id.rv_devicelist)
    RecyclerView rv_devicelist;
    DeviceListAdapter adapter;

    ArrayList<BLEDevice> bleDevices;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devicelist);

        ButterKnife.bind(this);

        bleDevices=new ArrayList<>();

        EventBus.getDefault().register(this);

        rv_devicelist.setLayoutManager(new LinearLayoutManager(this));
        rv_devicelist.setHasFixedSize(true);
        adapter=new DeviceListAdapter(this, bleDevices);
        rv_devicelist.setAdapter(adapter);

        progressDialog= ProgressDialog.show(DeviceListActivity.this, "提示", "正在扫描");
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String state) {
        if (state.equals(""+ BLEFramework.STATE_SCANNED)) {
            progressDialog.dismiss();
        }
    }

    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BLEDevice device) {
        bleDevices.add(device);
        adapter.notifyDataSetChanged();
        if (getIntent().getStringExtra("scanTarget")!=null) {
            JNIUtils jniUtils=new JNIUtils();
            boolean isFind=false;
            for (BLEDevice bleDevice : bleDevices) {
                byte[] scanRecord=bleDevice.getScanRecord();
                byte[] password={scanRecord[11], scanRecord[12], scanRecord[13],
                        scanRecord[14], scanRecord[15], scanRecord[16], scanRecord[17],
                        scanRecord[18], scanRecord[19], scanRecord[20], scanRecord[21],
                        scanRecord[22], scanRecord[23], scanRecord[24], scanRecord[25],
                        scanRecord[26]};
                byte[] mic={scanRecord[27], scanRecord[28], scanRecord[29],
                        scanRecord[30]};
                byte[] b3=jniUtils.senddecode(password, mic, 16);
                byte[] b4=new byte[6];
                b4[0]=b3[0];
                b4[1]=b3[1];
                b4[2]=b3[2];
                b4[3]=b3[3];
                b4[4]=b3[4];
                b4[5]=b3[5];
                try {
                    String sn=new String(b4, "utf-8");
                    Log.d("DeviceListActivity", sn);
                    if (sn.toLowerCase().equals(getIntent().getStringExtra("scanTarget").toLowerCase())) {
                        isFind=true;
                        choice(bleDevice, sn);
                    }
                    break;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (!isFind) {
                Toast.makeText(this, "未找到相关设备，请重试", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void choice(BLEDevice device, String sn) {
        Intent intent=new Intent();
        intent.putExtra("rssi", device.getRssi());
        intent.putExtra("sn", sn);
        intent.putExtra("device", device.getDevice());
        setResult(RESULT_OK, intent);
        finish();
    }
}
