package com.example.user_admin.sensores;

import android.app.Activity;
import android.widget.TextView;
import java.util.TimerTask;
import static com.example.user_admin.sensores.MainActivity.SENSORSDATAFILENAME;


/**
 * Created by USER-Admin on 28/03/2018.
 */

public class DataCollectionTimerTask extends TimerTask {

    GPSTracker gps;
    FileManager fileManager;
    Permissions permissions;
    Utils utils;
    TextView logsTxtBox;

    private final Activity mActivity;

    public DataCollectionTimerTask(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void run() {

        // create GPSTracker class object
        gps = new GPSTracker(mActivity);

        // create Utils class object
        utils = new Utils(mActivity);

        // create FileManager class object
        fileManager = new FileManager(mActivity);

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        double altitude = gps.getAltitude();

        //store sensors data in internal storage file
        fileManager.writeDataToFile(SENSORSDATAFILENAME, latitude + "," + longitude + "," + altitude + "," + utils.getCurrentDate("dd-MM-yy") + "," + utils.getCurrentTime("hh:mm:ss") + "\n");

        mActivity.runOnUiThread(new Runnable(){

            @Override
            public void run() {
                //read sensors data file  stored in internal memory
                String fileContent = fileManager.readFromFileInputStream(SENSORSDATAFILENAME);
                logsTxtBox=  mActivity.findViewById(R.id.logsTxtBox);
                logsTxtBox.setText(fileContent);
            }});
    }
}
