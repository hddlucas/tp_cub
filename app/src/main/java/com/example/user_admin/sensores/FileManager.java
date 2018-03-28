package com.example.user_admin.sensores;

import android.content.Context;
import java.io.BufferedReader;
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
    private  static String TAG_WRITE_READ_FILE = "TAG_WRITE_READ_FILE";

    public FileManager(Context context) {
        this.context = context;
    }

    // This method will read data from internal storage file
    public String readFromFileInputStream(String fileName)
    {
        try {
            String Message;
            FileInputStream fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            while ((Message=bufferedReader.readLine())!=null){
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
    public  void writeDataToFile(String filename, String dataToSave)
    {
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            // append to file
            outputStream.write(dataToSave.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method will save data in internal storage file
    public  void deleteFile(String filename)
    {
        try {
            context.deleteFile(filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
