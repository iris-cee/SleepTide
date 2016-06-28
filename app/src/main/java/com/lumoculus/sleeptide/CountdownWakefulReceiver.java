package com.lumoculus.sleeptide;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class CountdownWakefulReceiver extends WakefulBroadcastReceiver {
    public CountdownWakefulReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mTimerIntent = new Intent(context, CountdownWakefulService.class);
        if(intent.getExtras() != null) //in the case of fixed countdown timer, where countdown length may be different from the default
            mTimerIntent.putExtras(intent.getExtras());
        startWakefulService(context, mTimerIntent);
    }
}
