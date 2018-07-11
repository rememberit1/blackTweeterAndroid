package com.blacktweeter.android.twitter.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by benakinlosotuwork on 7/10/18.
 */


//https://www.youtube.com/watch?v=Jhv-Pb8l05M
public class BTFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "BTFirebaseInstanceIDSer";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "ben!  firbase instance id Token: " + token);
    }
}
