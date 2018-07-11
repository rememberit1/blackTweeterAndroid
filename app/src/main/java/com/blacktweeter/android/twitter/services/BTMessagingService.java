package com.blacktweeter.android.twitter.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by benakinlosotuwork on 7/10/18.
 */

//https://www.youtube.com/watch?v=Jhv-Pb8l05M
public class BTMessagingService extends FirebaseMessagingService{


    private static final String TAG = "BTMessagingService";

    //this will be done when the app is in the foreground/open
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //payload value is only used when app is open, use this information to highlight the latest section if the app is already open later.
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "ben! Notification title: " + remoteMessage.getNotification().getTitle());
        String value = remoteMessage.getData().get("payload");
        Log.d(TAG, "ben! notification payload " + value);
    }
}
