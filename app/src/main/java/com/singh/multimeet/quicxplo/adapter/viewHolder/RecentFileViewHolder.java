package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.AppController;

import java.lang.reflect.Field;

/**
 * Created by multimeet on 30/1/18.
 */

public class RecentFileViewHolder extends RecyclerView.ViewHolder {

    public TextView name,size,folder,date;
    public ImageView marker,icon;
    public RelativeLayout main;
    public RecentFileViewHolder(View v){
        super(v);
        name=v.findViewById(com.singh.multimeet.quicxplo.R.id.name);
        size=v.findViewById(com.singh.multimeet.quicxplo.R.id.size);
        folder=v.findViewById(com.singh.multimeet.quicxplo.R.id.folder);
        date=v.findViewById(com.singh.multimeet.quicxplo.R.id.date);
        marker=v.findViewById(com.singh.multimeet.quicxplo.R.id.marker);
        icon=v.findViewById(com.singh.multimeet.quicxplo.R.id.icon);
        main=(RelativeLayout)v;
        setTypeFace();
    }
    public void setTypeFace(){
        try {
            Typeface typeface= AppController.getTypeface();
            for (Field field : this.getClass().getFields()) {
                if (field.get(this) instanceof TextView) {
                    ((TextView)field.get(this)).setTypeface(typeface);
                }
            }
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }
}
