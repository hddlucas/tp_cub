package com.example.user_admin.sensores;

import android.content.Context;

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
}
