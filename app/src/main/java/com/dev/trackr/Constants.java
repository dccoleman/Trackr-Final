package com.dev.trackr;

import android.os.Environment;

public class Constants {

    public static class Permissions {
        public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
        public static final int MY_PERMISSIONS_REQUEST_FILES = 100;
    }

    public static class Location {
        public static final float LOCATION_RADIUS = 1;
        public static final float LOCATION_OUTLIER = 10;
    }

    public static class Intents {
        public class IntentExtras {
            public static final String LOCATION_NUMBER = "locationNumber";
            public static final String LOCATION_UUID = "UUID";
        }

        public static final int REQUEST_PHOTO = 0;
    }

    public static class SavedInstanceStateAccessors {
        public static final String STORED_POINTS = "storedPoints";
    }

    public static class FileSystem {
        public static final String FILE_DIR = Environment.getExternalStorageDirectory() + "/" + "trackr/";
    }
}
