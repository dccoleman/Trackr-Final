package com.dev.trackr.dbSchema;

import com.orm.SugarRecord;

/**
 * Created by Bacon on 2/28/2017.
 */

public class MarkerWrapper extends SugarRecord {
    private String UUID, name;
    private int loc;
    private double lat, lng;

    public MarkerWrapper() {}

    public MarkerWrapper(String UUID, String name, int loc, double lat, double lng) {
        this.UUID = UUID;
        this.name = name;
        this.loc = loc;
        this.lat = lat;
        this.lng = lng;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
