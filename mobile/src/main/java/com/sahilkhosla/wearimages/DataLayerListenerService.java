package com.sahilkhosla.wearimages;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import static com.sahilkhosla.wearimages.Constants.TAG;
import static com.sahilkhosla.wearimages.Constants.WEAR_MESSAGE_PATH;

/**
 * Created by sahil on 11/14/15.
 */
public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {

            String text = new String(messageEvent.getData());
            Log.d(TAG, "Message received from node: " + text);

            //start service to download image
            Intent fetchImageIntent = new Intent(this, FetchImageService.class);
            startService(fetchImageIntent);
        }
    }

}


