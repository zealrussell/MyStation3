package com.zeal.mystation3.utils;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.Collection;

/**
 * Java对象的常用处理
 */
public final class ObjectUtils {
    private ObjectUtils() {
    }

    public static boolean isEmpty(Object obj) {
        if (obj instanceof CharSequence) {
            return isEmpty((CharSequence) obj);
        } else if (obj instanceof Collection) {
            return isEmpty((Collection) obj);
        }
        return obj == null;
    }

    public static boolean isEmpty(Bundle bundle) {
        return bundle == null || bundle.isEmpty();
    }

    public static boolean isEmpty(Collection list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean isEmpty(T[] list) {
        return list == null || list.length == 0;
    }

    public static boolean isEmpty(CharSequence value) {
        return TextUtils.isEmpty(value);
    }

    public static int toInt(CharSequence value) {
        if (isEmpty(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static long toLong(CharSequence value) {
        if (isEmpty(value)) {
            return -1;
        }
        try {
            return Long.parseLong(value.toString().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static double toDobule(CharSequence value) {
        if (isEmpty(value)) {
            return -1;
        }
        try {
            return Double.parseDouble(value.toString().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int toInt(boolean boo) {
        if (boo) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean toBoolean(int i) {
        if (i == 1) {
            return true;
        }
        return false;
    }

    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value.toString().trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String intToIp(int ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }


    public static int bytesToInt2(byte[] b) {
        int mask = 0xff;
        int temp = 0;
        int n = 0;
        for (int i = 0; i < b.length; i++) {
            n <<= 8;
            temp = b[i] & mask;
            n |= temp;
        }
        return n;
    }

    /**
     * 将byte[2]转换成short
     * 小端在前，大端在后
     *
     * @param b
     * @return
     */
    public static short byte2ToShort(byte[] b) {
        return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
    }

    /**
     * 将byte[2]转换成short
     * * 大端在前，小端在后
     *
     * @param b
     * @return
     */
    public static short byte2ToShort2(byte[] b) {
        return (short) (((b[1] & 0xff) << 8) | (b[0] & 0xff));
    }

    /**
     * bytes 转 hex字符
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if ((src == null) || (src.length <= 0)) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * convert HexString to byte[]
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToByte(String hexString) {
        if (isEmpty(hexString)) {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1)
                return byteArray;
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

    /**
     * hexString 转 字符串
     *
     * @param src
     * @return
     */
    public static String hexStringToString(String src) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < src.length() / 2; i++) {
            temp.append((char) Integer.valueOf(src.substring(i * 2, i * 2 + 2),
                    16).byteValue());
        }
        return temp.toString();
    }

    private static String toHexUtil(int n) {
        String rt = "";
        switch (n) {
            case 10:
                rt += "A";
                break;
            case 11:
                rt += "B";
                break;
            case 12:
                rt += "C";
                break;
            case 13:
                rt += "D";
                break;
            case 14:
                rt += "E";
                break;
            case 15:
                rt += "F";
                break;
            default:
                rt += n;
        }
        return rt;
    }

    public static String toHex(int n) {
        StringBuilder sb = new StringBuilder();
        if (n / 16 == 0) {
            return toHexUtil(n);
        } else {
            String t = toHex(n / 16);
            int nn = n % 16;
            sb.append(t).append(toHexUtil(nn));
        }
        return sb.toString();
    }

    public static byte[] parseAscii(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++)
            sb.append(toHex(bs[i]));
        return hexStringToByte(sb.toString());
    }

    /**
     * byte[4]转int
     *
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static int toInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    public static byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

}
