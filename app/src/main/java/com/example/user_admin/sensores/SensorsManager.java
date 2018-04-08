package com.example.user_admin.sensores;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;


public class SensorsManager <T extends LocationListener & SensorEventListener> {

    private SensorManager sensorManager;
    private Sensor gravity, accelerometer, illuminance, gyroscope;
    private T listener;

    private static SensorsManager instance;
    private LocationManager locationManager;

    private SensorsManager() {
    }

    public static SensorsManager getInstance(Context context) throws NullPointerException {

        if (instance == null) {
            SensorsManager createSensorsManager = new SensorsManager();

            createSensorsManager.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (createSensorsManager.locationManager != null) {
                createSensorsManager.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

                if (createSensorsManager.sensorManager != null) {
                    createSensorsManager.gravity = createSensorsManager.sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                    createSensorsManager.accelerometer = createSensorsManager.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    createSensorsManager.illuminance = createSensorsManager.sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                    createSensorsManager.gyroscope = createSensorsManager.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

                    if (createSensorsManager.gravity != null && createSensorsManager.accelerometer != null
                            && createSensorsManager.illuminance != null && createSensorsManager.gyroscope != null) {
                        return (instance = createSensorsManager);
                    }
                }
            }
        } else {
            return instance;
        }

        throw new NullPointerException("Any sensor asked");
    }

    public void startSensors(T listener) throws SecurityException {

        if (sensorManager != null && gravity != null && accelerometer != null && illuminance != null && gyroscope != null) {
            this.listener = listener;
            sensorManager.registerListener(this.listener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this.listener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this.listener, gravity, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this.listener, illuminance, SensorManager.SENSOR_DELAY_FASTEST);


            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000,
                    10, this.listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    10, this.listener);
        }
    }

    public void stopSensors() {
        if (sensorManager != null && listener != null) {
            sensorManager.unregisterListener(listener);
            locationManager.removeUpdates(listener);
        }
    }

}
