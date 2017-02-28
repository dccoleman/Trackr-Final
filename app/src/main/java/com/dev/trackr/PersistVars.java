package com.dev.trackr;

import com.orm.SugarRecord;

/**
 * Created by Bacon on 2/28/2017.
 */

public class PersistVars extends SugarRecord {
    private String UUID;
    private int photos, locations;

    public PersistVars() {}

    public PersistVars(String UUID) {
        this.UUID = UUID;
        photos = 0;
        locations = 0;
    }

    public PersistVars(String UUID, int photos, int locations) {
        this.UUID = UUID;
        this.photos = photos;
        this.locations = locations;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getPhotos() {
        return photos;
    }

    public void setPhotos(int photos) {
        this.photos = photos;
    }

    public int getLocations() {
        return locations;
    }

    public void setLocations(int locations) {
        this.locations = locations;
    }

    public int incPhotos() {
        return this.photos += 1;
    }

    public int decPhotos() {
        return --this.photos;
    }

    public int incLocations() {
        return this.locations += 1;
    }

    public int decLocations() {
        return --this.locations;
    }
}
