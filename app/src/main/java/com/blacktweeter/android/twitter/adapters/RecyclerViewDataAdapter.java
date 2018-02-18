package com.blacktweeter.android.twitter.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.data.SectionDataModel;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by benakinlosotuwork on 2/13/18.
 */

public class RecyclerViewDataAdapter extends RecyclerView.Adapter<RecyclerViewDataAdapter.ItemColumnHolder> {

    private ArrayList<SectionDataModel> allSectionsMadeOfTweets;
    private Context mContext;

    public RecyclerViewDataAdapter(Context context, ArrayList<SectionDataModel> allSectionsMadeOfTweets) {
        this.allSectionsMadeOfTweets = allSectionsMadeOfTweets;
        this.mContext = context;
    }

    @Override
    public ItemColumnHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        ItemColumnHolder mh = new ItemColumnHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemColumnHolder itemColumnHolder, int i) {

        final String sectionName = allSectionsMadeOfTweets.get(i).getHeaderTitle();

        ArrayList <Status> singleSectionOfTweets = allSectionsMadeOfTweets.get(i).getAllItemsInSection();

        itemColumnHolder.itemTitle.setText(sectionName);

        VerticalAdapter verticalAdapter = new VerticalAdapter(mContext, singleSectionOfTweets);

       // itemColumnHolder.recycler_view_list.setHasFixedSize(false);
        itemColumnHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        itemColumnHolder.recycler_view_list.setAdapter(verticalAdapter);



       /* Glide.with(mContext)
                .load(feedItem.getImageURL())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.bg)
                .into(feedListRowHolder.thumbView);*/
    }

    @Override
    public int getItemCount() {
        return (null != allSectionsMadeOfTweets ? allSectionsMadeOfTweets.size() : 0);
    }

    public class ItemColumnHolder extends RecyclerView.ViewHolder {

        protected TextView itemTitle;

        protected RecyclerView recycler_view_list;





        public ItemColumnHolder(View view) {
            super(view);

            this.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            this.recycler_view_list = (RecyclerView) view.findViewById(R.id.recycler_view_list);


        }

    }

}
