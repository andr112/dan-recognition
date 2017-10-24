package com.dan.rec;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dan.rec.bean.DetectionBroadcastReceiver;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.dan.rec.utils.LogTrace;
import com.dan.rec.view.RecCurrentHolder;
import com.dan.rec.view.RecHistoryHolder;


public class RecognitionActivity extends Activity {
    protected final String TAG = "RecognitionActivity";

    protected DetectionBroadcastReceiver mBroadcastReceiver;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    private RecCurrentHolder mRecCurrentHolder;
    private RecHistoryHolder mRecHistoryHolder;
    private TextView mCurOrHistorySwitchTv;
    private long lastRequestTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DebugLog.i(TAG, "xixitest onCreate ...");
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
        mCurOrHistorySwitchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchNowOrHistoryClick(v);
            }
        });
        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        mBroadcastReceiver = new DetectionBroadcastReceiver(mRecCurrentHolder);
        // Kick off the request to build GoogleApiClient.
        IntentFilter intentFilter= new IntentFilter(Constants.BROADCAST_ACTION);
        intentFilter.addAction(Constants.BROADCAST_ACTION_STATUS);
        registerReceiver(mBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DebugLog.d(TAG, "xixitest onDestroy ...");
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DebugLog.d(TAG, "xixitest onStart ...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        DebugLog.d(TAG, "xixitest onStop ...");
        LogTrace.getInstance().clean();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DebugLog.d(TAG, "xixitest onResume ...");
        wakeLock.acquire();
        if (mRecCurrentHolder != null) {
            mRecCurrentHolder.setButtonsEnabledState();
        }
    }

    @Override
    protected void onPause() {
        DebugLog.d(TAG, "xixitest onPause ...");
        wakeLock.release();
        super.onPause();
    }

    public void onRequestClick(View view) {
        long temp = System.currentTimeMillis();
        if (temp - lastRequestTime < 500) {
            return;
        }
        lastRequestTime = temp;
        if (!RecGAClientHolper.getInstance().isConnected()) {
            String tip = getString(R.string.not_connected);
            Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
            DebugLog.i(TAG, "onRequestClick " + tip);
            return;
        }
        RecGAClientHolper.getInstance().requestActivityUpdates();
        mRecCurrentHolder.setButtonsEnabledState();
    }


    public void onRemoveClick(View view) {
        if (!RecGAClientHolper.getInstance().isConnected()) {
            String tip = getString(R.string.not_connected);
            Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
            DebugLog.i(TAG, "onRemoveClick " + tip);
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        RecGAClientHolper.getInstance().removeActivityUpdates();
    }

    public void onSwitchNowOrHistoryClick(View view) {
        DebugLog.i(TAG, "onSwitchNowOrHistoryClick ");
        mRecHistoryHolder.showOrHide();
        if (mRecHistoryHolder.isShowing()) {
            mCurOrHistorySwitchTv.setText(R.string.current);
        } else {
            mCurOrHistorySwitchTv.setText(R.string.history);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        mRecCurrentHolder.onSaveInstanceState(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }
}