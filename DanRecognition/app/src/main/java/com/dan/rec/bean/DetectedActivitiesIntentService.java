package com.dan.rec.bean;

import android.app.IntentService;
import android.content.Intent;

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
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        ArrayList<DetectedActivity> detectedActivity = Recognition.getInstence().weightedMean(detectedActivities);
        if (detectedActivity != null && detectedActivity.size() > 0) {
            localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivity);
        }
        DebugLog.d(TAG, " onHandleIntent : " + detectedActivity);

        // Broadcast the list of detected activities.
        // localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        sendBroadcast(localIntent);
    }
}