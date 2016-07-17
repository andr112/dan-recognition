package com.dan.rec.bean;

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
    private boolean isNeedInited = true;
    private boolean isByMax = true;
    private int confidenceTotal;
    private Map<Integer, Integer> actMap;
    private ActRecordTime time;
    private static Recognition instence = new Recognition();
    private static volatile String weightedResultStr;
    private final String Block_Str = "   ";

    public static Recognition getInstence() {
        return instence;
    }

    public void init() {
        if (isNeedInited) {
            reset();
        }
        isNeedInited = false;
    }

    public synchronized ArrayList<DetectedActivity> weightedMean(ArrayList<DetectedActivity> detectedActivities) {
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
    }

    private void packDatas(ArrayList<DetectedActivity> detectedActivities) {
        for (DetectedActivity item : detectedActivities) {
            int itemC = item.getConfidence();
            int itemT = item.getType();
            updateToActMap(itemT, itemC);
        }
    }

    private void updateToActMap(int actType, int actConfidence) {
        if (actConfidence > 0) {
            confidenceTotal += actConfidence;
            int confidence = actMap.containsKey(actType) ? actMap.get(actType) : 0;
            if (confidence > 0) {
                confidence += actConfidence;
            } else {
                confidence = actConfidence;
            }
            actMap.put(actType, confidence);
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
        int maxType = getMaxConfidenceType();
        if (time.isRipe() && maxType != DetectedActivity.UNKNOWN) {
            result = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : actMap.entrySet()) {
                DebugLog.d(tag, entry.getKey() + "--->" + entry.getValue());
                Integer type = entry.getKey();
                int confidence = (entry.getValue() * 100) / confidenceTotal;
                result.add(new DetectedActivity(type, confidence));
                if (confidence > 50) {
                    writeLog(confidence, type);
                }
            }
            reset();
        }
        return result;
    }

    private int getMaxConfidenceType() {
        int maxtType = DetectedActivity.UNKNOWN;
        for (Map.Entry<Integer, Integer> entry : actMap.entrySet()) {
            Integer key = entry.getKey();
            // System.out.println(key + "--->" + entry.getValue());
            int confidence = (entry.getValue() * 100) / confidenceTotal;
            if (confidence > 50) {
                maxtType = key;
            }
        }
        return maxtType;
    }

    private DetectedActivity getMaxConfidenceActivity(ArrayList<DetectedActivity> detectedActivities) {
        DetectedActivity result = detectedActivities.get(0);
        for (DetectedActivity item : detectedActivities) {
            DebugLog.i(tag, "getMaxConfidenceActivity  : " + item.toString());
            if (item.getConfidence() > result.getConfidence()) {
                result = item;
            }
        }
        DebugLog.i(tag, "getMaxConfidenceActivity result : " + result.toString());
        return result;
    }


    private void weightedMeanByAverage(ArrayList<DetectedActivity> detectedActivities) {
        packDatas(detectedActivities);
    }

    private void writeLog(float confidence, Integer cType) {
        ActType type = getActType(cType);
        if (type == null) {
            return;
        }
        int resId = type.getTypeNameStrResId();
        String actN = RecognitionApplication.getContext().getResources().getString(resId);
        weightedResultStr = actN;
        float confidenceF = confidence / 100.0f;
        LogTrace.getInstance().write(tag, " result ", "result : " + weightedResultStr + Block_Str + confidenceF, false);
    }

    private void writeLog(int actType, int actConfidence) {
        String actN = Constants.getActivityString(RecognitionApplication.getContext(), actType);
        LogTrace.getInstance().write(tag, "writeLog item ", actN + Block_Str + actConfidence, true);
    }
}
