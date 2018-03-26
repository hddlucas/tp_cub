package com.example.user_admin.sensores;
import com.jcraft.jsch.*;

/**
 * Created by USER-Admin on 24/03/2018.
 */

public class SFTP {

    public void uplodateFileWithExternalSFTPServer(){
    try {
        JSch ssh = new JSch();
        Session session = ssh.getSession("username", "myip90000.ordomain.com", 22);
        // Remember that this is just for testing and we need a quick access, you can add an identity and known_hosts file to prevent
        // Man In the Middle attacks
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword("Passw0rd");

        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftp = (ChannelSftp) channel;

        //sftp.cd(directory);
        // If you need to display the progress of the upload, read how to do it in the end of the article

        // use the put method , if you are using android remember to remove "file://" and use only the relative path
        sftp.put("/storage/0/myfile.txt", "/var/www/remote/myfile.txt");

        Boolean success = true;

        if(success){
            // The file has been uploaded succesfully
        }

        channel.disconnect();
        session.disconnect();
    } catch (JSchException e) {
        System.out.println(e.getMessage().toString());
        e.printStackTrace();
    } catch (SftpException e) {
        System.out.println(e.getMessage().toString());
        e.printStackTrace();
    }
    }
}