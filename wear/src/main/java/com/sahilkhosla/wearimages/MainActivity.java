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
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import static com.sahilkhosla.wearimages.Constants.TAG;

public class MainActivity extends Activity implements MainView {

    private GoogleApiClient mGoogleApiClient;
    private BoxInsetLayout boxInsetLayout;
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final int SPEECH_REQUEST_CODE = 0;

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
                new IntentFilter("my-event"));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "OnResume...");
        super.onResume();
        sendMessage(WEAR_MESSAGE_PATH, "fetchImage");
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }


    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG, "On activity result...");

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            Log.d(TAG, "Text: " + spokenText);
            sendMessage(WEAR_MESSAGE_PATH, spokenText);
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                            Log.d(TAG, "Failed to send message: " + text);
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

        /*Intent displayImageIntent = new Intent(this, DisplayImageActivity.class);
        displayImageIntent.putExtra("imageBitmap", bitmap);
        startActivity(displayImageIntent);*/
    }

    // handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "On receive...");
            // Extract data included in the Intent
            Bitmap bitmap = (Bitmap) intent.getExtras().get("image");
            Log.d(TAG, "Got image: " + bitmap.getHeight());
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
