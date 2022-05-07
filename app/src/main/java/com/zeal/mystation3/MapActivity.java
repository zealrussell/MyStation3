package com.zeal.mystation3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.zeal.mystation3.entity.MyPosition;
import com.zeal.mystation3.view.SplashActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.mavsdk.System;
import io.mavsdk.action.Action;
import io.mavsdk.mavsdkserver.MavsdkServer;
import io.mavsdk.telemetry.Telemetry;


public class MapActivity extends Activity implements View.OnClickListener {

    //private static final Logger logger = LoggerFactory.getLogger(MapActivity.class);
    private static final DecimalFormat df = new DecimalFormat("#0.000000");
    private static final String TAG = "ZEALTAG";
    private static final String BACKEND_IP_ADDRESS = "127.0.0.1";
    private static int PORT = 0;
    private boolean scaleFlag;
    private boolean connectFlag;

    // 无人机相关
    private final MavsdkServer mavsdkServer = new MavsdkServer();
    private System drone;

    private Action action;
    private Telemetry telemetry;

    // Handler消息
    public static final int UPDATE_POSITION = 1;
    public static final int UPDATE_TOAST = 2;

    // 方向有关变量
    private static double OFFSET = 0.00001;
    private static double LATITUDE = 0.0;
    private static double LONGITUDE = 0.0;
    private static float AAM = 0F;
    private static float RAM = 0F;
    private static double HEADING = 0.0;
    // 一些经纬度
    private static final LatLng GEO_NJUST = new LatLng(32.024927, 118.854396);
    private static LatLng GEO_HOME;
    private static LatLng GEO_CURRENT;
    private static LatLng GEO_DESTINATION;

    //
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

