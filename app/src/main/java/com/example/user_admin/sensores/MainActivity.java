package com.example.user_admin.sensores;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    //Classes
    GPSTracker gps;
    FileManager fm;
    Permissions permissions;

    //variables
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private  String TAG_WRITE_READ_FILE = "TAG_WRITE_READ_FILE";

    //elements
    Button startBtn;
    TextView logsTxtBox;
    private String dataFileName = "sensores.csv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = (Button) findViewById(R.id.startBtn);
        logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);

        //check location permissions (run time permissions)
        permissions = new Permissions(MainActivity.this);
        permissions.checkLocationPermissions();

        //check external write permissions
        //permissions.checkWriteExternalStoragePermission();


        String filename = "myfile";
        String fileContents = "Hello world!";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    double altitude = gps.getAltitude();

                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    TextView logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);
                    logsTxtBox.append("Posição Atual: Lat: " + latitude + " Long: " + longitude + " Altitude: " + altitude + "\n");


                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
    }



    public void stopBtnClick(View view) throws FileNotFoundException {

        //stop using gps
        gps.stopUsingGPS();
        logsTxtBox.setText("");
        Toast.makeText(this, "Pausou a recolha de dados!", Toast.LENGTH_SHORT).show();
    }

    public void submitBtnClick(View view) {
        Toast.makeText(this, "Submetido com sucesso!", Toast.LENGTH_SHORT).show();
    }


}





