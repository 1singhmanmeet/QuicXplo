package com.singh.multimeet.quicxplo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;

import com.bumptech.glide.RequestManager;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.OnDataLoadListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.viewHolder.RecentFileViewHolder;

import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by multimeet on 28/1/18.
 */

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFileViewHolder> {

    List<String> keyList=new ArrayList<>();
    List<FileDirectory> totalList=new ArrayList<>();
    Map<String,List<FileDirectory>> recentMap=new HashMap<>();
    OnRecyclerItemClickListener onRecyclerItemClickListener;
    OnDataLoadListener onDataLoadListener;
    Context context;
    Calendar calendar=Calendar.getInstance();
    private RequestManager requestManager;
    final String TAG=RecentFilesAdapter.class.getSimpleName();
    RelativeLayout.LayoutParams imgParams=new RelativeLayout.LayoutParams(200,200);
    static final String[] IMG=new String[]{"jpg","png","gif"};

    public RecentFilesAdapter(Context context, Map<String,List<FileDirectory>> recentMap,
                              List<String> keyList,
                              RequestManager requestManager){
        this.context=context;
        this.recentMap=recentMap;
        this.keyList=keyList;
        this.requestManager=requestManager;
    }

    public void setOnDataLoadListener(OnDataLoadListener onDataLoadListener){
        this.onDataLoadListener=onDataLoadListener;
    }

    List<FileDirectory> getTotalItems(){
        totalList.clear();
        for(String key:keyList){
            for(FileDirectory fileDirectory:recentMap.get(key))
                totalList.add(fileDirectory);
        }
        return totalList;
    }

    public List<FileDirectory> getTotalList(){return totalList;}

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener
                                               onRecyclerItemClickListener){
        this.onRecyclerItemClickListener=onRecyclerItemClickListener;
    }



    @Override
    public RecentFileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(onDataLoadListener!=null)
            onDataLoadListener.onDataLoaded(true);
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_item_view,parent,false);

        return new RecentFileViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecentFileViewHolder holder, int position) {
        holder.main.setOnClickListener((view)->{
            if(onRecyclerItemClickListener!=null){
                onRecyclerItemClickListener.onClick(view,position);
            }
        });

        holder.main.setOnLongClickListener(view -> {
            if(onRecyclerItemClickListener!=null)
                onRecyclerItemClickListener.onLongClick(view,position);
            return true;
        });

        String parentDir=Util.getParentDirName(new File(totalList.get(position)
                .getPath()).getParent());

        // getting time difference from today.
        holder.date.setText(getTimeDifference(Util.getDateFromPath(totalList.get(position)
                .getPath() ),calendar.getTime()));

        String image=totalList.get(position).getName();
        if(parentDir.equalsIgnoreCase(".Statuses")) {
            holder.folder.setText(String.format("from %s","WhatsApp"));
            holder.name.setText("WhatsApp Story");
        }
        else {
            holder.folder.setText(String.format("from %s",parentDir));
            holder.name.setText(Util.getTrimmed(totalList.get(position).getName()));
        }
        holder.size.setText(totalList.get(position).getSize());

        if(isImage(image)){
            imgParams.setMargins(0,0,10,0);
            imgParams.addRule(RelativeLayout.CENTER_VERTICAL);
            holder.icon.setLayoutParams(imgParams);
            requestManager.load(totalList.get(position).getPath())
                    .apply(AppController.getRequestOptions())
                    .thumbnail(0.7f)
                    .into(holder.icon);
            if(holder.icon.getDrawable()==null){
                totalList.remove(position);
                notifyItemRemoved(position);
            }
        }
        else if(Util.getImageResIdFromExension(totalList.get(position).getName())==R.drawable.video){
            loadThumbnail(holder,position,Util.VIDEO_ART);
        }

        else{

            holder.icon.setImageResource(Util.getImageResIdFromExension(totalList
                    .get(position).getName()));
        }
    }

    String getTimeDifference(Date start,Date end){

        long duration  = end.getTime() - start.getTime();
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        if(diffInSeconds<60)
            return diffInSeconds+" seconds ago";
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        if(diffInMinutes<60)
            return diffInMinutes+" minutes ago";
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);

        if(diffInHours<24)
            return diffInHours+" hour(s) ago";
        return (diffInHours/24)+" day(s) ago";
    }

    void loadThumbnail(RecentFileViewHolder holder, int position, int selection){

        Util.loadAlbumArt(totalList.get(position).getPath(),selection)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<Bitmap>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        if(bitmap!=null){
                            if(selection==Util.VIDEO_ART ){
                                requestManager.load(bitmap)
                                        .apply(AppController.getRequestOptions())
                                        .into(holder.icon);
                            }
                            else if(selection==Util.MUSIC_ART ){
                                requestManager.load(bitmap)
                                        .apply(AppController.getCircleRequestOptions())
                                        .into(holder.icon);
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(RecentFilesAdapter.class.getSimpleName(),"Error: "+t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    boolean isImage(String name){
        String ext= MimeTypeMap.getFileExtensionFromUrl(name);
        for(String img:IMG){
            if(ext.equalsIgnoreCase(img))
                return true;
        }
        return false;
    }


    @Override
    public int getItemCount() {
        return getTotalItems().size();
    }
}
