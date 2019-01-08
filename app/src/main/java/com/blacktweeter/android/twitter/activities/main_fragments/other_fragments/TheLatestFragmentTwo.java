package com.blacktweeter.android.twitter.activities.main_fragments.other_fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.activities.drawer_activities.DrawerActivity;
import com.blacktweeter.android.twitter.activities.main_fragments.MainFragment;
import com.blacktweeter.android.twitter.activities.main_fragments.home_fragments.HomeFragment;
import com.blacktweeter.android.twitter.adapters.ActivityCursorAdapter;
import com.blacktweeter.android.twitter.adapters.ArrayListLoader;
import com.blacktweeter.android.twitter.adapters.HoriCategoryAdapter;
import com.blacktweeter.android.twitter.adapters.TheLatestAdapter;
import com.blacktweeter.android.twitter.adapters.VerticalAdapter;
import com.blacktweeter.android.twitter.data.App;
import com.blacktweeter.android.twitter.data.FBCategory;
import com.blacktweeter.android.twitter.data.FBTweet;
import com.blacktweeter.android.twitter.data.SectionDataModel;
import com.blacktweeter.android.twitter.settings.AppSettings;
import com.blacktweeter.android.twitter.utils.AdapterCallback;
import com.blacktweeter.android.twitter.utils.Utils;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.co.senab.bitmapcache.BitmapLruCache;

import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by benakinlosotuwork on 8/1/18.
 */

public class TheLatestFragmentTwo extends MainFragment implements SwipeRefreshLayout.OnRefreshListener { //implements AdapterCallback {
    public static final int ACTIVITY_REFRESH_ID = 131;

    public int unread = 0;

    private DatabaseReference firebaseRef;
    public static boolean latestIsVisible = false;

    public View layout;
    private ImageView loadingGifIV;
    public AppSettings settings;
    public SharedPreferences sharedPrefs;
    public RecyclerView realVertRecycler;
    private TextView catgoryHeaderText;
    private TextView refreshText;
    private boolean refreshJustClicked = false;

    Long versionNumber = 2L;
    long  legitChecker = 0;
    long testingChecker = 1;


    VerticalAdapter mClickedAdapter;
    private HoriCategoryAdapter horizontalAdapter;
    private LinearLayoutManager horizontalLayoutManager;
    public RecyclerView realHoriRecycler;
    SwipeRefreshLayout mSwipeRefreshLayout;


    //******************************************************************************************************************************///
    Map<String, FBCategory> mFirebaseDictionary = new HashMap<>();
    String firstFBTopicKey;
    Map<String, ArrayList<Status>> twitterDictionary = new HashMap<>();
    ArrayList<FBCategory> mFirebaseList;
    int changingFirebaseCount = 5;

    FBCategory changeableFBCategory;
    String changeableTopicKey;


    //******************************************************************************************************************************///


    ArrayList<TheLatestFragmentTwo.LatestTweetModel> allLatestTweetModels = new ArrayList<>();
    ArrayList<Long> allTweetIDsLong = new ArrayList<>();
    Map<String, ArrayList<TheLatestFragmentTwo.LatestTweetModel>> mEachTheseSection = new HashMap<>();//all the tweets in total seperated/labeled by it category string
    int numberOfTopics;

    ArrayList<SectionDataModel> listOfSectionDataModels;
    ArrayList<List<Status>> mGroupOfListOfStatuses = new ArrayList<List<Status>>();
    public LinearLayout spinner;
    public String screenName;

    //Map<String, Object> allTweetsByTopic = new HashMap<>(); //never used

    public Paging paging = new Paging(1, 20);
    public boolean hasMore = true;
    public boolean canRefresh = false;


    public BroadcastReceiver refreshActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getCursorAdapter(false);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        firebaseRef = FirebaseDatabase.getInstance().getReference();

        settings = AppSettings.getInstance(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                0);

        screenName = settings.myScreenName;

        inflater = LayoutInflater.from(context);


