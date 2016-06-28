package com.lumoculus.sleeptide;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity{
    private Intent mIntentReceived;
    private boolean isFirstTime; //if true, user came directly from the onboarding screen

    @Override
    public void onBuildHeaders(List<Header> target) {
        mIntentReceived = getIntent();
        if(mIntentReceived.getExtras() !=null)
            isFirstTime = mIntentReceived.getBooleanExtra(getString(R.string.Sender), true);

        loadHeadersFromResource(R.xml.pref_headers, target);

        setContentView(R.layout.activity_settings);
        Toolbar mToolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar mBar = getSupportActionBar();
        mBar.setTitle(getString(R.string.title_activity_settings));
    }
    @Override
    protected boolean isValidFragment(String fragmentName){
        return SettingsFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_check){
            if(isFirstTime){
                Intent mCountdownChooserIntent = new Intent(this, CountdownActivity.class);
                mCountdownChooserIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mCountdownChooserIntent);
                finish();
            }else
                onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
