package com.blacktweeter.android.twitter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blacktweeter.android.twitter.R;
import com.blacktweeter.android.twitter.data.FBCategory;
import com.blacktweeter.android.twitter.utils.AdapterCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by benakinlosotuwork on 7/31/18.
 */

public class HoriCategoryAdapter extends RecyclerView.Adapter<HoriCategoryAdapter.FBCategoryViewHolder> {

    private ArrayList<FBCategory> fbCategoryArrayList = new ArrayList<>();
    private Context context;
    private AdapterCallback mAdapterCallback;

    public HoriCategoryAdapter(Context context,ArrayList<FBCategory> categoryList, AdapterCallback callback) {
        this.fbCategoryArrayList = categoryList;
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

        fbCategoryViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, fbCategoryViewHolder.categoryText.getText() +" - "+position, Toast.LENGTH_SHORT).show();
                mAdapterCallback.onMethodCallback((String) fbCategoryViewHolder.categoryText.getText());
            }
        });

    }

    public class FBCategoryViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView categoryText;

        public FBCategoryViewHolder(View itemView) {
            super(itemView);
            // itemView.setOnClickListener(this);
            imageView = (ImageView) itemView.findViewById(R.id.category_Imageview);
            categoryText = (TextView) itemView.findViewById(R.id.category_text);
        }


    }


}
