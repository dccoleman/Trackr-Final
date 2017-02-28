package com.dev.trackr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Bitmap> files;

    private int MAX_WIDTH, MAX_HEIGHT;

    private static final int PICTURE_SIZE = 1000;

    public ImageAdapter(Context c, ArrayList<File> files) {
        mContext = c;
        this.files = new ArrayList<>();



        MAX_WIDTH = Resources.getSystem().getDisplayMetrics().widthPixels;
        MAX_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;

        for(File x : files) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(x.getAbsolutePath(),bmOptions);
            this.files.add(scaleBitmapAndKeepRation(bitmap, MAX_WIDTH/2,MAX_HEIGHT/2));
    }
    }

    public int getCount() {
        return files.size();
    }

    public Object getItem(int position) {
        return files.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(PICTURE_SIZE, PICTURE_SIZE));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(files.get(position));
        return imageView;
    }

    public static Bitmap scaleBitmapAndKeepRation(Bitmap TargetBmp,int reqHeightInPixels,int reqWidthInPixels)
    {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, TargetBmp.getWidth(), TargetBmp.getHeight()), new RectF(0, 0, reqWidthInPixels, reqHeightInPixels), Matrix.ScaleToFit.CENTER);
        Bitmap scaledBitmap = Bitmap.createBitmap(TargetBmp, 0, 0, TargetBmp.getWidth(), TargetBmp.getHeight(), m, true);
        return scaledBitmap;
    }
}