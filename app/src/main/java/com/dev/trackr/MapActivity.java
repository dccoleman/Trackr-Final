package com.dev.trackr;

import android.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private List<Location> points;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = "Maps Activity++++";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_tracker);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        points = new ArrayList<>();

        requestLocationPermission();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TrackerService.REQUEST_LOCATION_PERMISSION);
        filter.addAction(TrackerService.UPDATE_LOCATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        Intent serviceIntent = new Intent(MapActivity.this, TrackerService.class);
        startService(serviceIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            Log.d(TAG, "Location permissions acquired");
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            Log.d(TAG, "No location permissions");
        }

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
    }

    //* handler for messages sent from the service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()) {
                case TrackerService.REQUEST_LOCATION_PERMISSION:
                    Log.d(TAG,"Permission request from service");
                    requestLocationPermission();
                    break;

                case TrackerService.UPDATE_LOCATION:
                    Location loc = intent.getParcelableExtra(TrackerService.UPDATE_LOCATION);
                    if(loc != null) {
                        Log.d(TAG, "Location update from service: " + loc.toString());
                        points.add(loc);
                        redrawLine();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
                    }
                    break;

                default:
                    Log.d(TAG, "Unrecognized Intent");
            }
        }
    };

    public void requestLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    //* this is called when the permissions request is answered
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //* Create service here
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void redrawLine(){

        mMap.clear();  //clears all Markers and Polylines

        Location prevPoint = null;

        PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            Location place = points.get(i);

            options.add(new LatLng(place.getLatitude(),place.getLongitude()));
            /*if(prevPoint == null) {
                prevPoint = place;
            } else if(prevPoint.distanceTo(place) > LOCATION_RADIUS) {
            Filter by picture location, not line
            }*/
        }
        mMap.addPolyline(options); //add Polyline
    }
}
