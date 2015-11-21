package com.sahilkhosla.wearimages;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import static com.sahilkhosla.wearimages.Constants.TAG;

/**
 * Created by sahil on 11/14/15.
 */
public class FetchImageService extends IntentService {

    private GoogleApiClient mGoogleApiClient;

    //TODO: add 1 or more image urls here
    private static final String[] urls = {};

    public FetchImageService() {
        super("FetchImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "OnHandleIntent start...");
        String url = getRandomUrl();
        Bitmap bitmap = null;

        if (!url.isEmpty()) {
            bitmap = getBitmap(url);
        }

        if (bitmap == null) {
            //return default bitmap stored on device
            bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.violin)).getBitmap();
        }

        //publish result, once image is downloaded
        publishImage(bitmap);
    }

    private void publishImage(Bitmap bitmap) {

        connectGoogleApiClient();
        Asset asset = createAssetFromBitmap(bitmap);
        PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
        dataMap.getDataMap().putAsset("profileImage", asset);
        dataMap.getDataMap().putLong("timeStamp", new Date().getTime()); //to ensure data is changed everytime
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        Log.d(TAG, "Bitmap sent to node...");

    }

    private void connectGoogleApiClient() {
        //---Build a new Google API client---
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private String getRandomUrl() {
        String url = "";

        int min = 0;
        int max = urls.length - 1;

        if (max > 0) {
            int randomIndex = new Random().nextInt(max - min + 1) + min;
            url =  urls[randomIndex];
        }

        return url;
    }

    private Bitmap getBitmap(String url) {
        Bitmap bitmap = null;

        try {
            //size for moto 360 v1
            bitmap = Picasso.with(this).load(url).resize(320, 290).centerCrop().get();
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image: " + e.getMessage());
        }

        return bitmap;
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

}
