package com.zeal.mystation3.entity;
import com.baidu.mapapi.model.LatLng;

public class MyPosition {
    private double latitude;
    private double longitude;
    private float aam;
    private float ram;

    private float roll;
    private float pitch;
    private float yaw;
    private long timestamp;

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
    }

    public void setAll(double latitude, double longitude, float aam, float ram) {
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

    public String formLog() {
        return "LATITUDE=" + latitude +
                ", LONGITUDE=" + longitude +
                ", AAM=" + aam +
                ", RAM=" + ram +
                ", ROLL=" + roll +
                ", PITCH=" + pitch +
                ", YAM=" + yaw +
                ", TIMESTAMP=" + timestamp;
    }

    @Override
    public String toString() {
        return  "Latitude=" + latitude + "\n" +
                "Longitude=" + longitude + "\n" +
                "Aam=" + aam + "\n" +
                "Ram=" + ram;
    }
}
