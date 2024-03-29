package com.blacktweeter.android.twitter.activities.tweet_viewer.users_popup;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.blacktweeter.android.twitter.adapters.ArrayListLoader;
import com.blacktweeter.android.twitter.adapters.PeopleArrayAdapter;
import com.blacktweeter.android.twitter.data.App;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.views.swipe_refresh_layout.FullScreenSwipeRefreshLayout;
import com.blacktweeter.android.twitter.views.swipe_refresh_layout.SwipeProgressBar;

import org.lucasr.smoothie.AsyncListView;
import org.lucasr.smoothie.ItemManager;

import twitter4j.User;
import uk.co.senab.bitmapcache.BitmapLruCache;

import java.util.ArrayList;
import java.util.List;

public abstract class UserListFragment extends Fragment {

    protected AppSettings settings;
    protected Context context;

    private AsyncListView listView;

    private long tweetId;

    private LinearLayout spinner;
    private LinearLayout noContent;
    public FullScreenSwipeRefreshLayout mPullToRefreshLayout;

    protected abstract List<User> findUsers(long tweetId);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }

    public void changeNoRetweetersText(View layout) {
        // do nothing here
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, null);

        settings = AppSettings.getInstance(context);

        View layout = LayoutInflater.from(context).inflate(com.blacktweeter.android.twitter.R.layout.ptr_list_layout, container, false);

        changeNoRetweetersText(layout);

        mPullToRefreshLayout = (FullScreenSwipeRefreshLayout) layout.findViewById(com.blacktweeter.android.twitter.R.id.swipe_refresh_layout);
        mPullToRefreshLayout.setOnRefreshListener(new FullScreenSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshStarted();
            }
        });

        if (settings.addonTheme) {
            mPullToRefreshLayout.setColorScheme(settings.accentInt,
                    SwipeProgressBar.COLOR2,
                    settings.accentInt,
                    SwipeProgressBar.COLOR3);
        } else {
            if (settings.theme != AppSettings.THEME_LIGHT) {
                mPullToRefreshLayout.setColorScheme(context.getResources().getColor(com.blacktweeter.android.twitter.R.color.app_color),
                        SwipeProgressBar.COLOR2,
                        context.getResources().getColor(com.blacktweeter.android.twitter.R.color.app_color),
                        SwipeProgressBar.COLOR3);
            } else {
                mPullToRefreshLayout.setColorScheme(context.getResources().getColor(com.blacktweeter.android.twitter.R.color.app_color),
                        getResources().getColor(com.blacktweeter.android.twitter.R.color.light_ptr_1),
                        context.getResources().getColor(com.blacktweeter.android.twitter.R.color.app_color),
                        getResources().getColor(com.blacktweeter.android.twitter.R.color.light_ptr_2));
            }
        }

        spinner = (LinearLayout) layout.findViewById(com.blacktweeter.android.twitter.R.id.list_progress);
        noContent = (LinearLayout) layout.findViewById(com.blacktweeter.android.twitter.R.id.no_content);

        listView = (AsyncListView) layout.findViewById(com.blacktweeter.android.twitter.R.id.listView);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
        builder.setThreadPoolSize(4);

        listView.setItemManager(builder.build());

        tweetId = getArguments().getLong("id", 0);

        onRefreshStarted();

        return layout;
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    public void onRefreshStarted() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    users = findUsers(tweetId);

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new PeopleArrayAdapter(context, users);
                            listView.setAdapter(adapter);
                            listView.setVisibility(View.VISIBLE);

                            spinner.setVisibility(View.GONE);

                            if (users.size() == 0) {
                                noContent.setVisibility(View.VISIBLE);
                            }

                            mPullToRefreshLayout.setRefreshing(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();

                } catch (OutOfMemoryError e) {
                    e.printStackTrace();

                }
            }
        }).start();
    }

    private List<User> users = new ArrayList<User>();
    private PeopleArrayAdapter adapter;
}
