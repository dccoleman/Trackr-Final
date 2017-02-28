package com.dev.trackr;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;

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
        // TODO Auto-generated method stub
        return adventures.size();
    }

    @Override
    public Adventure getItem(int position) {
        return adventures.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
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
                    Toast.makeText(context, "Deleting adventure " + adventures.get(position).getName(), Toast.LENGTH_SHORT).show();
                    adventures.remove(position).delete();
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
        for(Adventure a : adventures) {
            Log.v("Adventures Found", a.toString());
        }
        notifyDataSetChanged();
    }

} 