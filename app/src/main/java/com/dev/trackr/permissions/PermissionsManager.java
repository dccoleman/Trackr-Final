package com.dev.trackr.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.dev.trackr.activity.MapActivity;

/**
 * Created by bacon on 2/28/17.
 */
public class PermissionsManager {

    private Activity context;

    private PermissionsManager() {}

    public PermissionsManager(Activity c) {
        this.context = c;
    }



    public void requestPermissionsIfNecessary() {
        requestLocationPermission();
    }

    public void requestLocationPermission() {
        if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MapActivity.MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MapActivity.MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void requestFilePermission() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MapActivity.MY_PERMISSIONS_REQUEST_FILES);
        }
    }
}
