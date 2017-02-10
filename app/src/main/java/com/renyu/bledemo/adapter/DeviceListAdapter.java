package com.renyu.bledemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.renyu.bledemo.R;
import com.renyu.bledemo.activity.DeviceListActivity;
import com.renyu.blelibrary.bean.BLEDevice;
import com.renyu.iitebletest.jniLibs.JNIUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by renyu on 2017/2/10.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListHolder> {

    Context context;
    ArrayList<BLEDevice> bleDevices;

    JNIUtils jniUtils;

    public DeviceListAdapter(Context context, ArrayList<BLEDevice> bleDevices) {
        this.context=context;
        this.bleDevices=bleDevices;

        jniUtils=new JNIUtils();
    }

    @Override
    public DeviceListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.adapter_devicelist, parent, false);
        return new DeviceListHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceListHolder holder, int position) {
        byte[] scanRecord=bleDevices.get(position).getScanRecord();
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
            final String sn=new String(b4, "utf-8");
            final BLEDevice device=bleDevices.get(position);
            String type="";
            if (b3[6]==0x10) {
                type="标签";
            }
            else if (b3[6]==0x20) {
                type="精灵";
            }
            holder.adapter_devicelist_text.setText(device.getDevice().getAddress()+"\n"+type+"\n"+sn);
            holder.adapter_devicelist_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DeviceListActivity) context).choice(device, sn);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return bleDevices.size();
    }

    public class DeviceListHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.adapter_devicelist_text)
        TextView adapter_devicelist_text;

        public DeviceListHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
