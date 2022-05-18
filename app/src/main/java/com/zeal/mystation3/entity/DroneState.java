package com.zeal.mystation3.entity;
import io.mavsdk.telemetry.Telemetry;
/**
 * WHAT THE ZZZZEAL
 *
 * @author zeal
 * @version 1.0
 * @date 2022/5/17 22:52
 */
public class DroneState {
    private boolean isArmed;
    private boolean isInAir;
    private boolean isConnected;
    private boolean isAllOk;
    // 头朝向
    private Telemetry.Heading heading;
    // 位置信息
    private Telemetry.Position position;
    // home点
    private Telemetry.Position home;
    // raw
    private Telemetry.RawGps rawGps;
    //
    private Telemetry.GpsGlobalOrigin gpsGlobalOrigin;
    // gps自身信息
    private Telemetry.GpsInfo gpsInfo;
    // 信息
    private Telemetry.GroundTruth groundTruth;
    // row pitch yaw轴信息
    private Telemetry.EulerAngle eulerAngle;
    // X Y Z轴信息
    private Telemetry.Quaternion quaternion;
    // 健康信息
    private Telemetry.Health health;
    // 降落状态
    private Telemetry.LandedState landedState;
    // 飞行状态
    private Telemetry.FlightMode flightMode;
    // 电池信息
    private Telemetry.Battery battery;
    //
    private Telemetry.Imu imu;

    // 距离传感器
    private Telemetry.DistanceSensor distanceSensor;
    // 风速传感器
    private Telemetry.FixedwingMetrics fixedwingMetrics;

    public DroneState() {
        heading = new Telemetry.Heading(0.0);
        position = new Telemetry.Position(0.0,0.0,0f,0f);
        home = new Telemetry.Position(0.0,0.0,0f,0f);
        eulerAngle = new Telemetry.EulerAngle(0f,0f,0f, 0l);
        health = new Telemetry.Health(false,false,false,false,false,false,false);
        quaternion = new Telemetry.Quaternion(0f,0f,0f,0f,0l);

        gpsGlobalOrigin = new Telemetry.GpsGlobalOrigin(0.0,0.0,0f);
        gpsInfo = new Telemetry.GpsInfo(0,null);
        groundTruth = new Telemetry.GroundTruth(0.0,0.0,0f);
        flightMode =  Telemetry.FlightMode.UNKNOWN;
        landedState = Telemetry.LandedState.UNKNOWN;
        battery = new Telemetry.Battery(0,0f,0f);
        imu= new Telemetry.Imu(null,null,null,0f, 0l);
        distanceSensor = new Telemetry.DistanceSensor(0f,0f,0f);
        fixedwingMetrics = new Telemetry.FixedwingMetrics(0f,0f,0f);

    }

    public Telemetry.RawGps getRawGps() {
        return rawGps;
    }

    public void setRawGps(Telemetry.RawGps rawGps) {
        this.rawGps = rawGps;
    }

    public Telemetry.GpsGlobalOrigin getGpsGlobalOrigin() {
        return gpsGlobalOrigin;
    }

    public void setGpsGlobalOrigin(Telemetry.GpsGlobalOrigin gpsGlobalOrigin) {
        this.gpsGlobalOrigin = gpsGlobalOrigin;
    }

    public Telemetry.GpsInfo getGpsInfo() {
        return gpsInfo;
    }

    public void setGpsInfo(Telemetry.GpsInfo gpsInfo) {
        this.gpsInfo = gpsInfo;
    }

    public Telemetry.GroundTruth getGroundTruth() {
        return groundTruth;
    }

    public void setGroundTruth(Telemetry.GroundTruth groundTruth) {
        this.groundTruth = groundTruth;
    }

    public Telemetry.Quaternion getQuaternion() {
        return quaternion;
    }

    public void setQuaternion(Telemetry.Quaternion quaternion) {
        this.quaternion = quaternion;
    }

    public boolean isArmed() {
        return isArmed;
    }

    public void setArmed(boolean armed) {
        isArmed = armed;
    }

    public boolean isInAir() {
        return isInAir;
    }

    public void setInAir(boolean inAir) {
        isInAir = inAir;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isAllOk() {
        return isAllOk;
    }

    public void setAllOk(boolean allOk) {
        isAllOk = allOk;
    }

    public Telemetry.Heading getHeading() {
        return heading;
    }

    public void setHeading(Telemetry.Heading heading) {
        this.heading = heading;
    }

    public Telemetry.Position getPosition() {
        return position;
    }

    public void setPosition(Telemetry.Position position) {
        this.position = position;
    }

    public Telemetry.Position getHome() {
        return home;
    }

    public void setHome(Telemetry.Position home) {
        this.home = home;
    }

    public Telemetry.EulerAngle getEulerAngle() {
        return eulerAngle;
    }

    public void setEulerAngle(Telemetry.EulerAngle eulerAngle) {
        this.eulerAngle = eulerAngle;
    }

    public Telemetry.Health getHealth() {
        return health;
    }

    public void setHealth(Telemetry.Health health) {
        this.health = health;
    }

    public Telemetry.LandedState getLandedState() {
        return landedState;
    }

    public void setLandedState(Telemetry.LandedState landedState) {
        this.landedState = landedState;
    }

    public Telemetry.FlightMode getFlightMode() {
        return flightMode;
    }

    public void setFlightMode(Telemetry.FlightMode flightMode) {
        this.flightMode = flightMode;
    }

    public Telemetry.Battery getBattery() {
        return battery;
    }

    public void setBattery(Telemetry.Battery battery) {
        this.battery = battery;
    }

    public Telemetry.Imu getImu() {
        return imu;
    }

    public void setImu(Telemetry.Imu imu) {
        this.imu = imu;
    }

    public Telemetry.DistanceSensor getDistanceSensor() {
        return distanceSensor;
    }

    public void setDistanceSensor(Telemetry.DistanceSensor distanceSensor) {
        this.distanceSensor = distanceSensor;
    }

    public Telemetry.FixedwingMetrics getFixedwingMetrics() {
        return fixedwingMetrics;
    }

    public void setFixedwingMetrics(Telemetry.FixedwingMetrics fixedwingMetrics) {
        this.fixedwingMetrics = fixedwingMetrics;
    }
}
