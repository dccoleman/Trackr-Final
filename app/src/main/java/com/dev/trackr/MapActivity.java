package com.dev.trackr;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private List<Location> points;

    private static boolean databaseEnabled = false;

    private static Intent mServiceIntent;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_FILES = 100;

    private static final String TAG = "Maps Activity++++";

    //private static final String UUID = "067e6162-3b6f-4ae2-a171-2470b63dff00";
    private static final String UUID = "167e6162-3b6f-4ae2-a171-2470b63dff00";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SugarContext.init(getApplicationContext());

        setContentView(R.layout.activity_map_tracker);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        points = new ArrayList<>();

        //requestLocationPermission();
        requestFilePermission();

        //* Create database
        SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS POINTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, LAT DOUBLE, LNG DOUBLE, TIME LONG)");

        redrawLine();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TrackerService.REQUEST_LOCATION_PERMISSION);
        filter.addAction(TrackerService.UPDATE_LOCATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        mServiceIntent = new Intent(MapActivity.this, TrackerService.class);
        startService(mServiceIntent);

        Button buttonOne = (Button) findViewById(R.id.setScrollable);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                SugarRecord.executeQuery("DROP TABLE POINTS");
                SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS POINTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, LAT DOUBLE, LNG DOUBLE, TIME LONG)");
                redrawLine();
            }
        });
    }

    @Override
    public void onDestroy() {
        stopService(mServiceIntent);
        SugarContext.terminate();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        if(databaseEnabled) {
            points.clear();
            List<Points> p = Points.find(Points.class, "UUID = ?", UUID);
            Log.d(TAG,"Found " + p.size() + " points for UUID " + UUID);
            for (Points x : p) {
                points.add(pointsToLocation(x));
            }
        }
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            Log.d(TAG, "No location permissions");
        }

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        Log.d(TAG,"MAP READY");
        redrawLine();
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
                        Points p = new Points(UUID, loc.getLatitude(), loc.getLongitude(), loc.getTime());
                        p.save();

                        Log.d(TAG, "Location update from service: " + loc.toString());
                        points.add(loc);
                        redrawLine();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));

                        databaseEnabled = true;
                    }
                    break;

                default:
                    Log.d(TAG, "Unrecognized Intent");
            }
        }
    };

    public Location pointsToLocation(Points p) {
        Location loc = new Location("");
        loc.setLatitude(p.getLat());
        loc.setLongitude(p.getLng());
        loc.setTime(p.getTime());
        return loc;
    }

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

    public void requestFilePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_FILES);
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
        if(mMap != null) {
            mMap.clear();  //clears all Markers and Polylines

            Location prevPoint = null;

            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
            for (int i = 0; i < points.size(); i++) {
                Location place = points.get(i);

                options.add(new LatLng(place.getLatitude(), place.getLongitude()));
                /*if(prevPoint == null) {
                    prevPoint = place;
                } else if(prevPoint.distanceTo(place) > LOCATION_RADIUS) {
                Filter by picture location, not line
                }*/
            }
            mMap.addPolyline(options); //add Polyline
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
