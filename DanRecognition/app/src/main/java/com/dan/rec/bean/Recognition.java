package com.dan.rec.bean;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.dan.rec.RecognitionApplication;
import com.dan.rec.utils.Constants;
import com.dan.rec.utils.DebugLog;
import com.dan.rec.utils.LogTrace;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangdan on 16/4/30.
 */
public class Recognition {
    private final String tag = "Recognition";
    private final int InvalidType = -1;
    private boolean isNeedInited = true;
    private boolean isByMax = true;
    private int confidenceTotal;
    private int maxConfidenceType;
    private Map<Integer, Integer> actMap;
    private ActRecordTime time;
    private static Recognition instence = new Recognition();
    private static volatile String weightedResultStr;
    private final String Block_Str="   ";

    public static Recognition getInstence() {
        return instence;
    }

    public void init() {
        if (isNeedInited) {
            reset();
        }
        isNeedInited = false;
    }

    public synchronized ArrayList<DetectedActivity> weightedMean(ArrayList<DetectedActivity> detectedActivities) throws Exception {
        ArrayList<DetectedActivity> result = null;
        if (isByMax) {
            result = weightedMeanByMax(detectedActivities);
        } else {
            weightedMeanByAverage(detectedActivities);
        }
        return result;
    }

    private ActType getActType(int detectedActivityType) {
        ActType result = null;
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                result = ActType.Vehicle;
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                result = ActType.OnFoot;
                break;
            case DetectedActivity.STILL:
                result = ActType.Still;
                break;
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
            case DetectedActivity.ON_BICYCLE:
            default:
                break;
        }
        return result;
    }

    private void reset() {
        if (time == null) {
            time = new ActRecordTime();
        } else {
            time.reset();
        }
        if (actMap == null) {
            actMap = new HashMap<>();
        } else {
            actMap.clear();
        }
        confidenceTotal = 0;
        maxConfidenceType = InvalidType;
    }

    private void packDatas(ArrayList<DetectedActivity> detectedActivities) {
        for (DetectedActivity item : detectedActivities) {
            int itemC = item.getConfidence();
            int itemT = item.getType();
            updateToActMap(itemT, itemC);
        }
    }

    private void updateToActMap(int actType, int actConfidence) {
        int lastMaxC = Math.max(maxConfidenceType != InvalidType ? actMap.get(maxConfidenceType) : 0, 0);
        if (actConfidence > 0) {
            confidenceTotal += actConfidence;
            int confidence = actMap.containsKey(actType) ? actMap.get(actType) : 0;
            if (confidence > 0) {
                confidence += actConfidence;
            } else {
                confidence = actConfidence;
            }
            actMap.put(actType, confidence);
            if (confidence > lastMaxC) {
                maxConfidenceType = actType;
            }
        }
        DebugLog.d(tag, "updateToActMap :" + actType + "," + actConfidence);
        writeLog(actType, actConfidence);
    }

    private ArrayList<DetectedActivity> weightedMeanByMax(ArrayList<DetectedActivity> detectedActivities) {
        ArrayList<DetectedActivity> result = null;
        DetectedActivity maxConfidenceAct = getMaxConfidenceActivity(detectedActivities);
        switch (maxConfidenceAct.getType()) {
            case DetectedActivity.IN_VEHICLE:
                updateToActMap(maxConfidenceAct.getType(), maxConfidenceAct.getConfidence());
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                updateToActMap(DetectedActivity.ON_FOOT, maxConfidenceAct.getConfidence());
                break;
            case DetectedActivity.STILL:
                updateToActMap(maxConfidenceAct.getType(), maxConfidenceAct.getConfidence());
                break;
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.TILTING:
            case DetectedActivity.UNKNOWN:
            default:
                updateToActMap(DetectedActivity.UNKNOWN, maxConfidenceAct.getConfidence());
                break;
        }

        if (time.isRipe() && maxConfidenceType != DetectedActivity.UNKNOWN) {
            int maxC = actMap.get(maxConfidenceType);
            int confidence = (maxC * 100) / confidenceTotal;
            if (confidence > 50) {
                result = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : actMap.entrySet()) {
                    System.out.println(entry.getKey() + "--->" + entry.getValue());
                    confidence = (entry.getValue() * 100) / confidenceTotal;
                    result.add(new DetectedActivity(entry.getKey(), confidence));
                }
                writeLog(confidence);
                reset();
            }
        }
        return result;
    }

    private DetectedActivity getMaxConfidenceActivity(ArrayList<DetectedActivity> detectedActivities) {
        DetectedActivity result = detectedActivities.get(0);
        for (DetectedActivity item : detectedActivities) {
            if (item.getConfidence() > result.getConfidence()) {
                result = item;
            }
        }
        return result;
    }


    private void weightedMeanByAverage(ArrayList<DetectedActivity> detectedActivities) {
        packDatas(detectedActivities);
    }

    private void writeLog(float confidence) {
        ActType type = getActType(maxConfidenceType);
        if (type == null) {
            return;
        }
        int resId = type.getTypeNameStrResId();
        String actN = RecognitionApplication.getContext().getResources().getString(resId);
        weightedResultStr = actN;
        float confidenceF = confidence / 100.0f;
        LogTrace.write(tag, "writeLog result ", "result : " + weightedResultStr + Block_Str + confidenceF, false);
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        localIntent.putExtra(Constants.ACTIVITY_EXTRA_TYPE, weightedResultStr);
        LocalBroadcastManager.getInstance(RecognitionApplication.getContext()).sendBroadcast(localIntent);
    }

    private void writeLog(int actType, int actConfidence) {
        String actN = Constants.getActivityString(RecognitionApplication.getContext(), actType);
        LogTrace.write(tag, "writeLog item ", actN + Block_Str + actConfidence, true);
    }
}
