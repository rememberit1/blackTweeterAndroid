package com.blacktweeter.android.twitter.activities.main_fragments.other_fragments;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.activities.drawer_activities.DrawerActivity;
import com.blacktweeter.android.twitter.activities.main_fragments.MainFragment;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import uk.co.senab.bitmapcache.BitmapLruCache;

import android.net.Uri;

/**
 * Created by benakinlosotuwork on 8/1/18.
 */

public class TheLatestFragmentTwo extends MainFragment implements AdapterCallback {
    public static final int ACTIVITY_REFRESH_ID = 131;

    public int unread = 0;

    private DatabaseReference firebaseRef;
    public static boolean latestIsVisible = false;

    public View layout;
    ImageView loadingGifIV;
    public AppSettings settings;
    public SharedPreferences sharedPrefs;
    public RecyclerView realVertRecycler;


    private HoriCategoryAdapter horizontalAdapter;
    private LinearLayoutManager horizontalLayoutManager;
    public RecyclerView realHoriRecycler;


    //******************************************************************************************************************************///
    Map<String, FBCategory> mFirebaseCategories = new HashMap<>();
    String firstFBTopicKey;
    Map<String, ArrayList<Status>> twitterDictionary = new HashMap<>();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        firebaseRef = FirebaseDatabase.getInstance().getReference();

        settings = AppSettings.getInstance(context);
        sharedPrefs = context.getSharedPreferences("com.klinker.android.twitter_world_preferences",
                0);

        screenName = settings.myScreenName;

        inflater = LayoutInflater.from(context);


        layout = inflater.inflate(R.layout.the_latest_fragment, null);
        realHoriRecycler = (RecyclerView) layout.findViewById(R.id.horizontalCatgoryView);
        realVertRecycler = (RecyclerView) layout.findViewById(R.id.latest_real_recycler_vert);


        //Glide.with(this).asGif().load("").placeholder

        loadingGifIV = (ImageView) layout.findViewById(R.id.loading_gif);
        loadingGifIV.setAlpha((float) 0.7);

        BitmapLruCache cache = App.getInstance(context).getBitmapCache();
        ArrayListLoader loader = new ArrayListLoader(cache, context);

