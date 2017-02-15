package com.renyu.bledemo.params;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by renyu on 2017/2/9.
 */

public class AddRequestBean extends RealmObject {
    String deviceID;
    @PrimaryKey
    String machineId;
    String machineName;
    String sn;
    String testResult;
    String testDate;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
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

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }
}
