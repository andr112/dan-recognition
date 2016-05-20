package com.dan.rec.bean;

import com.dan.rec.R;

/**
 * Created by zhangdan on 16/4/30.
 */
public enum ActType {
    OnFoot(R.string.on_foot), Vehicle(R.string.in_vehicle), Still(R.string.still), Unkown(R.string.unknown), ON_BICYCLE(R.string.on_bicycle);
    private int typeNameStrResId;

    private ActType(int typeNameStrResId) {
        this.typeNameStrResId = typeNameStrResId;
    }

    public int getTypeNameStrResId() {
        return typeNameStrResId;
    }

}
