package com.dev.trackr;

import com.orm.SugarRecord;

/**
 * Created by Bacon on 2/28/2017.
 */

public class PictureWrapper extends SugarRecord {
    private String UUID;
    private int loc, pic;

    public PictureWrapper() {}

    public PictureWrapper(String UUID, int loc, int pic) {
        this.UUID = UUID;
        this.loc = loc;
        this.pic = pic;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getPic() {
        return pic;
    }

    public void setPic(int pic) {
        this.pic = pic;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }
}
