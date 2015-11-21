package com.sahilkhosla.wearimages;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.sahilkhosla.wearimages.Constants.TAG;

/**
 * Created by sahil on 11/14/15.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String WEAR_MESSAGE_PATH = "/message";
    private Bitmap bitmap;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {

            String text = new String(messageEvent.getData());
            Log.d(TAG, "Message received..." + text);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Data changed...");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image")) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");

                Log.d(TAG, "profile asset: " + profileAsset.toString());

                new LoadImageTask().execute(new Asset[] {profileAsset});

            }
        }
    }

    private class LoadImageTask extends AsyncTask<Asset, Void, Bitmap> {
        private GoogleApiClient mGoogleApiClient;

        public LoadImageTask() {
            connectGoogleApiClient();
        }

        @Override
        protected Bitmap doInBackground(Asset... assets) {
            Log.d(TAG, "do in background...");
            Bitmap bitmap = null;
            if (assets.length > 0) {
                bitmap = loadBitmapFromAsset(assets[0]);
            } else {
                Log.d(TAG, "assets length is 0");
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "on post execute...");
            sendMessage(bitmap);
            mGoogleApiClient.disconnect();
        }

        private void sendMessage(Bitmap bitmap) {
            Log.d(TAG, "sending message to main...");
            Intent intent = new Intent("my-event");
            // add data
            intent.putExtra("image", bitmap);
            LocalBroadcastManager.getInstance(DataLayerListenerService.this).sendBroadcast(intent);
        }

        private void connectGoogleApiClient() {
            //---Build a new Google API client---
            mGoogleApiClient = new GoogleApiClient.Builder(DataLayerListenerService.this)
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();
        }

        private Bitmap loadBitmapFromAsset(Asset asset) {
            if (asset == null) {
                throw new IllegalArgumentException("Asset must be non-null");
            }
            ConnectionResult result =
                    mGoogleApiClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
            if (!result.isSuccess()) {
                return null;
            }
            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            mGoogleApiClient.disconnect();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return null;
            }
            // decode the stream into a bitmap
            return BitmapFactory.decodeStream(assetInputStream);
        }
    }

}
