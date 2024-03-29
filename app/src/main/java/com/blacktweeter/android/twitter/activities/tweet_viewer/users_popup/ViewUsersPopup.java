/*
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blacktweeter.android.twitter.activities.tweet_viewer.users_popup;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.adapters.UserListPagerAdapter;
import com.blacktweeter.android.twitter.utils.Utils;

public class ViewUsersPopup extends Activity {

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_slide_down);

        Utils.setUpPopupTheme(this, AppSettings.getInstance(this));
        setUpWindow();

        setContentView(R.layout.search_pager);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setPadding(0,0,0,0);

        UserListPagerAdapter adapter = new UserListPagerAdapter(getFragmentManager(), this, getIntent().getLongExtra("id", 0l));
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);

        AppSettings settings = AppSettings.getInstance(this);
        if (settings.addonTheme) {
            PagerTitleStrip strip = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
            strip.setBackgroundColor(settings.pagerTitleInt);
        }
    }

    public void setUpWindow() {

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Params for the window.
        // You can easily set the alpha and the dim behind the window from here
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 1.0f;    // lower than one makes it more transparent
        params.dimAmount = .75f;  // set it higher if you want to dim behind the window
        getWindow().setAttributes(params);

        // Gets the display size so that you can set the window to a percent of that
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        // You could also easily used an integer value from the shared preferences to set the percent
        if (height > width) {
            getWindow().setLayout((int) (width * .9), (int) (height * .8));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

    }
}
