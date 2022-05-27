package com.zeal.mystation3.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.zeal.mystation3.R;
import com.zeal.mystation3.application.MyApplication;
import com.zeal.mystation3.entity.DroneState;
import com.zeal.mystation3.entity.MyPosition;
import com.zeal.mystation3.utils.FileUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.mavsdk.System;
import io.mavsdk.action.Action;
import io.mavsdk.mavsdkserver.MavsdkServer;
import io.mavsdk.telemetry.Telemetry;


public class MapActivity extends Activity implements View.OnClickListener {

    private static final DecimalFormat df = new DecimalFormat("#0.000000");
    private static final String TAG = "ZEALTAG";
    private static final String BACKEND_IP_ADDRESS = "127.0.0.1";
    private static final String ADDRESS = "udp://:14540";
    private static int PORT = 0;


    // Handler消息:更新状态、更新位置、显示toast
    private static final int UPDATE_STATE = 0;
    public static final int UPDATE_POSITION = 1;
    public static final int UPDATE_TOAST = 2;

    // 无人机相关
    private final MavsdkServer mavsdkServer = new MavsdkServer();
    private System drone;

    private static final DroneState droneState = new DroneState();
    private Action action;
    private Telemetry telemetry;

    // 方向有关变量
    private static double OFFSET = 0.00001; // 1m
    private static double LATITUDE = 32.030827;
    private static double LONGITUDE = 118.859596;
    private static float AAM = 48F;
    private static float RAM = 0F;
    private static double HEADING = 0.0;

    private static float ROLL;
    private static float PITCH;
    private static float YAW;
    private static long TIMESTAMP;
    // 一些经纬度
    private static final LatLng GEO_NJUST = new LatLng(32.030827, 118.859596);
    private static LatLng GEO_HOME;

    // 用于移动操作
    private static final MyPosition current = new MyPosition();
    private static final MyPosition next = new MyPosition();

    // 地图有关
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private MapStatus.Builder builder;
    private static Overlay lineOverlay;
    private static Overlay droneOverlay;
    private static Overlay homeOverlay;
    private final List<LatLng> points = new ArrayList<>();
    private final List<MyPosition> positionsLog = new ArrayList<>();

    // UI控件
    private TextView tv;
    private ImageButton scaleBtn, changeTextBtn;
    private boolean scaleFlag, infoFlag;

    //------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initView();
        initMap();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    // 结束时取消订阅、停止连接、存储文件
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        try {
            drone.dispose();
            mavsdkServer.stop();
        } catch (Exception e){
            e.printStackTrace();
        }

        //如果没飞，直接退出
        if(positionsLog.isEmpty()) return;

