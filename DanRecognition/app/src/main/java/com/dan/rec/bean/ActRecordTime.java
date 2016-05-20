package com.dan.rec.bean;

/**
 * Created by zhangdan on 16/4/30.
 */
public class ActRecordTime {
    private final int Timestamp = 60000;//1 min
    private volatile long startT;

    public ActRecordTime() {
        reset();
    }

    public boolean isRipe() {
        boolean isRipe = false;
        long curT = System.currentTimeMillis();
        if (curT - startT > Timestamp) {
            isRipe = true;
        }
        return isRipe;
    }

    public void reset() {
        startT = System.currentTimeMillis();
    }

}
