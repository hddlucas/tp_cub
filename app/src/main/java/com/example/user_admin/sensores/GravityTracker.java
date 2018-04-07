package com.example.user_admin.sensores;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;

/**
 * Created by MarcoSequeira-PC on 06/04/2018.
 */

public class GravityTracker implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorListener;


    float standardGravity = SensorManager.STANDARD_GRAVITY;
    float thresholdGraqvity = standardGravity/2;


    @Override
    public void onSensorChanged(SensorEvent event) {

        float z = event.values[2];

        float x = event.values[0];

        float y = event.values[1];


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
