package com.renyu.bledemo.params;

import java.util.UUID;

/**
 * Created by renyu on 2017/2/3.
 */

public class CommonParams {

    public static final UUID UUID_SERVICE=UUID.fromString("0a2be667-2416-4373-b583-1147d905e39f");
    public static final UUID UUID_Characteristic=UUID.fromString("0000cdd2-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DESCRIPTOR=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int ERROR_RSSI=0x00;
    public static final int ERROR_RESP=0xf1;
    public static final int SET_SN_REQ=0xa7;
    public static final int SET_SN_RESP=0xa8;
    public static final int SET_MAGIC_REQ=0xa9;
    public static final int SET_MAGIC_RESP=0xaa;
    public static final int GET_DEVICE_CURRENT_REQ=0x9c;
    public static final int GET_DEVICE_CURRENT_RESP=0x9d;
    public static final int SET_DEVICEID_REQ=0x9a;
    public static final int SET_DEVICEID_RESP=0x9b;

    public static final int SCANDEVICE=1001;
    public static final int QRCODESCAN=1002;

    // 施工使用
    public static final String READFILE_S="test_input.xls";
    public static final String WRITEFILE_S="test.xls";
    // 半制测试生成二维码
    public static final String BARCODEFILE="code.jpg";
    // 半制测试使用
    public static final String WRITEFILE_B="testb.xls";
    // 全制测试使用
    public static final String WRITEFILE_Q="testq.xls";

    public static final int RSSIWRONG=-80;
}
