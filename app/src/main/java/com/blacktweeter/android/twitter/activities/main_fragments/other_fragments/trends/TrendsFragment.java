package com.blacktweeter.android.twitter.activities.main_fragments.other_fragments.trends;

import android.util.Log;
import android.view.View;

import com.blacktweeter.android.twitter.adapters.TrendsArrayAdapter;
import com.blacktweeter.android.twitter.data.sq_lite.HashtagDataSource;
import com.blacktweeter.android.twitter.activities.main_fragments.MainFragment;
import twitter4j.Trend;
import twitter4j.Trends;

import java.util.ArrayList;

public abstract class TrendsFragment extends MainFragment {

    @Override
    public void setUpListScroll() {
        // don't do anything here
    }

    @Override
    public void getCursorAdapter(boolean showSpinner) {

        if (showSpinner) {
            listView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Trends trends = getTrends();

                if (trends == null) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listView.setVisibility(View.GONE);
                                spinner.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {

                    }

                    return;
                }

                final ArrayList<String> currentTrends = new ArrayList<String>();

                for(Trend t: trends.getTrends()){
                    String name = t.getName();
                    currentTrends.add(name);
                }

                try {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (currentTrends != null) {
                                    listView.setAdapter(new TrendsArrayAdapter(context, currentTrends));
                                    listView.setVisibility(View.VISIBLE);
                                }

                                spinner.setVisibility(View.GONE);

                                refreshLayout.setRefreshing(false);
                            } catch (Exception e) {
                                // not attached to activity
                            }
                        }
                    });
                } catch (Exception e) {
                    
                }

                HashtagDataSource source = HashtagDataSource.getInstance(context);

                for (String s : currentTrends) {
                    Log.v("talon_hashtag", "trend: " + s);
                    if (s.contains("#")) {
                        // we want to add it to the auto complete
                        Log.v("talon_hashtag", "adding: " + s);

                        // could be much more efficient by querying and checking first, but I
                        // just didn't feel like it when there is only ever 10 of them here
                        source.deleteTag(s);

                        // add it to the userAutoComplete database
                        source.createTag(s);
                    }
                }
            }
        }).start();
    }

    protected abstract Trends getTrends();
}
