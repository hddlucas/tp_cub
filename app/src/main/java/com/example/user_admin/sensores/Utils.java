package com.example.user_admin.sensores;

import android.content.Context;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.TEST_ARFFCSVFILENAME;
import static com.example.user_admin.sensores.MainActivity.TEST_ARFFFILENAME;
import static com.example.user_admin.sensores.MainActivity.TRAIN_ARFFFILENAME;


/**
 * Created by USER-Admin on 26/03/2018.
 */

public class Utils {
    private Context context;
    private List<String[]> rows = new ArrayList<>();
    private Utils utils;
    private NoiseFilter noiseFilter;
    private Arff arff;
    private FFT fft;
    private int N = 64;
    List<String[]> fftData = new ArrayList<>();


    public Utils(Context context) {
        this.context = context;
        noiseFilter = new NoiseFilter(context);
        arff = new Arff(context);
        fft = new FFT(context);
    }

    //this method is used to get the current date in a format sent as input parameter of the function
    public String getCurrentDate(String dateFormat) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(dateFormat);
        String currentDate = df.format(c);

        return currentDate;
    }

    //this method is used to get the current time in a format sent as input parameter of the function
    public String getCurrentTime(String timeFormat) {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat(timeFormat);
        String currentTime = df.format(c);

        return currentTime;
    }

    public static void showToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    //this method is used to gcalculate angular velocity
    public double calculateAngularVelocity(double x, double y, double z) {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    public <T> List<List<T>> transpose(List<List<T>> table) {
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

    public void preprocessesData(Float[] sensorsData, long timestamp, String activity, boolean automaticMode) {
        String[] collection_line = new String[sensorsData.length + 2];
        String predictedActivity = "";
        try {
            int aux = 0;
            for (int i = 0; i < sensorsData.length; i++) {
                collection_line[aux] = sensorsData[i].toString();
                if (i == 2) {
                    aux++;
                    collection_line[aux] = String.valueOf(timestamp);
                }
                aux++;
            }
            collection_line[aux] = activity;
            rows.add(collection_line);
            if (rows.size() == N) {
                //appply noise filter
                fftData.add(noiseFilter.ApplyNoiseFilter(rows, aux));
                if (fftData.size() == N) {
                    //generate arff file with fft transform
                    arff.generateRealTimeArffFile(fftData, activity, automaticMode);

                    if (automaticMode) {
                        // do something for a debug build
                        //Convert CSV to ARFF
                        arff.convertCSVtoArff(TEST_ARFFCSVFILENAME, TEST_ARFFFILENAME);

                        //Classify activity
                        predictedActivity = arff.classifyFromARFF(TRAIN_ARFFFILENAME, TEST_ARFFFILENAME);
                        if (predictedActivity != null) {
                            Utils.showToast(context, predictedActivity + "!");
                        }
                    } /*else {
                        //decision tree
                        ArrayList<List<String>> dataArff = new ArrayList<>();
                        dataArff = fft.applyFourrierTransform(fftData);
                        List<String> accelerometer = dataArff.get(0);
                        List<String> gyroscope = dataArff.get(1);
                        List<String> gravity =dataArff.get(2);

                        if (Double.parseDouble(gyroscope.get(0)) <= 14.873763) {
                            if (Double.parseDouble(accelerometer.get(7)) <= 2.484912)
                                predictedActivity = "DRIVING";
                            predictedActivity = "GO_UPSTAIRS";
                        } else if (Double.parseDouble(gyroscope.get(0)) > 14.873763) {
                            if (Double.parseDouble(gyroscope.get(0)) <= 45.015215) {
                                if (Double.parseDouble(accelerometer.get(22)) <= -1.93394)
                                    predictedActivity = "GO_DOWNSTAIRS";
                                else {
                                    if (Double.parseDouble(accelerometer.get(0)) <= 624.991925)
                                        if (Double.parseDouble(accelerometer.get(2)) <= -12.62418)
                                            predictedActivity = "GO_DOWNSTAIRS";
                                        else {
                                            if (Double.parseDouble(gravity.get(4)) <= -7.23825)
                                                predictedActivity = "GO_UPSTAIRS";
                                            else {
                                                if (Double.parseDouble(gravity.get(31)) <= 4.402636) {
                                                    if (Double.parseDouble(accelerometer.get(10)) <= -16.574187)
                                                        predictedActivity = "GO_UPSTAIRS";
                                                    predictedActivity = "WALKING";
                                                } else {
                                                    if (Double.parseDouble(gyroscope.get(11)) <= 0.088658)
                                                        predictedActivity = "GO_UPSTAIRS";
                                                    predictedActivity = "GO_DOWNSTAIRS";
                                                }
                                            }
                                        }
                                    predictedActivity = "DRIVING";
                                }
                            } else {
                                if (Double.parseDouble(gyroscope.get(7)) <= -7.566398)
                                    predictedActivity = "DRIVING";
                                predictedActivity = "RUNNING";
                            }
                        }

                        Utils.showToast(context, predictedActivity + "!");
                    }*/
                    fftData = new ArrayList<>();
                }
                rows = new ArrayList<>();
            }
        } catch (Exception e) {
            Utils.showToast(context, "Ocorreu um erro ao processar Dados");
        }
    }

}
