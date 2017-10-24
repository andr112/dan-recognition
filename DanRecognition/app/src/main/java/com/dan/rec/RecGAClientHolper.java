package com.dan.rec;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/7/17 16:38
 */
public class RecGAClientHolper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private final String TAG = "RecGAClientHolper";

    private GoogleApiClient mGoogleApiClient;

    private boolean isRequested;

    private static RecGAClientHolper instance = new RecGAClientHolper();

    private RecGAClientHolper() {
        buildGoogleApiClient();
    }

    public static RecGAClientHolper getInstance() {
        return instance;
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    private synchronized void buildGoogleApiClient() {
        DebugLog.i(TAG, "buildGoogleApiClient ..");
        mGoogleApiClient = new GoogleApiClient.Builder(RecognitionApplication.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // setUpdatesRequestedState(true);
        DebugLog.i(TAG, "onConnected ..");
        requestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        DebugLog.i(TAG, "Connection suspended");
        connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // setUpdatesRequestedState(false);
        String tip = "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode();
        DebugLog.i(TAG, tip);
        showToast(tip);
    }

    @Override
    public void onResult(Status status) {
        boolean isSuc = status.isSuccess();
        DebugLog.i(TAG, "onResult : " + isSuc);
        if (isSuc) {
            // Toggle the status of activity updates requested, and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);
            DebugLog.i(TAG, "onResult requestingUpdates : " + requestingUpdates);

            // Update the UI. Requesting activity updates enables the Remove Activity Updates
            // button, and removing activity updates enables the Add Activity Updates button.
            Context context = RecognitionApplication.getContext();
            String tip = context.getString(requestingUpdates ? R.string.activity_updates_added :
                    R.string.activity_updates_removed);
            showToast(tip);
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION_STATUS);
            context.sendBroadcast(localIntent);
        } else {
            DebugLog.e(TAG, "onResult : Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    private void showToast(String tip) {
        if (TextUtils.isEmpty(tip)) {
            return;
        }
        Context context = RecognitionApplication.getContext();
        Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
    }

    public void connect() {
        DebugLog.i(TAG, "connect ...");
        if (mGoogleApiClient == null) {
            buildGoogleApiClient();
        }
        if (mGoogleApiClient != null) {
            isRequested = false;
            mGoogleApiClient.connect();
        }
    }

    public void disconnect() {
        DebugLog.i(TAG, "disconnect ... ");
        if (isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isConnected() {
        boolean result = mGoogleApiClient != null && mGoogleApiClient.isConnected();
        DebugLog.i(TAG, "isConnected : " + result);
        return result;
    }

    public boolean requestActivityUpdates() {
        DebugLog.i(TAG, "requestActivityUpdates");
        if (isConnected() && !isRequested) {
            isRequested = true;
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    Constants.DETECTION_INTERVAL_MILLISECONDS,
                    Constants.getActivityDetectionPendingIntent(null)
            ).setResultCallback(this);
            return true;
        }
        return false;
    }

    public void removeActivityUpdates() {
        DebugLog.i(TAG, "removeActivityUpdates");
        isRequested = false;
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                Constants.getActivityDetectionPendingIntent(null)
        ).setResultCallback(this);
    }

    private static SharedPreferences getSharedPreferencesInstance() {
        return RecognitionApplication.getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }


    public static boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }


    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance()
                .edit()
                .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
                .commit();
    }

}
