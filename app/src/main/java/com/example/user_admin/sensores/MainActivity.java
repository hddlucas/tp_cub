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

import java.io.File;
import java.util.Calendar;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.StrictMath.sqrt;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    //Classes
    GPSTracker gps;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    FFT fft;
    Arff arff;
    NoiseFilter noiseFilter;

    SensorsManager<MainActivity> sensorsManager;

    private float lastX, lastY, lastZ;
    private float deltaXMax = 0;
    private float deltaZMax = 0;
    private float timestamp;
    private final float[] deltaRotationVector = new float[4];
    private SensorManager sensorManager;

    //variables
    public static Float[] sensorsData = new Float[13];
    private static final float NS2S = 1.0f / 1000000000.0f;

    //file to store sensors data
    public static final String SENSORSDATAFILENAME = "sensors.csv";
    public static final String SENSORSDATAAVERAGEFILENAME = "average.csv";
    public static final String ARFFSENSORSDATAAVERAGEFILENAME = "average.arff";
    public static final String ARFFCSVFILENAME = "arff.csv";
    public static final String FILTERED_NOISE_ARFFCSVFILENAME = "filtered_noise_arff.csv";
    public static final String FILTERED_NOISE_ARFFFILENAME = "filtered_noise_arff.arff";
    public static final String ARFFFILENAME = "arff.arff";
    public static final String FFTFILENAME = "fft.csv";
    public static final String FILTERED_NOISE_FFTFILENAME = "filtered_noise_fft.csv";
    public static final String FILEHEADER = "lat,lng,alt,timestamp,x_acc,y_acc,z_acc,x_gyro,y_gyro,z_gyro,x_grav,y_grav,z_grav,lum,activity\n";
    public static final String FFTFILEHEADER = "Time,Data ACC,FFT freq ACC,Serie,FFT mag ACC,FFT Complex ACC,Data GYRO,FFT freq GYRO,Serie,FFT mag GYRO,FFT Complex GYRO,Data GRAV,FFT freq GRAV,Serie,FFT mag GRAV,FFT Complex GRAV,Activity\n";
    public static final String[] ACTIVITIES = new String[]{"WALKING","RUNNING","DRIVING","GO_UPSTAIRS","GO_DOWNSTAIRS"};


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

        //uncomment to generate offline files for use in WEKA
        //fft = new FFT(this.getApplicationContext());
        //fft.generateFourierTransform(true);

        //arff = new Arff(this.getApplicationContext());
        //arff.generateArffFile(true);

        //noiseFilter = new NoiseFilter(this.getApplicationContext());
        //noiseFilter.calculateAverage();

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

        logsTxtBox.setText("Lista de Sensores:\n\n" +
                "GPS" + "\n" +
                "Acelerómetro" + "\n" +
                "Luminosidade" + "\n" +
                "Gravidade" + "\n" +
                "Giroscópio" + "\n"
        );
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
            fileManager.createFile(this.getFilesDir() + "/" + SENSORSDATAFILENAME,FILEHEADER);

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
        } catch (Exception ex) {
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
            sensorsData[0] = (float) location.getLatitude();
            sensorsData[1] = (float) location.getLongitude();
            sensorsData[2] = (float) location.getAltitude();

            String activity = getSelectedActivity();
            if (activity == null) return;

            //confirm if all values are completed
            for (int i = 0; i < sensorsData.length; i++) {
                if (sensorsData[i] == null) {
                    return;
                }
            }
            long timestamp = Calendar.getInstance().getTimeInMillis();
            fileManager.writeDataToFile(SENSORSDATAFILENAME, sensorsData, timestamp, getSelectedActivity());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (sensorsData) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:

                    // get the change of the x,y,z values of the accelerometer
                    float deltaX = Math.abs(lastX - event.values[0]);
                    float deltaY = Math.abs(lastY - event.values[1]);
                    float deltaZ = Math.abs(lastZ - event.values[2]);

                    // if the change is below 2, it is just plain noise
                    if (deltaX < 2)
                        deltaX = 0;
                    if (deltaY < 2)
                        deltaY = 0;

                    // get the change of the x,y,z values of the accelerometer
                    deltaX = Math.abs(lastX - event.values[0]);
                    deltaY = Math.abs(lastY - event.values[1]);
                    deltaZ = Math.abs(lastZ - event.values[2]);

                    // if the change is below 2, it is just plain noise
                    if (deltaX < 2)
                        deltaX = 0;
                    if (deltaY < 2)
                        deltaY = 0;
                    if (deltaZ < 2)
                        deltaZ = 0;

                    // set the last know values of x,y,z
                    lastX = event.values[0];
                    lastY = event.values[1];
                    lastZ = event.values[2];

                    sensorsData[3] = event.values[0];
                    sensorsData[4] = event.values[1];
                    sensorsData[5] = event.values[2];
                    acelerometroTextView.setText("X: " + event.values[0] + " Y: " + event.values[1] + " Z: " + event.values[2]);
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    // This timestep's delta rotation to be multiplied by the current rotation
                    // after computing it from the gyro sample data.
                    if (timestamp != 0) {
                        final float dT = (event.timestamp - timestamp) * NS2S;
                        // Axis of the rotation sample, not normalized yet.
                        float axisX = event.values[0];
                        float axisY = event.values[1];
                        float axisZ = event.values[2];

                        // Calculate the angular speed of the sample
                        float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                        // Normalize the rotation vector if it's big enough to get the axis
                        // (that is, EPSILON should represent your maximum allowable margin of error)
                        if (omegaMagnitude > EPSILON) {
                            axisX /= omegaMagnitude;
                            axisY /= omegaMagnitude;
                            axisZ /= omegaMagnitude;
                        }

                        // Integrate around this axis with the angular speed by the timestep
                        // in order to get a delta rotation from this sample over the timestep
                        // We will convert this axis-angle representation of the delta rotation
                        // into a quaternion before turning it into the rotation matrix.
                        float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                        float sinThetaOverTwo = (float) sin(thetaOverTwo);
                        float cosThetaOverTwo = (float) cos(thetaOverTwo);
                        deltaRotationVector[0] = sinThetaOverTwo * axisX;
                        deltaRotationVector[1] = sinThetaOverTwo * axisY;
                        deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                        deltaRotationVector[3] = cosThetaOverTwo;
                    }

                    timestamp = event.timestamp;
                    float[] deltaRotationMatrix = new float[9];
                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                    // User code should concatenate the delta rotation we computed with the current rotation
                    // in order to get the updated rotation.
                    // rotationCurrent = rotationCurrent * deltaRotationMatrix;

                    sensorsData[6] = event.values[0];
                    sensorsData[7] = event.values[1];
                    sensorsData[8] = event.values[2];

                case Sensor.TYPE_GRAVITY:
                    sensorsData[9] = event.values[0];
                    sensorsData[10] = event.values[1];
                    sensorsData[11] = event.values[2];
                    break;
                case Sensor.TYPE_LIGHT:
                    sensorsData[12] = event.values[0];

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
            fileManager.writeDataToFile(SENSORSDATAFILENAME, sensorsData, timestamp, getSelectedActivity());
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





