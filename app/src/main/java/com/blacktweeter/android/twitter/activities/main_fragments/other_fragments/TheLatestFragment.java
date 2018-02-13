package com.blacktweeter.android.twitter.activities.main_fragments.other_fragments;

/**
 * Created by benakinlosotuwork on 2/2/18.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.blacktweeter.android.twitter.adapters.ArrayListLoader;
import com.blacktweeter.android.twitter.adapters.HorizontalAdapter;
import com.blacktweeter.android.twitter.adapters.TheLatestAdapter;
import com.blacktweeter.android.twitter.data.App;
import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.adapters.ActivityCursorAdapter;
import com.blacktweeter.android.twitter.activities.drawer_activities.DrawerActivity;
import com.blacktweeter.android.twitter.activities.main_fragments.MainFragment;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.utils.Utils;


import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.co.senab.bitmapcache.BitmapLruCache;

import java.util.ArrayList;

public class TheLatestFragment extends MainFragment {

    public static final int ACTIVITY_REFRESH_ID = 131;

    public int unread = 0;


    public View layout;
    public AppSettings settings;
    public SharedPreferences sharedPrefs;
    //public AsyncListView listView;
    public RecyclerView rListView;
    public LinearLayout spinner;
    public String screenName;



    public BroadcastReceiver refreshActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(false);
        }
    };


    /**
     *to get a colletion of specific tweets (black tweets) use twitter.lookup
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);



        settings = AppSettings.getInstance(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                0);
      //  screenName = sharedPrefs.getString("twitter_screen_name_1", "");
        screenName = settings.myScreenName;

        inflater = LayoutInflater.from(context);

       // layout = inflater.inflate(R.layout.list_fragment, null);
        layout = inflater.inflate(R.layout.the_latest_fragment, null);



        rListView = (RecyclerView) layout.findViewById(R.id.latest_recycler_view);
       // spinner = (LinearLayout) layout.findViewById(R.id.spinner);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

//        ItemManager.Builder builder = new ItemManager.Builder(loader);
//        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(50);
//        builder.setThreadPoolSize(4);

//        listView.setItemManager(builder.build());
//        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView absListView, int i) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                final int lastItem = firstVisibleItem + visibleItemCount;
//
//                if(lastItem == totalItemCount && canRefresh) {
//                  //  getMore();
//                }
//            }
//        });
        Button testButton;
        testButton = (Button) layout.findViewById(R.id.testButton);
        testButton.setAlpha((float) 0.5);

        doSearch();

        return layout;
    }

    public ArrayList<Status> tweets = new ArrayList<Status>();
    public Paging paging = new Paging(1, 20);
    public boolean hasMore = true;
    public boolean canRefresh = false;
    public HorizontalAdapter normalAdapter;

    public void doSearch() {
       // spinner.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);

                    ResponseList<Status> result;
                    try {
                        //change this to twitter.lookup(); //and keep evryting the same
                        tweets.clear();
                        result = twitter.lookup(20L, 962875856409407488L, 1000L);
                       // result = twitter.getFavorites(screenName, paging);
                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    tweets.clear();

                    for (Status status : result) {
                        tweets.add(status);
                    }

                    if (result.size() > 17) {
                      //  hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            normalAdapter = new HorizontalAdapter(context, tweets);
                            rListView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                            Log.d("ben!",  "gotten tweets: " + normalAdapter.getItemCount());
                            rListView.setAdapter(normalAdapter);
                            rListView.setVisibility(View.VISIBLE);

                           // spinner.setVisibility(View.GONE);
                            canRefresh = true;

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // spinner.setVisibility(View.GONE);
                            canRefresh = false;
                        }
                    });

                }

            }
        }).start();
    }

    public void getMore() {
        canRefresh = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);

                    paging.setPage(paging.getPage() + 1);

                    ResponseList<Status> result = twitter.getFavorites(screenName, paging);

                    for (twitter4j.Status status : result) {
                      //  tweets.add(status);
                    }

                    if (result.size() > 17) {
                       // hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            normalAdapter.notifyDataSetChanged();
                            canRefresh = true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            canRefresh = false;
                            hasMore = false;
                        }
                    });

                }

            }
        }).start();
    }








    public View getLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.the_latest_fragment, null);
    }

    protected void setSpinner(View layout) {
        spinner = (LinearLayout) layout.findViewById(R.id.no_content);
//        View button = layout.findViewById(R.id.activity_info);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Uri weburi = Uri.parse("https://plus.google.com/117432358268488452276/posts/gz3FLfDqTkU");
//                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, weburi);
//                launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(launchBrowser);
//            }
//        });
    }



    @Override
    public void setUpListScroll() {

    }

    public Twitter getTwitter() throws TwitterException {

         twitter = Utils.getTwitter(context, DrawerActivity.settings);
         String myScreenname = twitter.getScreenName();
         Log.d("ben!",  "screenname test: " +  myScreenname);
         return twitter;
    }

//    @Override
    public void onRefreshStarted() {
//        new AsyncTask<Void, Void, Cursor>() {
//
//            private boolean update = false;
//            @Override
//            protected void onPreExecute() {
//                DrawerActivity.canSwitch = false;
//            }
//
////            @Override
//            protected Cursor doInBackground(Void... params) {
//
//                ActivityUtils utils = new ActivityUtils(getActivity());
//
//                update = utils.refreshActivity();
//
//                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//                long now = new Date().getTime();
//                long alarm = now + DrawerActivity.settings.activityRefresh;
//
//                PendingIntent pendingIntent = PendingIntent.getService(context, ACTIVITY_REFRESH_ID, new Intent(context, ActivityRefreshService.class), 0);
//
//                if (DrawerActivity.settings.activityRefresh != 0)
//                    am.setRepeating(AlarmManager.RTC_WAKEUP, alarm, DrawerActivity.settings.activityRefresh, pendingIntent);
//                else
//                    am.cancel(pendingIntent);
//
//                if (settings.syncSecondMentions) {
//                    context.startService(new Intent(context, SecondActivityRefreshService.class));
//                }
//
//                return ActivityDataSource.getInstance(context).getCursor(currentAccount);
//            }
////
////            @Override
//            protected void onPostExecute(Cursor cursor) {
//
//                Cursor c = null;
//                try {
//                    c = cursorAdapter.getCursor();
//                } catch (Exception e) {
//
//                }
//
//                cursorAdapter = setAdapter(cursor);
//
//                try {
//                    listView.setAdapter(cursorAdapter);
//                } catch (Exception e) {
//
//                }
//
//                if (cursor.getCount() == 0) {
//                    spinner.setVisibility(View.VISIBLE);
//                } else {
//                    spinner.setVisibility(View.GONE);
//                }
//
//                try {
//                    if (update) {
//                        showToastBar(getString(R.string.new_activity), getString(R.string.ok), 400, true, toTopListener);
//                    }
//                } catch (Exception e) {
//                    // user closed the app before it was done
//                }
//
//                refreshLayout.setRefreshing(false);
//
//                DrawerActivity.canSwitch = true;
//
//                try {
//                    c.close();
//                } catch (Exception e) {
//
//                }
//            }
//        }.execute();
    }
//
    public ActivityCursorAdapter setAdapter(Cursor c) {
        return new ActivityCursorAdapter(context, c);
    }

    @Override
    public void onResume() {

//        try {
//            getTwitter();
//        } catch (TwitterException e) {
//            e.printStackTrace();
//            Log.d("ben!", "did not get twitter");
//        }

        super.onResume();


        list = new ArrayList<>();
        for (int i =0; i <15; i++) {
            list.add("This is cool.");
        }
//        recyclerView = (RecyclerView) layout.findViewById(R.id.latest_recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        theLatestAdapter = new TheLatestAdapter(getActivity(), list);
       // recyclerView.setAdapter(theLatestAdapter);


        if (sharedPrefs.getBoolean("refresh_me_activity", false)) {
            getCursorAdapter(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sharedPrefs.edit().putBoolean("refresh_me_activity", false).commit();
                }
            },1000);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.twitter.REFRESH_ACTIVITY");
        filter.addAction("com.klinker.android.twitter.NEW_ACTIVITY");
        context.registerReceiver(refreshActivity, filter);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
//
    public void getCursorAdapter(boolean showSpinner) {
//        if (showSpinner) {
//            try {
//                listView.setVisibility(View.GONE);
//            } catch (Exception e) { }
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final Cursor cursor;
//                try {
//                    cursor = ActivityDataSource.getInstance(context).getCursor(currentAccount);
//                } catch (Exception e) {
//                    ActivityDataSource.dataSource = null;
//                    getCursorAdapter(true);
//                    return;
//                }
//
//                try {
//                    Log.v("talon_databases", "mentions cursor size: " + cursor.getCount());
//                } catch (Exception e) {
//                    ActivityDataSource.dataSource = null;
//                    getCursorAdapter(true);
//                    return;
//                }
//
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Cursor c = null;
//                        if (cursorAdapter != null) {
//                            c = cursorAdapter.getCursor();
//                        }
//
//                        cursorAdapter = new ActivityCursorAdapter(context, cursor);
//
//                        try {
//                            listView.setVisibility(View.VISIBLE);
//                        } catch (Exception e) { }
//
//                        try {
//                            listView.setAdapter(cursorAdapter);
//                        } catch (Exception e) {
//
//                        }
//
//                        if (cursor.getCount() == 0) {
//                          //  spinner.setVisibility(View.VISIBLE);
//                        } else {
//                         //   spinner.setVisibility(View.GONE);
//                        }
//
//                        try {
//                            c.close();
//                        } catch (Exception e) {
//
//                        }
//                    }
//                });
//            }
//        }).start();
    }

//    @Override
    public void onPause() {
        context.unregisterReceiver(refreshActivity);
        super.onPause();
    }
}
