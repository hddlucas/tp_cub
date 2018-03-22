package com.example.user_admin.sensores;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // GPSTracker class
    GPSTracker gps;
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    
    //elements
    Button startBtn;
    TextView logsTxtBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        startBtn = (Button) findViewById(R.id.startBtn);
        logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);

        // show location button click event
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(MainActivity.this);

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    TextView logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);
                    logsTxtBox.append("Posição Atual: Lat: " + latitude + " Long: " + longitude + "\n");

                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
    }

    public void stopBtnClick(View view) {
        gps.stopUsingGPS();
        Toast.makeText(this, "Pausou a recolha de dados!", Toast.LENGTH_SHORT).show();
    }

    public void submitBtnClick(View view) {
        Toast.makeText(this, "Submetido com sucesso!", Toast.LENGTH_SHORT).show();
    }


}





