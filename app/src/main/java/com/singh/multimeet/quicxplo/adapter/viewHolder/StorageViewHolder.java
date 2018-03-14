package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.AppController;

import java.lang.reflect.Field;

/**
 * Created by multimeet on 13/1/18.
 */

public class StorageViewHolder extends RecyclerView.ViewHolder{

    public TextView used,total,free,percentage,label;
    public ProgressBar progressBar;
    public CardView rootView;

    public StorageViewHolder(View v){
        super(v);
        used=v.findViewById(com.singh.multimeet.quicxplo.R.id.used);
        total=v.findViewById(com.singh.multimeet.quicxplo.R.id.total);
        free=v.findViewById(com.singh.multimeet.quicxplo.R.id.free);
        label=v.findViewById(com.singh.multimeet.quicxplo.R.id.label);
        percentage=v.findViewById(com.singh.multimeet.quicxplo.R.id.percentage);
        progressBar=v.findViewById(com.singh.multimeet.quicxplo.R.id.progressBar);
        rootView=(CardView) v;
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
