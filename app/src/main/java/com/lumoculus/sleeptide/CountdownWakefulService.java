package com.lumoculus.sleeptide;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class CountdownWakefulService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static CountDownTimer sTimer;
    private long countdown = 600000; //default = 10 min given in ms
    private boolean disSound;
    private AudioManipulation audioManip;
    private SharedPreferences mSPrefs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        countdown = intent.getLongExtra(getString(R.string.countdown_val), countdown);
        startCountdown();
        CountdownWakefulReceiver.completeWakefulIntent(intent);
        stopSelf();
        return START_REDELIVER_INTENT;
    }

    private void startCountdown() {
        audioManip = new AudioManipulation(this);
        mSPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        disSound = mSPrefs.getBoolean(getString(R.string.dis_sound), true);
        if(countdown ==0){
            if(disSound)
                audioManip.setVolToZero();
            disable();
        }
        else{
            long interval = countdown / mSPrefs.getInt(getString(R.string.max_vol), ((AudioManager) getSystemService(AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC)); //stream with largest volume range
            if(interval == countdown) //ensures that onTick() is called at least once; CountdownTimer only calls onTick() when remaining time is greater than the interval
                interval /=2;
            sTimer = new CountDownTimer(countdown, interval) { //steadily decrease audio
                public void onTick(long millisUntilFinished) {
                    if (disSound)
                        audioManip.reduceVolumes();
                }

                public void onFinish() {
                    disable();
                }
            };
            sTimer.start();
        }
    }
    private void disable(){ //coordinator for all disable methods
        boolean isSuccess = audioManip.reqAudioFocus();
        sTimer = null;
        if (mSPrefs.getBoolean(getString(R.string.dis_wifi), false))
            disableWifi();
        if (mSPrefs.getBoolean(getString(R.string.dis_bluetooth), false))
            disableBluetooth();
        while (!isSuccess)
            isSuccess = audioManip.reqAudioFocus();
    }
    private void disableWifi() {
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);
    }
    private void disableBluetooth() {
        BluetoothAdapter mBluetoothAdapter;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        else {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
    }

    static void releaseCDTimer(){
        if (sTimer != null)
            sTimer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key) {
        if (key.equals(getString(R.string.dis_sound)))
            disSound = sPrefs.getBoolean(getString(R.string.dis_sound), true);
    }
}
