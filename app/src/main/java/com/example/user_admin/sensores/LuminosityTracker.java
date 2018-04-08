package com.example.user_admin.sensores;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by MarcoSequeira-PC on 06/04/2018.
 */

public class LuminosityTracker implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorListener;
    float l;

    @Override
    public void onSensorChanged(SensorEvent event) {
        l = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
