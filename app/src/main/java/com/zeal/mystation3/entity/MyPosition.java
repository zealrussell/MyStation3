package com.zeal.mystation3.entity;
import com.baidu.mapapi.model.LatLng;

public class MyPosition {
    private double latitude;
    private double longitude;
    private float aam;
    private float ram;

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

    public void setAll(double latitude, double longitude, float aam, float ram) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aam = aam;
        this.ram = ram;
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

    @Override
    public String toString() {
        return  "Latitude=" + latitude + "\n" +
                "Longitude=" + longitude + "\n" +
                "Aam=" + aam + "\n" +
                "Ram=" + ram;
    }
}
