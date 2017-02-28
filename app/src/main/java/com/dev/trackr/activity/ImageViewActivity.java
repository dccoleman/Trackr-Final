package com.dev.trackr.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.location.Location;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.trackr.R;
import com.dev.trackr.adapters.PhotoAdapter;
import com.dev.trackr.dbSchema.MarkerWrapper;
import com.dev.trackr.dbSchema.PictureWrapper;
import com.google.android.gms.maps.model.LatLng;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView img = (ImageView) findViewById(R.id.originalView);
        Bitmap bitmap = BitmapFactory.decodeFile(new File(getIntent().getStringExtra(GalleryViewActivity.FILE_PATH)).getAbsolutePath());
        img.setImageBitmap(bitmap);

        String path = getIntent().getStringExtra(GalleryViewActivity.FILE_PATH);

        ArrayList<String> a = new ArrayList<>();
        for(String s : path.split("/")) {
            a.add(s);
        }

        String end = a.get(a.size() - 1);
        String index = end.substring(0,end.length() - 4);
        Log.d("TG", index);

        String UUID = getIntent().getStringExtra(GalleryViewActivity.PICTURE_UUID);

        PictureWrapper p = PictureWrapper.find(PictureWrapper.class, "UUID = ? and PIC = ?", UUID, index).get(0);

        MarkerWrapper m = MarkerWrapper.find(MarkerWrapper.class, "UUID = ? and LOC = ?", UUID, p.getLoc() + "").get(0);


        TextView t = (TextView) findViewById(R.id.locationText);
        t.setText("Latitude: " + m.getLat() + " Longitude: " + m.getLng());


    }
}
