package com.singh.multimeet.quicxplo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.adapter.viewHolder.QuickAccessViewHolder;
import com.singh.multimeet.quicxplo.ui.browse.home.Home;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by multimeet on 15/1/18.
 */

public class QuickAccessAdapter extends RecyclerView.Adapter<QuickAccessViewHolder> {

    List<String> quickAccessList=new ArrayList<>();
    OnRecyclerItemClickListener onRecyclerItemClickListener;

    public QuickAccessAdapter(List<String> quickAccessList){
        this.quickAccessList=quickAccessList;
    }


    @Override
    public QuickAccessViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.quick_access_view,parent,false);
        return new QuickAccessViewHolder(v);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener
                                                       onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }

    @Override
    public void onBindViewHolder(QuickAccessViewHolder holder, int position) {
        holder.label.setText(quickAccessList.get(position));


        switch (quickAccessList.get(position)){
            case Home.DOCUMENTS:
                holder.icon.setImageResource(R.drawable.file);
                break;

            case Home.VIDEO:
                holder.icon.setImageResource(R.drawable.video);
                break;

            case Home.IMAGES:
                holder.icon.setImageResource(R.drawable.picture);
                break;

            case Home.AUDIO:
                holder.icon.setImageResource(R.drawable.music);
        }

        holder.constraintLayout.setOnClickListener(view -> {
            if(onRecyclerItemClickListener!=null){
                onRecyclerItemClickListener.onClick(holder.constraintLayout,position);
            }
        });

        holder.constraintLayout.setOnLongClickListener(view -> {

            if(onRecyclerItemClickListener!=null)
                onRecyclerItemClickListener.onLongClick(holder.constraintLayout,position);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return quickAccessList.size();
    }
}
