package com.sahilkhosla.wearimages;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import static com.sahilkhosla.wearimages.Constants.IMAGE_RECEIVED_INTENT;
import static com.sahilkhosla.wearimages.Constants.TAG;
import static com.sahilkhosla.wearimages.Constants.WEAR_MESSAGE_PATH;

public class MainActivity extends Activity implements MainView {

    private GoogleApiClient mGoogleApiClient;
    private BoxInsetLayout boxInsetLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boxInsetLayout = (BoxInsetLayout) findViewById(R.id.boxInsetLayout);

        //---Build a new Google API client---
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(IMAGE_RECEIVED_INTENT));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume...");
        super.onResume();
        sendMessage(WEAR_MESSAGE_PATH, "fetchImage");
    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                                path, text.getBytes()).await();
                    if (result.getRequestId() == MessageApi.UNKNOWN_REQUEST_ID) {
                        Log.e(TAG, "Failed to send message: " + text);
                    }
                }
            }
        }).start();
    }

    @Override
    public void setBackground(Bitmap bitmap) {
        Log.d(TAG, "Set background...");
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        boxInsetLayout.setBackground(drawable);
    }

    // handler for received Intents for the IMAGE_RECEIVED_INTENT event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "On receive...");
            Bitmap bitmap = (Bitmap) intent.getExtras().get("image");
            setBackground(bitmap);
        }
    };

    @Override
    protected void onPause() {
        Log.d(TAG, "On pause...");
        mGoogleApiClient.disconnect();
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }
}
