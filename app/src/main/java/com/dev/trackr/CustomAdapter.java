package com.dev.trackr;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    ArrayList<Adventure> adventures;

    private static LayoutInflater inflater=null;
    public CustomAdapter(MainMenuActivity mainActivity) {
        adventures = new ArrayList<>();
        refreshView();
        context=mainActivity;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return adventures.size();
    }

    @Override
    public Adventure getItem(int position) {
        return adventures.get(position);
    }

    @Override
    public long getItemId(int position) {
        return adventures.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            View rowView;
            rowView = inflater.inflate(R.layout.list_item, null);
            TextView tv = (TextView) rowView.findViewById(R.id.adventureNameField);
            tv.setText(adventures.get(position).getName());

            Button b = (Button) rowView.findViewById(R.id.deleteItem);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Deleting " + adventures.get(position).getName(), Toast.LENGTH_SHORT).show();
                    Adventure a = adventures.remove(position);

                    PersistVars.find(PersistVars.class, "UUID = ?", a.getUUID()).get(0).delete();
                    List<MarkerWrapper> mw = MarkerWrapper.find(MarkerWrapper.class, "UUID = ?", a.getUUID());
                    for(MarkerWrapper m : mw) {
                        m.delete();
                    }

                    List<PictureWrapper> pw = PictureWrapper.find(PictureWrapper.class, "UUID + ?", a.getUUID());
                    for(PictureWrapper p : pw) {
                        p.delete();
                    }

                    File dir = new File(Environment.getExternalStorageDirectory() + "/" + "Trackr/" + a.getUUID());
                    if (dir.isDirectory())
                    {
                        String[] children = dir.list();
                        for (int i = 0; i < children.length; i++)
                        {
                            new File(dir, children[i]).delete();
                        }
                    }
                    dir.delete();

                    a.delete();
                    refreshView();
                }
            });

            return rowView;
        } else {
            TextView tv = (TextView) convertView.findViewById(R.id.adventureNameField);
            tv.setText(adventures.get(position).getName());
            return convertView;
        }

    }

    public void refreshView() {
        adventures.clear();
        adventures.addAll(Adventure.listAll(Adventure.class));
        Collections.reverse(adventures);
        notifyDataSetChanged();
    }

} 