package com.example.user_admin.sensores;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.ACTIVITIES;
import static com.example.user_admin.sensores.MainActivity.ARFFSENSORSDATAAVERAGEFILENAME;
import static com.example.user_admin.sensores.MainActivity.FILEHEADER;
import static com.example.user_admin.sensores.MainActivity.SENSORSDATAAVERAGEFILENAME;

/**
 * Created by USER-Admin on 13/07/2018.
 */

public class NoiseFilter {

    private FileManager fileManager;
    private Complex complex;
    private Utils utils;
    private Arff arff;
    private Context context;

    public NoiseFilter(Context context) {
        this.context = context;
    }

    public void calculateAverage(){
        arff = new Arff(context);
        fileManager = new FileManager(context);
        utils= new Utils(context);
        fileManager.deleteFile(SENSORSDATAAVERAGEFILENAME);
        List<String[]> rows = new ArrayList<>();
        try {
            rows = fileManager.readCSV("treino.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }

        int N = 64;
        for (int a = 0; a < ACTIVITIES.length; a++) {

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

                if (rows.get(j)[14].equals(ACTIVITIES[a])) {
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
                        fftExcelData.get(14).add(ACTIVITIES[a]);

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

            fileManager.createFile(context.getFilesDir() + "/" + SENSORSDATAAVERAGEFILENAME,FILEHEADER);
            FileOutputStream outputStream;
            String s="";
            try {
                outputStream = context.openFileOutput(SENSORSDATAAVERAGEFILENAME, Context.MODE_APPEND);
                Iterator<List<String>> iter = fftExcelData.iterator();
                while (iter.hasNext()) {
                    Iterator<String> siter = iter.next().iterator();
                    s="";
                    while (siter.hasNext()) {
                        s+= siter.next() + ",";
                    }
                    //remove last comma
                    s = s.substring(0, s.length() - 1);
                    outputStream.write((s+"\n").getBytes());
                }
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            //Convert csv to arff file
            arff.convertCSVtoArff(SENSORSDATAAVERAGEFILENAME,ARFFSENSORSDATAAVERAGEFILENAME);
        }
        long time = System.currentTimeMillis();
    }

    public String[] ApplyNoiseFilter(List<String[]> rows,int sensorsDataLength){
        int N=64;
        String[]media = new String[sensorsDataLength+1];
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

        for (int j = 0; j < rows.size(); j++) {
            if (j == N-1) {
                media[0]=Double.toString(media_lat / N);
                media[1]=Double.toString(media_lng / N);
                media[2]=Double.toString(media_alt / N);

                media[3]=rows.get(j)[3];

                media[4]=Double.toString(media_x_acc / N);
                media[5]=Double.toString(media_y_acc / N);
                media[6]=Double.toString(media_z_acc / N);

                media[7]=Double.toString(media_x_gyro / N);
                media[8]=Double.toString(media_y_gyro / N);
                media[9]=Double.toString(media_z_gyro / N);

                media[10]=Double.toString(media_x_grav / N);
                media[11]=Double.toString(media_y_grav / N);
                media[12]=Double.toString(media_z_grav / N);

                media[13]=Double.toString(media_lum / N);
                media[14]=rows.get(j)[14];

                return media;
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
        }
        return media;
    }

}
