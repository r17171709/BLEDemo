package com.renyu.iitebletest.jniLibs;

/**
 * Created by renyu on 2017/1/3.
 */

public class JNIUtils {

    static {
        System.loadLibrary("jniLib");
    }

    public native String stringFromJni();

    public native byte[] sendencode(byte[] values, int payloadLength);

    public native byte[] senddecode(byte[] values, byte[] tags, int payloadLength);
}
