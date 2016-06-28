package com.lumoculus.sleeptide;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;

public class CountdownActivity extends AppCompatActivity {
    private boolean isResetScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);
        addToolbarView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_countdown, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isResetScreen) {
            isResetScreen = false;
            setContentView(R.layout.activity_countdown);
            addToolbarView();
            new AudioManipulation(this).resetVolumes();
            releaseResourcesOnReset();
        } else
            super.onBackPressed();
    }

    private void addToolbarView() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    public void startSmartTimer(View view) { //when smart_countdown_button clicked
        if (hasAccelerometer()) {
            startService(new Intent(this, SensorService.class));
            chooseResetScreenVs();
            setAudioSettings();
        }
    }

    private boolean hasAccelerometer() { //checks phone for accelerometer
        SensorManager mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
            AlertDialog mNoAccelAlertDialog = new AlertDialog.Builder(this).create();
            mNoAccelAlertDialog.setTitle(getString(R.string.alert));
            mNoAccelAlertDialog.setMessage(getString(R.string.no_accel));
            mNoAccelAlertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mNoAccelAlertDialog.show();
            setCountdown();
            return false;
        }
        return true;
    }

    public void setCountdown(View view) { //when manual countdown button clicked
        setCountdown();
    } //when fixed_timer_button clicked

    private void setCountdown() {
        final Context mContext = this;
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        LayoutInflater mInflater = this.getLayoutInflater();
        View mPickerView = mInflater.inflate(R.layout.setcountdown_dialog, null);

        final NumberPicker hr = (NumberPicker) mPickerView.findViewById(R.id.hrPicker);
        final NumberPicker min = (NumberPicker) mPickerView.findViewById(R.id.minPicker);

        mBuilder.setView(mPickerView).setNegativeButton(getString(R.string.set_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long countdown = hr.getValue() * 60 * 60 * 1000 + min.getValue() * 60 * 1000;
                chooseResetScreenVs();
                setAudioSettings();
                Intent mLaunchCountdown = new Intent(mContext, CountdownWakefulReceiver.class);
                mLaunchCountdown.setAction(".CountdownWakefulService");
                mLaunchCountdown.putExtra(getString(R.string.countdown_val), countdown);
                sendBroadcast(mLaunchCountdown);
            }
        }).setPositiveButton(getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        hr.setMinValue(0);
        hr.setMaxValue(23);
        min.setMinValue(0);
        min.setMaxValue(59);
        mBuilder.show();
    }

    private void chooseResetScreenVs() { //switches from timer (smart/fixed) chooser view to reset screen (change indicates sensor/timer is running)
        if (Build.VERSION.SDK_INT < 17)
            setContentView(R.layout.activity_countdown_reset);
        else
            setContentView(R.layout.activity_countdown_reset_v17);
        addToolbarView();
        ActionBar mABar = getSupportActionBar();
        if (mABar != null)
            mABar.setDisplayHomeAsUpEnabled(true);
        ImageView serviceOn = (ImageView) findViewById(R.id.sleepmode);
        if (serviceOn != null)
            serviceOn.setVisibility(View.VISIBLE);
        isResetScreen = true;
    }

    private void setAudioSettings() { //evaluates & saves intial audio environment
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE); //check if music is playing already
        if (!mAudioManager.isMusicActive()) {
            AlertDialog mNoActiveMusicAlertDialog = new AlertDialog.Builder(this).create();
            mNoActiveMusicAlertDialog.setTitle(getString(R.string.reminder));
            mNoActiveMusicAlertDialog.setMessage(getString(R.string.no_active_music));
            mNoActiveMusicAlertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mNoActiveMusicAlertDialog.show();
        }
        startService(new Intent(this, AudioService.class)); //save initial volumes
    }

    private void enableWifi() {
        WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
    }

    private void enableBluetooth() {
        BluetoothAdapter mBluetoothAdapter;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        else {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    public void reset(View view) { //when reset_button clicked
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean enableSound = sPrefs.getBoolean(getString(R.string.en_sound), true);
        boolean enableWiFi = sPrefs.getBoolean(getString(R.string.en_wifi), false);
        boolean enableBluetooth = sPrefs.getBoolean(getString(R.string.en_bluetooth), false);

        releaseResourcesOnReset();

        if (enableSound)
            new AudioManipulation(this).resetVolumes();
        if (enableWiFi)
            enableWifi();
        if (enableBluetooth)
            enableBluetooth();

        ImageView serviceOn = (ImageView) findViewById(R.id.sleepmode);
        if (serviceOn != null)
            serviceOn.setVisibility(View.GONE);
    }

    private void releaseResourcesOnReset() {
        SensorService.releaseWakeLock();
        if (SensorService.isRunning)
            stopService(new Intent(this, SensorService.class));
        CountdownWakefulService.releaseCDTimer();
    }
}