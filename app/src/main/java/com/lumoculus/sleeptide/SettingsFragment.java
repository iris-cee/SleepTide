package com.lumoculus.sleeptide;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private SharedPreferences mSPrefs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        getActivity().setTheme(R.style.AppTheme);

        if(getArguments() !=null){
            String settings_type = getArguments().getString(getString(R.string.settings_frag)); //nested getString() statements because BaseBundle getString() takes a String, not an int
            if(settings_type.equals(getString(R.string.dis_header))) {
                addPreferencesFromResource(R.xml.preferences_disable);
                checkVolPolicy(getString(R.string.dis_sound));
            }else if(settings_type.equals(getString(R.string.en_header))) {
                addPreferencesFromResource(R.xml.preferences_reset);
                checkVolPolicy(getString(R.string.en_sound));
                if(!mSPrefs.getBoolean(getString(R.string.dis_sound), true))
                    findPreference(getString(R.string.en_sound)).setEnabled(false);
            }
        }

    }
    private void checkVolPolicy(String key){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //device has fixed volume policy
            AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            if (mAudioManager.isVolumeFixed()) {
                SharedPreferences.Editor mEditor = mSPrefs.edit();
                mEditor.putBoolean(key, false).commit();
                CheckBoxPreference mCheckbox = (CheckBoxPreference) findPreference(key);
                mCheckbox.setSummary(getString(R.string.fix_vol_policy));
                mCheckbox.setEnabled(false);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View mLayout = inflater.inflate(R.layout.activity_settings, container, false);
        if(mLayout != null){
            AppCompatPreferenceActivity mActivity = (AppCompatPreferenceActivity) getActivity();
            Toolbar mToolbar = (Toolbar)mLayout.findViewById(R.id.toolbar);
            mActivity.setSupportActionBar(mToolbar);
            ActionBar mBar = mActivity.getSupportActionBar();
            mBar.setTitle(getPreferenceScreen().getTitle());
        }
        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSPrefs.registerOnSharedPreferenceChangeListener(this);
        if (getView() != null) {
            View mFrame = (View) getView().getParent();
            if (mFrame != null)
                mFrame.setPadding(0, 0, 0, 0);
        }
    }
    @Override
    public void onPause(){
        super.onStop();
        mSPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sPrefs, String key){
        SharedPreferences.Editor mEditor = this.mSPrefs.edit();
        if(key.equals(getString(R.string.dis_sound))) { //
            if(!sPrefs.getBoolean(getString(R.string.dis_sound), true))
                mEditor.putBoolean(getString(R.string.en_sound), false).commit();
        }
    }
}
