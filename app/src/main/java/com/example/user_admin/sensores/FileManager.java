package com.example.user_admin.sensores;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Created by USER-Admin on 24/03/2018.
 */

public class FileManager {

    private Context context;

    public FileManager(Context context) {
        this.context = context;
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

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            String x="";
            // append to file
            for(int i=0;i<sensorsData.length;i++){
                x+=sensorsData[i].toString() +",";
                if(i==2) {
                    x+=timestamp+",";
                }
            }

            x+=activity+"\n";
            outputStream.write(x.getBytes());
            x=null;

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
    public static void createFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                String fileHeader = "lat,lng,alt,timestamp,x_acc,y_acc,z_acc,x_gyro,y_gyro,z_gyro,x_grav,y_grav,z_grav,lum,activity\n";
                file.createNewFile();
                FileOutputStream writer = new FileOutputStream(path);
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

}
