package com.dev.trackr;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.orm.SugarRecord;

import java.util.UUID;

public class MainMenuActivity extends AppCompatActivity {

    public static final String NEW_ADVENTURE = "new_adventure";
    public static final String RETURN_TO_MENU = "back_to_menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu_activity);

        Button createButton = (Button) findViewById(R.id.createAdventure);
        createButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                UUID uuid = UUID.randomUUID();

                intent.putExtra(NEW_ADVENTURE, uuid.toString());
                startActivity(intent);

            }
        });



        }

}
