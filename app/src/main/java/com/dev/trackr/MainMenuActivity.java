package com.dev.trackr;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    public static final String NEW_ADVENTURE = "new_adventure";
    public static final String RETURN_TO_MENU = "back_to_menu";
    public static final String TAG = "Main Menu====";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_activity);

        SugarContext.init(getApplicationContext());

        if(savedInstanceState == null) {
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS POINTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, LAT DOUBLE, LNG DOUBLE, TIME LONG)");
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS ADVENTURE (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, NAME TEXT)");
        }

        ArrayList<Adventure> a = new ArrayList<>();
        a.addAll(Adventure.listAll(Adventure.class));
        Collections.reverse(a);

        //ArrayAdapter<Adventure> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, a);
        CustomAdapter itemsAdapter = new CustomAdapter(this);

        ListView l = (ListView) findViewById(R.id.past_adventures);
        l.setAdapter(itemsAdapter);

        l.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                CustomAdapter c = (CustomAdapter) adapter.getAdapter();
                Adventure value = c.getItem(position);
                Log.d(TAG,value.getUUID());
                launchMap(value.getUUID());
            }
        });

        Button createButton = (Button) findViewById(R.id.createAdventure);
        final EditText textfield = (EditText) findViewById(R.id.adventureName);
        createButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                UUID uuid = UUID.randomUUID();
                Adventure a = new Adventure(uuid.toString(), textfield.getText().toString());
                a.save();
                launchMap(uuid.toString());
            }
        });

        }

    public void launchMap(String uuid) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);

        intent.putExtra(NEW_ADVENTURE, uuid);

        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        SugarContext.terminate();
    }

}
