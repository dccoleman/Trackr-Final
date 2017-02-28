package com.dev.trackr.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class TrackerService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //* Basically an enum
    public static final String UPDATE_LOCATION = "com.dev.trackr.updatedLoc";
    public static final String REQUEST_LOCATION_PERMISSION = "com.dev.tracker.getPermission";

    public static final String TAG = "TrackerService----";

    private static GoogleApiClient mApiClient;

    //* A variable for the location request
    private LocationRequest mLocationRequest;

    //* The local broadcast manager
    private LocalBroadcastManager bm;

    //* constructors
    public TrackerService() {
        super("TrackerService");
    }

    public TrackerService(String name) {
        super(name);
    }

    //* Similar to onCreate() for activities, used to test if service was running
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mApiClient = buildGoogleAPIClient();
        mApiClient.connect();

        bm = LocalBroadcastManager.getInstance(this);

        mLocationRequest = new LocationRequest()
                .setInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(5000);

        Log.d(TAG,"Service Started");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service stopping");
        LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient,this);
        mApiClient.disconnect();
    }

    //* sending the intent to the activity to alter the ui
    public void sendLocation(Location loc) {
        Intent intent = new Intent(UPDATE_LOCATION);
        intent.putExtra(UPDATE_LOCATION, loc);
        bm.sendBroadcast(intent);
    }

    public void requestLocationPermission() {
        Intent intent = new Intent(REQUEST_LOCATION_PERMISSION);
        bm.sendBroadcast(intent);
    }

    //* builds the google api client needed to access the location services
    private GoogleApiClient buildGoogleAPIClient() {
        return new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    @Override
    public void onLocationChanged(Location location) {
        //* Perform any logic dealing with 'locations' here, probably
        Log.d(TAG,"Location updated");
        sendLocation(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
        } else {
            requestLocationPermission();
        }
    }

    //* simply checks for the ability to access location and requests permission to do so
    public boolean checkLocationPermission(){
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //* how to handle an intent if one is passed to the service
    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
