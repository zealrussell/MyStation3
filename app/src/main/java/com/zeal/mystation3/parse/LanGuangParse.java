package com.zeal.mystation3.parse;


import com.zeal.mystation3.listener.OnDataReceiveListener;

public class LanGuangParse {

    public static final int TYPE_DRONE_STATUS = 3000;   //飞机状态信息
    public static final int TYPE_AROUND_INFO = 3001;   //应答环绕提示
    public static final int TYPE_GPS_INFO = 3002;   //GPS位置信息
    public static final int TYPE_POINT_INFO = 3003;   //航点信息
    public static final int TYPE_SETTING_INFO = 3004;   //基本设置信息
    public static final int TYPE_REAL_TIME_INFO3 = 3005;   //黑飞--返回飞机实时信息3
    public static final int TYPE_REAL_TIME_INFO3B = 3006;   //黑飞--返回飞机实时信息3B
    public static final int TYPE_TEST_INFO = 3007;   //黑飞--返回飞机实时信息3B

    /**
     * 数据解析
     */
    public static void parse(OnDataReceiveListener analysisListener, byte[] data) {
        byte[] newData = new byte[data.length - 3 - 1];
        System.arraycopy(data, 3, newData, 0, newData.length);
        switch (data[1]) {
            case (byte) 0x8B:  // 飞机状态信息
                if (analysisListener != null) {
                    analysisListener.data(TYPE_DRONE_STATUS, newData);
                }
                break;

            case (byte) 0x83://应答环绕数据接收
                if (analysisListener != null) {
                    analysisListener.data(TYPE_AROUND_INFO, new byte[]{data[3]});
                }
                break;

            case (byte) 0x8C://GPS信息
                if (analysisListener != null) {
                    analysisListener.data(TYPE_GPS_INFO, new byte[]{data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14], data[15]});
                }
                break;
            case (byte) 0x84://应答航点数据接收
                if (analysisListener != null) {
                    analysisListener.data(TYPE_POINT_INFO, new byte[]{data[3]});
                }
                break;
            case (byte) 0x8D:
                if (analysisListener != null) {
                    analysisListener.data(TYPE_TEST_INFO, newData);
                }
                break;
            case(byte) 0x8A:
//                MyApplication.lgDroneType = "DFLLG401";
//                LogUtils.d("lgDroneType", MyApplication.lgDroneType);
                if (analysisListener != null) {
                    analysisListener.data(TYPE_SETTING_INFO, new byte[]{data[3], data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11], data[12], data[13], data[14]});
                }
                break;
            case (byte) 0x8E:
                if (analysisListener != null) {
                    analysisListener.data(TYPE_REAL_TIME_INFO3, new byte[]{data[3], data[4], data[5], data[6]});
                }
                break;
        }
    }
}
