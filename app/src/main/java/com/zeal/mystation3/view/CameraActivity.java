package com.zeal.mystation3.view;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vison.baselibrary.base.BasePlayActivity;
import com.vison.baselibrary.listeners.OnPhotographListener;
import com.vison.baselibrary.listeners.OnRecordListener;
import com.vison.baselibrary.model.MediaPixel;
import com.vison.baselibrary.model.MediaSave;
import com.vison.baselibrary.utils.LogUtils;
import com.vison.baselibrary.utils.WifiDeviceUpgradeUtils;

import com.zeal.mystation3.R;
import com.zeal.mystation3.application.MyApplication;
import com.zeal.mystation3.thread.DetectorHandThread;

import java.io.File;

public class CameraActivity extends BasePlayActivity implements View.OnClickListener {

    private static final int CLICK_FILTER = 100; // 点击事件过滤
    private static final int RECOVERY_DETECTOR_ACTION = 101; // 恢复识别处理
    private FrameLayout glLayout;
    private GLSurfaceView glSurfaceView;
    private Button takePictureBtn, recordVideoBtn, wifiSignalBtn, updateFirmwareBtn, startPlayBtn, stopPlayBtn, detectorHandBtn;
    private MediaPixel mediaPixel = null;
    private boolean isRecoding;
    private boolean isRecordAudio; // 是否录音（请注意权限）
    private String saveFilePath;
    protected float mScaleFactor = 1.0f; //缩放因数 默认1.0
    private WifiDeviceUpgradeUtils wifiDeviceUpgradeUtils;
    private ProgressDialog progressDialog;
    private MediaSave mediaSave = MediaSave.LOCAL_REMOTE; // 保存类型
    private DetectorHandThread detectorHandThread;

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        //1、@NonNull 表示参数、字段或方法返回值永远不能为null。这是一个标记注释，没有特定属性。
        //2、负责处理来自其它线程的消息
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WifiDeviceUpgradeUtils.UPLOAD_FAIL:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    showToast("上传失败");
                    break;
                case WifiDeviceUpgradeUtils.UPLOAD_PROGRESS:
                    int progress = (int) msg.obj;
                    if (progressDialog != null) {
                        progressDialog.setMessage("正在上传");
                        progressDialog.setProgress(progress);
                    }
                    break;
                case WifiDeviceUpgradeUtils.UPLOAD_COMPLETE:
                    if (progressDialog != null) {
                        progressDialog.setMessage("上传完成、正在检查升级文件");
                    }
                    if (wifiDeviceUpgradeUtils != null) {
                        wifiDeviceUpgradeUtils.startUpgrade();
                    }
                    break;
                case WifiDeviceUpgradeUtils.UPGRADE_CHECK_ERROR:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    showToast("文件不正确，停止升级");
                    break;
                case WifiDeviceUpgradeUtils.UPGRADE_START:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                    showToast("开始升级，等待升级成功，重新连接");
                    LogUtils.i("开始升级，等待升级成功，重新连接");
                    break;
                case CLICK_FILTER:
                    startPlayBtn.setEnabled(true);
                    stopPlayBtn.setEnabled(true);
                    break;
                case DetectorHandThread.DETECTOR_HAND_PHOTOGRAPH:
                    // 识别到拍照回调
                    if (detectorHandThread != null) {
                        // 暂停识别 处理拍照
                        detectorHandThread.setAction(false);
                    }
                    showToast("识别到拍照");
                    takePictureBtn.callOnClick();

