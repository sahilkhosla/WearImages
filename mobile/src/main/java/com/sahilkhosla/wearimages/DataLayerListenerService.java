package com.sahilkhosla.wearimages;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import static com.sahilkhosla.wearimages.Constants.TAG;

/**
 * Created by sahil on 11/14/15.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String WEAR_MESSAGE_PATH = "/message";
    private FetchImageReceiver fetchImageReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        registerFetchImageReceiver();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {

            String text = new String(messageEvent.getData());
            Log.d(TAG, "Message received..." + text);

            //start service to download image
            Intent fetchImageIntent = new Intent(this, FetchImageService.class);
            fetchImageIntent.putExtra(FetchImageService.PARAM_IN_MSG, "input message");
            startService(fetchImageIntent);
        }
    }

    private void registerFetchImageReceiver() {
        IntentFilter filter = new IntentFilter(FetchImageService.IMAGE_NOTIFICATION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        fetchImageReceiver = new FetchImageReceiver();
        registerReceiver(fetchImageReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(fetchImageReceiver);
    }

}


