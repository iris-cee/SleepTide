package com.lumoculus.sleeptide;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;

public class SensorService extends Service implements SensorEventListener {
    private static PowerManager.WakeLock sSensorWakeLock;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean initialized;
    static boolean isRunning;
    private long startTime;
    private float lastX, lastY, lastZ;
    private long lastTime;
    private final float SLEEP_THRESH = (float) 0.2;
    private final long TIME_NOMVMT_THRESH_SEC = 300; //5 min
    private final long MAX_RUNNING_TIME = 10800; //max 3 hrs to find no mvmt
    private final long NS_TO_S = 1000000000;

    public SensorService() {
    }

    @Override
    public void onCreate(){
        PowerManager mPowerMgr = (PowerManager) getSystemService(POWER_SERVICE);
        sSensorWakeLock = mPowerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SleepTide sensor");
        sSensorWakeLock.acquire();
        initialized = false;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        mSensorManager.registerListener(this, mAccelerometer, 150000000); //ideal sampling rate: every 2.5 min
        isRunning = true;
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    public void onSensorChanged(SensorEvent event) {
        Sensor mSensor = event.sensor;
        if (mSensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (initialized) {
                float deltaX = Math.abs(x - lastX);
                float deltaY = Math.abs(y - lastY);
                float deltaZ = Math.abs(z - lastZ);

                if (deltaX < SLEEP_THRESH || deltaY < SLEEP_THRESH || deltaZ < SLEEP_THRESH) {
                    if (((event.timestamp - lastTime) / NS_TO_S) > TIME_NOMVMT_THRESH_SEC) //user has not moved, start countdown
                        startCountdown();
                    return; //don't need to update lastX/Y/Z
                }else if ((event.timestamp - startTime) / NS_TO_S > MAX_RUNNING_TIME){
                    startCountdown();
                    return;
                }else
                    lastTime = event.timestamp;
            }else{ //!initialized
                startTime = event.timestamp;
                lastTime = startTime;
                initialized = true;
            }
            lastX = x;
            lastY = y;
            lastZ = z;
        }
    }

    private void startCountdown(){
        Intent mLaunchCountdown = new Intent(this, CountdownWakefulReceiver.class);
        mLaunchCountdown.setAction(".CountdownWakefulService");
        sendBroadcast(mLaunchCountdown);
        mSensorManager.unregisterListener(this);
        isRunning = false;
        releaseWakeLock();
        stopSelf();
    }

    static void releaseWakeLock(){
        if(sSensorWakeLock !=null && sSensorWakeLock.isHeld())
            sSensorWakeLock.release();
    }
}
