package com.lumoculus.sleeptide;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class AudioService extends IntentService { //saves the stream volumes prior to starting CountdownTimer (sTimer)
    private AudioManager mAudioManager;
    private SharedPreferences mSPrefs;

    public AudioService(){
        super("AudioService");
    }
    @Override
    public void onHandleIntent(Intent intent) {
        mSPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        saveInitialSoundVols();
    }

    private void saveInitialSoundVols() {
        int musicVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int notifVol = mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        int maxVol = Math.max(musicVol, notifVol);
        if(maxVol==0)
            maxVol=1;
        SharedPreferences.Editor editor = mSPrefs.edit();
        editor.putInt(getString(R.string.mus_vol), musicVol);
        editor.putInt(getString(R.string.notif_vol), notifVol);
        editor.putInt(getString(R.string.max_vol), maxVol);
        editor.commit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}