package com.blacktweeter.android.twitter.data;

import twitter4j.Status;

/**
 * Created by benakinlosotuwork on 7/31/18.
 */

public class FBTweet {
    String name;
    int order;
    Long tweetId;
    Status status;

    public FBTweet() {

    }

    public FBTweet(String name, int order, Long tweetId, Status status) {
        this.name = name;
        this.order = order;
        this.tweetId = tweetId;
        this.status = status;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setTweetId(Long tweetId) {
        this.tweetId = tweetId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public Long getTweetId() {
        return tweetId;
    }

    public Status getStatus() {
        return status;
    }

}
