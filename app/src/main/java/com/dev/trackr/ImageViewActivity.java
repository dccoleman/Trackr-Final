package com.dev.trackr;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        ImageView img = (ImageView) findViewById(R.id.originalView);
        Bitmap bitmap = BitmapFactory.decodeFile(new File(getIntent().getStringExtra(GalleryViewActivity.FILE_PATH)).getAbsolutePath());
        img.setImageBitmap(bitmap);
    }
}
