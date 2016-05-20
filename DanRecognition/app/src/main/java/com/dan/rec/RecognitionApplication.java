package com.dan.rec;

import android.content.Context;
import android.content.Intent;
import android.os.Debug;

import com.dan.rec.bean.Recognition;
import com.dan.rec.utils.LogTrace;

import org.litepal.LitePalApplication;

/**
 * Created by zhangdan on 16/4/30.
 */
public class RecognitionApplication extends LitePalApplication {
    private static final String TAG = RecognitionApplication.class.getSimpleName();

    private static RecognitionApplication mSelf;

    public static RecognitionApplication getInstance() {
        return mSelf;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSelf = this;
        initApp();
    }

    private void initApp() {
        //动态调试保护
        if (Debug.isDebuggerConnected()) {
            killProcessMySelf(this);
        }
        LogTrace.init(this);
        Recognition.getInstence().init();
    }

    private void killProcessMySelf(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_HOME);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
