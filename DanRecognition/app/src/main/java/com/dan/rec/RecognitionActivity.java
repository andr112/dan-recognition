package com.dan.rec;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.rec.bean.DetectedActivitiesIntentService;
import com.dan.rec.bean.DetectionBroadcastReceiver;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.dan.rec.utils.LogTrace;
import com.dan.rec.view.RecCurrentHolder;
import com.dan.rec.view.RecHistoryHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.ActivityRecognition;


public class RecognitionActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    protected final String TAG = "RecognitionActivity";

    protected DetectionBroadcastReceiver mBroadcastReceiver;

    protected GoogleApiClient mGoogleApiClient;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    private RecCurrentHolder mRecCurrentHolder;
    private RecHistoryHolder mRecHistoryHolder;
    private TextView mCurOrHistorySwitchTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.i(TAG, "onCreate ...");
        setContentView(R.layout.activity_recognition);
        powerManager = (PowerManager) this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        // Get the UI widgets.
        View curView = findViewById(R.id.currentRoot);
        mRecCurrentHolder = new RecCurrentHolder();
        mRecCurrentHolder.onCreate(curView, savedInstanceState);
        View hisView = findViewById(R.id.historyLs);
        mRecHistoryHolder = new RecHistoryHolder(hisView);
        mCurOrHistorySwitchTv = (TextView) findViewById(R.id.switchTv);
        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        mBroadcastReceiver = new DetectionBroadcastReceiver(mRecCurrentHolder);
        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        DebugLog.i(TAG, "buildGoogleApiClient ...");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DebugLog.i(TAG, "onStart ...");
        mGoogleApiClient.connect();
        DebugLog.d("xixitest","test1...");
        LogTrace.getInstance().writeCommonLog("test.111...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugLog.i(TAG, "onStop ...");
        mGoogleApiClient.disconnect();
        LogTrace.getInstance().clean();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DebugLog.i(TAG, "onResume ...");
        registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
        wakeLock.acquire();
    }

    @Override
    protected void onPause() {
        DebugLog.i(TAG, "onPause ...");
        unregisterReceiver(mBroadcastReceiver);
        wakeLock.release();
        super.onPause();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        DebugLog.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        DebugLog.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        DebugLog.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    public void onRequestClick(View view) {
        if (!mGoogleApiClient.isConnected()) {
            String tip = getString(R.string.not_connected);
            Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
            DebugLog.i(TAG, "onRequestClick " + tip);
            return;
        }
        DebugLog.i(TAG, "onRequestClick");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(mRecCurrentHolder);
    }


    public void onRemoveClick(View view) {
        if (!mGoogleApiClient.isConnected()) {
            String tip = getString(R.string.not_connected);
            Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
            DebugLog.i(TAG, "onRemoveClick " + tip);
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        DebugLog.i(TAG, "onRemoveClick");
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(mRecCurrentHolder);
    }

    public void onSwitchNowOrHistoryClick(View view) {
        mRecHistoryHolder.showOrHide();
        if (mRecHistoryHolder.isShowing()) {
            mCurOrHistorySwitchTv.setText(R.string.current);
        } else {
            mCurOrHistorySwitchTv.setText(R.string.history);
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        mRecCurrentHolder.onSaveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

}