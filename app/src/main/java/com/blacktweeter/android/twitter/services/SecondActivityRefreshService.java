package com.blacktweeter.android.twitter.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.utils.ActivityUtils;
import com.blacktweeter.android.twitter.utils.Utils;

public class SecondActivityRefreshService extends IntentService {

    SharedPreferences sharedPrefs;

    public SecondActivityRefreshService() {
        super("SecondActivityRefreshService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        AppSettings settings = AppSettings.getInstance(this);
        ActivityUtils utils = new ActivityUtils(this, true);

        if (Utils.getConnectionStatus(this) && !settings.syncMobile) {
            return;
        }

        boolean newActivity = utils.refreshActivity();

        if (settings.notifications && settings.activityNot && newActivity) {
            utils.postNotification(ActivityUtils.SECOND_NOTIFICATION_ID);
        }
    }
}
