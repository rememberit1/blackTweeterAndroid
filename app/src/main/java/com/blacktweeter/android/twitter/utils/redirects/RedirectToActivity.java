package com.blacktweeter.android.twitter.utils.redirects;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.blacktweeter.android.twitter.activities.MainActivity;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.adapters.TimelinePagerAdapter;


public class RedirectToActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);

        SharedPreferences sharedPrefs = getSharedPreferences("com.klinker.android.twitter_world_preferences",
                0);

        int currentAccount = sharedPrefs.getInt("current_account", 1);

        int page = 0;
        for (int i = 0; i < TimelinePagerAdapter.MAX_EXTRA_PAGES; i++) {
            String pageIdentifier = "account_" + currentAccount + "_page_" + (i + 1);
            int type = sharedPrefs.getInt(pageIdentifier, AppSettings.PAGE_TYPE_NONE);

            if (type == AppSettings.PAGE_TYPE_ACTIVITY) {
                page = i;
            }
        }

        Intent mentions = new Intent(this, MainActivity.class);

        sharedPrefs.edit().putBoolean("open_a_page", true).commit();
        sharedPrefs.edit().putInt("open_what_page", page).commit();

        finish();

        overridePendingTransition(0,0);

        startActivity(mentions);
    }
}