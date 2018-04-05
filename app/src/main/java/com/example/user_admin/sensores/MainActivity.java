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
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Classes
    GPSTracker gps;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    DataCollectionTimerTask dataCollectionTimerTask;

    //variables
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private  String TAG_WRITE_READ_FILE = "TAG_WRITE_READ_FILE";

    //file to store sensors data
    public static final String SENSORSDATAFILENAME = "sensors.csv";
    public static final int COLLECTIONTIMEINTERVAL = 5000; // Collection time interval i.e 5000 = 5seconds
    public static final int COLLECTIONTIMEDELAY= 1000; // Collection time interval i.e 1000 = 1second

    //layout elements
    Button startBtn;
    Button stopBtn;
    TextView logsTxtBox;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find elements on view
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        logsTxtBox = (TextView) findViewById(R.id.logsTxtBox);

        //check location permissions (run time permissions)
        permissions = new Permissions(MainActivity.this);
        permissions.checkLocationPermissions();

        //check external write permissions
        //permissions.checkWriteExternalStoragePermission();

        // create FileManager class object
        fileManager = new FileManager(MainActivity.this);
        // delete file (only for test , this shoud be removed later)
        fileManager.deleteFile(SENSORSDATAFILENAME);

        // show location button click event
        startBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                gps = new GPSTracker(MainActivity.this);

                if(gps.canGetLocation) {
                    Toast.makeText(getApplicationContext(), "Iniciou a recolha de dados", Toast.LENGTH_LONG).show();

                    if (timer != null) {
                        timer.cancel();
                    }

                    timer = new Timer();
                    dataCollectionTimerTask = new DataCollectionTimerTask(MainActivity.this);

                    //schedule task
                    timer.schedule(dataCollectionTimerTask, COLLECTIONTIMEDELAY, COLLECTIONTIMEINTERVAL);

                }
                else{
                    gps.showSettingsAlert();
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (timer!=null){
                    Toast.makeText(getApplicationContext(), "Pausou a recolha de dados", Toast.LENGTH_LONG).show();
                    timer.cancel();
                    timer = null;
                }
            }
        });
    }

    public void submitBtnClick(View view) {
        Toast.makeText(this, "Ficheiro submetido com sucesso!", Toast.LENGTH_SHORT).show();
    }



}





