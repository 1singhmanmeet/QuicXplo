package com.singh.multimeet.quicxplo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;

import com.singh.multimeet.quicxplo.adapter.viewHolder.StorageChooserViewHolder;
import com.singh.multimeet.quicxplo.model.StorageSelection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by multimeet on 22/1/18.
 */

public class StorageChooserAdapter extends RecyclerView.Adapter<StorageChooserViewHolder> {

    List<StorageSelection> storageSelectionList=new ArrayList<>();
    OnRecyclerItemClickListener onRecyclerItemClickListener;

    public StorageChooserAdapter(List<StorageSelection> storageSelectionList){
        this.storageSelectionList=storageSelectionList;
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener
                                               onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }
    @Override
    public StorageChooserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_chooser_layout,parent,false);
        return new StorageChooserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StorageChooserViewHolder holder, int position) {
        holder.constraintLayout.setOnClickListener(view -> {
            if(onRecyclerItemClickListener!=null)
                onRecyclerItemClickListener.onClick(view,position);
        });
        holder.title.setText(storageSelectionList.get(position).getTitle());
        holder.path.setText("("+storageSelectionList.get(position).getPath()+")");
        if(storageSelectionList.get(position).getTitle().charAt(0)=='I'){
            holder.icon.setImageResource(R.drawable.smartphone);
        }else
            holder.icon.setImageResource(R.drawable.sd_card);
    }

    @Override
    public int getItemCount() {
        return storageSelectionList.size();
    }
}
