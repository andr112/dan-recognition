package com.dan.rec.bean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/21 11:40
 */
public class DetectionBroadcastReceiver extends BroadcastReceiver {
    protected static final String TAG = "activity-detection-response-receiver";
    DetectionUpdateListener mUpdateListener;

    public DetectionBroadcastReceiver(DetectionUpdateListener updateListener) {
        mUpdateListener = updateListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // ArrayList<DetectedActivity> updatedActivities =
        //         intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
        // updateDetectedActivitiesList(updatedActivities);
        ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
        DebugLog.d(TAG, "onReceive updatedActivities : " + updatedActivities);
        DebugLog.d(TAG, "onReceive mUpdateListener : " + mUpdateListener);
        if (mUpdateListener != null) {
            mUpdateListener.updates(updatedActivities);
        }
    }


}
