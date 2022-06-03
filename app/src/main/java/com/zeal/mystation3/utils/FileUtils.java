package com.zeal.mystation3.utils;

import android.util.Log;

import com.zeal.mystation3.entity.MyPosition;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class FileUtils {
    private static final String TAG = "FileUtils";
    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String content, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFile(filePath, fileName);
        String mFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String mContent = content + "\r\n";
        try {
            File file = new File(mFilePath);
            if (!file.exists()) {
                Log.d(TAG, "创建文件: " + mFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile mRandomAccessFile = new RandomAccessFile(file, "rwd");
            mRandomAccessFile.seek(file.length());
            mRandomAccessFile.write(mContent.getBytes());
            mRandomAccessFile.close();
        } catch (IOException e) {
            Log.d(TAG, "写入错误: " + e.toString());
        }
    }

    public static boolean writeTxtToFileFromList(List<MyPosition> myPositionListst, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFile(filePath, fileName);
        String mFilePath = filePath + fileName;

        try {
            File file = new File(mFilePath);
            if (!file.exists()) {
                Log.d(TAG, "创建文件: " + mFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile mRandomAccessFile = new RandomAccessFile(file, "rwd");
            mRandomAccessFile.seek(file.length());
            mRandomAccessFile.write("------------开始日志------------/n".getBytes());
            for(MyPosition position : myPositionListst){
                // 每次写入时，都换行写
                String mContent = position.formLog() + "\r\n";
                mRandomAccessFile.write(mContent.getBytes());
            }
            mRandomAccessFile.write("------------结束日志------------".getBytes());
            mRandomAccessFile.close();
            return true;
        } catch (IOException e) {
            Log.d(TAG, "写入错误: " + e.getMessage());
            return false;
        }
    }

    //生成文件
    public static File makeFile(String filePath, String fileName) {
        File file = null;
        makeDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.d(TAG, "生成文件错误: " + e.toString());
        }
        return file;
    }
    //生成文件夹
    public static void makeDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.d(TAG, "生成文件夹错误: " + e.toString());
        }
    }
}
