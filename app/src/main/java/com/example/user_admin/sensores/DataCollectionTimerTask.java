package com.example.user_admin.sensores;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import java.util.TimerTask;
import static com.example.user_admin.sensores.MainActivity.SENSORSDATAFILENAME;


/**
 * Created by USER-Admin on 28/03/2018.
 */

public class DataCollectionTimerTask extends TimerTask {

    GPSTracker gps;
    AccelerometerTracker acc;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    TextView logsTxtBox;
    TextView gpsTextView;

    private final Activity mActivity;

    public DataCollectionTimerTask(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void run() {

        // create GPSTracker class object
        gps = new GPSTracker(mActivity);
        acc = new AccelerometerTracker (mActivity);


        // create Utils class object
        utils = new Utils(mActivity);

        // create FileManager class object
        fileManager = new FileManager(mActivity);

        final double latitude = gps.getLatitude();
        final double longitude = gps.getLongitude();
        double altitude = gps.getAltitude();

        float accX = acc.getLastX();
        float accY = acc.getLastY();
        float accZ = acc.getLastZ();

        //store sensors data in internal storage file
        fileManager.writeDataToFile(SENSORSDATAFILENAME, latitude + "," + longitude + "," + accX + "," + accY + "," + accZ + "," + altitude + "," + utils.getCurrentDate("dd-MM-yy") + "," + utils.getCurrentTime("hh:mm:ss") + "\n");

        mActivity.runOnUiThread(new Runnable(){

            @Override
            public void run() {

                //Update GPS position label:
                gpsTextView=  mActivity.findViewById(R.id.gpsTextView);
                gpsTextView.setText("GPS: " + latitude + " ; " + longitude);

                //read sensors data file  stored in internal memory
                String fileContent = fileManager.readFromFileInputStream(SENSORSDATAFILENAME);
                logsTxtBox=  mActivity.findViewById(R.id.logsTxtBox);
                logsTxtBox.setText(fileContent);
            }});
    }
}
