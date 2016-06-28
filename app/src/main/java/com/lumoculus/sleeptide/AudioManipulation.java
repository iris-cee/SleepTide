package com.lumoculus.sleeptide;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;


public class AudioManipulation implements SharedPreferences.OnSharedPreferenceChangeListener{
    private Context mContext;
    private SharedPreferences mSPrefs;
    private AudioManager mAudioManager;
    private boolean disableMusic, disableNotif;
    private boolean enableMusic,enableNotif;

    public AudioManipulation(Context context){ //Java class needs Context object in order to access application environment & resources
        this.mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        setPrefs();
    }
    private void setPrefs(){
        mSPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        disableMusic = mSPrefs.getBoolean(mContext.getString(R.string.dis_sound_mus), true);
        disableNotif = mSPrefs.getBoolean(mContext.getString(R.string.dis_sound_notif), true);
        enableMusic = mSPrefs.getBoolean(mContext.getString(R.string.en_sound_mus), true);
        enableNotif = mSPrefs.getBoolean(mContext.getString(R.string.en_sound_notif), true);
    }

    boolean reqAudioFocus() { //called when CountdownTimer sTimer finishes
        final AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    mAudioManager.abandonAudioFocus(this); //does not play media, so remove right away
                }
            }
        };
        int reqStatus = mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return reqStatus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    void reduceVolumes() {
        if (disableMusic && mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0)
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        if (disableNotif && mAudioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) > 0)
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    void resetVolumes() {
        SharedPreferences mSPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        int mMusicVol = mSPrefs.getInt(mContext.getString(R.string.mus_vol), mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)/2);
        int mNotifVol = mSPrefs.getInt(mContext.getString(R.string.notif_vol), mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)/2);

        if (enableMusic)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMusicVol, AudioManager.FLAG_SHOW_UI);
        if (enableNotif)
            mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, mNotifVol, AudioManager.FLAG_SHOW_UI);


    }

    void setVolToZero() { //special case when countdown running time is 0
        if (disableMusic)
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
        if (disableNotif)
            mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key){
        if(key.equals(mContext.getString(R.string.dis_sound_mus)))
            disableMusic = sPrefs.getBoolean(mContext.getString(R.string.dis_sound_mus), true);
        else if(key.equals(mContext.getString(R.string.dis_sound_notif)))
            disableNotif = sPrefs.getBoolean(mContext.getString(R.string.dis_sound_notif), true);
        else if(key.equals(mContext.getString(R.string.en_sound_mus)))
            enableMusic = sPrefs.getBoolean(mContext.getString(R.string.en_sound_mus), true);
        else if(key.equals(mContext.getString(R.string.en_sound_notif)))
            enableNotif = sPrefs.getBoolean(mContext.getString(R.string.en_sound_notif), true);
    }
}
