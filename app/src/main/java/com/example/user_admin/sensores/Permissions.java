package com.example.user_admin.sensores;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by USER-Admin on 24/03/2018.
 */

public class Permissions extends Activity{
    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 1;

    private Context context;
    public Permissions(Context context) {
        this.context = context;
    }

    //check Location Run Time Permissions
    public void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_ACCESS_COARSE_LOCATION);

        }
    }

    //check External Storage Permissions
    public void checkWriteExternalStoragePermission() {
        final int MY_PERMISSIONS_REQUEST_PHONE_CALL = 1;
        ContextCompat.checkSelfPermission(context,      Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

}
