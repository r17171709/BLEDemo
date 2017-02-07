package com.renyu.bledemo.params;

import java.util.UUID;

/**
 * Created by renyu on 2017/2/3.
 */

public class CommonParams {

    public static final UUID UUID_SERVICE=UUID.fromString("0a2be667-2416-4373-b583-1147d905e39f");
    public static final UUID UUID_Characteristic=UUID.fromString("0000cdd2-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_DESCRIPTOR=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int ERROR_RESP=0xf1;
    public static final int SET_SN_REQ=0xa7;
    public static final int SET_SN_RESP=0xa8;
    public static final int SET_MAGIC_REQ=0xa9;
    public static final int SET_MAGIC_RESP=0xaa;
    public static final int GET_DEVICE_CURRENT_REQ=0x9c;
}
