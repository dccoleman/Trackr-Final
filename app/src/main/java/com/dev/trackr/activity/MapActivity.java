package com.dev.trackr.activity;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dev.trackr.Constants;
import com.dev.trackr.R;
import com.dev.trackr.service.TrackerService;
import com.dev.trackr.dbSchema.MarkerWrapper;
import com.dev.trackr.dbSchema.PersistVars;
import com.dev.trackr.dbSchema.PictureWrapper;
import com.dev.trackr.dbSchema.Points;
import com.dev.trackr.permissions.PermissionsManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private ArrayList<Location> points;

    private ArrayList<Polyline> lines;

    private HashMap<LatLng, Integer> markerMap;

    private ArrayList<Marker> markers;

    private PermissionsManager perms;

    private static Intent mServiceIntent;

    private static final String TAG = "Maps Activity++++";

    private static final int MAX_IGNORE_LOCATION_UPDATES = 2;

    private static int mNumIgnores = MAX_IGNORE_LOCATION_UPDATES;

    private static Location mLastLocation = null;

    private static String UUID = "";

    private boolean firstRun = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_tracker);

        UUID = getIntent().getStringExtra(Constants.Intents.IntentExtras.NEW_ADVENTURE);
        Log.d(TAG,UUID);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        PersistVars variables;
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
            points = savedInstanceState.getParcelableArrayList(Constants.SavedInstanceStateAccessors.STORED_POINTS);
        }

        perms = new PermissionsManager(this);

        perms.requestPermissionsIfNecessary();

        lines = new ArrayList<>();

        markerMap = new HashMap<>();
        markers = new ArrayList<>();

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

        TextView loading = (TextView) findViewById(R.id.loading);
        loading.setVisibility(VISIBLE);
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
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(Constants.SavedInstanceStateAccessors.STORED_POINTS, points);

        super.onSaveInstanceState(savedInstanceState);
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

        mMap.getUiSettings().setZoomGesturesEnabled(true);

        mMap.setOnMarkerClickListener(this);

        redrawMap();

        Button buttonOne = (Button) findViewById(R.id.takePicture);
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                PersistVars variables = PersistVars.find(PersistVars.class, "UUID = ?", UUID).get(0);

                Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File imagesFolder = new File(Constants.FileSystem.FILE_DIR + UUID);
                imagesFolder.mkdirs();

                File image = new File(imagesFolder, variables.getPhotos() + ".png");
                Uri uriSavedImage = Uri.fromFile(image);

                Log.d(TAG,variables.getPhotos() + "");

                variables.save();

                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(imageIntent, Constants.Intents.IntentExtras.REQUEST_PHOTO);
            }
        });

        Button button = (Button) findViewById(R.id.resetPath);
        button.setOnClickListener(new Button.OnClickListener() {
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
        PersistVars variables = PersistVars.find(PersistVars.class, "UUID = ?", UUID).get(0);
        // Check which request we're responding to
        if (requestCode == Constants.Intents.IntentExtras.REQUEST_PHOTO) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if(mLastLocation != null) {
                    int closeMarker = -1;
                    for (Marker m : markers) {
                        Location pos = new Location("");
                        pos.setLatitude(m.getPosition().latitude);
                        pos.setLongitude(m.getPosition().longitude);

                        if (mLastLocation.distanceTo(pos) < Constants.Location.LOCATION_RADIUS ) {
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

                        MarkerWrapper m = new MarkerWrapper(UUID, "", variables.getLocations(), pos.latitude, pos.longitude);
                        nameRequest(UUID, variables.getLocations());
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
            } else {
                List<PictureWrapper> z = PictureWrapper.find(PictureWrapper.class,"UUID = ?", UUID);
                for(PictureWrapper x : z) {
                    Log.d(TAG,"Picture " + x.getPic() + " goes with location " + x.getLoc());
                }
                variables.save();
            }
        }
        variables.save();
    }

    private void nameRequest(final String UUID, final int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Name for Location");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();
                MarkerWrapper m = MarkerWrapper.find(MarkerWrapper.class, "UUID = ? and LOC = ?", UUID, location + "").get(0);
                m.setName(text);
                m.save();

            }
        });

        builder.show();
    }

    //* handler for messages sent from the service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch(intent.getAction()) {
                case TrackerService.REQUEST_LOCATION_PERMISSION:
                    Log.e(TAG,"Permission request from service");
                    perms.requestPermissionsIfNecessary();
                    break;

                case TrackerService.UPDATE_LOCATION:
                    Location loc = intent.getParcelableExtra(TrackerService.UPDATE_LOCATION);
                    if(mLastLocation == null) {
                        mLastLocation = loc;
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
                        removeLoadingText();
                    }

                    if(loc != null) {
                        if (mNumIgnores >= MAX_IGNORE_LOCATION_UPDATES || (loc.distanceTo(mLastLocation) > Constants.Location.LOCATION_RADIUS && loc.distanceTo(mLastLocation) < Constants.Location.LOCATION_OUTLIER)) {
                            Points p = new Points(UUID, loc.getLatitude(), loc.getLongitude(), loc.getTime());
                            p.save();

                            //Log.v(TAG, "Location update from service: " + loc.toString());
                            points.add(loc);
                            redrawMap();
                            if(firstRun) {
                                firstRun = false;
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(), loc.getLongitude())));
                            }
                            removeLoadingText();

                            mLastLocation = loc;

                            mNumIgnores = 0;
                        } else {
                            Log.v(TAG, "Location is either within " + Constants.Location.LOCATION_RADIUS + " OR outside " + Constants.Location.LOCATION_OUTLIER + " meters. Will be ignored.");
                            Log.v(TAG, "Location has been ignored " + mNumIgnores + " times. After " + MAX_IGNORE_LOCATION_UPDATES + " times the location will be accepted");
                            mNumIgnores++;
                            removeLoadingText();
                        }
                    }
                    break;

                default:
                    Log.e(TAG, "Unrecognized Intent");
            }
        }
    };

    private void removeLoadingText() {
        TextView loadText = (TextView) findViewById(R.id.loading);
        loadText.setVisibility(INVISIBLE);
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
        int locationNum = markerMap.get(marker.getPosition());
        launchGallery(UUID, locationNum);
        return true;
    }

    public void launchGallery(String uuid, int location) {
        Intent intent = new Intent(getApplicationContext(), GalleryViewActivity.class);
        intent.putExtra(Constants.Intents.IntentExtras.LOCATION_UUID, uuid);
        intent.putExtra(Constants.Intents.IntentExtras.LOCATION_NUMBER, location);
        startActivity(intent);
    }
}
