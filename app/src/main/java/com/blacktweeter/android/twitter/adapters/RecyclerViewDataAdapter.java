package com.blacktweeter.android.twitter.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.data.EachSectionDataModel;

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by benakinlosotuwork on 2/13/18.
 */

public class RecyclerViewDataAdapter extends RecyclerView.Adapter<RecyclerViewDataAdapter.ItemRowHolder> {

    private ArrayList<EachSectionDataModel> allSectionsMadeOfTweets;
    private Context mContext;

    public RecyclerViewDataAdapter(Context context, ArrayList<EachSectionDataModel> allSectionsMadeOfTweets) {
        this.allSectionsMadeOfTweets = allSectionsMadeOfTweets;
        this.mContext = context;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, null);
        ItemRowHolder mh = new ItemRowHolder(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(ItemRowHolder itemRowHolder, int i) {

        final String sectionName = allSectionsMadeOfTweets.get(i).getHeaderTitle();

        ArrayList <Status> singleSectionOfTweets = allSectionsMadeOfTweets.get(i).getAllItemsInSection();

        itemRowHolder.itemTitle.setText(sectionName);

        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(mContext, singleSectionOfTweets);

        itemRowHolder.recycler_view_list.setHasFixedSize(true);
        itemRowHolder.recycler_view_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        itemRowHolder.recycler_view_list.setAdapter(horizontalAdapter);



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

    public class ItemRowHolder extends RecyclerView.ViewHolder {

        protected TextView itemTitle;

        protected RecyclerView recycler_view_list;





        public ItemRowHolder(View view) {
            super(view);

            this.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            this.recycler_view_list = (RecyclerView) view.findViewById(R.id.recycler_view_list);


        }

    }

}
