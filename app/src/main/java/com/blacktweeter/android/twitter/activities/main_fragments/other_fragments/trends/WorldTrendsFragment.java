package com.blacktweeter.android.twitter.activities.main_fragments.other_fragments.trends;

import com.blacktweeter.android.twitter.utils.Utils;
import twitter4j.Trends;
import twitter4j.Twitter;

public class WorldTrendsFragment extends TrendsFragment {

    @Override
    protected Trends getTrends() {
        try {
            Twitter twitter = Utils.getTwitter(context, settings);
            return twitter.getPlaceTrends(1);
        } catch (Exception e) {
            return null;
        }
    }
}
