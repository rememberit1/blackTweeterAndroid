package com.blacktweeter.android.twitter.activities.tweet_viewer.users_popup;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.blacktweeter.android.twitter.utils.FavoriterUtils;
import com.blacktweeter.android.twitter.R;

import twitter4j.User;

import java.util.List;

public class FavoritersFragment extends UserListFragment {

    public static FavoritersFragment getFragment(long tweetId) {
        Bundle args = new Bundle();
        args.putLong("id", tweetId);

        FavoritersFragment frag = new FavoritersFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    protected List<User> findUsers(long tweetId) {
        return (new FavoriterUtils()).getFavoriters(context, tweetId);
    }

    @Override
    public void changeNoRetweetersText(View layout) {
        TextView tv = (TextView) layout.findViewById(R.id.no_retweeters_text);

        if (tv != null) {
            tv.setText(R.string.no_favorites);
        }
    }
}