    // UI控件
    private TextView tv;
    private ImageButton scaleBtn;


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        try {
            drone.dispose();
            mavsdkServer.stop();
        }catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }

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
                connect();
                return true;
            } else if (id == R.id.btn_takeoff) {
                takeoff();
                return true;
            } else if (id == R.id.btn_land) {
                land();
                return true;
            } else if (id == R.id.btn_camera) {
                Intent intent = new Intent(MapActivity.this, SplashActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.btn_about) {

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
                moveTo(1);
                break;
            case R.id.btn_backward:
                moveTo(2);
                break;
            case R.id.btn_leftward:
                moveTo(3);
                break;
            case R.id.btn_rightward:
                moveTo(4);
                break;
            case R.id.btn_hold:
                hold();
                break;
            case R.id.btn_upward:
                moveTo(5);
                break;
            case R.id.btn_downward:
                moveTo(6);
                break;
            case R.id.btn_scale:
                changeScale();
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
        getPosition();
        if (GEO_HOME == null)
            GEO_HOME = current.getLatLng();

        Log.i(TAG,"THE HOME IS: " + GEO_HOME);

        // 设置HOME的位置为 起飞地
        points.clear();
        points.add(GEO_HOME);
        clearOverlay(homeOverlay);
        homeOverlay = drawOverlay(GEO_CURRENT, R.drawable.home);

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

                String tvText = "latitude: " + df.format(LATITUDE) + "\n" +
                        "longitude: " + df.format(LONGITUDE) + "\n" +
                        "aam: " + df.format(AAM) + "\n" +
                        "ram: " + df.format(RAM) + "\n" +
                        "heading:" + HEADING;
                //String.format("%.6f",data.getRam());
                tv.setText(tvText);

                // 清除无人机图标 并 重设
                clearOverlay(droneOverlay);
                droneOverlay = drawOverlay(
                        new LatLng(LATITUDE, LONGITUDE)
                        , R.drawable.plane);
            } else if (msg.what == MapActivity.UPDATE_TOAST) {
                showToast((String) msg.obj);
            }
        }
    };

    /**
     * 连接无人机： 检查是否连接-》
     */
    @SuppressLint("CheckResult")
    private void connect(){
        if(drone != null) return;
        PORT = mavsdkServer.run();
        showToast("Mavport is " + PORT);
        drone = new System(BACKEND_IP_ADDRESS, PORT);

        action = drone.getAction();
        telemetry = drone.getTelemetry();


        // 每500ms更新 位置信息
        telemetry.getPosition()
                .doOnComplete(() -> {
                    toastMessage("Position subscribed");
                    Log.i(TAG, "Position subscribed");
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

                            Message updatePosMessage = Message.obtain();
                            updatePosMessage.what = MapActivity.UPDATE_POSITION;
                            myHandler.sendMessage(updatePosMessage);


                            Log.v("MyPOSITION", "GOT THE POSITION:"
                                    + LATITUDE + " "
                                    + LONGITUDE + " "
                                    + AAM + " "
                                    + RAM);
                        }
                );

        telemetry.getHeading().subscribe(heading -> {
            HEADING = heading.getHeadingDeg();
        });


    }

    /**
     *起飞: arm -》 设置飞行高度 -》 起飞
     */
    @SuppressLint("CheckResult")
    public void takeoff(){
        initHome();

        telemetry.getArmed()
                .take(1)
                .subscribe(isArmed -> {
                    toastMessage("Check arm: " + isArmed);
                    if(isArmed) {
                        action.setTakeoffAltitude(2.5f)
                                .andThen(action.takeoff()
                                                .doOnComplete(() -> {
                                                    toastMessage("Take off with armed!");
                                                    Log.i(TAG, "Take off whit armed");
                                                })
                                ).subscribe();
                    } else {
                        action.arm()
                                .delay(200,TimeUnit.MILLISECONDS)
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
                });


    }

    /**
     * 降落
     */
    @SuppressLint("CheckResult")
    public void land(){
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
    public void returnToLaunch() {
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
    private void moveTo(int pos) {
        getPosition();

        if(current.getAam() <= 0.5f) {
            showToast("Please takeoff before move!");
            return;
        }

        if(pos == 1) {
            next.setAll(current.getLatitude() + OFFSET,
                    current.getLongitude(),
                    current.getAam(),
                    current.getRam());
        } else if (pos == 2) {
            next.setAll(current.getLatitude() - OFFSET,
                    current.getLongitude(),
                    current.getAam(),
                    current.getRam());
        } else if (pos == 3) {
            next.setAll(current.getLatitude(),
                    current.getLongitude() - OFFSET,
                    current.getAam(),
                    current.getRam());
        } else if (pos == 4) {
            next.setAll(current.getLatitude(),
                    current.getLongitude() + OFFSET,
                    current.getAam(),
                    current.getRam());
        } else if (pos == 5) {
            if(current.getRam() >= 8.0f) {
                showToast("The drone is to high!");
                return;
            }
            next.setAll(current.getLatitude(),
                    current.getLongitude(),
                    current.getAam() + 0.5f,
                    current.getRam());
        } else if (pos == 6) {
            if (current.getRam() <= 0.7f) {
                showToast("The drone is to low!");
            }
            next.setAll(current.getLatitude(),
                    current.getLongitude(),
                    current.getAam() - 0.5f,
                    current.getRam());
        } else return;


        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + pos + "\n" + next);
                    Log.i(TAG,"Move to " + pos + next);
                    GEO_DESTINATION = next.getLatLng();
                    if(pos != 5 && pos != 6) {
                        addPosition(GEO_DESTINATION);
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
            OFFSET = 0.0001;
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


    /*----------------------------------- 一些工具函数 ------------------------------*/

    @Deprecated
    private void checkConnect(){
        if(drone == null) return;
        drone.getCore()
                .getConnectionState()
                .doOnError(throwable -> {
                    toastMessage("Failed to connect: " + throwable.getMessage());
                    connectFlag = true;
                })
                .subscribe(connectionState -> {
                    if (connectionState.getIsConnected() == null) connectFlag = true;
                    connectFlag = connectionState.getIsConnected();
                    Log.v(TAG, "CheckConnect: " + connectFlag);
                });

    }

    /**
     * 更新位置，刷新
     */
    private void getPosition(){
        current.setAll(LATITUDE,LONGITUDE,AAM,RAM);
        GEO_CURRENT = current.getLatLng();
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






    /*!----------------Deprecated----------*/
    @Deprecated
    public void goForward(){
        getPosition();
        next.setAll(current.getLatitude() + OFFSET,
                current.getLongitude(),
                current.getAam(),
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to "  + "\n" + next);
                    Log.i(TAG,"Move to "  + next);
                    GEO_DESTINATION = next.getLatLng();
                    addPosition(GEO_DESTINATION);
                    drawTrajectory();

                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();

    }

    @Deprecated
    public void goBackward(){
        getPosition();
        next.setAll(current.getLatitude() - OFFSET,
                current.getLongitude(),
                current.getAam(),
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + "\n" + next);
                    Log.i(TAG,"Move to " + next);
                    GEO_DESTINATION = next.getLatLng();
                    addPosition(GEO_DESTINATION);
                    drawTrajectory();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }

    @Deprecated
    public void goLeftward(){
        getPosition();
        next.setAll(current.getLatitude(),
                current.getLongitude() + OFFSET,
                current.getAam(),
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + "\n" + next);
                    Log.i(TAG,"Move to " + next);
                    GEO_DESTINATION = next.getLatLng();
                    addPosition(GEO_DESTINATION);
                    drawTrajectory();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }

    @Deprecated
    public void goRightward(){
        getPosition();
        next.setAll(current.getLatitude(),
                current.getLongitude() - OFFSET,
                current.getAam(),
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + "\n" + next);
                    Log.i(TAG,"Move to " + next);
                    GEO_DESTINATION = next.getLatLng();
                    addPosition(GEO_DESTINATION);
                    drawTrajectory();

                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }

    @Deprecated
    public void goUpward(){
        getPosition();
        next.setAll(current.getLatitude(),
                current.getLongitude(),
                current.getAam() + 0.5f,
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + "\n" + next);
                    Log.i(TAG,"Move to " + next);
                    GEO_DESTINATION = next.getLatLng();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }

    @Deprecated
    public void goDownward(){
        getPosition();
        next.setAll(current.getLatitude() + OFFSET,
                current.getLongitude(),
                current.getAam() - 0.5f,
                current.getRam());
        action.gotoLocation(next.getLatitude(), next.getLongitude(), next.getAam(),next.getRam())
                .doOnComplete( ()->{
                    toastMessage("    Move to " + "\n" + next);
                    Log.i(TAG,"Move to " + next);
                    GEO_DESTINATION = next.getLatLng();
                })
                .doOnError(throwable -> {
                    toastMessage("Failed to move: " + throwable.getMessage());
                    Log.e(TAG, "Failed to move: " + throwable.getMessage());
                })
                .subscribe();
    }




}