package com.blacktweeter.android.twitter.data;

/**
 * Created by benakinlosotuwork on 7/31/18.
 */

public class FBTweet {
    String name;
    int order;
    String tweetId;

    public FBTweet(){

    }

    public FBTweet(String name, int order, String tweetId) {
        this.name = name;
        this.order = order;
        this.tweetId = tweetId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }




    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public String getTweetId() {
        return tweetId;
    }
}
