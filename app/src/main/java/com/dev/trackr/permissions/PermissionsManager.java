package com.dev.trackr.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.dev.trackr.Constants;
import com.dev.trackr.activity.MapActivity;

public class PermissionsManager {

    private Activity context;

    private static final String TAG = "Permissions****";

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
                        Constants.Permissions.MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.Permissions.MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    public void requestFilePermission() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.Permissions.MY_PERMISSIONS_REQUEST_FILES);
        }
    }

    public void handlePermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.Permissions.MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG,"Location Permissions Acquired");
                        requestFilePermission();
                    }

                } else {
                    Log.e(TAG,"Location Permissions Denied");
                }
                break;
            }
            case Constants.Permissions.MY_PERMISSIONS_REQUEST_FILES: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG,"File Permissions Acquired");
                    }
                } else {
                    Log.e(TAG,"File Permissions Denied");
                }
            }
        }
    }
}