        layout = inflater.inflate(R.layout.the_latest_fragment, null);
        realHoriRecycler =  layout.findViewById(R.id.horizontalCatgoryView);
        realVertRecycler =  layout.findViewById(R.id.latest_real_recycler_vert);
        catgoryHeaderText = layout.findViewById(R.id.catHeaderText);
        refreshText = layout.findViewById(R.id.refreshText);


        refreshText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
                refreshJustClicked = true;
                pureReload();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.black_tweeter_teal,
                android.R.color.holo_green_dark,
                android.R.color.holo_blue_bright,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                // Fetching data from server
               // Toast.makeText(context, "Refreshing...", Toast.LENGTH_LONG).show();
                refreshJustClicked = true;
                pureReload();
            }
        });


        //Glide.with(this).asGif().load("").placeholder

        loadingGifIV = (ImageView) layout.findViewById(R.id.loading_gif);
        loadingGifIV.setAlpha((float) 0.7);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
       // ArrayListLoader loader = new ArrayListLoader(cache, context);

        //firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
        //firebaseRef.addChildEventListener(new ChildEventListener() {



        firebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals("VersionGoogle")){
                    Long myLong = (Long)dataSnapshot.getValue();
                    // if (myLong < 2){
                    Log.d("ben!", "The Version is: " + dataSnapshot.getValue());
                    if (myLong == versionNumber){
                        childEventListener2();
                    }else {
                        Toast.makeText(context, "Dang, You don't have the latest version, Get It from the Play Store", Toast.LENGTH_LONG ).show();

                    }

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return layout;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!latestIsVisible) {//this happens when we load the app for the first time or it has been killed

                Log.d("ben!the", "IS visible in setUserVisibleHint");
                Log.d("ben!the", "will reload now");
                latestIsVisible = true;
            }

        } else {//this happens only when we swipe away but stay in the same app
            latestIsVisible = false;
            Log.d("ben!the", "is NOT visible");
        }
    }

    private void pureReload() {
        //because of the clearing, no need to do notifiy dataset change.
        mSwipeRefreshLayout.setRefreshing(true);
        mFirebaseDictionary.clear();
        twitterDictionary.clear();
        childEventListener2();
    }




    private void childEventListener2() {

        Glide.with(context).load(R.drawable.fourcirclemakeasquare).into(loadingGifIV);

       // mFirebaseDictionary.clear();//Maybe unnecessary or cause a bug. Watch out.

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {//were doing this to wait for all firebase data BEFORE we go to twitter. https://stackoverflow.com/questions/34530566
            //this happens second (look at the website in the comments)
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("ben! We're done loading the second section: " + dataSnapshot.getChildrenCount() + " item(s)");
               // doSearch2();
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        //this happens FIRST and completes BEFORE moving on to api.twitter.com
        firebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getKey().equals("TheLatest")){

             //   Map<String, Object> eachTopic = new HashMap<>();

                numberOfTopics = (int) dataSnapshot.getChildrenCount();
                    System.out.println("ben! We're done loading the first section " + dataSnapshot.getChildrenCount() + " item(s)");
                    legitChecker = dataSnapshot.getChildrenCount();

              //  Map<String, ArrayList<TheLatestFragmentTwo.LatestTweetModel>> eachTheseSection = new HashMap<>();//everytime the data "changes" we clear it
                Map<String, FBCategory> firebaseCategories = new HashMap<>();

                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();


                for (DataSnapshot category : dataSnapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals("TheLatest")) {

                       // ArrayList<TheLatestFragmentTwo.LatestTweetModel> listOfTheseTweets = new ArrayList<>();
                        FBCategory fbCategory = new FBCategory();

                        ArrayList<FBTweet> fbTweetList = new ArrayList<>();

                        Map<String, String> tweetIDs = (Map<String, String>) category.getValue();//this are hashmaps

                        fbCategory.setName(category.getKey());
                        Log.d("ben!", "topics: " + category);
                        for (DataSnapshot topicMetaData : category.getChildren()) {//for every meta data of this category (picture, order, tweet...)


                            if (topicMetaData.getKey().startsWith("tweet")) {

                                FBTweet fbTweet = new FBTweet();
                                for (DataSnapshot tweetMetaData : topicMetaData.getChildren()) {
                                    if (tweetMetaData.getKey().startsWith("url")) {
                                        if (tweetMetaData.getValue().getClass().getName().equals("java.lang.Long")) {//this may cause problems, test this with a long ass twitterID number on ios too
                                            fbTweet.setTweetId((Long) tweetMetaData.getValue());
                                        } else if (tweetMetaData.getValue().getClass().getName().equals("java.lang.String")) {//this could be wrong again. check it out

                                            Uri uri = Uri.parse(tweetMetaData.getValue().toString());
                                            String tweetIdString = uri.getLastPathSegment();
                                            fbTweet.setTweetId(Long.valueOf(tweetIdString));

                                        }
                                    }
                                    if (tweetMetaData.getKey().startsWith("tweet_order")) {
                                        fbTweet.setOrder((Long) tweetMetaData.getValue());
                                    }
                                }
                                fbTweetList.add(fbTweet);
                            } else if (topicMetaData.getKey().startsWith("picture")) {
                                fbCategory.setPictureUrl((String) topicMetaData.getValue());
                            } else if (topicMetaData.getKey().startsWith("topic_order")) {
                                fbCategory.setOrderNumber((Long) topicMetaData.getValue());
                            }
                            fbCategory.setTweetArray(fbTweetList);

                        }
                        firebaseCategories.put(category.getKey(), fbCategory);

                    }



                }
                    mFirebaseDictionary = firebaseCategories;
                    int total = 0;
                    for (FBCategory l : mFirebaseDictionary.values()) {
                        total = total + 1;
                    }
                    Log.d("ben", "we're the total number of categories: " + total);
                    testingChecker = total;
                    if(legitChecker == testingChecker) {
                        doSearch2();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    public void doSearch2() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);
                    ResponseList<Status> result;
                    try {
                        //  tweets.clear();


                        allTweetIDsLong.clear();
                        for (Map.Entry<String, FBCategory> categoryEntry : mFirebaseDictionary.entrySet()) {
                            for (FBTweet fbTweet : categoryEntry.getValue().getTweetArray()) {
                                allTweetIDsLong.add(fbTweet.getTweetId());
                            }
                        }

                        Long[] idsNonPrimitive = allTweetIDsLong.toArray(new Long[allTweetIDsLong.size()]);
                        long[] ids = ArrayUtils.toPrimitive(idsNonPrimitive);

                        result = twitter.lookup(ids);
                        Log.d("ben!", "looking up tweets from twitter.com");

                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    for (Map.Entry<String, FBCategory> categoryEntry : mFirebaseDictionary.entrySet()) {
                        for (FBTweet fbTweet : categoryEntry.getValue().getTweetArray()) {
                            for (Status status : result) {
                                if (status.getId() == fbTweet.getTweetId()) {
                                    fbTweet.setStatus(status);
                                    //Log.d("ben!", "this is the text we actuall got we're: " +  fbTweet.getStatus().getText());
                                }
                            }
                        }
                    }


                    if (result.size() > 17) {
                        //  hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {



//                            AdapterCallback myAdaptercallback = new AdapterCallback() {
//                                @Override
//                                public void onItemClick(View v, int position) {
//                                    List<FBCategory> firebaseList = new ArrayList<>(mFirebaseDictionary.values());
//                                    changeableTopicKey = firebaseList.get(position).getName();
//                                }
//                            };

                            //WE NEED TO MAKE FIREBASE LIST A MEMBER VARIABLE, AND ARRANGE IT FIRST then attach it to horizontal adapter

                            mFirebaseList = new ArrayList<>(mFirebaseDictionary.values());
                            Collections.sort(mFirebaseList);

                            horizontalAdapter = new HoriCategoryAdapter(context, mFirebaseList, new AdapterCallback() {
                                @Override
                                public void onItemClick(View v, int position) {
                                   // List<FBCategory> firebaseList = new ArrayList<>(mFirebaseDictionary.values());
                                    Log.d("ben!", "clicked position: " +  mFirebaseList.get(position).getName());
                                    changeableTopicKey = mFirebaseList.get(position).getName();
                                    catgoryHeaderText.setText(changeableTopicKey);
                                    mClickedAdapter = new VerticalAdapter(context, mFirebaseDictionary.get(changeableTopicKey), "");
                                    realVertRecycler.setAdapter(mClickedAdapter);
                                    mClickedAdapter.notifyDataSetChanged();
                                }
                            });
                            horizontalLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                            realHoriRecycler.setLayoutManager(horizontalLayoutManager);
                            realHoriRecycler.setAdapter(horizontalAdapter);

                            if(changeableTopicKey == null || refreshJustClicked){
                           // if (changeableTopicKey == null){
                                //changeableTopicKey = new ArrayList<>(mFirebaseDictionary.values()).get(0).getName();
                                changeableTopicKey = mFirebaseList.get(0).getName();
                                catgoryHeaderText.setText(changeableTopicKey);
                                refreshJustClicked = false;
                            }
                            changeableFBCategory = mFirebaseDictionary.get(changeableTopicKey);
                          //  Log.d("ben!", "beat "+ mFirebaseDictionary.get("I Still Beat").getTweetArray());
                            mClickedAdapter = new VerticalAdapter(context, changeableFBCategory, "");
                            realVertRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                            realVertRecycler.setAdapter(mClickedAdapter);
                            realVertRecycler.setVisibility(View.VISIBLE);
                            mClickedAdapter.notifyDataSetChanged();

                            loadingGifIV.setVisibility(View.GONE);
                            // spinner.setVisibility(View.GONE);
                            mSwipeRefreshLayout.setRefreshing(false);
                            canRefresh = true;

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingGifIV.setVisibility(View.GONE);
                            // spinner.setVisibility(View.GONE);
                            mSwipeRefreshLayout.setRefreshing(false);
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

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //   canRefresh = true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    ((Activity) context).runOnUiThread(new Runnable() {
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


    @Override
    public void setUpListScroll() {

    }

    public Twitter getTwitter() throws TwitterException {

        twitter = Utils.getTwitter(context, DrawerActivity.settings);
        String myScreenname = twitter.getScreenName();
        Log.d("ben!", "screenname test: " + myScreenname);
        return twitter;
    }


    //
    public ActivityCursorAdapter setAdapter(Cursor c) {
        return new ActivityCursorAdapter(context, c);
    }

    @Override
    public void onResume() {

        Log.d("ben!", "refresh? " + HomeFragment.shouldRefreshNow);
        Log.d("ben!", "onResume is called");
        if (HomeFragment.shouldRefreshNow){
            //do reload
            HomeFragment.shouldRefreshNow = false;
        }

        super.onResume();

        if (latestIsVisible) {//this happens when we come in the app the first time but the setUserVisibleHint function has already made the bool true
            //do nothing
        } else {
            Log.d("ben!the", "IS visible in onresume");
            Log.d("ben!the", "reloading in onresume");
            latestIsVisible = true;
        }


//        list = new ArrayList<>();
//        for (int i = 0; i < 15; i++) {
//            list.add("This is cool.");
//        }
//        theLatestAdapter = new TheLatestAdapter(getActivity(), list);


        if (sharedPrefs.getBoolean("refresh_me_activity", false)) {
            getCursorAdapter(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sharedPrefs.edit().putBoolean("refresh_me_activity", false).commit();
                }
            }, 1000);
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

    }

    @Override
    public void onPause() {
        context.unregisterReceiver(refreshActivity);
        super.onPause();
        latestIsVisible = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //this is probably unnecessary
        latestIsVisible = false;

    }

    @Override
    public void onRefresh() {
        Toast.makeText(context, "Refreshing...", Toast.LENGTH_SHORT).show();
        refreshJustClicked = true;
        pureReload();
    }

    private class LatestTweetModel {
        Status status = null;
        Long tweetID = null;
        String topic = null;
    }
}

