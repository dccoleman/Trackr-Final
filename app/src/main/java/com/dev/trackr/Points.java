package com.dev.trackr;

import com.orm.SugarRecord;

/**
 * Created by Bacon on 2/26/2017.
 */

public class Points extends SugarRecord {
    private String UUID;
    private double lat,lng;
    private long time;

    public Points() {}

    public Points(String UUID, double lat, double lng, long time) {
        this.UUID = UUID;
        this.lat = lat;
        this.lng = lng;
        this.time = time;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
