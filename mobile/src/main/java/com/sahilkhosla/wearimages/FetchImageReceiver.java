package com.sahilkhosla.wearimages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.sahilkhosla.wearimages.Constants.TAG;

/**
 * Created by sahil on 11/15/15.
 */
public class FetchImageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast received....");
        String text = intent.getStringExtra(FetchImageService.PARAM_OUT_MSG);
        Log.d(TAG, "Message: " + text);
    }

}
