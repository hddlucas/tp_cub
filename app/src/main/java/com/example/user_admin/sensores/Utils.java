package com.example.user_admin.sensores;

import android.content.Context;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by USER-Admin on 26/03/2018.
 */

public class Utils {
    private Context context;
    private List<String[]> rows = new ArrayList<>();
    private Utils utils;
    private NoiseFilter noiseFilter;
    private Arff arff;
    private int N=64;
    List<String[]> fftData = new ArrayList<>();


    public Utils(Context context) {
        this.context = context;
        noiseFilter =new NoiseFilter(context);
        arff = new Arff(context);
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

    public void preprocessesData(Float[] sensorsData,long timestamp,String activity){
        String [] collection_line= new String[sensorsData.length+2];
        try {
            int aux=0;
            for(int i=0;i<sensorsData.length;i++){
                collection_line[aux]=sensorsData[i].toString();
                if(i==2) {
                    aux++;
                    collection_line[aux]= String.valueOf(timestamp);
                }
                aux++;
            }
            collection_line[aux]=activity;
            rows.add(collection_line);
            if(rows.size()==N) {
                //appply noise filter
                fftData.add(noiseFilter.ApplyNoiseFilter(rows,aux));
                if(fftData.size()==N){
                    //generate arff file with fft transform
                    arff.generateRealTimeArffFile(fftData,activity);

                    fftData = new ArrayList<>();
                }
                rows = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
