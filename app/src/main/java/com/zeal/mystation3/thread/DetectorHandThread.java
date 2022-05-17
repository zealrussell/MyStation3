package com.zeal.mystation3.thread;

import android.os.Handler;

import com.vison.baselibrary.utils.LogUtils;
import com.vison.macrochip.sdk.JNIManage;

/**
 * 手势识别线程
 */
public class DetectorHandThread extends Thread {
    public static final int DETECTOR_HAND_DETAILS = 180500;
    public static final int DETECTOR_HAND_PHOTOGRAPH = 180501;      // 拍照
    public static final int DETECTOR_HAND_RECORD = 180502;          // 录像
    private boolean action = true;     // 是否执行
    private byte[] nowYuvData;
    private int yuvWidth;
    private int yuvHeight;
    private Handler callBackHandler;
    private boolean recording;          // 录像中
    private boolean run = true;
    private int detectorType = -1;   //0=录像 1=拍照

    public DetectorHandThread(Handler handler) {
        this.callBackHandler = handler;
    }

    @Override
    public void run() {
        super.run();
        while (run) {
            if (null != nowYuvData && action) {
                long lastTime = System.currentTimeMillis();
                int state = JNIManage.detectorHand(yuvWidth, yuvHeight, nowYuvData, true);
                String s = "手势检测耗时=" + (System.currentTimeMillis() - lastTime) + "\n结果=" + JNIManage.handConvert(state) + "  " + state;
                LogUtils.print(s);

                if (recording && state != 0) {
                    LogUtils.i("录像中，跳过");
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                } else if (-2 == state) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (-1 == state) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (detectorType == -1) {
                    action = false;
                    if (0 == state) {
                        callBackHandler.sendEmptyMessage(DETECTOR_HAND_RECORD);
                    } else {
                        callBackHandler.sendEmptyMessage(DETECTOR_HAND_PHOTOGRAPH);
                    }
                } else if (0 == state && 0 == detectorType) {
                    action = false;
                    callBackHandler.sendEmptyMessage(DETECTOR_HAND_RECORD);
                } else if (1 == state && 1 == detectorType) {
                    action = false;
                    callBackHandler.sendEmptyMessage(DETECTOR_HAND_PHOTOGRAPH);
                }

                nowYuvData = null;
            }
        }
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void setYuvInfo(byte[] nowYuvData, int width, int height){
        this.nowYuvData = nowYuvData;
        this.yuvWidth = width;
        this.yuvHeight = height;
    }


    public void setDetectorType(int detectorType) {
        this.detectorType = detectorType;
    }

    public void cancel() {
        run = false;
        this.interrupt();
    }
}
