package com.singh.multimeet.quicxplo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.adapter.viewHolder.StorageViewHolder;
import com.singh.multimeet.quicxplo.model.Storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by multimeet on 13/1/18.
 */

public class StorageAdapter extends RecyclerView.Adapter<StorageViewHolder> {

    private List<Storage> storageList=new ArrayList<>();
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public StorageAdapter(List<Storage> storageList,
                          OnRecyclerItemClickListener onRecyclerItemClickListener){
        this.storageList=storageList;
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }

    @Override
    public StorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_status_view,parent,false);
        return new StorageViewHolder(v);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }

    @Override
    public void onBindViewHolder(StorageViewHolder holder, int position) {
        holder.label.setText(storageList.get(position).getTitle());
        holder.progressBar.setProgress(storageList.get(position).getPercentage());
        holder.percentage.setText(storageList.get(position).getPercentage()+"%");
        holder.free.setText(storageList.get(position).getFree());
        holder.total.setText(storageList.get(position).getTotal());
        holder.used.setText(storageList.get(position).getUsed());
        holder.rootView.setOnClickListener(view -> {
            if(onRecyclerItemClickListener!=null)
                onRecyclerItemClickListener.onClick(view,position);
        });
    }

    @Override
    public int getItemCount() {
        return storageList.size();
    }
}
