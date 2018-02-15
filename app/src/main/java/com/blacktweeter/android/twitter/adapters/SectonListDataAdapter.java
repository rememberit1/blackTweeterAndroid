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

import java.util.ArrayList;

import twitter4j.Status;

/**
 * Created by benakinlosotuwork on 2/13/18.
 */

//public class SectionListDataAdapter extends RecyclerView.Adapter<SectionListDataAdapter.SingleItemRowHolder> {
//
//    private ArrayList<Status> itemsList;
//    private Context mContext;
//
//    public SectionListDataAdapter(Context context, ArrayList<Status> itemsList) {
//        this.itemsList = itemsList;
//        this.mContext = context;
//    }
//
//    @Override
//    public SingleItemRowHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_single_card, null);
//        SingleItemRowHolder mh = new SingleItemRowHolder(v);
//        return mh;
//    }
//
//    @Override
//    public void onBindViewHolder(SingleItemRowHolder holder, int i) {
//
//        Status singleItem = itemsList.get(i);
//
//        holder.tvTitle.setText(singleItem.getName());
//
//
//       /* Glide.with(mContext)
//                .load(feedItem.getImageURL())
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .centerCrop()
//                .error(R.drawable.bg)
//                .into(feedListRowHolder.thumbView);*/
//    }
//
//    @Override
//    public int getItemCount() {
//        return (null != itemsList ? itemsList.size() : 0);
//    }
//
//    public class SingleItemRowHolder extends RecyclerView.ViewHolder {
//
//        protected TextView tvTitle;
//
//        protected ImageView itemImage;
//
//
//        public SingleItemRowHolder(View view) {
//            super(view);
//
//            this.tvTitle = (TextView) view.findViewById(R.id.tvTitle);
//           // this.itemImage = (ImageView) view.findViewById(R.id.itemImage);
//
//
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//
//                    Toast.makeText(v.getContext(), tvTitle.getText(), Toast.LENGTH_SHORT).show();
//
//                }
//            });
//        }
//
//    }
//
//
//}

