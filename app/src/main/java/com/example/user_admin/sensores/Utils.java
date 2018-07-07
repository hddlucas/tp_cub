package com.example.user_admin.sensores;

import android.content.Context;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.SENSORSDATAAVERAGEFILENAME;

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

    public  <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

    public void calculateAverage(){
        FileManager fileManager;
        Utils utils;

        fileManager = new FileManager(context);
        fileManager.deleteFile(SENSORSDATAAVERAGEFILENAME);
        utils = new Utils(context);
        List<String[]> rows = new ArrayList<>();

        try {
            FileManager csvReader = new FileManager(context);
            rows = csvReader.readCSV("treino.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int N = 64;
        String[] activities = new String[]{"WALKING","DRIVING","RUNNING","GO_UPSTAIRS","GO_DOWNSTAIRS"};
        for (int a = 0; a < activities.length; a++) {

            List<List<String>> fftExcelData = new ArrayList<List<String>>();

            List<String> lat = new ArrayList<String>();
            List<String> lng = new ArrayList<String>();
            List<String> alt = new ArrayList<String>();

            List<String> timestamp = new ArrayList<String>();

            List<String> x_acc = new ArrayList<String>();
            List<String> y_acc = new ArrayList<String>();
            List<String> z_acc = new ArrayList<String>();

            List<String> x_gyro = new ArrayList<String>();
            List<String> y_gyro = new ArrayList<String>();
            List<String> z_gyro = new ArrayList<String>();

            List<String> x_grav = new ArrayList<String>();
            List<String> y_grav = new ArrayList<String>();
            List<String> z_grav = new ArrayList<String>();
            List<String> lum = new ArrayList<String>();
            List<String> current_activity = new ArrayList<String>();

            fftExcelData.add(lat);
            fftExcelData.add(lng);
            fftExcelData.add(alt);

            fftExcelData.add(timestamp);

            fftExcelData.add(x_acc);
            fftExcelData.add(y_acc);
            fftExcelData.add(z_acc);

            fftExcelData.add(x_gyro);
            fftExcelData.add(y_gyro);
            fftExcelData.add(z_gyro);

            fftExcelData.add(x_grav);
            fftExcelData.add(y_grav);
            fftExcelData.add(z_grav);

            fftExcelData.add(lum);
            fftExcelData.add(current_activity);


            double media_lat = 0.0,
                    media_lng = 0.0,
                    media_alt = 0.0,

                    media_timestamp = 0.0,
                    media_x_acc = 0.0,
                    media_y_acc = 0.0,
                    media_z_acc = 0.0,
                    media_x_gyro = 0.0,
                    media_y_gyro = 0.0,
                    media_z_gyro = 0.0,
                    media_x_grav = 0.0,
                    media_y_grav = 0.0,
                    media_z_grav = 0.0,
                    media_lum = 0.0;

            int aux = 0;

            for (int j = 0, x = 0; j < rows.size(); j++) {
                //stop cycle
                if (j + N > rows.size())
                    break;

                if (rows.get(j)[14].equals(activities[a])) {
                    if (x != 0 && x % N == 0) {

                        fftExcelData.get(0).add(Double.toString(media_lat / N));
                        fftExcelData.get(1).add(Double.toString(media_lng / N));
                        fftExcelData.get(2).add(Double.toString(media_alt / N));

                        fftExcelData.get(3).add(rows.get(j)[3]);

                        fftExcelData.get(4).add(Double.toString(media_x_acc / N));
                        fftExcelData.get(5).add(Double.toString(media_y_acc / N));
                        fftExcelData.get(6).add(Double.toString(media_z_acc / N));

                        fftExcelData.get(7).add(Double.toString(media_x_gyro / N));
                        fftExcelData.get(8).add(Double.toString(media_y_gyro / N));
                        fftExcelData.get(9).add(Double.toString(media_z_gyro / N));

                        fftExcelData.get(10).add(Double.toString(media_x_grav / N));
                        fftExcelData.get(11).add(Double.toString(media_y_grav / N));
                        fftExcelData.get(12).add(Double.toString(media_z_grav / N));

                        fftExcelData.get(13).add(Double.toString(media_lum / N));
                        fftExcelData.get(14).add(activities[a]);

                        media_lat = 0.0;
                        media_lng = 0.0;
                        media_alt = 0.0;
                        media_timestamp = 0.0;
                        media_x_acc = 0.0;
                        media_y_acc = 0.0;
                        media_z_acc = 0.0;
                        media_x_gyro = 0.0;
                        media_y_gyro = 0.0;
                        media_z_gyro = 0.0;
                        media_x_grav = 0.0;
                        media_y_grav = 0.0;
                        media_z_grav = 0.0;
                        media_lum = 0.0;
                    }

                    media_lat += Double.parseDouble(rows.get(j)[0]);
                    media_lng += Double.parseDouble(rows.get(j)[1]);
                    media_alt += Double.parseDouble(rows.get(j)[2]);

                    media_x_acc += Double.parseDouble(rows.get(j)[4]);
                    media_y_acc += Double.parseDouble(rows.get(j)[5]);
                    media_z_acc += Double.parseDouble(rows.get(j)[6]);

                    media_x_gyro += Double.parseDouble(rows.get(j)[7]);
                    media_y_gyro += Double.parseDouble(rows.get(j)[8]);
                    media_z_gyro += Double.parseDouble(rows.get(j)[9]);

                    media_x_grav += Double.parseDouble(rows.get(j)[10]);
                    media_y_grav += Double.parseDouble(rows.get(j)[11]);
                    media_z_grav += Double.parseDouble(rows.get(j)[12]);
                    media_lum += Double.parseDouble(rows.get(j)[13]);
                    x++;
                }
            }

            //transpose
            fftExcelData = utils.transpose(fftExcelData);

            fileManager.createFile(context.getFilesDir() + "/" + SENSORSDATAAVERAGEFILENAME);
            FileOutputStream outputStream;

            try {
                outputStream = context.openFileOutput(SENSORSDATAAVERAGEFILENAME, Context.MODE_APPEND);
                Iterator<List<String>> iter = fftExcelData.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    while (siter.hasNext()) {
                        String s = siter.next() + ",";
                        outputStream.write((s).getBytes());
                    }
                    outputStream.write(("\n").getBytes());
                }
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long time = System.currentTimeMillis();
    }



}
