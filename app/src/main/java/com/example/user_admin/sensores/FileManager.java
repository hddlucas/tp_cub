package com.example.user_admin.sensores;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.example.user_admin.sensores.MainActivity.FFTFILENAME;
import static com.example.user_admin.sensores.MainActivity.FILTERED_NOISE_FFTFILENAME;


/**
 * Created by USER-Admin on 24/03/2018.
 */

public class FileManager {

    public Context context;
    private List<String[]> rows = new ArrayList<>();
    private Utils utils;
    private NoiseFilter noiseFilter;

    public FileManager(Context context) {
        this.context = context;
        noiseFilter =new NoiseFilter(context);
        utils= new Utils(context);
    }

    // This method will read data from internal storage file
    public String readFromFileInputStream(String fileName) {
        try {
            String Message;
            FileInputStream fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message = bufferedReader.readLine()) != null) {
                stringBuffer.append(Message + "\n");

            }
            return stringBuffer.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    // This method will save data in internal storage file
    public void writeDataToFile(String filename, Float[] sensorsData,long timestamp,String activity) {


        FileOutputStream outputStream;
        String [] collection_line= new String[sensorsData.length+1];
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            String x="";
            // append to file
            for(int i=0;i<sensorsData.length;i++){
                x+=sensorsData[i].toString() +",";
                collection_line[i]=sensorsData[i].toString();
                if(i==2) {
                    x+=timestamp+",";
                    collection_line[i]= String.valueOf(timestamp);
                }
            }

            x+=activity+"\n";
            collection_line[sensorsData.length]=activity;
            outputStream.write(x.getBytes());
            x=null;

//            rows.add(collection_line);
//            if(rows.size()==64) {
//                rows = new ArrayList<>();
//            }

            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Create file if not exist.
     *
     * @param path For example: "D:\foo.xml"
     */
    public static void createFile(String path,String fileHeader) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
                FileOutputStream writer = new FileOutputStream(path);
                if(fileHeader!=null)
                    writer.write((fileHeader).getBytes());
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This method will save data in internal storage file
    public void deleteFile(String filename) {
        try {
            context.deleteFile(filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  List<String[]> readCSV(String fileName) throws IOException {
        List<String[]> rows = new ArrayList<>();
        InputStream is = context.getAssets().open(fileName);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        String csvSplitBy = ";";

        br.readLine();

        while ((line = br.readLine()) != null) {
            String[] row = line.split(csvSplitBy);
            rows.add(row);
        }
        return rows;
    }

}
