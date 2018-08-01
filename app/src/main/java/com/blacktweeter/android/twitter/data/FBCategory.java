package com.blacktweeter.android.twitter.data;

import java.util.ArrayList;

/**
 * Created by benakinlosotuwork on 7/31/18.
 */

public class FBCategory {
    String name;
    String pictureUrl;
    int orderNumber;
    ArrayList<FBTweet> tweetArray = new ArrayList<>();

    public FBCategory(){

    }

    public FBCategory(String name, String pictureUrl, int orderNumber, ArrayList<FBTweet> tweetArray) {
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.orderNumber = orderNumber;
        this.tweetArray = tweetArray;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setTweetArray(ArrayList<FBTweet> tweetArray) {
        this.tweetArray = tweetArray;
    }




    public String getName() {
        return name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public ArrayList<FBTweet> getTweetArray() {
        return tweetArray;
    }
}
