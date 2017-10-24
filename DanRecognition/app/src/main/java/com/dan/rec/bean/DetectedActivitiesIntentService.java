package com.dan.rec.bean;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.dan.rec.RecServer;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * IntentService for handling incoming intents that are generated as a result of requesting
 * activity updates using
 * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";

    private BroadcastReceiver mBR;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                Intent a = new Intent(DetectedActivitiesIntentService.this, RecServer.class);
                startService(a);
            }
        };
        IntentFilter mIF = new IntentFilter();
        mIF.addAction(Constants.BROADCAST_ACTION_DET);
        registerReceiver(mBR, mIF);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_REC);
        sendBroadcast(intent);
        unregisterReceiver(mBR);
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        if (result == null || result.getProbableActivities() == null) {
            DebugLog.d(TAG, " onHandleIntent result : " + result);
            return;
        }
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        ArrayList<DetectedActivity> detectedActivity = Recognition.getInstence().weightedMean(detectedActivities);
        DebugLog.d(TAG, " onHandleIntent : " + detectedActivity);
        if (detectedActivity != null && detectedActivity.size() > 0) {
            // Broadcast the list of detected activities.
            // localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
            localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivity);
            sendBroadcast(localIntent);
        }
    }
}