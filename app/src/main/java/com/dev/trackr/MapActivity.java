package com.dev.trackr;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private ArrayList<Location> points;

    private ArrayList<Polyline> lines;

    private HashMap<LatLng, Integer> markerMap;

    private ArrayList<Marker> markers;

    private PersistVars variables;

    private static Intent mServiceIntent;

    private static final int REQUEST_PHOTO = 0;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_FILES = 100;

    private static final String TAG = "Maps Activity++++";

    private static String UUID = "";
    private static final String STORED_POINTS = "storedPoints";
    private static final String STORED_LOCATIONS = "storedLocations";
    private static final String FILE_DIR = Environment.getExternalStorageDirectory() + "/" + "Trackr/";
    private static final float LOCATION_RADIUS = 1;

    private static Location mLastLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID = getIntent().getStringExtra(MainMenuActivity.NEW_ADVENTURE);
        Log.d(TAG,UUID);

        setContentView(R.layout.activity_map_tracker);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(savedInstanceState == null) {
            variables = new PersistVars(UUID);
            variables.save();

            points = new ArrayList<>();
            ArrayList<Points> l = new ArrayList<>();
            l.addAll(Points.find(Points.class, "UUID = ?", UUID));

            for(Points p : l) {
                points.add(pointsToLocation(p));
            }

            lines = new ArrayList<>();
        } else {
            points = savedInstanceState.getParcelableArrayList(STORED_POINTS);
            variables = PersistVars.find(PersistVars.class, "UUID = ?", UUID).get(0);
        }

        lines = new ArrayList<>();

        markerMap = new HashMap<>();
        markers = new ArrayList<>();


        //requestLocationPermission();
        requestFilePermission();

        redrawMap();

        IntentFilter filter = new IntentFilter();
        filter.addAction(TrackerService.REQUEST_LOCATION_PERMISSION);
        filter.addAction(TrackerService.UPDATE_LOCATION);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);

        mServiceIntent = new Intent(MapActivity.this, TrackerService.class);
        startService(mServiceIntent);

        Button backButton = (Button) findViewById(R.id.back);
        backButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                stopService(mServiceIntent);
                finish();
                startActivity(intent);

            }
        });
    }

    private Location pointsToLocation(Points p) {
        Location l = new Location("");
        l.setLatitude(p.getLat());
        l.setLongitude(p.getLng());
        l.setTime(p.getTime());
        return l;
    }

    @Override
    public void onDestroy() {
        stopService(mServiceIntent);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        mLastLocation = null;
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(STORED_POINTS, points);
        //savedInstanceState.putParcelableArrayList(STORED_LOCATIONS, locations);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        redrawMap();
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try{
            mMap.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            Log.e(TAG, "No location permissions");
            return;
        }

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(false);

        mMap.setOnMarkerClickListener(this);

        redrawMap();

        Button buttonOne = (Button) findViewById(R.id.takePicture);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //* CameraIntent here, wait for result indicating picture has been stored
                //* if(result == stored) {
                // Create marker & add to map
                // Store marker in list of markers, markerMap, & database
                // Save pictureWrapper
                // use variables.getPhotos() to name photo under UUID

                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(FILE_DIR + UUID);
                imagesFolder.mkdirs();

                File image = new File(imagesFolder, variables.getPhotos() + ".png");
                Uri uriSavedImage = Uri.fromFile(image);

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(imageIntent, REQUEST_PHOTO);


            }
        });

        buttonOne = (Button) findViewById(R.id.resetPath);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                List<Points> p = Points.find(Points.class, "UUID = ?", UUID);
                for(Points x : p) {
                    x.delete();
                }
                points.clear();
                redrawMap();
            }
        });

        Log.v(TAG,"Map Ready");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_PHOTO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if(mLastLocation != null) {
                    int closeMarker = -1;
                    for (Marker m : markers) {
                        Location pos = new Location("");
                        pos.setLatitude(m.getPosition().latitude);
                        pos.setLongitude(m.getPosition().longitude);

                        if (mLastLocation.distanceTo(pos) < LOCATION_RADIUS * 10) {
                            closeMarker = markerMap.get(new LatLng(pos.getLatitude(), pos.getLongitude()));
                            break;
                        }
                    }
                    if (closeMarker == -1) {
                        LatLng pos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        MarkerOptions marker = new MarkerOptions()
                                .position(pos)
                                .title(PersistVars.find(PersistVars.class, "UUID = ?", UUID).get(0).getLocations() + "");

                        markerMap.put(pos, variables.getLocations());

                        PictureWrapper p = new PictureWrapper(UUID, variables.getLocations(), variables.getPhotos());
                        p.save();
                        variables.incPhotos();

                        MarkerWrapper m = new MarkerWrapper(UUID, variables.getLocations(), pos.latitude, pos.longitude);
                        m.save();
                        variables.incLocations();

                        variables.save();

                        markers.add(mMap.addMarker(marker));
                    } else {
                        PictureWrapper p = new PictureWrapper(UUID, closeMarker,variables.getPhotos());
                        variables.incPhotos();
                        variables.save();
                        p.save();
                    }
                }
            }
        }
    }

    //* handler for messages sent from the service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()) {
                case TrackerService.REQUEST_LOCATION_PERMISSION:
                    Log.v(TAG,"Permission request from service");
                    requestLocationPermission();
                    break;

                case TrackerService.UPDATE_LOCATION:
                    Location loc = intent.getParcelableExtra(TrackerService.UPDATE_LOCATION);

                    if(loc != null) {
                        if (mLastLocation == null || loc.distanceTo(mLastLocation) > LOCATION_RADIUS) {
                            Points p = new Points(UUID, loc.getLatitude(), loc.getLongitude(), loc.getTime());
                            p.save();

                            //Log.v(TAG, "Location update from service: " + loc.toString());
                            points.add(loc);
                            redrawMap();
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));

                            mLastLocation = loc;
                        } else {
                            Log.v(TAG, "Location is within " + LOCATION_RADIUS + " meters of last location. Will be ignored.");
                        }
                    }
                    break;

                default:
                    Log.e(TAG, "Unrecognized Intent");
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
                    }

                } else {
                    Log.e(TAG,"Location Permissions Denied");
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FILES: {
                return;
            }
        }
    }

    private void redrawMap(){
        if(mMap != null) {
            for(Polyline p : lines) {
                p.remove();
            }
            lines.clear();

            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
            for (int i = 0; i < points.size(); i++) {
                Location place = points.get(i);
                options.add(new LatLng(place.getLatitude(), place.getLongitude()));
            }
            lines.add(mMap.addPolyline(options));

            for(Marker m : markers) {
                m.remove();
            }
            markers.clear();
            markerMap.clear();

            List<MarkerWrapper> l = MarkerWrapper.find(MarkerWrapper.class, "UUID = ?", UUID);
            for(MarkerWrapper m : l) {
                LatLng pos = new LatLng(m.getLat(),m.getLng());

                MarkerOptions marker = new MarkerOptions()
                        .position(pos)
                        .title(m.getLoc() + "");
                markers.add(mMap.addMarker(marker));

                markerMap.put(pos, m.getLoc());
            }
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        List<PictureWrapper> pw = PictureWrapper.find(PictureWrapper.class, "UUID = ? and LOC = ?", UUID, markerMap.get((marker.getPosition())) + "");
        String pictures = "";
        for(PictureWrapper p : pw) {
            pictures += p.getPic() + " ";
        }
        Log.d(TAG,"Marker " + marker.getTitle() +  " at " + marker.getPosition().toString() + " clicked with pictures " + pictures + ".");
        return true;
    }
}
