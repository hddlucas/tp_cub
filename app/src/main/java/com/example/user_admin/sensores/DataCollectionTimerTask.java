package com.example.user_admin.sensores;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import java.util.TimerTask;
import java.util.ArrayList;


/**
 * Created by USER-Admin on 28/03/2018.
 */

public class DataCollectionTimerTask extends TimerTask {

    GPSTracker gps;
    AccelerometerTracker acc;
    FileManager fileManager;
    TextView logsTxtBox;
    TextView gpsTextView;
    private final Activity mActivity;
    Float[] data2;

    public DataCollectionTimerTask(Activity activity, Float[] data ) {
        mActivity = activity;
        data2=data;

    }

    @Override
    public void run() {

        //store sensors data in internal storage file
        //fileManager.writeDataToFile(SENSORSDATAFILENAME, latitude + "," + longitude + "," + accX + "," + accY + "," + accZ + "," + altitude + "," + utils.getCurrentDate("dd-MM-yy") + "," + utils.getCurrentTime("hh:mm:ss") + "\n");

        mActivity.runOnUiThread(new Runnable(){

            @Override
            public void run() {

                //read sensors data file  stored in internal memory
                //String fileContent = fileManager.readFromFileInputStream(SENSORSDATAFILENAME);
                logsTxtBox=  mActivity.findViewById(R.id.logsTxtBox);

            }});
    }
}
