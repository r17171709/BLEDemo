package com.renyu.bledemo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.renyu.bledemo.R;
import com.renyu.bledemo.adapter.DeviceListAdapter;
import com.renyu.blelibrary.bean.BLEDevice;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    public void onEventMainThread(BLEDevice device) {
        bleDevices.add(device);
        adapter.notifyDataSetChanged();

        try {
            progressDialog.dismiss();
        } catch (Exception e) {

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