                    myHandler.sendEmptyMessageDelayed(RECOVERY_DETECTOR_ACTION, 5000);
                    break;
                case DetectorHandThread.DETECTOR_HAND_RECORD:
                    // 识别到录像回调
                    if (detectorHandThread != null) {
                        // 暂停识别 处理录像
                        detectorHandThread.setAction(false);
                    }
                    showToast("识别到录像");
                    recordVideoBtn.callOnClick();
                    myHandler.sendEmptyMessageDelayed(RECOVERY_DETECTOR_ACTION, 5000);
                    break;
                case RECOVERY_DETECTOR_ACTION:
                    if (detectorHandThread != null) {
                        detectorHandThread.setAction(true);
                    }
                    break;
            }
        }
    };

    //实例化一个抽象类
    private final OnRecordListener videoOnRecordListener = new OnRecordListener() {
        @Override
        public void onStart() {
            recordVideoBtn.setText("录像中");
        }

        @Override
        public void onStop() {
            recordVideoBtn.setText("录像");
            if (mediaSave != MediaSave.REMOTE) {
                showToast("视频已保存：" + saveFilePath);
            } else {
                showToast("录像完成");
            }
        }

        @Override
        public void onError(Throwable throwable) {
            super.onError(throwable);
            // 录像异常
            LogUtils.e(throwable);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 不休眠
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);

        findViewById(R.id.stop_play_btn).setOnClickListener(this);
        findViewById(R.id.start_play_btn).setOnClickListener(this);
        findViewById(R.id.take_picture_btn).setOnClickListener(this);
        findViewById(R.id.record_video_btn).setOnClickListener(this);

        takePictureBtn = findViewById(R.id.take_picture_btn);
        recordVideoBtn = findViewById(R.id.record_video_btn);
        startPlayBtn = findViewById(R.id.start_play_btn);
        stopPlayBtn = findViewById(R.id.stop_play_btn);
        glLayout = findViewById(R.id.gl_layout);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeCallbacksAndMessages(null);

        if (isStartPlayed()) {
            stopPlay();
        }
    }

    //监听事件处理
    @Override
    public void onClick(View v) {
        //用来判断ID 执行相关操作
        switch (v.getId()) {
            case R.id.stop_play_btn:
                if (true) {
                    if (isStartPlayed()) {
                        // 停止录像
                        if (isRecoding) {
                            recordVideoBtn.callOnClick();
                        }

                        stopPlay();
                        setOutputFrame(false);
                        takePictureBtn.setEnabled(false);
                        recordVideoBtn.setEnabled(false);

                        stopPlayBtn.setEnabled(false);
                        startPlayBtn.setEnabled(false);
                        myHandler.removeMessages(CLICK_FILTER);
                        myHandler.sendEmptyMessageDelayed(CLICK_FILTER, 3000);
                    }
                } else {
                    showToast("未连接wifi相机");
                }
                break;
            case R.id.start_play_btn:
                if (true) {
                    if (!isBindSurfaceViewed()) {
                        // 由于GLSurfaceView的设计，不能在布局文件里定义
                        glSurfaceView = new GLSurfaceView(this);
                        bindSurfaceView(glSurfaceView);
                        glLayout.addView(glSurfaceView);
                    }
                    if (!isStartPlayed()) {
                        startPlay();
                        setOutputFrame(true);
                        takePictureBtn.setEnabled(true);
                        recordVideoBtn.setEnabled(true);
                    }
                    stopPlayBtn.setEnabled(false);
                    startPlayBtn.setEnabled(false);
                    myHandler.removeMessages(CLICK_FILTER);
                    myHandler.sendEmptyMessageDelayed(CLICK_FILTER, 3000);
                } else {
                    showToast("未连接wifi相机");
                }
                break;
            case R.id.take_picture_btn:// 拍照
                if (true) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请权限
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
                        showToast("没有写入权限");
                        return;
                    }

                    if (mediaSave != MediaSave.REMOTE) {
                        // 创建照片保存路径
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
                        saveFilePath = new File(dir, fileName).getAbsolutePath();
                    } else {
                        saveFilePath = null;
                    }

//                    如果板子插内存卡，取内存卡原图；不插内存卡，取视频流截图。
                    mediaPixel = null;

                    takePicture(mediaPixel, mediaSave, saveFilePath, new OnPhotographListener() {
                        @Override
                        public void onSuccess() {
                            if (mediaSave != MediaSave.REMOTE) {
                                showToast("照片已保存：" + saveFilePath);
                            } else {
                                showToast("拍照成功");
                            }
                        }
                    });
                } else {
                    showToast("未连接wifi相机");
                }
                break;
            case R.id.record_video_btn: // 录像
                if (true) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请权限
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
                        showToast("没有写入权限");
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请权限
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1234);
                        showToast("没有读取权限");
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED) {
                        // 申请权限
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1234);
                        showToast("没有录音权限");
                        return;
                    }

                    isRecoding = !isRecoding;

                    if (mediaSave != MediaSave.REMOTE) {
                        // 创建视频保存路径
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        String fileName = "Video_" + System.currentTimeMillis() + ".mp4";
                        saveFilePath = new File(dir, fileName).getAbsolutePath();
                    } else {
                        saveFilePath = null;
                    }


                    mediaPixel = null;
                    isRecordAudio = false;
                    recordVideo(mediaPixel, mediaSave, isRecoding, isRecordAudio, saveFilePath, videoOnRecordListener);
                } else {
                    showToast("未连接wifi相机");
                }
                break;
            case R.id.scale_add_btn:
                mScaleFactor += 0.2;
                setZoomScale(mScaleFactor);
                break;
            case R.id.scale_sub_btn:
                mScaleFactor -= 0.2;
                setZoomScale(mScaleFactor);
                break;
        }
    }

    /**
     * 原始yuv流数据
     * @param bytes
     * @param width
     * @param height
     */
    @Override
    public void onUpdateFrame(byte[] bytes, int width, int height) {
        super.onUpdateFrame(bytes, width, height);

        if (detectorHandThread != null) {
            detectorHandThread.setYuvInfo(bytes, width, height);
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}