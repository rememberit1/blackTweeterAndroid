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

package com.blacktweeter.android.twitter.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.view.*;
import android.widget.ListView;

import android.widget.Toast;

import com.blacktweeter.android.twitter.activities.MainActivity;
import com.blacktweeter.android.twitter.adapters.ChangelogAdapter;
import com.blacktweeter.android.twitter.utils.XmlChangelogUtils;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.utils.Utils;

public class SettingsActivity extends Activity {

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_zoom_enter, R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.slide_in_left, R.anim.activity_zoom_exit);

        AppSettings.invalidate();

        Utils.setUpTheme(this, AppSettings.getInstance(this));

        ActionBar ab = getActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainPrefFrag())
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_settings, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_whats_new:
                final Context context = this;
                final ListView list = new ListView(this);
                list.setDividerHeight(0);

                new AsyncTask<Spanned[], Void, Spanned[]>() {
                    @Override
                    public Spanned[] doInBackground(Spanned[]... params) {
                        return XmlChangelogUtils.parse(context);
                    }

                    @Override
                    public void onPostExecute(Spanned[] result) {
                        list.setAdapter(new ChangelogAdapter(context, result));
                    }
                }.execute();

                new AlertDialog.Builder(this)
                        .setTitle(R.string.changelog)
                        .setView(list)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                return true;
            case R.id.menu_rate_it:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_get_help:
                showSettings(8, getString(R.string.get_help_settings));
                return true;
            case R.id.menu_other_apps:
                showSettings(9, getString(R.string.other_apps));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        AppSettings.invalidate();
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
    }

    private void showSettings(int position, String title) {
        startActivity(new Intent(this, PrefActivity.class)
                .putExtra("position", position)
                .putExtra("title", title));
    }
}