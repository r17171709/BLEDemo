package com.renyu.bledemo.utils;

import com.renyu.blelibrary.ble.BLEFramework;
import com.renyu.iitebletest.jniLibs.JNIUtils;

/**
 * Created by renyu on 2017/2/4.
 */

public class DataUtils {

    private static volatile DataUtils dataUtils;

    public static DataUtils getInstance() {
        if (dataUtils==null) {
            synchronized (DataUtils.class) {
                if (dataUtils==null) {
                    dataUtils=new DataUtils();
                }
            }
        }
        return dataUtils;
    }

    private void addCommand(BLEFramework bleFramework, byte command, byte[] info, int currentPackageSeq, int totalPackageNum) {
        JNIUtils jniUtils=new JNIUtils();
        int payloadLength=info.length;
        byte[] password=jniUtils.sendencode(info, payloadLength);
        byte[] sendValue=new byte[4+payloadLength+4];
        sendValue[0]= (byte) currentPackageSeq;
        sendValue[1]= (byte) totalPackageNum;
        sendValue[2]= command;
        sendValue[3]= (byte) (payloadLength+4);
        for (int i=0;i<payloadLength;i++) {
            sendValue[4+i]=password[i];
        }
        for (int i=0;i<4;i++) {
            sendValue[4+payloadLength+i]=password[16+i];
        }
        bleFramework.addCommand(sendValue);
    }

    /**
     * 添加命令
     * @param command
     * @param info
     */
    public void addCommand(BLEFramework bleFramework, byte command, byte[] info) {
        if (info.length>12) {
            byte[] info1=new byte[12];
            for (int i = 0; i < 12; i++) {
                info1[i]=info[i];
            }
            addCommand(bleFramework, command, info1, 1, 2);
            byte[] info2=new byte[info.length-12];
            for (int i=0;i<info.length-12;i++) {
                info2[i]=info[12+i];
            }
            addCommand(bleFramework, command, info2, 2, 2);
        }
        else {
            addCommand(bleFramework, command, info, 1, 1);
        }
    }

    /**
     * 获取返回解密数据
     * @param value
     * @return
     */
    public byte[] decodeResult(byte[] value) {
        int length=-1;
        for (int i = 0; i < value.length; i++) {
            if (value[i]==0) {
                length=i;
                break;
            }
        }
        if (length==-1) {
            return new byte[0];
        }
        int currentPackageSeq=value[0];
        int totalPackageNum=value[1];
        int command=(int) value[2]&0xff;
        int payloadLength=(int) value[3]-4;
        byte[] mic=new byte[4];
        for (int i = 0; i < mic.length; i++) {
            mic[i]=value[length-4+i];
        }
        byte[] password=new byte[16];
        for (int i = 0; i < 16; i++) {
            if (i<payloadLength) {
                password[i]=value[i+4];
            }
            else {
                password[i]=0;
            }
        }
        JNIUtils jniUtils=new JNIUtils();
        byte[] temp=jniUtils.senddecode(password, mic, payloadLength);
        byte[] result=new byte[payloadLength];
        for (int i=0;i<payloadLength;i++) {
            result[i]=temp[i];
        }
        return result;
    }
}
