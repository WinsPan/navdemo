package com.test.navdemo.eventbus;

import com.amap.api.services.core.LatLonPoint;

import java.io.Serializable;

public class RouteEvent implements Serializable {
    private LatLonPoint mStartPoint;//起点，116.335891,39.942295
    private LatLonPoint mEndPoint;//终点，116.481288,39.995576

    public LatLonPoint getmStartPoint() {
        return mStartPoint;
    }

    public void setmStartPoint(LatLonPoint mStartPoint) {
        this.mStartPoint = mStartPoint;
    }

    public LatLonPoint getmEndPoint() {
        return mEndPoint;
    }

    public void setmEndPoint(LatLonPoint mEndPoint) {
        this.mEndPoint = mEndPoint;
    }
}
