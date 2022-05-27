package com.zeal.mystation3.entity;
import com.baidu.mapapi.model.LatLng;

import java.text.SimpleDateFormat;

// 飞行数据类
public class MyPosition {
    // 经纬高
    private double latitude;
    private double longitude;
    private float aam;
    private float ram;
    //俯仰航
    private float roll;
    private float pitch;
    private float yaw;

    //时间
    private long timestamp;
    private String date;

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MyPosition() {
    }

    public MyPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public MyPosition(double latitude, double longitude, float aam, float ram) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aam = aam;
        this.ram = ram;
    }

    public MyPosition(double latitude, double longitude, float aam, float ram, float roll, float pitch, float yaw, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aam = aam;
        this.ram = ram;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.timestamp = timestamp;
        this.date = formatter.format(timestamp);
    }

    public void setByPosition(double latitude, double longitude, float aam, float ram) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aam = aam;
        this.ram = ram;
    }
    public void setPos(double latitude, double longitude, float aam, float yaw) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aam = aam;
        this.yaw = yaw;
    }

    public LatLng getLatLng(){
        return new LatLng(latitude,longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAam() {
        return aam;
    }

    public void setAam(float aam) {
        this.aam = aam;
    }

    public float getRam() {
        return ram;
    }

    public void setRam(float ram) {
        this.ram = ram;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // TODO 自定义输出格式
    /**
     * 生成飞行数据文本
     * @return 文本
     */
    public String formLog() {
        return "[" + timestamp  +"]" + "\n" +
                "LATITUDE=" + latitude + "\n" +
                ", LONGITUDE=" + longitude + "\n" +
                ", AAM=" + aam +
                ", RAM=" + ram + "\n" +
                ", ROLL=" + roll +
                ", PITCH=" + pitch +
                ", YAM=" + yaw + "\n";
    }

    @Override
    public String toString() {
        return  "Latitude=" + latitude + "\n" +
                "Longitude=" + longitude + "\n" +
                "Aam=" + aam + "\n" +
                "Ram=" + ram  + "\n\n";
    }
}
