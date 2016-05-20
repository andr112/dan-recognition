package com.dan.rec.utils;

import android.util.Log;

/**
 * Created by zhangdan on 16/4/16.
 */
public class DebugLog {
    private static String Tag = "xixitest";

    public static void d(String tag, String info) {
        Log.d(Tag, tag + " --> " + info);
    }

    public static void i(String tag, String info) {
        Log.i(Tag, tag + " --> " + info);
    }

    public static void e(String tag, String info) {
        Log.e(Tag, tag + " --> " + info);
    }
}
