package com.blacktweeter.android.twitter.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blacktweeter.android.twitter.R;

import java.util.List;

/**
 * Created by benakinlosotuwork on 2/9/18.
 */

public class TheLatestAdapter extends RecyclerView.Adapter<TheLatestAdapter.TheLatestViewHolder>{

    Context context;
    List<String> list;

    public TheLatestAdapter (Context context, List<String> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public TheLatestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.the_latest_row, parent, false );
        return new TheLatestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TheLatestViewHolder holder, int position) {
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TheLatestViewHolder extends RecyclerView.ViewHolder{

        TextView textView;


        public TheLatestViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
