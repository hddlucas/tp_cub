package com.example.user_admin.sensores;

import android.content.Context;
import android.widget.Toast;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import static com.example.user_admin.sensores.MainActivity.SENSORSDATAFILENAME;

/**
 * Created by USER-Admin on 24/03/2018.
 */

public class SFTP implements Runnable {

    private Context context;
    FileManager fileManager;

    public SFTP(Context context) {
        this.context = context;
    }


    //this method is used to upload file with external SFTP Server
    //server:urbysense.dei.uc.pt login: cubistudent pw: mis_cubi_2018
    @Override
    public void run() {
        try {

            File file = new File(context.getFilesDir() + "/" + SENSORSDATAFILENAME);

            if (file.exists()) {

                JSch ssh = new JSch();
                Session session = ssh.getSession("cubistudent", "urbysense.dei.uc.pt", 22);
                // Remember that this is just for testing and we need a quick access, you can add an identity and known_hosts file to prevent
                // Man In the Middle attacks
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                session.setConfig(config);
                session.setPassword("mis_cubi_2018");

                session.connect();
                Channel channel = session.openChannel("sftp");
                channel.connect();

                ChannelSftp sftp = (ChannelSftp) channel;

                sftp.cd("a21220082_a21230131");
                // If you need to display the progress of the upload, read how to do it in the end of the article

                // use the put method , if you are using android remember to remove "file://" and use only the relative path
                long timestamp = Calendar.getInstance().getTimeInMillis();

                sftp.put(file.getPath() + "", "sensors_" + timestamp);

                Boolean success = true;

                if (success) {
                    Toast.makeText(context, "Ficheiro submetido com sucesso!", Toast.LENGTH_SHORT).show();
                    fileManager = new FileManager(context);
                    fileManager.deleteFile(SENSORSDATAFILENAME);

                } else {
                    Toast.makeText(context, "Ocorreu um problema ao transferir o ficheiro!!", Toast.LENGTH_SHORT).show();
                }

                channel.disconnect();
                session.disconnect();
            } else {
                Toast.makeText(context, "O ficheiro não existe!", Toast.LENGTH_SHORT).show();
            }

        } catch (JSchException e) {
            System.out.println(e.getMessage().toString());
            e.printStackTrace();
        } catch (SftpException e) {
            System.out.println(e.getMessage().toString());
            e.printStackTrace();
        }

    }
}
