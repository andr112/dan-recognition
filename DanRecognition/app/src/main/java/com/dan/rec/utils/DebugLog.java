package com.dan.rec.utils;

import android.os.Build;
import android.util.Log;

import com.dan.rec.BuildConfig;

/**
 * Created by zhangdan on 16/4/16.
 */
public class DebugLog {
    private static String Tag = "xixitest" + BuildConfig.VERSION_NAME;

    public static void d(String tag, String info) {
        Log.d(Tag, Build.MANUFACTURER + "_" + Build.MODEL + "_" + Build.VERSION.RELEASE + " --> " + tag + " --> " + info);
    }

    public static void i(String tag, String info) {
        Log.i(Tag, tag + " --> " + info);
    }

    public static void e(String tag, String info) {
        Log.e(Tag, tag + " --> " + info);
    }
}
