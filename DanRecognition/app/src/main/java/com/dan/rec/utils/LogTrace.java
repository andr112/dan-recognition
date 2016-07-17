package com.dan.rec.utils;

import android.os.Environment;

import com.dan.rec.RecognitionApplication;
import com.dan.rec.db.DbManager;
import com.dan.rec.db.LogItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogTrace {
    private static String TOG_FLAG = "DanRecognition";
    private static LogTrace instance;

    public static void print(String message) {
        DebugLog.i(TOG_FLAG, message);
    }

    private FileOutputStream outputStream;

    private LogTrace() {
    }

    public static LogTrace getInstance() {
        if (instance == null) {
            synchronized (LogTrace.class) {
                if (instance == null) {
                    instance = new LogTrace();
                }
            }
        }
        return instance;
    }

    public void init() {
        if (null == outputStream) {
            try {
                outputStream = new FileOutputStream(getLogFile(), true);
            } catch (FileNotFoundException e) {
                DebugLog.e(TOG_FLAG, "init  IOException : " + e.toString());
                e.printStackTrace();
            }
        }
    }

    public void clean() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                DebugLog.e(TOG_FLAG, "clean  IOException : " + e.toString());
                e.printStackTrace();
            }
            outputStream = null;
        }
    }

    private String getFilePath() {
        String file_dir = "";
        // SD卡是否存在
        boolean isSDCardExist = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        // Environment.getExternalStorageDirectory()相当于File file=new File("/sdcard")
        boolean isRootDirExist = Environment.getExternalStorageDirectory().exists();
        DebugLog.e(TOG_FLAG, "getFilePath isSDCardExist : " +isSDCardExist+" "+ isRootDirExist);
        if (isSDCardExist && isRootDirExist) {
            file_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            // MyApplication.getInstance().getFilesDir()返回的路劲为/data/data/PACKAGE_NAME/files，其中的包就是我们建立的主Activity所在的包
            file_dir = RecognitionApplication.getInstance().getFilesDir().getAbsolutePath();
        }
        return file_dir + "/" + TOG_FLAG;
    }

    private File getLogFile() {
        String filePath = getFilePath();
        DebugLog.e(TOG_FLAG, "getLogFile localCachePath : " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
        String fileName = "DebugLog.txt";
        file = new File(file, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                DebugLog.e(TOG_FLAG, "getLogFile IOException :" + e.toString());
                e.printStackTrace();
            }
        }
        return file;
    }

    public void write(String TAG, String lable, String info, boolean isTimeSensitive) {
        DebugLog.d(TOG_FLAG, TAG + " ---> " + lable + " ---> " + info);
        long time = System.currentTimeMillis();
        LogItem logItem = new LogItem(time, info, isTimeSensitive);
        if (!isTimeSensitive) {
            DbManager.insertGoods(logItem);
        }
        writeToFile(logItem.getLog());
    }

    public void writeCommonLog(String info) {
        long time = System.currentTimeMillis();
        writeToFile(String.format(Constants.TimeF_HM, time) + " : " + info);
    }

    private void writeToFile(String info) {
        if (outputStream == null) {
            init();
            DebugLog.e(TOG_FLAG, "writeToFile outputStream :" + outputStream);
        }
        try {
            outputStream.write((info + "\n").getBytes());
            outputStream.flush();
        } catch (FileNotFoundException e) {
            DebugLog.e(TOG_FLAG, "writeToFile FileNotFoundException :" + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            DebugLog.e(TOG_FLAG, "writeToFile IOException :" + e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            DebugLog.e(TOG_FLAG, "writeToFile Exception :" + e.toString());
            e.printStackTrace();
        }
    }
}
