package com.dan.rec;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dan.rec.bean.DetectedActivitiesIntentService;
import com.dan.rec.utils.Constants;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/7/17 15:56
 */
public class RecServer extends Service {
    private BroadcastReceiver mBR;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, RecognitionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification noti = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            noti = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("Running")
                    .setSmallIcon(android.R.drawable.ic_lock_lock)
                    .setContentIntent(pendingIntent)
                    .build();
        }
        startForeground(12346, noti);
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBR = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent a = new Intent(RecServer.this, DetectedActivitiesIntentService.class);
                startService(a);
            }
        };
        IntentFilter mIF = new IntentFilter();
        mIF.addAction(Constants.BROADCAST_ACTION_REC);
        registerReceiver(mBR, mIF);
        RecGAClientHolper.getInstance().connect();
        // RecGAClientHolper.getInstance().requestActivityUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RecGAClientHolper.getInstance().removeActivityUpdates();
        RecGAClientHolper.getInstance().disconnect();
        Intent intent = new Intent();
        intent.setAction(Constants.BROADCAST_ACTION_DET);
        sendBroadcast(intent);
        unregisterReceiver(mBR);

    }
}
