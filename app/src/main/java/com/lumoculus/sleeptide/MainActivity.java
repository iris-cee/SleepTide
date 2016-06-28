package com.lumoculus.sleeptide;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {
    private boolean checkRunBefore(){
        final String PREFS_NAME = getString(R.string.ST_prefs_mainActivity);
        final String PREF_VERSION_CODE_KEY = getString(R.string.ST_version_code);
        final int DOESNT_EXIST = -1;
        SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean hasRunBefore = (BuildConfig.VERSION_CODE == mPrefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST));
        if(!hasRunBefore)
            mPrefs.edit().putInt(PREF_VERSION_CODE_KEY, BuildConfig.VERSION_CODE).apply();
        return hasRunBefore;
    }

    private void startCountdownActivity(){
        Intent mCountdownChooserIntent = new Intent(this, CountdownActivity.class);
        mCountdownChooserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mCountdownChooserIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar); //add toolbar posing as actionbar
        setSupportActionBar(mToolbar);
        ActionBar mABar = getSupportActionBar();
        if(mABar !=null)
            mABar.setDisplayShowTitleEnabled(false);

       if(checkRunBefore())
           startCountdownActivity();
       else{
           //Onboarding
           Intent mIntroIntent = new Intent(this, IntroActivity.class);
           mIntroIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           startActivity(mIntroIntent);
           finish();
       }
    }
}
