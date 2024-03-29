package com.blacktweeter.android.twitter.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

import com.blacktweeter.android.twitter.activities.tweet_viewer.users_popup.RetweetersFragment;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.activities.tweet_viewer.users_popup.FavoritersFragment;


public class UserListPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    private long tweetId;

    public UserListPagerAdapter(FragmentManager fm, Context context, long tweetId) {
        super(fm);
        this.context = context;
        this.tweetId = tweetId;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return RetweetersFragment.getFragment(tweetId);
            case 1:
                return FavoritersFragment.getFragment(tweetId);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.retweets);
            case 1:
                return context.getResources().getString(R.string.favorites);
        }
        return null;
    }
}
