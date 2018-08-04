package com.blacktweeter.android.twitter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.data.FBCategory;
import com.blacktweeter.android.twitter.data.FBTweet;
import com.blacktweeter.android.twitter.utils.AdapterCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by benakinlosotuwork on 7/31/18.
 */

public class HoriCategoryAdapter extends RecyclerView.Adapter<HoriCategoryAdapter.FBCategoryViewHolder> {

    private ArrayList<FBCategory>fbCategoryArrayList = new ArrayList<>();
    private ArrayList<String>stringArrayList = new ArrayList<>();
    private Context context;
    private AdapterCallback mAdapterCallback;
    private CardView cardView;



    public HoriCategoryAdapter(Context context, Map<String, FBCategory> categoryMap, AdapterCallback callback) {


        for (Map.Entry<String, FBCategory> categoryEntry : categoryMap.entrySet()) {
            this.fbCategoryArrayList.add(categoryEntry.getValue());
        }

        this.context = context;
        this.mAdapterCallback = callback;
    }

    @Override
    public FBCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new FBCategoryViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hori_category_adapter, parent, false));
    }

    @Override
    public int getItemCount() {
        return fbCategoryArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return  super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(final FBCategoryViewHolder holder, final int position) {
        FBCategory fbCategory = fbCategoryArrayList.get(position);//This must be created first (this may cause problems later)
        final FBCategoryViewHolder fbCategoryViewHolder = (FBCategoryViewHolder) holder;

        Picasso.with(context).load(fbCategory.getPictureUrl()).into(fbCategoryViewHolder.imageView);
        fbCategoryViewHolder.categoryText.setText(fbCategory.getName());
    }

    public class FBCategoryViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView categoryText;

        public FBCategoryViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.category_Imageview);
            categoryText = (TextView) itemView.findViewById(R.id.category_text);
        }


    }


}

