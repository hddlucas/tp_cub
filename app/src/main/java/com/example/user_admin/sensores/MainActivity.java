package com.example.user_admin.sensores;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Classes
    GPSTracker gps;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;

    //variables
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private  String TAG_WRITE_READ_FILE = "TAG_WRITE_READ_FILE";
    //file to store sensors data
    public static final String sensorsDataFilename = "sensors.csv";

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

        // create GPSTracker class object
        gps = new GPSTracker(MainActivity.this);

        // create Utils class object
        utils = new Utils(MainActivity.this);

        // create FileManager class object
        fileManager = new FileManager(MainActivity.this);

        fileManager.deleteFile(sensorsDataFilename);

        // show location button click event
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // check if GPS enabled
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    double altitude = gps.getAltitude();

                    //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    TextView logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);
                    logsTxtBox.append("Posição Atual: Lat: " + latitude + " Long: " + longitude + " Altitude: " + altitude + "\n");
                    
                    fileManager.writeDataToFile(sensorsDataFilename,latitude + "," + longitude + "," + altitude + "," + utils.getCurrentDate("dd-mm-yy") + "," + utils.getCurrentTime("hh:mm:ss") + "\n");


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

        // test if information is being saved on internal storage file
        logsTxtBox.append(fileManager.readFromFileInputStream(sensorsDataFilename));

    }

    public void submitBtnClick(View view) {
        Toast.makeText(this, "Submetido com sucesso!", Toast.LENGTH_SHORT).show();
    }


}





