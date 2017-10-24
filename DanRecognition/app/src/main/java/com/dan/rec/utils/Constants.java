package com.dan.rec.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.dan.rec.R;
import com.dan.rec.RecognitionApplication;
import com.dan.rec.bean.DetectedActivitiesIntentService;
import com.google.android.gms.location.DetectedActivity;

/**
 * Constants
 */
public final class Constants {

    private Constants() {
    }

    public static final String PACKAGE_NAME = "com.dan.rec";
    public static final String BROADCAST_ACTION = "com.dan.rec.BROADCAST_ACTION";
    public static final String BROADCAST_ACTION_STATUS = "com.dan.rec.BROADCAST_ACTION_STATUS";
    public static final String BROADCAST_ACTION_REC = "com.dan.rec.BROADCAST_ACTION_REC";
    public static final String BROADCAST_ACTION_DET = "com.dan.rec.BROADCAST_ACTION_DET";

    public static final String ACTIVITY_EXTRA_STATUS = PACKAGE_NAME + ".ACTIVITY_EXTRA_STATUS";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";
    public static final String ACTIVITY_EXTRA_TYPE = PACKAGE_NAME + ".ACTIVITY_EXTRA_TYPE";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES";

    public static final String ACTIVITY_UPDATES_REQUESTED_KEY = PACKAGE_NAME +
            ".ACTIVITY_UPDATES_REQUESTED";

    public static final String DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";
    public static final String DETECTED_RESULT = PACKAGE_NAME + ".DETECTED_RESULT";

    public static final String TimeF_HM = "%tF %<tR";
    public static final String TimeF_HMS = "%tF %<tT";

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;

    /**
     * List of DetectedActivity types that we monitor in this sample.
     */
    public static final int[] MONITORED_ACTIVITIES = {
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.IN_VEHICLE
    };
    /* DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
            */

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    public static PendingIntent getActivityDetectionPendingIntent(Context context) {
        if (context == null) {
            context = RecognitionApplication.getContext();
        }
        Intent intent = new Intent(context, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
