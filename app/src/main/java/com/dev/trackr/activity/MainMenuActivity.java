package com.dev.trackr.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.dev.trackr.Constants;
import com.dev.trackr.R;
import com.dev.trackr.adapters.AdventureAdapter;
import com.dev.trackr.dbSchema.Adventure;
import com.dev.trackr.permissions.PermissionsManager;
import com.orm.SugarContext;
import com.orm.SugarRecord;

import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    public static final String TAG = "Main Menu====";

    private PermissionsManager perms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_activity);

        //* Initialize database
        SugarContext.init(getApplicationContext());

        //* Create tables if they do not exist, but only do so the first time this activity is loaded
        if(savedInstanceState == null) {
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS POINTS (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, LAT DOUBLE, LNG DOUBLE, TIME LONG)");
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS ADVENTURE (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, NAME TEXT)");
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS MARKER_WRAPPER (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, NAME TEXT, LOC INT, LAT DOUBLE, LNG DOUBLE)");
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS PICTURE_WRAPPER (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, LOC INT, PIC INT)");
            SugarRecord.executeQuery("CREATE TABLE IF NOT EXISTS PERSIST_VARS (ID INTEGER PRIMARY KEY AUTOINCREMENT, UUID TEXT, PHOTOS INT, LOCATIONS INT)");
            /*SugarRecord.executeQuery("DROP TABLE POINTS");
            SugarRecord.executeQuery("DROP TABLE ADVENTURE");
            SugarRecord.executeQuery("DROP TABLE MARKER_WRAPPER");
            SugarRecord.executeQuery("DROP TABLE PICTURE_WRAPPER");
            SugarRecord.executeQuery("DROP TABLE PERSIST_VARS");*/
        }

        AdventureAdapter itemsAdapter = new AdventureAdapter(this);

        ListView l = (ListView) findViewById(R.id.past_adventures);
        l.setAdapter(itemsAdapter);

        l.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3)
            {
                AdventureAdapter c = (AdventureAdapter) adapter.getAdapter();
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

        perms = new PermissionsManager(this);
        perms.requestPermissionsIfNecessary();
        }

    public void launchMap(String uuid) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra(Constants.Intents.IntentExtras.NEW_ADVENTURE, uuid);

        startActivity(intent);
    }

    //* this is called when the permissions request is answered
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        perms.handlePermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        SugarContext.terminate();
        super.onDestroy();
    }

}
