package com.dan.rec.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LogTrace
{
    private static String TOG_FLAG = "DanRecognition";

    private static Context sContext;
    private static final String TimeF_HM = "%tF %<tR";
    private static final String TimeF_HMS = "%tF %<tT";
    
    public static void print(String message)
    {
        Log.v(TOG_FLAG, message);
    }
    
     private static FileOutputStream outputStream;
     
     public synchronized static void init(Context context)
     {
         if (null == outputStream)
         {
             sContext = context;
             try
             {
                 outputStream = new FileOutputStream(getLogFile(), true);
             }
             catch (FileNotFoundException e)
             {
                 e.printStackTrace();
             }
         }
     }
    
    private static File getLogFile()
    {
        String localCachePath = "";
        if (Environment.MEDIA_MOUNTED.endsWith(Environment.getExternalStorageState()))
        {
            localCachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        else
        {
            localCachePath = sContext.getCacheDir().getAbsolutePath();
        }
        localCachePath +=  "/" +TOG_FLAG;
        File file = new File(localCachePath);
        if (!file.exists())
        {
            file.mkdir();
        }
        file = new File(localCachePath + "/log.txt");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return file;
    }
    
    public static void d(String TAG, String methodName, String info)
    {
        Log.d(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info);
    }
    
    public static void e(String TAG, String methodName, String info)
    {
        Log.e(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info);
    }
    
    public static void i(String TAG, String methodName, Object info)
    {
        Log.i(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info.toString());
    }
    
    public static void v(String TAG, String methodName, String info)
    {
        Log.v(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info);
    }
    
    public static void w(String TAG, String methodName, String info)
    {
        Log.w(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info);
    }

    public static void write(String TAG, String methodName, String info, boolean isTimeSensitive) {
        Log.w(TOG_FLAG, TAG + " ---> " + methodName + " ---> " + info);
        writeToFile(info, isTimeSensitive ? TimeF_HMS : TimeF_HM);
    }

    private synchronized static void writeToFile(String info) {
        writeToFile(info, TimeF_HM);
    }

    private synchronized static void writeToFile(String info, String timeFormatStr) {
        try
         {
             outputStream.write((String.format(timeFormatStr, System.currentTimeMillis()) + "   " + info + "\n").getBytes());
             outputStream.flush();
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
    }
    
}
