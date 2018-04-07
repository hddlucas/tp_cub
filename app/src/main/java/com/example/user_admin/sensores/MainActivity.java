package com.example.user_admin.sensores;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Classes
    GPSTracker gps;
    AccelerometerTracker acc;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    DataCollectionTimerTask dataCollectionTimerTask;
    SFTP sftp;

    //variables
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private  String TAG_WRITE_READ_FILE = "TAG_WRITE_READ_FILE";

    //file to store sensors data
    public static final String SENSORSDATAFILENAME = "sensors.csv";
    public static final int COLLECTIONTIMEINTERVAL = 5000; // Collection time interval i.e 125 = 8 per sec
    public static final int COLLECTIONTIMEDELAY= 0; // Collection time interval i.e 1000 = 1second

    //layout elements
    Button startBtn;
    Button stopBtn;
    TextView gpsTextView;
    RadioGroup atividadesRadioGrp;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find elements on view
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);

        //check location permissions (run time permissions)
        permissions = new Permissions(MainActivity.this);
        permissions.checkLocationPermissions();
        permissions.checkInternetPermissions();

        this.getSelectedActivity();


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
                acc = new AccelerometerTracker(MainActivity.this);

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

        Runnable sftp = new SFTP(MainActivity.this);

        Thread t = new Thread(sftp);
        t.setDaemon(true);
        t.start();

    }

    private String getSelectedActivity() {

        atividadesRadioGrp = (RadioGroup) findViewById(R.id.atividadesRadioGrp);
        int index = atividadesRadioGrp.indexOfChild(findViewById(atividadesRadioGrp.getCheckedRadioButtonId()));

        switch (index) {
            case 0:
                return "WALKING";
            case 1:
                return "RUNNING";
            case 2:
                return "GO_DOWNSTAIRS";
            case 3:
                return "GO_UPSTAIRS";
            case 4:
                return "DRIVING";
            default:
                return null;
        }

    }

}





