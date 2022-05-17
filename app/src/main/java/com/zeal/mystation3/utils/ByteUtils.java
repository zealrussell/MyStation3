package com.zeal.mystation3.utils;


import java.text.DecimalFormat;

/**
 *
 */

public class ByteUtils {


    private ByteUtils() {
    }


    /**
     * 将byte[2]转换成short
     *
     * @param b
     * @return
     */
    public static short byte2Short(byte[] b) {
        return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
    }

    public static int byte2ToInt(byte[] b) {
        return (((b[0] & 0xff) << 8) | (b[1] & 0xff));
    }

    public static short byte2ToShort2(byte[] b) {
        return (short)((b[1] & 255) << 8 | b[0] & 255);
    }

    public static short byteToShort(byte[] b) {
        return (short) (((b[1] & 0xff) << 8) | (b[0] & 0xff));
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    public static float toFloat(byte b){
        int i = byteToInt(b);
        return i / 10f;
    }

    public static String getFloatToString(float num){
        DecimalFormat df = new DecimalFormat("###0.00");
        return df.format(num);
    }


    /**
     * 注释：short到字节数组的转换！
     * @return
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();
            //将最低位保存在最低位
            temp = temp >> 8; // 向右移8位
        }
        return b;
    }
}
