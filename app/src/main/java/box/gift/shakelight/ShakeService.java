package box.gift.shakelight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ShakeService extends Service
{
    private SensorManager sense;
    private SensorEventListener listener;

    private Camera camera;

    private long lastShakeTime = 0;

    private int shakes = 0;

    private static final int THRESH = 14;
    private static final int DELAY_MS = 300;
    private static final int TIMEOUT_MS = 800;

    private float lastX = THRESH / 2;
    private float lastY = THRESH / 2;
    private float lastZ = THRESH / 2;

    public ShakeService()
    {

    }

    @Override
    public void onCreate()
    {
        camera = Camera.open();
        sense = (SensorManager) getSystemService(SENSOR_SERVICE);
        listener = new SensorEventListener()
        {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent)
            {
                if (!sensorEvent.sensor.equals(sense.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) || sensorEvent.values.length < 3)
                {
                    return;
                }

                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];

                float change = Math.abs(x + y + z - lastX - lastY - lastZ);
                if (change > THRESH)
                {
                    long currentTime = System.currentTimeMillis();
                    Log.w("shake", String.valueOf(change));
                    if (currentTime - lastShakeTime > TIMEOUT_MS)
                    {
                        shakes = 0;
                    }
                    if (currentTime - lastShakeTime > DELAY_MS)
                    {
                        shakes++;

                        if (shakes >= 2)
                        {
                            if (camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH))
                            {
                                offFlash();
                            }
                            else
                            {
                                onFlash();
                            }
                            shakes = 0;
                        }

                        lastShakeTime = currentTime;
                    }
                    else
                    {
                        Log.i("Shake", "too soon");
                        Log.i("current time", String.valueOf(currentTime));
                        Log.i("last time", String.valueOf(lastShakeTime));
                        Log.i("DIFF", String.valueOf(currentTime - lastShakeTime));
                    }
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i)
            {

            }
        };
        sense.registerListener(listener, sense.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("MissingPermission")
    public void onFlash()
    {
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        camera.startPreview();
    }

    @SuppressLint("MissingPermission")
    public void offFlash()
    {
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        camera.stopPreview();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy()
    {
        sense.unregisterListener(listener);
        sense = null;
        listener = null;
        if (camera != null)
        {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
