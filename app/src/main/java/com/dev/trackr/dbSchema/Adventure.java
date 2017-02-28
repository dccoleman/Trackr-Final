package com.dev.trackr.dbSchema;

import com.orm.SugarRecord;

public class Adventure extends SugarRecord {

    private String UUID;
    private String name;

    public Adventure() {}

    public Adventure(String UUID, String name) {
        this.UUID = UUID;
        this.name = name;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
