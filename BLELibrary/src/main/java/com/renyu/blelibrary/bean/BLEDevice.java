package com.renyu.blelibrary.bean;

import android.bluetooth.BluetoothDevice;

/**
 * Created by renyu on 2017/1/12.
 */

public class BLEDevice {
    BluetoothDevice device;
    byte[] scanRecord;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }
}
