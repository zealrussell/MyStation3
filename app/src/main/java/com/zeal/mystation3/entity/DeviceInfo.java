package com.zeal.mystation3.entity;

/**
 * USB设备信息
 */

public class DeviceInfo {
    private int VID;
    private int PID;

    public DeviceInfo() {
    }

    public int getVID() {
        return VID;
    }

    public void setVID(int VID) {
        this.VID = VID;
    }

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }
}
