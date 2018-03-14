package com.singh.multimeet.quicxplo.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.bumptech.glide.Glide;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.viewHolder.FileViewHolder;

import java.io.File;
import java.util.List;

/**
 * Created by multimeet on 6/1/18.
 */

public class SearchListAdapter extends RecyclerView.Adapter<FileViewHolder> {


    List<FileDirectory> fileDirectoryList;
    Context context;
    String[] IMG_TYPES=new String[]{"jpg","png","gif"};
    OnRecyclerItemClickListener onRecyclerItemClickListener;

    public SearchListAdapter(List<FileDirectory> fileDirectoryList, Context context){
        this.fileDirectoryList=fileDirectoryList;
        this.context=context;
    }

    public void destroyContext(){
        this.context=null;
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener
                                                       onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }

    public void setFileDirectoryList(List<FileDirectory> list){
        this.fileDirectoryList=list;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.file_directory_view,parent,false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.name.setText(Util.getTrimmed(fileDirectoryList.get(position).getName()));
        holder.info.setText(fileDirectoryList.get(position).getDate());
        String ext=MimeTypeMap.getFileExtensionFromUrl(fileDirectoryList.get(position).getName());
        holder.mainView.setOnClickListener(view ->{
            if(onRecyclerItemClickListener!=null){
                onRecyclerItemClickListener.onClick(view,position);
            }
        } );

        holder.mainView.setOnLongClickListener(view -> {
            if(onRecyclerItemClickListener!=null)
                onRecyclerItemClickListener.onLongClick(view,position);
            return true;
        });
        if(fileDirectoryList.get(position).getFileOrDir()== FileDirectory.DIR){
            holder.size.setText("");
            holder.icon.setImageResource(R.drawable.folder);
            return;
        }
        else if(ext.length()<=3 && (ext.equals(IMG_TYPES[0]) || ext.equals(IMG_TYPES[1])
                || ext.equals(IMG_TYPES[2]))){
            holder.size.setText(fileDirectoryList.get(position).getSize());
            Glide.with(context).load(Uri.fromFile(new File(fileDirectoryList
                    .get(position).getPath()))).into(holder.icon);
        }
        else {
            holder.size.setText(fileDirectoryList.get(position).getSize());
            holder.icon.setImageResource(Util.getImageResIdFromExension(fileDirectoryList
                    .get(position).getName()));
        }
    }


    @Override
    public int getItemCount() {
        return fileDirectoryList.size();
    }

}
