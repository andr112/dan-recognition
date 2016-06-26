package com.dan.rec.view;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.rec.R;
import com.dan.rec.RecognitionApplication;
import com.dan.rec.bean.DetectionUpdateListener;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/21 10:44
 */

public class RecCurrentHolder implements ResultCallback<Status>, DetectionUpdateListener {

    private final String TAG = "RecCurrentHolder";
    private View mRootView; // UI elements.
    private Button mRequestBt;

    private Button mRemoveBt;

    private ListView mDetectedActivitiesLv;

    private TextView mActResultTv;

    private Context mContext;

    /**
     * Adapter backed by a list of DetectedActivity objects.
     */
    private DetectedActivitiesAdapter mAdapter;


    private ArrayList<DetectedActivity> mDetectedActivities;
    private String mResultStr;


    public RecCurrentHolder() {
        mContext = RecognitionApplication.getContext();
    }

    public void onCreate(View rootView, Bundle savedInstanceState) {
        init(rootView);
        // Enable either the Request Updates button or the Remove Updates button depending on
        // whether activity updates have been requested.
        setButtonsEnabledState();
        if (savedInstanceState != null && savedInstanceState.containsKey(
                Constants.DETECTED_ACTIVITIES)) {
            mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(
                    Constants.DETECTED_ACTIVITIES);
            String mResultStr = savedInstanceState.getString(Constants.DETECTED_RESULT);
            updateWeightResult(mResultStr);
        } else {
            mDetectedActivities = new ArrayList<DetectedActivity>();

            // Set the confidence level of each monitored activity to zero.
            for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
                mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
            }
        }

        // Bind the adapter to the ListView responsible for display data for detected activities.
        mAdapter = new DetectedActivitiesAdapter(mContext, mDetectedActivities);
        mDetectedActivitiesLv.setAdapter(mAdapter);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
        savedInstanceState.putString(Constants.DETECTED_RESULT, mResultStr);
    }

    @Override
    public void updates(ArrayList<DetectedActivity> updatedActivities) {
        if (updatedActivities != null) {
            updateDetectedActivitiesList(updatedActivities);
            if (updatedActivities.size() >= 0) {
                DetectedActivity maxCon = updatedActivities.get(0);
                for (DetectedActivity temp : updatedActivities) {
                    if (maxCon.getConfidence() < temp.getConfidence()) {
                        maxCon = temp;
                    }
                }
                String name = Constants.getActivityString(mContext, maxCon.getType());
                Date currentTime = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
                String dateString = formatter.format(currentTime);
                String result = dateString + " result : " + name;
                updateWeightResult(result);
            }
        }
    }

    protected void updateDetectedActivitiesList(ArrayList<DetectedActivity> detectedActivities) {
        DebugLog.e(TAG, "updateDetectedActivitiesList : " + detectedActivities);
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

    protected void updateWeightResult(String result) {
        if (!TextUtils.isEmpty(result)) {
            mActResultTv.setText(result);
        }
        DebugLog.e(TAG, "updateWeightResult : " + result);
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

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Toggle the status of activity updates requested, and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);

            // Update the UI. Requesting activity updates enables the Remove Activity Updates
            // button, and removing activity updates enables the Add Activity Updates button.
            setButtonsEnabledState();
            String tip = mContext.getString(requestingUpdates ? R.string.activity_updates_added :
                    R.string.activity_updates_removed);
            Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
        } else {
            DebugLog.e(TAG, "onResult : Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    private void init(View rootView) {
        this.mRootView = rootView;
        mRequestBt = (Button) rootView.findViewById(R.id.request_activity_updates_button);
        mRemoveBt = (Button) rootView.findViewById(R.id.remove_activity_updates_button);
        mDetectedActivitiesLv = (ListView) rootView.findViewById(R.id.detected_activities_listview);
        mActResultTv = (TextView) rootView.findViewById(R.id.detected_activities_resultTv);

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
        return RecognitionApplication.getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Activity.MODE_PRIVATE);
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
}