        childEventListener2();

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


//    private void childEventListener() {
//        Glide.with(context).load(R.drawable.fourcirclemakeasquare).into(loadingGifIV);
//
//        mFirebaseCategories.clear();//Maybe unnecessary or cause a bug. Watch out.
//
//        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {//were doing this to wait for all firebase data BEFORE we go to twitter. https://stackoverflow.com/questions/34530566
//            //this happens second (look at the website in the comments)
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("ben! We're done loading the initial " + dataSnapshot.getChildrenCount() + " item");
//                doSearch2();
//            }
//
//            public void onCancelled(DatabaseError firebaseError) {
//            }
//        });
//
//        //this happens first
//        firebaseRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Map<String, Object> eachTopic = new HashMap<>();
//
//                numberOfTopics = (int) dataSnapshot.getChildrenCount();
//
//                Map<String, ArrayList<TheLatestFragmentTwo.LatestTweetModel>> eachTheseSection = new HashMap<>();//everytime the data "changes" we clear it
//                Map<String, FBCategory> firebaseCategories = new HashMap<>();
//
//                for (DataSnapshot topic : dataSnapshot.getChildren()) {
//
//                    ArrayList<TheLatestFragmentTwo.LatestTweetModel> listOfTheseTweets = new ArrayList<>();
//                    FBCategory fbCategory = new FBCategory();
//
//                    Map<String, String> tweetIDs = (Map<String, String>) topic.getValue();//this are hashmaps
//                    // Log.d("ben!", "tweet as shown in db: " + topic);//looks like: DataSnapshot { key = Engineering, value = {id1=https://twitter.com/ycschools_us/status/938164182058496001,
//                    for (DataSnapshot tweet : topic.getChildren()) {
//                        TheLatestFragmentTwo.LatestTweetModel latestTweetModel = new TheLatestFragmentTwo.LatestTweetModel();
//                        latestTweetModel.topic = topic.getKey();
//                        if (tweet.getValue().getClass().getName().equals("java.lang.Long")) {
//                            latestTweetModel.tweetID = (Long) tweet.getValue();
//                        } else {
//                            if (tweet.getValue().toString().length() > 21) {
//                                String tweetWithID = tweet.getValue().toString();
//                                tweetWithID = tweetWithID.substring(tweetWithID.lastIndexOf('/') + 1);
//                                latestTweetModel.tweetID = Long.valueOf(tweetWithID);
//                            } else {
//                                latestTweetModel.tweetID = Long.valueOf(tweet.getValue().toString());
//                            }
//                        }
//                        allLatestTweetModels.add(latestTweetModel);//this where all of them are stored
//                        Log.d("ben!", "tweet key: " + latestTweetModel.topic + " and value: " + latestTweetModel.tweetID);
//                        listOfTheseTweets.add(latestTweetModel);
//                    }
//                    //{Scholarships={id4=https://twitter.com/blackenterprise/status/956756340831019009, id1=https://twitter.com/Becauseofthem/status/955622587165442048}, Engineering={id1=https://twi
//                    eachTopic.put(topic.getKey(), tweetIDs);
//                    eachTheseSection.put(topic.getKey(), listOfTheseTweets);
//                }
//
//                //we need a map with a key of the name of the topic (probably going to use this below)
//
//                mEachTheseSection = eachTheseSection;
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }


    private void childEventListener2() {
        Glide.with(context).load(R.drawable.fourcirclemakeasquare).into(loadingGifIV);

        mFirebaseCategories.clear();//Maybe unnecessary or cause a bug. Watch out.

        firebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {//were doing this to wait for all firebase data BEFORE we go to twitter. https://stackoverflow.com/questions/34530566
            //this happens second (look at the website in the comments)
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("ben! We're done loading the initial " + dataSnapshot.getChildrenCount() + " item");
                doSearch2();
            }

            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        //this happens first
        firebaseRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> eachTopic = new HashMap<>();

                numberOfTopics = (int) dataSnapshot.getChildrenCount();

                Map<String, ArrayList<TheLatestFragmentTwo.LatestTweetModel>> eachTheseSection = new HashMap<>();//everytime the data "changes" we clear it
                Map<String, FBCategory> firebaseCategories = new HashMap<>();

                Iterator<DataSnapshot> it  =dataSnapshot.getChildren().iterator();
                //get the first topic (this may need to change)
//                if (it.hasNext()) {
//                   // String string = it.next();
//                    DataSnapshot firstTopic = it.next();
//                    firstFBTopicKey = firstTopic.getKey();
//                    Log.d("ben!", "first topic: " + firstFBTopicKey);
//                    //first behaviour
//                    while (it.hasNext()) {
//                        Log.d("ben!", "do nothing");
//                    }
//                }

                for (DataSnapshot topic : dataSnapshot.getChildren()) {

                    ArrayList<TheLatestFragmentTwo.LatestTweetModel> listOfTheseTweets = new ArrayList<>();
                    FBCategory fbCategory = new FBCategory();
                    ArrayList<FBTweet> fbTweetList = new ArrayList<>();

                    Map<String, String> tweetIDs = (Map<String, String>) topic.getValue();//this are hashmaps

                    fbCategory.setName(topic.getKey());
                    Log.d("ben!", "topics: " + topic);
                    for (DataSnapshot topicMetaData : topic.getChildren()) {//for every meta data of this category (picture, order, tweet...)


                        if (topicMetaData.getKey().startsWith("tweet")) {

//                        TheLatestFragmentTwo.LatestTweetModel latestTweetModel = new TheLatestFragmentTwo.LatestTweetModel();
//                        latestTweetModel.topic = topic.getKey();
//                        if (tweet.getValue().getClass().getName().equals("java.lang.Long")) {

                            //latestTweetModel.tweetID = (Long) tweet.getValue();
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
//                        else {
//                            if (tweet.getValue().toString().length() > 21) {
//                                String tweetWithID = tweet.getValue().toString();
//                                tweetWithID = tweetWithID.substring(tweetWithID.lastIndexOf('/') + 1);
//                                latestTweetModel.tweetID = Long.valueOf(tweetWithID);
//                            } else {
//                                latestTweetModel.tweetID = Long.valueOf(tweet.getValue().toString());
//                            }
//                        }

//                        allLatestTweetModels.add(latestTweetModel);//this where all of them are stored
//                        Log.d("ben!", "tweet key: " + latestTweetModel.topic + " and value: " + latestTweetModel.tweetID);
//                        listOfTheseTweets.add(latestTweetModel);
                    }
                    firebaseCategories.put(topic.getKey(), fbCategory);
                    // mFirebaseCategories.put(topic.getKey(), fbCategory);

                    //{Scholarships={id4=https://twitter.com/blackenterprise/status/956756340831019009, id1=https://twitter.com/Becauseofthem/status/955622587165442048}, Engineering={id1=https://twi
//                    eachTopic.put(topic.getKey(), tweetIDs);
//                    eachTheseSection.put(topic.getKey(), listOfTheseTweets);
                }


                mFirebaseCategories = firebaseCategories;
                Log.d("ben!", "firebasecategories: " +mFirebaseCategories.toString());
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


    public void arrangeDummyData(List<List<Status>> listOfListOfStatuses) {
        Random random = new Random();
        for (List<Status> listOfStatuses : listOfListOfStatuses) {

            SectionDataModel dm = new SectionDataModel();

            int n = random.nextInt(100) + 1;


            dm.setHeaderTitle("Section " + n);

//            for (Status status : listOfStatuses) {
//                //  tweets.add(status);
//            }
            dm.setAllItemsInSection((ArrayList<Status>) listOfStatuses);
            listOfSectionDataModels.add(dm);
        }
    }

    private void myFunction(Long... longs) {

    }

//    public void doSearch() {
//        // spinner.setVisibility(View.VISIBLE);
//        //  Glide.with(context).load(R.drawable.fourcirclemakeasquare).into(loadingGifIV);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Twitter twitter = Utils.getTwitter(context, settings);
//
//
//                    ResponseList<Status> result;
//                    try {
//                        //  tweets.clear();
//
//
//                        allTweetIDsLong.clear();
//                        for (TheLatestFragmentTwo.LatestTweetModel oneTweetModel : allLatestTweetModels) {
//                            allTweetIDsLong.add(oneTweetModel.tweetID);
//                        }
//
//                        // result = twitter.lookup(20L, 964658962892169216L, 964734223688110080L, 965098414089342976L, 963823438304604160L, Long.valueOf("965001815568912390"));
//                        Long[] idsNonPrimitive = allTweetIDsLong.toArray(new Long[allTweetIDsLong.size()]);
//                        long[] ids = ArrayUtils.toPrimitive(idsNonPrimitive);
//
//                        result = twitter.lookup(ids);
//
//                    } catch (OutOfMemoryError e) {
//                        return;
//                    }
//
//                    //tweets.clear();
//
//                    //iterate through this and then compare the LatestTweetModel.tweetId to result.get(5).getId();
//                    for (String key : mEachTheseSection.keySet()) {
//                        SectionDataModel dm = new SectionDataModel();
//                        ArrayList<Status> statuses = new ArrayList<>();
//                        ArrayList<TheLatestFragmentTwo.LatestTweetModel> listOfTweetModels = mEachTheseSection.get(key);
//                        for (TheLatestFragmentTwo.LatestTweetModel tweetModel : listOfTweetModels) {
//                            Long tweetId = tweetModel.tweetID;
//                            for (Status status : result) {
//                                if (tweetId == status.getId()) {
//                                    statuses.add(status);
//                                }
//                            }
//                        }
//                        dm.setAllItemsInSection(statuses);
//                        System.out.println("ben! list of all tweets this section sections " + dm.getAllItemsInSection());
//
//                        dm.setHeaderTitle(key);
//                        System.out.println("ben! headertitle " + dm.getHeaderTitle());
//                        listOfSectionDataModels.add(dm);
//                    }
//
//
//                    List<Status> result1 = new ArrayList<>();
//                    List<Status> result2 = new ArrayList<>();
//                    result1.add(result.get(0));
//                    result1.add(result.get(1));
//                    result1.add(result.get(2));
//                    result2.add(result.get(3));
//                    result2.add(result.get(4));
//                    result2.add(result.get(5));
//                    result.get(5).getId();
//                    mGroupOfListOfStatuses.add(result1);
//                    mGroupOfListOfStatuses.add(result2);
//
//                    //this/the replacement sets the list of statuses for a section and the section title. because we are using a map, this can be done quicly/in one heap
//                    // arrangeDummyData(mGroupOfListOfStatuses);//this is how we get all the tweets (through mGroupOfListOfStatuses)
//
//
//                    if (result.size() > 17) {
//                        //  hasMore = true;
//                    } else {
//                        hasMore = false;
//                    }
//
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            //  RecyclerViewDataAdapter horizontalAdapter = new RecyclerViewDataAdapter(context, listOfSectionDataModels); //we need to have vertical adapter only.
//                            horizontalAdapter = new HoriCategoryAdapter(context, )
//                            realVertRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
//                            realVertRecycler.setAdapter(horizontalAdapter);
//                            Log.d("ben!", "gotten sections: " + horizontalAdapter.getItemCount());
//                            recyclerRealVertWasHori.setVisibility(View.VISIBLE);
//
//                            //WHAT WE SHOULD ACTUALLY HAVE.
//                            VerticalAdapter verticalAdapter = new VerticalAdapter(mContext, singleSectionOfTweets);
//                            // itemColumnHolder.recycler_view_list.setHasFixedSize(false);
//                            itemColumnHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//                            itemColumnHolder.recycler_view_list.setAdapter(verticalAdapter);
//
//                            loadingGifIV.setVisibility(View.GONE);
//                            // spinner.setVisibility(View.GONE);
//                            canRefresh = true;
//
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    ((Activity) context).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            loadingGifIV.setVisibility(View.GONE);
//                            // spinner.setVisibility(View.GONE);
//                            canRefresh = false;
//                        }
//                    });
//
//                }
//
//            }
//        }).start();
//    }


    public void doSearch2() {
        // spinner.setVisibility(View.VISIBLE);
        //  Glide.with(context).load(R.drawable.fourcirclemakeasquare).into(loadingGifIV);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Twitter twitter = Utils.getTwitter(context, settings);
                    ResponseList<Status> result;
                    try {
                        //  tweets.clear();


                        allTweetIDsLong.clear();
                        for (Map.Entry<String, FBCategory> categoryEntry : mFirebaseCategories.entrySet()) {
                            for (FBTweet fbTweet : categoryEntry.getValue().getTweetArray()) {
                                allTweetIDsLong.add(fbTweet.getTweetId());
                            }
                        }
//                        for (LatestTweetModel oneTweetModel : allLatestTweetModels) {
//                            allTweetIDsLong.add(oneTweetModel.tweetID);
//                        }

                        // result = twitter.lookup(20L, 964658962892169216L, 964734223688110080L, 965098414089342976L, 963823438304604160L, Long.valueOf("965001815568912390"));
                        Long[] idsNonPrimitive = allTweetIDsLong.toArray(new Long[allTweetIDsLong.size()]);
                        long[] ids = ArrayUtils.toPrimitive(idsNonPrimitive);

                        result = twitter.lookup(ids);

                    } catch (OutOfMemoryError e) {
                        return;
                    }

                    for (Map.Entry<String, FBCategory> categoryEntry : mFirebaseCategories.entrySet()) {
                        for (FBTweet fbTweet : categoryEntry.getValue().getTweetArray()) {
                            for (Status status : result) {
                                if (status.getId() == fbTweet.getTweetId()) {
                                    fbTweet.setStatus(status);
                                }
                            }
                        }
                    }



//                    List<Status> result1 = new ArrayList<>();
//                    List<Status> result2 = new ArrayList<>();
//                    result1.add(result.get(0));
//                    result1.add(result.get(1));
//                    result1.add(result.get(2));
//                    result2.add(result.get(3));
//                    result2.add(result.get(4));
//                    result2.add(result.get(5));
//                    result.get(5).getId();
//                    mGroupOfListOfStatuses.add(result1);
//                    mGroupOfListOfStatuses.add(result2);

                    //this/the replacement sets the list of statuses for a section and the section title. because we are using a map, this can be done quicly/in one heap
                    // arrangeDummyData(mGroupOfListOfStatuses);//this is how we get all the tweets (through mGroupOfListOfStatuses)


                    if (result.size() > 17) {
                        //  hasMore = true;
                    } else {
                        hasMore = false;
                    }

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            //We're only commenting out so that we can log everything
                            horizontalAdapter = new HoriCategoryAdapter(context, mFirebaseCategories, TheLatestFragmentTwo.this);
                            horizontalLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                            realHoriRecycler.setLayoutManager(horizontalLayoutManager);
                            realHoriRecycler.setAdapter(horizontalAdapter);

                            changeableFBCategory = mFirebaseCategories.get(changeableTopicKey);
                            VerticalAdapter clickedAdapter = new VerticalAdapter(context, mFirebaseCategories.get("Wimminz in Yellow"), "");
                            realVertRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                            realVertRecycler.setAdapter(clickedAdapter);
                            realVertRecycler.setVisibility(View.VISIBLE);

//                            //WHAT WE SHOULD ACTUALLY HAVE.
//                            VerticalAdapter verticalAdapter = new VerticalAdapter(mContext, singleSectionOfTweets);
//                            // itemColumnHolder.recycler_view_list.setHasFixedSize(false);
//                            itemColumnHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
//                            itemColumnHolder.recycler_view_list.setAdapter(verticalAdapter);


                            //================================================================================================================================================//
                            //================================================================================================================================================//


                            loadingGifIV.setVisibility(View.GONE);
                            // spinner.setVisibility(View.GONE);
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

//        try {
//            getTwitter();
//        } catch (TwitterException e) {
//            e.printStackTrace();
//            Log.d("ben!", "did not get twitter");
//        }

        super.onResume();

        if (latestIsVisible) {//this happens when we come in the app the first time but the setUserVisibleHint function has already made the bool true
            //do nothing
        } else {
            Log.d("ben!the", "IS visible in onresume");
            Log.d("ben!the", "reloading in onresume");
            latestIsVisible = true;
        }


        list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add("This is cool.");
        }
        theLatestAdapter = new TheLatestAdapter(getActivity(), list);


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
    public void onMethodCallback(String clickedString) {
        Log.d("ben!", "changing topic");
    }

    private class LatestTweetModel {
        Status status = null;
        Long tweetID = null;
        String topic = null;
    }
}