        // 保存文件，成功|失败
        String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                        + MyApplication.DIRECTORY_NAME + File.separator + "data" + File.separator;
        if( FileUtils.writeTxtToFileFromList(positionsLog, PATH,
                    LocalDateTime.now() + "log.txt") ){
            showToast("Store the log");
        } else {
            showToast("Failed to store");
        }
        positionsLog.clear();

    }



    /*-------------------------------- 函数部分 --------------------------------------------*/

    /**
     * 初始化UI视图
     */
    private void initView(){

        //获取地图控件引用
        mMapView = findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();

        // 安卓UI控件
        Toolbar toolbar = findViewById(R.id.toolbar);
        tv = findViewById(R.id.textView);

        findViewById(R.id.btn_forward).setOnClickListener(this);
        findViewById(R.id.btn_backward).setOnClickListener(this);
        findViewById(R.id.btn_hold).setOnClickListener(this);
        findViewById(R.id.btn_leftward).setOnClickListener(this);
        findViewById(R.id.btn_rightward).setOnClickListener(this);
        findViewById(R.id.btn_upward).setOnClickListener(this);
        findViewById(R.id.btn_downward).setOnClickListener(this);
        scaleBtn = findViewById(R.id.btn_scale);
        scaleBtn.setOnClickListener(this);
        changeTextBtn = findViewById(R.id.btn_change_text);
        changeTextBtn.setOnClickListener(this);

        // toolbar返回按钮
        toolbar.setNavigationOnClickListener(v -> finish());
        // 设置toolbar,tool的按钮点击事件
        toolbar.inflateMenu(R.menu.menu_toolbar);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.btn_return_to_launch) {
                returnToLaunch();
                return true;
            } else if(id == R.id.btn_kill) {
                kill();
                return true;
            } else if(id == R.id.btn_connect) {
                try {
                    connect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (id == R.id.btn_takeoff) {
                takeoff();
                return true;
            } else if (id == R.id.btn_land) {
                land();
                return true;
            } else if (id == R.id.btn_camera) {
                Intent intent = new Intent(MapActivity.this, USBCameraActivity.class);
                startActivity(intent);
                return true;
            } else return false;
        });

    }

    /**
     * 按钮点击事件
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.btn_forward:
                goForward();
                break;
            case R.id.btn_backward:
                goBackward();
                break;
            case R.id.btn_leftward:
                goLeftward();
                break;
            case R.id.btn_rightward:
                goRightward();
                break;
            case R.id.btn_hold:
                hold();
                break;
            case R.id.btn_upward:
                goUpward();
                break;
            case R.id.btn_downward:
                goDownward();
                break;
            case R.id.btn_scale:
                changeScale();
                break;
            case R.id.btn_change_text:
                changeTextView();
                break;
        }
    }

    /**
     * 初始化地图
     */
    private void initMap(){
        builder = new MapStatus.Builder();
        //设置中心点为南理，缩放比例尺 5m
        builder.target(GEO_NJUST).zoom(20.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        // 开启卫星地图
        //mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        // 初始时将 南理工 作为home，连接后改成无人机的位置
        homeOverlay = drawOverlay(GEO_NJUST, R.drawable.home);
        showToast("Map created");
    }

    /**
     * 初始化HOME地点
     */
    private void initHome(){
        if (!droneState.getHealth().getIsHomePositionOk()) {
            showToast("Failed to set home!Please check.");
            return;
        }

        double latitudeDeg = droneState.getHome().getLatitudeDeg();
        double longitudeDeg = droneState.getHome().getLongitudeDeg();
        GEO_HOME = new LatLng(latitudeDeg,longitudeDeg);

        Log.i(TAG,"THE HOME IS: " + GEO_HOME);

        // 设置HOME的位置为 起飞地
        points.clear();
        points.add(GEO_HOME);
        clearOverlay(homeOverlay);
        homeOverlay = drawOverlay(GEO_HOME, R.drawable.home);
        // 重设地图焦点
        mBaiduMap.setMapStatus(MapStatusUpdateFactory
                .newMapStatus(builder
                        .target(GEO_HOME)
                        .zoom(21.0f)
                        .build())
        );

    }

    /*-----------------------------无人机部分--------------------------------*/

    /**
     * 使用handler在主线程中操作UI，每次位置更新后，刷新textview数据、无人机位置
     */
    @SuppressLint("HandlerLeak")
    private final Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MapActivity.UPDATE_POSITION) {

                String valueInfoText = "Latitude: " + df.format(LATITUDE) + "\n" +
                        "Longitude: " + df.format(LONGITUDE) + "\n" +
                        "Aam: " + df.format(AAM) + "\n" +
                        "Ram: " + df.format(RAM) + "\n" +
                        "Heading: " + df.format(HEADING) + "\n" +
                        "Pitch: " + df.format(PITCH) + "\n" +
                        "Roll: " + df.format(ROLL) + "\n";

                String stateInfoText = "Battery: " + droneState.getBattery().getRemainingPercent() + "\n" +
                        "Arm: " + droneState.isArmed() + "\n" +
                        "Connect: " + droneState.isConnected() + "\n" +
                        "AllOk: " + droneState.isAllOk() + "\n" +
                        "InAir: " + droneState.isInAir() + "\n" +
                        "FlightMode: " + droneState.getFlightMode() + "\n" +
                        "Home: " + droneState.getHome().getLatitudeDeg() + " "
                        + droneState.getHome().getLongitudeDeg() + "\n"
                        + droneState.getHome().getAbsoluteAltitudeM() + " "
                        + droneState.getHome().getRelativeAltitudeM();

                if(!infoFlag) {
                    tv.setText(valueInfoText);
                } else tv.setText(stateInfoText);

                // 将当前位置加入集合
                positionsLog.add(new MyPosition(LATITUDE,LONGITUDE,AAM,RAM,ROLL,PITCH,YAW,TIMESTAMP));
                // 清除无人机图标 并 重设
                clearOverlay(droneOverlay);
                droneOverlay = drawOverlay(
                        new LatLng(LATITUDE, LONGITUDE)
                        , R.drawable.plane,
                        (float) (360.0F - HEADING));

            } else if (msg.what == MapActivity.UPDATE_TOAST) {
                showToast((String) msg.obj);

            } else if (msg.what == MapActivity.UPDATE_STATE) {

                if (!droneState.getHealth().getIsLocalPositionOk()) {
                    toastMessage("LocalPosition failed");
                } else if (!droneState.getHealth().getIsArmable()) {
                    toastMessage("Arm failed");
                } else if (!droneState.getHealth().getIsHomePositionOk()) {
                    toastMessage("HomePosition failed");
                } else if (!droneState.getHealth().getIsGlobalPositionOk()) {
                    toastMessage("GlobalPosition failed");
                }
                if (droneState.getBattery().getRemainingPercent() < 0.2f) {
                    toastMessage("Battery too low");
                }
            }


        }
    };

    /**
     * 连接无人机： 订阅所有信息-》更新textview-》初始化无人机位置
     */
    @SuppressLint("CheckResult")
    private void connect() throws InterruptedException {
        if(drone != null) return;
        // TODO check the address before you generate your application
        try {
            PORT = mavsdkServer.run(ADDRESS);
        }catch (Exception e) {
            e.printStackTrace();
            showToast("Run server failed");
        }
        showToast("Mavport is " + PORT);

        drone = new System(BACKEND_IP_ADDRESS, PORT);
        action = drone.getAction();
        telemetry = drone.getTelemetry();

        subscribeAll();

        Thread.sleep(3000);
        initHome();

    }

    /**
     *起飞: 检查健康状态-》arm -》 设置飞行高度 -》 起飞
     */
    @SuppressLint("CheckResult")
    public void takeoff(){
        if (!droneState.isAllOk()) {
            showToast("Please check!");
            return;
        }
//        // 在实验环境下用简单版
//        action.takeoff()
//                .doOnError(throwable -> {
//                   toastMessage("take off failed");
//                }).subscribe();
        // 仿真环境下可用完整版
        if (droneState.isArmed()) {
            action.setTakeoffAltitude(2.5f)
                    .andThen(action.takeoff()
                            .doOnComplete(() -> {
                                toastMessage("The drone is takeoff to 2.5m");
                                Log.i(TAG, "The drone is takeoff to 2.5m");
                            })
                    ).subscribe();
        } else {
            action.arm()
                    .delay(1000,TimeUnit.MILLISECONDS)
                    .andThen(
                            action.setTakeoffAltitude(2.5f)
                                    .andThen(action.takeoff())
                    )
                    .doOnComplete(()-> {
                        toastMessage("The drone is takeoff to 2.5m");
                        Log.i(TAG, "The drone is takeoff to 2.5m");
                    })
                    .subscribe();
        }

    }

    /**
     * 降落
     */
    @SuppressLint("CheckResult")
    public void land(){
        if (!droneState.isInAir()) {
            showToast("Please takeoff before land!");
            return;
        }
        action.land()
                .doOnComplete( () -> {
                    toastMessage("The drone is landing...");
                    Log.i(TAG,"The drone is landing....");
                } )
                .doOnError(throwable -> {
                    toastMessage("Failed to land: " + throwable.getMessage());
                    Log.e(TAG,"Failed to land: " + throwable.getMessage());
                })
                .subscribe();
//        action.land()
//                .delay(10,TimeUnit.SECONDS)
//                .andThen(
//                    action.disarm()
//                        .doOnComplete(()-> {
//                            toastMessage("The drone is disarmed!");
//                            Log.i(TAG, "The drone is disarmed!");
//                        })
//                        .doOnError(throwable -> {
//                            toastMessage("Failed to disarm: " + throwable.getMessage());
//                            Log.e(TAG, "Failed to disarm: " + throwable.getMessage());
//                        })
//                )
//                .doOnComplete( () -> {
//                    toastMessage("Landed!!");
//                    Log.i(TAG,"The drone is landing....");
//                } )
//                .doOnError(throwable -> {
//                    toastMessage("Failed to land: " + throwable.getMessage());
//                    Log.e(TAG,"Failed to land: " + throwable.getMessage());
//                })
//                .subscribe();

    }

    /**
     * 返回到起飞点
     */
    @SuppressLint("CheckResult")
    public void returnToLaunch() {
        if (!droneState.isInAir()) {
            showToast("Please takeoff before return!");
            return;
        }

        action.setReturnToLaunchAltitude(5.0f)
                .andThen(
                        action
                                .returnToLaunch()
                                .doOnComplete(() -> {
                                    addPosition(GEO_HOME);
                                    drawTrajectory();
                                    toastMessage("The drone is returning to launch...");
                                    Log.i(TAG,"Return to launch!");
                                })
                                .doOnError( throwable -> {
                                    toastMessage("Failed to return: " + throwable.getMessage());
                                    Log.e(TAG,"Failed to return: " + throwable.getMessage());
                                })
        ).subscribe( ()->{
            toastMessage("Returned!!");
            Log.i(TAG, "Returned!!");
        });

    }

    /**
     * 强制停止
     */
    private void kill(){
        if (!droneState.isArmed()) {
            showToast("The drone even not arm!");
            return;
        }
        action.kill()
                .doOnComplete(() -> {
                    toastMessage("The drone is killed");
                    Log.i(TAG,"The drone is killed");
                } )
                .doOnError(throwable -> {
                    toastMessage("Failed to kill: " + throwable.getMessage());
                    Log.e(TAG,"Failed to kill: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 移动，成功则绘制懂轨迹
     * @param pos 方位：上 下 左 右 升 降
     */
    @Deprecated
    private void moveTo(int pos) {
        getPosition();
        // 必须先连接
        if (droneState.getFlightMode() != Telemetry.FlightMode.HOLD) return;
        if (!droneState.isInAir()) {
            showToast("Please takeoff before move");
            return;
        }

        if(pos == 1) {
            next.setPos(current.getLatitude() + OFFSET,
                    current.getLongitude(),
                    current.getAam(),
                    current.getYaw());
        } else if (pos == 2) {
            next.setPos(current.getLatitude() - OFFSET,
                    current.getLongitude(),
                    current.getAam(),
                    current.getYaw());
        } else if (pos == 3) {
            next.setPos(current.getLatitude(),
                    current.getLongitude() - OFFSET,
                    current.getAam(),
                    current.getYaw());
        } else if (pos == 4) {
            next.setPos(current.getLatitude(),
                    current.getLongitude() + OFFSET,
                    current.getAam(),
                    current.getYaw());
        } else if (pos == 5) {
            if(current.getRam() >= 8.0f) {
                showToast("The drone is too high!");
                return;
            }
            next.setPos(current.getLatitude(),
                    current.getLongitude(),
                    current.getAam() + 0.5f,
                    current.getYaw());
        } else if (pos == 6) {
            if (current.getRam() <= 0.2f) {
                showToast("The drone is too low!");
                return;
            }
            next.setPos(current.getLatitude(),
                    current.getLongitude(),
                    current.getAam() - 0.5f,
                    current.getYaw());
        } else return;


        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Move to " + pos + "\n" + next);
                    Log.i(TAG,"Move to " + pos + "\n" + next);
                    if(pos != 5 && pos != 6) {
                        addPosition(next.getLatLng());
                        drawTrajectory();
                    }
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 悬停：
     */
    private void hold(){
        if (!droneState.isInAir()) {
            showToast("Please takeoff before hold!");
            return;
        }
        action.hold()
                .doOnComplete(() -> {
                    toastMessage("Hold the drone");
                    Log.i(TAG, "Hold the drone");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to hold: " + throwable.getMessage());
                    Log.e(TAG, "Failed to hold: " + throwable.getMessage());
                } )
                .subscribe();
    }


    /**
     * 改变移动距离的大小，并更换图标
     */
    private void changeScale(){
        if(!scaleFlag) {
            //约 10 m
            OFFSET = 0.00005;
            scaleBtn.setBackgroundResource(R.drawable.divide);
            scaleFlag = true;
            showToast("Bigger!");
        } else {
            //约 1 m
            OFFSET = 0.00001;
            scaleBtn.setBackgroundResource(R.drawable.multiply);
            scaleFlag = false;
            showToast("Smaller!");
        }

    }

    /**
     * 点击切换TextView
     */
    private void changeTextView(){
        infoFlag = !infoFlag;
        if (infoFlag) {
            changeTextBtn.setBackgroundResource(R.drawable.value_info);
        } else changeTextBtn.setBackgroundResource(R.drawable.state_info);
    }


    /*-----------------------------------------------地图部分----------------------------*/
    // 地图有关函数

    /**
     * 清除覆盖物
     * @param overlay 覆盖物类
     */
    private void clearOverlay(Overlay overlay){
        if (overlay == null) return;
        List<Overlay> overlayList = new ArrayList<>();
        overlayList.add(overlay);
        mBaiduMap.removeOverLays(overlayList);
    }

    /**
     * 添加到点集
     * @param point 位置
     */
    private void addPosition(LatLng point){
        points.add(point);
        Log.d(TAG,"Add to points: " + point);
    }

    /**
     * 在地图上添加Overlay
     * @param point 覆盖物地点
     * @param bitmapID 覆盖物形状
     * @return 覆盖物类
     */
    private Overlay drawOverlay(LatLng point, int bitmapID) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(bitmapID);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .anchor(0.5f,0.5f);

        //Log.d("Marker","Draw a " + bitmapID + ": " + option);
        //在地图上添加Marker，并显示
        return mBaiduMap.addOverlay(option);

    }

    /**
     * 根据朝向添加Overlay
     * @param point 位置
     * @param bitmapID 图id
     * @param heading 朝向
     * @return 覆盖物对象
     */
    private Overlay drawOverlay(LatLng point, int bitmapID,float heading) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(bitmapID);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .rotate(heading)
                .anchor(0.5f,0.5f);

        Log.d("Marker","Draw a " + bitmapID + ": " + option);
        //在地图上添加Marker，并显示
        return mBaiduMap.addOverlay(option);

    }

    /**
     * 绘制轨迹
     */
    private void drawTrajectory() {
        //设置折线的属性:宽度、颜色、点
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(8)
                .lineCapType(PolylineOptions.LineCapType.LineCapRound)
                .color(0xAAFF0000)
                .points(points);

        clearOverlay(lineOverlay);
        //在地图上绘制折线
        lineOverlay = mBaiduMap.addOverlay(polylineOptions);
    }

    /*----------------------------------- 一些工具函数 ------------------------------*/

    /**
     * 更新位置，刷新
     */
    private void getPosition(){
        current.setPos(LATITUDE,LONGITUDE,AAM,YAW);
    }

    /**
     * 展示toast，并打印日志
     */
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        Log.d(TAG,text);
    }

    /**
     * 通过message传递信息
     * @param text 消息内容
     */
    private void toastMessage(String text) {
        Message message = Message.obtain();
        message.what = MapActivity.UPDATE_TOAST;
        message.obj = text;
        myHandler.sendMessage(message);
    }






    /*!----------------订阅----------*/

    /**
     * 统一订阅
     */
    private void subscribeAll(){
        if (drone == null) return;
        // 开始显示textview
        new TextViewThread().start();

        subscribeConnect();
        subscribeAllOk();
        subscribeHealth();
        subscribeArm();
        subscribeInAir();
        subscribeHome();
        subscribeBattery();
        subscribeFlightMode();

        subscribeAttitude();
        subscribeHeading();
        subscribePosition();

    }

    /**
     * 订阅Attitude信息：row pitch yaw
     */
    @SuppressLint("CheckResult")
    private void subscribeAttitude(){
        //获取xyz轴偏角、时间戳
        telemetry.getAttitudeEuler()
                .doOnError(throwable -> {
                    toastMessage("Failed to get euler: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get euler: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(eulerAngle -> {
                    ROLL = eulerAngle.getRollDeg();
                    PITCH = eulerAngle.getPitchDeg();
                    YAW = eulerAngle.getYawDeg();
                    TIMESTAMP = eulerAngle.getTimestampUs();
                    droneState.setEulerAngle(eulerAngle);

//                    Message updatePosMessage = Message.obtain();
//                    updatePosMessage.what = MapActivity.UPDATE_POSITION;
//                    myHandler.sendMessage(updatePosMessage);
                });
    }

    /**
     * 订阅Heading信息： +是顺时针，-是逆时针。HEADING和YAW是一个东西
     */
    @SuppressLint("CheckResult")
    private void subscribeHeading(){
        // 获取朝向
        telemetry.getHeading()
                .doOnError(throwable -> {
                    toastMessage("Failed to get heading: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get heading: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(heading -> {
                    HEADING = heading.getHeadingDeg();
                    droneState.setHeading(heading);
                });
    }

    /**
     * 订阅Position信息
     */
    @SuppressLint("CheckResult")
    private void subscribePosition(){
        // 每500ms更新
        // 获取位置信息
        telemetry.getPosition()
                .doOnComplete(() -> {
                    Log.i(TAG, "Finish position");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to get position: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get position: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(
                        pos -> {
                            LATITUDE = pos.getLatitudeDeg();
                            LONGITUDE = pos.getLongitudeDeg();
                            AAM = pos.getAbsoluteAltitudeM();
                            RAM = pos.getRelativeAltitudeM();

                            droneState.setPosition(pos);

//                            Log.v("MyPOSITION", "GOT THE POSITION:"
//                                    + LATITUDE + " "
//                                    + LONGITUDE + " "
//                                    + AAM + " "
//                                    + RAM);
                        }
                );
    }

    /**
     * 订阅arm信息 2s
     */
    @SuppressLint("CheckResult")
    private void subscribeArm(){
        telemetry.getArmed()
                .doOnError(throwable -> {
                    toastMessage("Failed to get isArm: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get isArm: " + throwable.getMessage());
                 })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(isArmed -> {
                    droneState.setArmed(isArmed);
                });
    }

    /**
     * 订阅Connect信息 2s
     */
    @SuppressLint("CheckResult")
    private void subscribeConnect(){
        drone.getCore()
                .getConnectionState()
                .doOnError(throwable -> {
                    toastMessage("Failed to get isConnect: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get isConnect: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(connectionState -> {
                    droneState.setConnected(connectionState.getIsConnected());
                });
    }

    /**
     * 订阅AllOk信息 2s
     */
    private void subscribeAllOk(){
        telemetry.getHealthAllOk()
                .doOnError(throwable -> {
                    toastMessage("Failed to get isAllOk: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get isAllOk: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(isAllOk -> {
                    droneState.setAllOk(isAllOk);
                });
    }

    /**
     * 订阅InAir信息 5s
     */
    private void subscribeInAir(){
        telemetry.getInAir()
                .doOnError(throwable -> {
                    toastMessage("Failed to get isInAir: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get isInAir: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(isInAir -> {
                    droneState.setInAir(isInAir);
                });
    }

    /**
     * 订阅RawGps
     */
    private void subscribeRawGps(){
        telemetry.getRawGps()
                .doOnComplete(() -> {
                    Log.i(TAG, "Finish position");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to get RawGps: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get RawGps: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(
                        rawGps -> {
                            LATITUDE = rawGps.getLatitudeDeg();
                            LONGITUDE = rawGps.getLongitudeDeg();
                            AAM = rawGps.getAbsoluteAltitudeM();
                            droneState.setRawGps(rawGps);
                        }
                );
    }

    /**
     * 订阅GroundTruth：经 纬 高
     */
    private void subscribeGroundTruth(){
        telemetry.getGroundTruth()
                .doOnComplete(() -> {
                    Log.i(TAG, "Finish position");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to get GroundTruth: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get GroundTruth: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(
                        groundTruth -> {
                            LATITUDE = groundTruth.getLatitudeDeg();
                            LONGITUDE = groundTruth.getLongitudeDeg();
                            AAM = groundTruth.getAbsoluteAltitudeM();
                            droneState.setGroundTruth(groundTruth);
                        }
                );
    }

    /**
     * 订阅GpsGlobalOrigin：经、纬、高
     */
    private void subscribeGpsGlobalOrigin(){
        telemetry.getGpsGlobalOrigin()
                .doOnError(throwable -> {
                    toastMessage("Failed to get GpsGlobalOrigin: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get GpsGlobalOrigin: " + throwable.getMessage());
                })
                .subscribe(
                       gpsGlobalOrigin -> {
                           LATITUDE = gpsGlobalOrigin.getLatitudeDeg();
                           LONGITUDE = gpsGlobalOrigin.getLongitudeDeg();
                           AAM = gpsGlobalOrigin.getAltitudeM();
                           droneState.setGpsGlobalOrigin(gpsGlobalOrigin);
                        }
                );
    }

    /**
     * 订阅GPS信息 2s
     */
    private void subscribeGpsInfo(){
        telemetry.getGpsInfo()
                .doOnComplete(() -> {
                    Log.i(TAG, "Finish position");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to get GpsInfo: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get GpsInfo: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(
                        gpsInfo -> {
                            droneState.setGpsInfo(gpsInfo);
                        }
                );
    }

    /**
     * 订阅电池信息：剩余电量、电压。 2s更新一次
     */
    private void subscribeBattery(){
        telemetry.getBattery()
                .doOnError(throwable -> {
                    toastMessage("Failed to get Battery: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get Battery: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(
                        battery -> {
                            droneState.setBattery(battery);
                        }
                );
    }

    /**
     * 订阅飞行状态：READY HOLD TAKEOFF LAND MISSION RETURN OFFBOARD FOLLOW 500ms
     */
    private void subscribeFlightMode(){
        telemetry.getFlightMode()
                .doOnError(throwable -> {
                    toastMessage("Failed to get FlightMode: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get FlightMode: " + throwable.getMessage());
                })
                .sample(500,TimeUnit.MILLISECONDS)
                .subscribe(
                        flightMode -> {
                            droneState.setFlightMode(flightMode);
                        }
                );
    }

    /**
     * 订阅LandedState信息 2s
     */
    private void subscribeLandedState(){
        telemetry.getLandedState()
                .doOnError(throwable -> {
                    toastMessage("Failed to get Home: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get Home: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(
                        landedState -> {
                            droneState.setLandedState(landedState);
                        }
                );
    }

    /**
     * 检查设备健康状态 2s
     */
    private void subscribeHealth(){
        telemetry.getHealth()
                .doOnError(throwable -> {
                    toastMessage("Failed to get Health: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get Health: " + throwable.getMessage());
                })
                .sample(2000,TimeUnit.MILLISECONDS)
                .subscribe(health -> {
                    droneState.setHealth(health);
                    if (!health.getIsLocalPositionOk()) {
                        toastMessage("LocalPosition failed");
                    } else if (!health.getIsArmable()) {
                        toastMessage("Arm failed");
                    } else if (!health.getIsHomePositionOk()) {
                        toastMessage("HomePosition failed");
                    } else if (!health.getIsGlobalPositionOk()) {
                        toastMessage("GlobalPosition failed");
                    }

                });
    }

    /**
     * 获取home信息 2s
     */
    @SuppressLint("CheckResult")
    private void subscribeHome(){
        telemetry.getHome()
                .doOnError(throwable -> {
                    toastMessage("Failed to get Home: " + throwable.getMessage());
                    Log.e(TAG, "Failed to get Home: " + throwable.getMessage());
                })
                .take(1)
                .subscribe(
                        home -> {
                           droneState.setHome(home);
                        }
                );
    }

    /**
     * 前进
     */
    public void goForward(){
        if(!droneState.isInAir()){
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        next.setPos(current.getLatitude() + OFFSET,
                current.getLongitude(),
                current.getAam(),
                current.getYaw());

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go forward " + OFFSET + "°");
                    Log.i(TAG,"Go forward " + OFFSET + "°");
                    addPosition(next.getLatLng());
                    drawTrajectory();

                })
                .doOnError(throwable -> {
                    toastMessage("Failed to forward: " + throwable.getMessage());
                    Log.e(TAG, "Failed to forward: " + throwable.getMessage());
                })
                .subscribe();

    }

    /**
     * 后退
     */
    public void goBackward(){
        if(!droneState.isInAir()){
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        next.setPos(current.getLatitude() - OFFSET,
                current.getLongitude(),
                current.getAam(),
                current.getYaw());

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go back " + OFFSET + "°");
                    Log.i(TAG,"Go back " + OFFSET + "°");
                    addPosition(next.getLatLng());
                    drawTrajectory();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to go back: " + throwable.getMessage());
                    Log.e(TAG, "Failed to go back: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 左移
     */
    public void goLeftward(){
        if(!droneState.isInAir()){
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        next.setPos(current.getLatitude(),
                current.getLongitude() - OFFSET,
                current.getAam(),
                current.getYaw());

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go left " + OFFSET + "°");
                    Log.i(TAG,"Go left " + OFFSET + "°");
                    addPosition(next.getLatLng());
                    drawTrajectory();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to go left: " + throwable.getMessage());
                    Log.e(TAG, "Failed to go left: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 右移
     */
    public void goRightward(){
        if(!droneState.isInAir()){
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        next.setPos(current.getLatitude(),
                current.getLongitude() + OFFSET,
                current.getAam(),
                current.getYaw());

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go right " + OFFSET + "°");
                    Log.i(TAG,"Go right " + OFFSET + "°");
                    addPosition(next.getLatLng());
                    drawTrajectory();

                })
                .doOnError(throwable -> {
                    toastMessage("Failed to go right: " + throwable.getMessage());
                    Log.e(TAG, "Failed to go right: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 上升
     */
    public void goUpward(){
        if (!droneState.isInAir()) {
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        if (current.getRam() >= 20) {
            showToast("The drone is too high!!");
            return;
        }
        next.setPos(current.getLatitude(),
                current.getLongitude(),
                current.getAam() + 0.5f,
                current.getYaw());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go up 0.5M");
                    Log.i(TAG,"Go up 0.5M");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to go up: " + throwable.getMessage());
                    Log.e(TAG, "Failed to go up: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 下降
     */
    public void goDownward(){
        if(!droneState.isInAir()){
            showToast("Please takeoff before move!");
            return;
        }
        getPosition();
        if(current.getRam() <= 0.6) {
            showToast("The drone is too low!");
            return;
        }
        next.setPos(current.getLatitude(),
                current.getLongitude(),
                current.getAam() - 0.5f,
                current.getYaw());

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Go down 0.5M");
                    Log.i(TAG,"Go down 0.5M");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to go down: " + throwable.getMessage());
                    Log.e(TAG, "Failed to go down: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 左转头45度(逆时针)
     */
    public void turnLeft(){
        getPosition();
        next.setPos(current.getLatitude(),
                current.getLongitude(),
                current.getAam(),
                current.getYaw() - 45f);

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Turn left 45");
                    Log.i(TAG,"Turn left 45");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to turn left: " + throwable.getMessage());
                    Log.e(TAG, "Failed to turn left: " + throwable.getMessage());
                })
                .subscribe();
    }

    /**
     * 右转头45度（顺时针）
     */
    public void turnRight(){
        getPosition();
        next.setPos(current.getLatitude(),
                current.getLongitude(),
                current.getAam(),
                current.getYaw() + 45f);

        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getYaw())
                .doOnComplete( ()->{
                    toastMessage("Turn right 45");
                    Log.i(TAG,"Turn right 45");
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to turn right: " + throwable.getMessage());
                    Log.e(TAG, "Failed to turn right: " + throwable.getMessage());
                })
                .subscribe();
    }


    // 更新TextView的线程  500ms
    class TextViewThread extends Thread {
        @Override
        public void run() {
            while (true){
                try {
                    //每隔1s执行一次
                    Thread.sleep(500);
                    Message msg = new Message();
                    msg.what = MapActivity.UPDATE_POSITION;
                    myHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // 更新无人机状态的线程 1s
    class StateThread extends Thread {
        @Override
        public void run() {
            while (true){
                try {
                    //每隔1s执行一次
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = MapActivity.UPDATE_STATE;
                    myHandler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}