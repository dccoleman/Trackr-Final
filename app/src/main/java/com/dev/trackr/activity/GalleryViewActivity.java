package com.dev.trackr.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.dev.trackr.Constants;
import com.dev.trackr.R;
import com.dev.trackr.adapters.PhotoAdapter;
import com.dev.trackr.dbSchema.MarkerWrapper;
import com.dev.trackr.dbSchema.PictureWrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryViewActivity extends AppCompatActivity {

    private int loc;
    private String UUID;
    private ArrayList<File> images;

    private static final String FILE_DIR = Environment.getExternalStorageDirectory() + "/" + "Trackr/";

    public static final String FILE_PATH = "file_path";
    public static final String PICTURE_UUID = "picture_uuid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery_view);

        loc = getIntent().getIntExtra(Constants.Intents.IntentExtras.LOCATION_NUMBER, -1);
        UUID = getIntent().getStringExtra(Constants.Intents.IntentExtras.LOCATION_UUID);

        images = new ArrayList<>();
        File f = new File(FILE_DIR + UUID);

        List<PictureWrapper> l = PictureWrapper.find(PictureWrapper.class, "UUID = ? and LOC = ?", UUID, loc + "");

        File[] files = f.listFiles();
        for(int i = 0; i < l.size(); i++) {
            images.add(files[l.get(i).getPic()]);
        }

        if(images.size() == 1) {
            startImageView(images.get(0).getAbsolutePath(),UUID);
        }

        MarkerWrapper m = MarkerWrapper.find(MarkerWrapper.class, "UUID = ? and LOC = ?", UUID, loc + "").get(0);

        TextView t = (TextView) findViewById(R.id.titleText);
        t.setText(m.getName());

        Log.d("GalleryView____", images.size() + "");

        GridView g = (GridView) findViewById(R.id.gridView);
        g.setNumColumns(2);
        g.setAdapter(new PhotoAdapter(this, images));

        g.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                startImageView(images.get(position).getAbsolutePath(), UUID);

            }
        });


    }

    public void startImageView(String file, String UUID) {
        Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
        intent.putExtra(FILE_PATH, file);
        intent.putExtra(PICTURE_UUID, UUID);

        startActivity(intent);
    }
}
