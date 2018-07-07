package com.example.user_admin.sensores;

import android.content.Context;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class CSV2Arff {

    private Context context;

    public CSV2Arff(Context context) {
        this.context = context;
    }

    public void convertCSVtoArff(String csvFile,String arffFile){

        try {
            // load CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(context.getFilesDir() + "/" + csvFile));
            //String[] options = {"-H"};
            //loader.setOptions(options);

            Instances data = loader.getDataSet();

            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File(context.getFilesDir() + "/" + arffFile));
            saver.setDestination(new File(context.getFilesDir() + "/" + arffFile));
            saver.writeBatch();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}