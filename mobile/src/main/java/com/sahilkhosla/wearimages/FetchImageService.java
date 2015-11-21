package com.sahilkhosla.wearimages;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
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

    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String IMAGE_NOTIFICATION = "com.sahilkhosla.wearimages.IMAGE_NOTIFICATION";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private GoogleApiClient mGoogleApiClient;

    public FetchImageService() {
        super("FetchImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "OnHandleIntent start...");
        String msg = intent.getStringExtra(PARAM_IN_MSG);

       /* SystemClock.sleep(3000); // 3 seconds
        Log.d(TAG, "OnHandleIntent end..." + resultTxt);*/

        String[] urls = {
                "http://vignette3.wikia.nocookie.net/goanimate-v2/images/7/77/Mrhappy0902_468x442.jpg",
                "http://caprisunminions.com/themes/minions2015/img/footer-dave.png",
                "http://images.sodahead.com/polls/002424213/494738859_TickleMeYouIcon512_xlarge.png",
        };

        //random.nextInt(max - min + 1) + min
        int randomIndex = new Random().nextInt(2 - 0 + 1) + 0;

        Bitmap bitmap = getBitmap(urls[randomIndex]);

        //publish result, once image is downloaded
        publishImage(bitmap);
    }

    private void publishImage(Bitmap bitmap) {
        //to broadcast message
        /*Intent broadCastIntent = new Intent();
        broadCastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
        broadCastIntent.setAction(IMAGE_NOTIFICATION);
        broadCastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadCastIntent);*/

        connectGoogleApiClient();
        sendBitmapToNode(bitmap);
        /*NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(),
                            WEAR_MESSAGE_PATH, "Image Dowloaded".getBytes()).await();
            if (result.getRequestId() == MessageApi.UNKNOWN_REQUEST_ID) {
                Log.d(TAG, "Failed to send message");
            }
        }*/
    }

    private void connectGoogleApiClient() {
        //---Build a new Google API client---
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private Bitmap getBitmap(String url) {
        Bitmap bitmap = null;

        try {
            bitmap = Picasso.with(this).load(url).resize(320, 290).centerCrop().get();
        } catch (IOException e) {
            Log.e(TAG, "Error downloading image: " + e.getMessage());
        }

        return bitmap;
    }

    private void sendBitmapToNode(Bitmap bitmap) {

        Asset asset = createAssetFromBitmap(bitmap);
        PutDataMapRequest dataMap = PutDataMapRequest.create("/image");
        dataMap.getDataMap().putAsset("profileImage", asset);
        dataMap.getDataMap().putLong("timeStamp", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        Log.d(TAG, "Bitmap sent to node...");
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

}
