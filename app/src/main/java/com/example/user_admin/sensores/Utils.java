package com.example.user_admin.sensores;

import android.content.Context;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by USER-Admin on 26/03/2018.
 */

public class Utils {

    private Context context;

    public Utils(Context context) {
        this.context = context;
    }

    //this method is used to get the current date in a format sent as input parameter of the function
    public String getCurrentDate(String dateFormat){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        String currentDate = df.format(c);

        return currentDate;
    }

    //this method is used to get the current time in a format sent as input parameter of the function
    public String getCurrentTime(String timeFormat){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(timeFormat);
        String currentTime = df.format(c);

        return currentTime;
    }

    public static void showToast(Context mContext,String message){
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    //this method is used to gcalculate angular velocity
    public double calculateAngularVelocity(double x, double y, double z){
        return  Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

}
