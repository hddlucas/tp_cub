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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.StrictMath.sqrt;

public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    //Classes
    GPSTracker gps;
    AccelerometerTracker acc;
    FileManager fileManager;
    Permissions permissions;
    Complex complex;
    Utils utils;
    SFTP sftp;
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
        utils = new Utils(this.getApplicationContext());
        //only for test
        //generateFourierTransform();
        //generateArffFile();
        //utils.calculateAverage();

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

    private void generateFourierTransform() {
        utils = new Utils(MainActivity.this);
        List<String[]> rows = new ArrayList<>();

        try {
            FileManager csvReader = new FileManager(this.getApplicationContext());
            rows = csvReader.readCSV("treino.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int N = 64;
        FFT fft = new FFT(N);
        double[] window = fft.getWindow();

        //acc
        double[] re_acc = new double[N];
        double[] im_acc = new double[N];

        //gyro
        double[] re_gyro = new double[N];
        double[] im_gyro = new double[N];

        //grav
        double[] re_grav = new double[N];
        double[] im_grav = new double[N];

        String fft_complex = "";

        double acc_sqrt, gyro_sqrt, grav_sqrt;

        ArrayList fft_complex_array = new ArrayList<String>();
        List<List<String>> fftExcelData = new ArrayList<List<String>>();
        List<String> timestamp = new ArrayList<String>();

        List<String> data_acc = new ArrayList<String>();
        List<String> fft_freq_acc = new ArrayList<String>();
        List<String> serie_acc = new ArrayList<String>();
        List<String> fft_mag_acc = new ArrayList<String>();
        List<String> fft_complex_acc = new ArrayList<String>();

        List<String> data_gyro = new ArrayList<String>();
        List<String> fft_freq_gyro = new ArrayList<String>();
        List<String> serie_gyro = new ArrayList<String>();
        List<String> fft_mag_gyro = new ArrayList<String>();
        List<String> fft_complex_gyro = new ArrayList<String>();

        List<String> data_grav = new ArrayList<String>();
        List<String> fft_freq_grav = new ArrayList<String>();
        List<String> serie_grav = new ArrayList<String>();
        List<String> fft_mag_grav = new ArrayList<String>();
        List<String> fft_complex_grav = new ArrayList<String>();
        List<String> current_activity = new ArrayList<String>();

        //acc
        fftExcelData.add(timestamp);
        fftExcelData.add(data_acc);
        fftExcelData.add(fft_freq_acc);
        fftExcelData.add(serie_acc);
        fftExcelData.add(fft_mag_acc);
        fftExcelData.add(fft_complex_acc);

        //gyro
        fftExcelData.add(data_gyro);
        fftExcelData.add(fft_freq_gyro);
        fftExcelData.add(serie_gyro);
        fftExcelData.add(fft_mag_gyro);
        fftExcelData.add(fft_complex_gyro);

        //grav
        fftExcelData.add(data_grav);
        fftExcelData.add(fft_freq_grav);
        fftExcelData.add(serie_grav);
        fftExcelData.add(fft_mag_grav);
        fftExcelData.add(fft_complex_grav);
        fftExcelData.add(current_activity);

        String activity = "DRIVING";
        int aux = 0;

        for (int j = 0, x = 0; j < rows.size(); j++) {
            //stop cycle
            if (j + N > rows.size())
                break;

            if (rows.get(j)[14].equals(activity)) {
                if (x != 0 && x % N == 0) {
                    //acc
                    fft.beforeAfter(fft, re_acc, im_acc);
                    for (int k = 0; k < re_acc.length; k++) {
                        if (im_acc[k] > 0)
                            fft_complex = String.valueOf(re_acc[k]) + "+" + String.valueOf(im_acc[k]) + "i";
                        else if (im_acc[k] < 0)
                            fft_complex = String.valueOf(re_acc[k]) + String.valueOf(im_acc[k]) + "i";
                        else
                            fft_complex = String.valueOf(re_acc[k]);

                        //fft_complex_array.add(fft_complex);
                        complex = new Complex(re_acc[k], im_acc[k]);

                        //FFT mag acc
                        fftExcelData.get(4).add(String.valueOf((double) 2 / N * complex.abs()));

                        //FFT complex acc
                        fftExcelData.get(5).add(fft_complex);
                    }
                    //gyro
                    fft.beforeAfter(fft, re_gyro, im_gyro);
                    for (int k = 0; k < re_gyro.length; k++) {
                        if (im_gyro[k] > 0)
                            fft_complex = String.valueOf(re_gyro[k]) + "+" + String.valueOf(im_gyro[k]) + "i";
                        else if (im_gyro[k] < 0)
                            fft_complex = String.valueOf(re_gyro[k]) + String.valueOf(im_gyro[k]) + "i";
                        else
                            fft_complex = String.valueOf(re_gyro[k]);

                        //fft_complex_array.add(fft_complex);
                        complex = new Complex(re_gyro[k], im_gyro[k]);

                        //FFT mag gyro
                        fftExcelData.get(9).add(String.valueOf((double) 2 / N * complex.abs()));

                        //FFT complex gyro
                        fftExcelData.get(10).add(fft_complex);
                    }
                    //grav
                    fft.beforeAfter(fft, re_grav, im_grav);
                    for (int k = 0; k < re_grav.length; k++) {
                        if (im_grav[k] > 0)
                            fft_complex = String.valueOf(re_grav[k]) + "+" + String.valueOf(im_grav[k]) + "i";
                        else if (im_grav[k] < 0)
                            fft_complex = String.valueOf(re_grav[k]) + String.valueOf(im_grav[k]) + "i";
                        else
                            fft_complex = String.valueOf(re_grav[k]);

                        //fft_complex_array.add(fft_complex);
                        complex = new Complex(re_grav[k], im_grav[k]);

                        //FFT mag grav
                        fftExcelData.get(14).add(String.valueOf((double) 2 / N * complex.abs()));

                        //FFT complex grav
                        fftExcelData.get(15).add(fft_complex);
                    }

                    aux = 0;

                    //acc
                    re_acc = new double[N];
                    im_acc = new double[N];

                    //gyro
                    re_gyro = new double[N];
                    im_gyro = new double[N];

                    //grav
                    re_grav = new double[N];
                    im_grav = new double[N];

                }

                //timestamp
                fftExcelData.get(0).add(rows.get(j)[3]);
                acc_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[4]), Double.parseDouble(rows.get(j)[5]), Double.parseDouble(rows.get(j)[6]));
                gyro_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[7]), Double.parseDouble(rows.get(j)[8]), Double.parseDouble(rows.get(j)[9]));
                grav_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[10]), Double.parseDouble(rows.get(j)[11]), Double.parseDouble(rows.get(j)[12]));

                //data_acc
                fftExcelData.get(1).add(String.valueOf(acc_sqrt));
                //data_gyro
                fftExcelData.get(6).add(String.valueOf(gyro_sqrt));
                //data_grav
                fftExcelData.get(11).add(String.valueOf(grav_sqrt));

                //FFT freq acc
                fftExcelData.get(2).add(String.valueOf((double) aux * 308 / N));
                //FFT freq gyro
                fftExcelData.get(7).add(String.valueOf((double) aux * 308 / N));
                //FFT freq grav
                fftExcelData.get(12).add(String.valueOf((double) aux * 308 / N));

                //serie acc
                fftExcelData.get(3).add(String.valueOf(aux));
                //serie gyro
                fftExcelData.get(8).add(String.valueOf(aux));
                //serie grav
                fftExcelData.get(13).add(String.valueOf(aux));


                fftExcelData.get(16).add(String.valueOf(activity));

                re_acc[aux] = acc_sqrt;
                im_acc[aux] = 0;

                re_gyro[aux] = gyro_sqrt;
                im_gyro[aux] = 0;

                re_grav[aux] = grav_sqrt;
                im_grav[aux] = 0;
                aux++;
                x++;
            }
        }

        //remove exceeded elements
        int size = timestamp.size();
        if (aux < N+1) {
            for (int j = 1; j < aux + 1; j++) {
                timestamp.remove(size - j);
                data_acc.remove(size - j);
                data_gyro.remove(size - j);
                data_grav.remove(size - j);
                serie_acc.remove(size - j);
                serie_gyro.remove(size - j);
                serie_grav.remove(size - j);
                fft_freq_acc.remove(size - j);
                fft_freq_gyro.remove(size - j);
                fft_freq_grav.remove(size - j);
                current_activity.remove(size - j);

            }
        }

        //transpose
        fftExcelData = utils.transpose(fftExcelData);

        //create new file to store sensors data, if doesn't exists
        fileManager.createFile(this.getFilesDir() + "/" + SENSORSDATAFILENAME);

        try {
            String path = this.getFilesDir() + "/" +activity + "_FFT.csv";

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream writer = new FileOutputStream(path);

                String fileHeader = "Time,Data ACC,FFT freq ACC,Serie,FFT mag ACC,FFT Complex ACC,Data GYRO,FFT freq GYRO,Serie,FFT mag GYRO,FFT Complex GYRO,Data GRAV,FFT freq GRAV,Serie,FFT mag GRAV,FFT Complex GRAV,Activity\n";
                writer.write((fileHeader).getBytes());

                Iterator<List<String>> iter = fftExcelData.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    while (siter.hasNext()) {
                        String s = siter.next() + ",";
                        writer.write((s).getBytes());
                    }
                    writer.write(("\n").getBytes());
                }

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis();
    }

    private void generateArffFile() {
        utils = new Utils(MainActivity.this);
        List<String[]> rows = new ArrayList<>();

        try {
            FileManager csvReader = new FileManager(this.getApplicationContext());
            rows = csvReader.readCSV("treino.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int N = 64;
        FFT fft = new FFT(N);
        double[] window = fft.getWindow();

        //acc
        double[] re_acc = new double[N];
        double[] im_acc = new double[N];

        //gyro
        double[] re_gyro = new double[N];
        double[] im_gyro = new double[N];

        //grav
        double[] re_grav = new double[N];
        double[] im_grav = new double[N];

        String fft_complex = "";

        double acc_sqrt, gyro_sqrt, grav_sqrt;

        List<List<String>> fftExcelData = new ArrayList<List<String>>();

        List<String> accValues = new ArrayList<String>();
        List<String> gyroValues = new ArrayList<String>();
        List<String> gravValues = new ArrayList<String>();

        //lines for sensors
        fftExcelData.add(accValues);
        fftExcelData.add(gyroValues);
        fftExcelData.add(gravValues);
        String activity = "WALKING";

        int aux = 0;
        for (int j = 0,x = 0; j < rows.size(); j++) {
            if (j + N > rows.size())
                break;
            if (rows.get(j)[14].equals(activity)) {
                if (x != 0 && x % N == 0) {
                    //acc
                    fft.beforeAfter(fft, re_acc, im_acc);
                    for (int k = 0; k < re_acc.length; k++) {
                        fft_complex = String.valueOf(re_acc[k]);
                        //FFT complex acc
                        fftExcelData.get(0).add(fft_complex);
                    }
                    //gyro
                    fft.beforeAfter(fft, re_gyro, im_gyro);
                    for (int k = 0; k < re_gyro.length; k++) {
                        fft_complex = String.valueOf(re_gyro[k]);

                        //FFT mag gyro
                        fftExcelData.get(1).add(fft_complex);
                    }
                    //grav
                    fft.beforeAfter(fft, re_grav, im_grav);
                    for (int k = 0; k < re_grav.length; k++) {
                        fft_complex = String.valueOf(re_grav[k]);

                        //FFT complex
                        fftExcelData.get(2).add(fft_complex);
                    }

                    //stop cycle
                    if (j + N > rows.size())
                        break;

                    aux = 0;

                    //acc
                    re_acc = new double[N];
                    im_acc = new double[N];

                    //gyro
                    re_gyro = new double[N];
                    im_gyro = new double[N];

                    //grav
                    re_grav = new double[N];
                    im_grav = new double[N];
                }

                acc_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[4]), Double.parseDouble(rows.get(j)[5]), Double.parseDouble(rows.get(j)[6]));
                gyro_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[7]), Double.parseDouble(rows.get(j)[8]), Double.parseDouble(rows.get(j)[9]));
                grav_sqrt = utils.calculateAngularVelocity(Double.parseDouble(rows.get(j)[10]), Double.parseDouble(rows.get(j)[11]), Double.parseDouble(rows.get(j)[12]));

                re_acc[aux] = acc_sqrt;
                im_acc[aux] = 0;

                re_gyro[aux] = gyro_sqrt;
                im_gyro[aux] = 0;

                re_grav[aux] = grav_sqrt;
                im_grav[aux] = 0;
                aux++;
                x++;

            }
        }

        int total_sensors=3;
        //transpose
        List<List<String>> dataArff = new ArrayList<List<String>>();
        int auxCont = 0;
        int aux2=0;
        for (int y = 0; y < fftExcelData.get(0).size(); y++) {
            if ((N + aux2) <= fftExcelData.get(0).size()) {
                dataArff.add(new ArrayList<String>());
                for (int l = 0; l < total_sensors; l++) {
                    for (int k = 0; k < N; k++) {
                        dataArff.get(auxCont).add(fftExcelData.get(l).get(k + aux2));
                    }
                }
                dataArff.get(auxCont).add(activity);

                aux2 += N;
                auxCont++;
            }
        }

        try {
            String path = this.getFilesDir() + "/" +activity + "_arffData.csv";

            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream writer = new FileOutputStream(path);

                String fileHeader = "";

                int i;

                for (i = 1; i <= N; i++) {
                    fileHeader += ("accelerometer" + i + ",");
                }

                //fileHeader += ("accelerometerMax" + ",");

                for (i = 1; i <= N; i++) {
                    fileHeader += ("gyroscope" + i + ",");
                }

                //fileHeader += ("accelerometerMax" + ",");

                for (i = 1; i <= N; i++) {
                    fileHeader += ("gravity" + i + ",");
                }

                //fileHeader += ("gravityMax" + ",");

                fileHeader += ("activity\n");

                writer.write((fileHeader).getBytes());

                Iterator<List<String>> iter = dataArff.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    while (siter.hasNext()) {
                        String s = siter.next() + ",";
                        writer.write((s).getBytes());
                    }
                    writer.write(("\n").getBytes());
                }

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





