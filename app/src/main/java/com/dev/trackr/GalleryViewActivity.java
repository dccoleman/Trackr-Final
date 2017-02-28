package com.dev.trackr;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GalleryViewActivity extends AppCompatActivity {

    private int loc;
    private String UUID;
    private ArrayList<File> images;

    private static final String FILE_DIR = Environment.getExternalStorageDirectory() + "/" + "Trackr/";

    public static final String FILE_PATH = "file_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery_view);

        loc = getIntent().getIntExtra(MapActivity.LOCATION_NUMBER, -1);
        UUID = getIntent().getStringExtra(MapActivity.LOCATION_UUID);

        images = new ArrayList<>();
        File f = new File(FILE_DIR + UUID);

        List<PictureWrapper> l = PictureWrapper.find(PictureWrapper.class, "UUID = ? and LOC = ?", UUID, loc + "");

        File[] files = f.listFiles();
        for(int i = 0; i < l.size(); i++) {
            images.add(files[l.get(i).getPic()]);
        }

        Log.d("GalleryView____", images.size() + "");

        GridView g = (GridView) findViewById(R.id.gridView);
        g.setNumColumns(2);
        g.setAdapter(new ImageAdapter(this, images));

        g.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Intent intent = new Intent(getApplicationContext(), ImageViewActivity.class);
                intent.putExtra(FILE_PATH, images.get(position).getAbsolutePath());

                startActivity(intent);

            }
        });


    }
}
