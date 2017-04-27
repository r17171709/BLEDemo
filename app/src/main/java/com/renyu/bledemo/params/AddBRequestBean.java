package com.renyu.bledemo.params;

/**
 * Created by renyu on 2017/2/18.
 */

public class AddBRequestBean {
    String sn;
    String rssi;
    String sn_state;
    String testResult;
    String testDate;
    String magic;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getSn_state() {
        return sn_state;
    }

    public void setSn_state(String sn_state) {
        this.sn_state = sn_state;
    }

    public String getMagic() {
        return magic;
    }

    public void setMagic(String magic) {
        this.magic = magic;
    }
}
