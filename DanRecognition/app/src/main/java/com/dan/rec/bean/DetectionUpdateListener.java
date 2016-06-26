package com.dan.rec.bean;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * @功能描述:
 * @作者:Xixi
 * @创建时间: 16/5/21 11:42
 */
public interface DetectionUpdateListener {
   void updates( ArrayList<DetectedActivity> updatedActivities);
}
