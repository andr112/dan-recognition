package com.dan.rec;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.rec.bean.DetectedActivitiesIntentService;
import com.dan.rec.bean.DetectionBroadcastReceiver;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
        wakeLock.acquire();
        Constants.updateFlag(true);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        wakeLock.release();
        super.onPause();
        Constants.updateFlag(false);
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
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(mRecCurrentHolder);
    }


    public void onRemoveClick(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
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