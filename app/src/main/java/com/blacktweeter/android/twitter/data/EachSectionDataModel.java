package com.blacktweeter.android.twitter.data;

import android.os.AsyncTask;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by benakinlosotuwork on 2/13/18.
 */

public class EachSectionDataModel {



    private String headerTitle;
    private ArrayList<Status> allItemsInSection;


    public EachSectionDataModel() {

    }
    public EachSectionDataModel(String headerTitle, ArrayList<Status> allItemsInSection) {
        this.headerTitle = headerTitle;
        this.allItemsInSection = allItemsInSection;
    }



    public String getHeaderTitle() {
        return headerTitle;
    }

    public void setHeaderTitle(String headerTitle) {
        this.headerTitle = headerTitle;
    }

    public ArrayList<Status> getAllItemsInSection() {
        return allItemsInSection;
    }

    public void setAllItemsInSection(ArrayList<Status> allItemsInSection) {
        this.allItemsInSection = allItemsInSection;
    }


}
