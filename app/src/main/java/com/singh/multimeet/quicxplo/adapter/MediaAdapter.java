package com.singh.multimeet.quicxplo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.RequestManager;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.OnItemSelectedListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.viewHolder.MediaViewHolder;
import com.singh.multimeet.quicxplo.ui.browse.quickAccess.QuickAccess;

import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by multimeet on 17/1/18.
 *
 *
 */

public class MediaAdapter extends RecyclerView.Adapter<MediaViewHolder> {

    List<FileDirectory> fileDirectoryList=new ArrayList<>();
    Context context;
    OnRecyclerItemClickListener onRecyclerItemClickListener;
    static final String TAG=MediaAdapter.class.getSimpleName();
    int type=0;
    Subscription subscription;
    private RequestManager glide;
    private final float SELECTED_ROTATION=45;
    OnItemSelectedListener onItemSelectedListener;
    List<FileDirectory> selectedList=new ArrayList<>();

    public MediaAdapter(List<FileDirectory> fileDirectoryList, Context context, int type, RequestManager glide){
        this.fileDirectoryList=fileDirectoryList;
        this.context=context;
        this.type=type;
        this.glide=glide;
    }

    public void setFileDirectoryList(List<FileDirectory> fileDirectoryList){
        this.fileDirectoryList=fileDirectoryList;
        notifyDataSetChanged();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener){
        this.onItemSelectedListener=onItemSelectedListener;
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.media_view,parent,false);
        return new MediaViewHolder(v);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener
                                               onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }

    public void disableSelection(){
        selectedList.clear();
        onItemSelectedListener.onItemListChanged(selectedList);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MediaViewHolder holder, int position) {
        if(onRecyclerItemClickListener!=null){
            holder.constraintLayout.setOnClickListener(view -> {
                if(holder.constraintLayout.getRotation()==SELECTED_ROTATION){
                    holder.constraintLayout.setRotation(0);
                    selectedList.remove(fileDirectoryList.get(position));
                    onItemSelectedListener.onItemListChanged(selectedList);
                }
                else if(selectedList.size()>0){
                    selectedList.add(fileDirectoryList.get(position));
                    holder.constraintLayout.setRotation(SELECTED_ROTATION);
                    onItemSelectedListener.onItemListChanged(selectedList);
                }
                else
                    onRecyclerItemClickListener.onClick(view,position);
            });
        }


        if(selectedList.size()>0 && selectedList.contains(fileDirectoryList.get(position))){
            holder.constraintLayout.setRotation(SELECTED_ROTATION);

        }else{
            holder.constraintLayout.setRotation(0);

        }
        holder.constraintLayout.setOnLongClickListener(view -> {
            if(!selectedList.contains(fileDirectoryList.get(position))) {
                selectedList.add(fileDirectoryList.get(position));
                holder.constraintLayout.setRotation(SELECTED_ROTATION);
                onItemSelectedListener.onItemListChanged(selectedList);
            }
            return true;
        });
        int width=(int)context.getResources().getDimension(R.dimen.media_view_width);
        int height=(int)context.getResources().getDimension(R.dimen.media_view_width);
        try {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(10,10,10,10);
            params.gravity= Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL;

            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            nameParams.setMargins(6,6,6,6);
            nameParams.gravity=Gravity.CENTER_HORIZONTAL;

            switch (type) {
                case QuickAccess.IMAGES:

                    glide.load(new File(fileDirectoryList.get(position).getPath()))
                         .apply(AppController.getRequestOptions())
                         .transition(GenericTransitionOptions.with(R.anim.item_fall_down))
                         .into(holder.thumbnail);
                    holder.name.setVisibility(View.GONE);

                    break;

                case QuickAccess.AUDIO:
                    holder.name.setText(Util.getTrimmed(fileDirectoryList.get(position).getName()));
                    holder.name.setTextSize(15);
                    holder.thumbnail.setLayoutParams(params);
                    if(!isImageSet(holder.thumbnail,R.drawable.file))
                        loadThumbnail(holder,position,Util.MUSIC_ART);
                    break;

                case QuickAccess.VIDEO:
                    holder.name.setText(Util.getTrimmed(fileDirectoryList.get(position).getName()));
                    holder.name.setTextSize(15);
                    holder.thumbnail.setLayoutParams(params);
                    if(!isImageSet(holder.thumbnail,R.drawable.file))
                        loadThumbnail(holder,position,Util.VIDEO_ART);
                    break;

                case QuickAccess.DOCUMENTS:
                    holder.thumbnail.setImageResource(Util.getImageResIdFromExension(fileDirectoryList.get(position).getName()));
                    holder.name.setTextSize(15);
                    holder.name.setText(Util.getTrimmed(fileDirectoryList.get(position).getName()));
                    holder.thumbnail.setLayoutParams(params);
                    break;

            }
        }catch (Exception e){
            Log.e(TAG,"glide error: "+e.getMessage());
            //e.printStackTrace();

        }
    }


    boolean isImageSet(ImageView imageView,int resId){
        Drawable.ConstantState current=imageView.getDrawable().getConstantState();
        Drawable.ConstantState toCheck;
        if(Build.VERSION.SDK_INT>=21)
            toCheck=context.getResources().getDrawable(resId,null).getConstantState();
        else
            toCheck=context.getResources().getDrawable(resId).getConstantState();

        if(current==toCheck)
            return false;
        return true;
    }

    void loadThumbnail(MediaViewHolder holder,int position,int selection){

        Util.loadAlbumArt(fileDirectoryList.get(position).getPath(),selection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<Bitmap>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription=s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        if(bitmap!=null){
                            if(selection==Util.VIDEO_ART ){
                                glide.load(bitmap)
                                    .apply(AppController.getRequestOptions())
                                    .into(holder.thumbnail);
                            }
                            else if(selection==Util.MUSIC_ART ){
                                glide.load(bitmap)
                                        .apply(AppController.getCircleRequestOptions())
                                        .into(holder.thumbnail);
                            }
                        }

                        else{
                            if(selection==Util.VIDEO_ART ){
                                glide.load(R.drawable.video)
                                        .apply(AppController.getRequestOptions())
                                        .into(holder.thumbnail);
                            }
                            else if(selection==Util.MUSIC_ART ){
                                glide.load(R.drawable.music)
                                        .apply(AppController.getCircleRequestOptions())
                                        .into(holder.thumbnail);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG,"Error: "+t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return fileDirectoryList.size();
    }
}
