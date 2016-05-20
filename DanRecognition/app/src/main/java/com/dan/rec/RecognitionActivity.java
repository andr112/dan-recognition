package com.dan.rec;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.rec.bean.DetectedActivitiesAdapter;
import com.dan.rec.bean.DetectedActivitiesIntentService;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class RecognitionActivity extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    protected final String TAG = "RecognitionActivity";


    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;


    protected GoogleApiClient mGoogleApiClient;

    // UI elements.
    private Button mRequestBt;

    private Button mRemoveBt;

    private ListView mDetectedActivitiesLv;

    private TextView mActResultTv;

    /**
     * Adapter backed by a list of DetectedActivity objects.
     */
    private DetectedActivitiesAdapter mAdapter;


    private ArrayList<DetectedActivity> mDetectedActivities;
    private String mResultStr;
    PowerManager powerManager = null;
    PowerManager.WakeLock wakeLock = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);
        powerManager = (PowerManager) this.getSystemService(this.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        // Get the UI widgets.
        mRequestBt = (Button) findViewById(R.id.request_activity_updates_button);
        mRemoveBt = (Button) findViewById(R.id.remove_activity_updates_button);
        mDetectedActivitiesLv = (ListView) findViewById(R.id.detected_activities_listview);
        mActResultTv = (TextView) findViewById(R.id.detected_activities_resultTv);

        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();

        // Enable either the Request Updates button or the Remove Updates button depending on
        // whether activity updates have been requested.
        setButtonsEnabledState();


        if (savedInstanceState != null && savedInstanceState.containsKey(
                Constants.DETECTED_ACTIVITIES)) {
            mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(
                    Constants.DETECTED_ACTIVITIES);
            mResultStr = savedInstanceState.getString(Constants.DETECTED_RESULT);
            updateWeightResult(mResultStr);
        } else {
            mDetectedActivities = new ArrayList<DetectedActivity>();

            // Set the confidence level of each monitored activity to zero.
            for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
                mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
            }
        }

        // Bind the adapter to the ListView responsible for display data for detected activities.
        mAdapter = new DetectedActivitiesAdapter(this, mDetectedActivities);
        mDetectedActivitiesLv.setAdapter(mAdapter);

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
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
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


    public void requestActivityUpdatesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }


    public void removeActivityUpdatesButtonHandler(View view) {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }


    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Toggle the status of activity updates requested, and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);

            // Update the UI. Requesting activity updates enables the Remove Activity Updates
            // button, and removing activity updates enables the Add Activity Updates button.
            setButtonsEnabledState();

            Toast.makeText(
                    this,
                    getString(requestingUpdates ? R.string.activity_updates_added :
                            R.string.activity_updates_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            DebugLog.e(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
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


    private void setButtonsEnabledState() {
        if (getUpdatesRequestedState()) {
            mRequestBt.setEnabled(false);
            mRemoveBt.setEnabled(true);
        } else {
            mRequestBt.setEnabled(true);
            mRemoveBt.setEnabled(false);
        }
    }

    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }


    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }


    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance()
                .edit()
                .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
                .commit();
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
        savedInstanceState.putString(Constants.DETECTED_RESULT, mResultStr);
        super.onSaveInstanceState(savedInstanceState);
    }


    protected void updateDetectedActivitiesList(ArrayList<DetectedActivity> detectedActivities) {
        if (detectedActivities != null) {
            if (mDetectedActivities == null) {
                mDetectedActivities = new ArrayList<>();
            } else {
                mDetectedActivities.clear();
            }
            int size = detectedActivities.size();
            for (int i = 0; i < size; i++) {
                DetectedActivity item = detectedActivities.get(i);
                if (isEfect(item)) {
                    mDetectedActivities.add(item);
                }
            }
            mAdapter.updateActivities(mDetectedActivities);
        }
    }

    private boolean isEfect(DetectedActivity detectedActivity) {
        boolean result = false;
        if (detectedActivity != null) {
            switch (detectedActivity.getType()) {
                case DetectedActivity.ON_FOOT:
                    result = true;
                    break;
                case DetectedActivity.STILL:
                    result = true;
                    break;
                case DetectedActivity.IN_VEHICLE:
                    result = true;
                    break;
            }
        }
        return result;
    }


    protected void updateWeightResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            mActResultTv.setText(result);
        }
    }


    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "activity-detection-response-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            // ArrayList<DetectedActivity> updatedActivities =
            //         intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            // updateDetectedActivitiesList(updatedActivities);
            ArrayList<DetectedActivity> updatedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA_TYPE);
            updates(updatedActivities);
        }
    }

    private void updates(ArrayList<DetectedActivity> updatedActivities) {
        if (updatedActivities != null) {
            updateDetectedActivitiesList(updatedActivities);
            if (updatedActivities.size() >= 0) {
                DetectedActivity maxCon = updatedActivities.get(0);
                for (DetectedActivity temp : updatedActivities) {
                    if (maxCon.getConfidence() < temp.getConfidence()) {
                        maxCon = temp;
                    }
                }
                String name = Constants.getActivityString(RecognitionActivity.this, maxCon.getType());
                Date currentTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
                String dateString = formatter.format(currentTime);
                String result = dateString + " result : " + name;
                updateWeightResult(result);
            }
        }
    }
}