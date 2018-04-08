package com.example.user_admin.sensores;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Timer;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {

    //Classes
    GPSTracker gps;
    AccelerometerTracker acc;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    SFTP sftp;
    SensorsManager<MainActivity> sensorsManager;

    private SensorManager sensorManager;

    //variables
    public static Float[] sensorsData = new Float[12];

    //file to store sensors data
    public static final String SENSORSDATAFILENAME = "sensors.csv";
    public static final int COLLECTIONTIMEINTERVAL = 500; // Collection time interval i.e 125 = 8 per sec
    public static final int COLLECTIONTIMEDELAY = 0; // Collection time interval i.e 1000 = 1second

    //layout elements
    Button startBtn;
    Button stopBtn;
    Button submitBtn;
    TextView gpsTextView;
    TextView acelerometroTextView;
    TextView logsTxtBox;
    RadioGroup atividadesRadioGrp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find elements on view
        startBtn = (Button) findViewById(R.id.startBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        submitBtn = (Button) findViewById(R.id.submitBtn);
        logsTxtBox = findViewById(R.id.logsTxtBox);
        gpsTextView = findViewById(R.id.gpsTextView);
        acelerometroTextView = findViewById(R.id.acelerometroTextView);
        atividadesRadioGrp = (RadioGroup) findViewById(R.id.atividadesRadioGrp);

        stopBtn.setEnabled(false);

        //check location permissions (run time permissions)
        permissions = new Permissions(MainActivity.this);
        permissions.checkLocationPermissions();
        //permissions.checkWriteExternalStoragePermission();

        // create FileManager class object
        fileManager = new FileManager(MainActivity.this);
        // delete file (only for test , this shoud be removed later)
        fileManager.deleteFile(SENSORSDATAFILENAME);


        try {
            sensorsManager = SensorsManager.getInstance(this);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            finish();
        }

    }

    //this method will start sensors data collect
    public void startCollectSensorsDataBtn(View v) {
        gps = new GPSTracker(MainActivity.this);
        utils = new Utils(MainActivity.this);

        if (gps.canGetLocation) {
            disableActivities();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            submitBtn.setEnabled(false);

            //create new file to store sensors data, if doesn't exists
            fileManager.createFile(this.getFilesDir() + "/" + SENSORSDATAFILENAME);

            //start sensors
            sensorsManager.startSensors(MainActivity.this);
            Utils.showToast(getApplicationContext(), "Iniciou a recolha de dados");

        } else {
            gps.showSettingsAlert();
        }

    }

    //this method will stop sensors data collect
    public void stopCollectSensorsDataBtn(View v) {
        enableActivities();
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        submitBtn.setEnabled(true);
        sensorsManager.stopSensors();

        Utils.showToast(getApplicationContext(), "Pausou a recolha de dados");

    }

    //this function is used to upload file via sftp server.
    public void submitBtnClick(View view) {
        try {
            if (isNetworkAvailable()) {
                File file = new File(this.getFilesDir() + "/" + SENSORSDATAFILENAME);
                if (file.exists()) {

                    Runnable sftp = new SFTP(MainActivity.this, file);
                    Thread t = new Thread(sftp);
                    t.setDaemon(true);
                    t.start();

                    Utils.showToast(getApplicationContext(), "Ficheiro transferido com sucesso");

                } else {
                    Utils.showToast(getApplicationContext(), "O ficheiro não existe!");
                }
            } else {
                permissions.checkInternetPermissions();
                Utils.showToast(getApplicationContext(), "Sem acesso á internet!");
            }
        }catch (Exception ex){
            Utils.showToast(getApplicationContext(), "Ocorreu um problema ao transferir o ficheiro");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
        enableActivities();
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        submitBtn.setEnabled(true);
        sensorsManager.stopSensors();
    }


    //this method is used to get current activity selected
    private String getSelectedActivity() {
        int index = atividadesRadioGrp.indexOfChild(findViewById(atividadesRadioGrp.getCheckedRadioButtonId()));
        switch (index) {
            case 0:
                return "RUNNING";
            case 1:
                return "WALKING";
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

    //we can select another activity once we pause the collection, so using this method allows user to choose another activity
    private void enableActivities() {
        for (int i = 0; i < atividadesRadioGrp.getChildCount(); i++) {
            atividadesRadioGrp.getChildAt(i).setEnabled(true);
        }
    }

    //User can't select another activity while collection sensors data
    private void disableActivities() {
        for (int i = 0; i < atividadesRadioGrp.getChildCount(); i++) {
            atividadesRadioGrp.getChildAt(i).setEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (sensorsData) {
            gpsTextView.setText("GPS: " + location.getLatitude() + " , " + location.getLongitude());
            sensorsData[0] = (float)location.getLatitude();
            sensorsData[1] = (float)location.getLongitude();
            sensorsData[2] = (float)location.getAltitude();

            String activity = getSelectedActivity();
            if (activity == null) return;

            //confirm if all values are completed
            for (int i = 0; i < sensorsData.length; i++) {
                if (sensorsData[i] == null) {
                    return;
                }
            }

            long timestamp = Calendar.getInstance().getTimeInMillis();
            fileManager.writeDataToFile(SENSORSDATAFILENAME,sensorsData,timestamp,getSelectedActivity());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (sensorsData) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    sensorsData[3] = event.values[0];
                    sensorsData[4] = event.values[1];
                    sensorsData[5] = event.values[2];
                    acelerometroTextView.setText("X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    sensorsData[6] = event.values[0];
                    sensorsData[7] = event.values[1];
                    sensorsData[8] = event.values[2];

                case Sensor.TYPE_GRAVITY:
                    sensorsData[9] = event.values[0];
                    sensorsData[10] = event.values[1];
                    sensorsData[11] = event.values[2];
                    break;

                default:
                    return;
            }

            String activity = getSelectedActivity();
            if (activity == null) return;

            //confirm if all values are completed
           for (int i = 0; i < sensorsData.length; i++) {
                if (sensorsData[i] == null) {
                    return;
                }
            }
            long timestamp = Calendar.getInstance().getTimeInMillis();
            fileManager.writeDataToFile(SENSORSDATAFILENAME,sensorsData,timestamp,getSelectedActivity());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}





