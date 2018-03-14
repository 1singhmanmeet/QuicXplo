package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.AppController;

import java.lang.reflect.Field;


/**
 * Created by multimeet on 22/1/18.
 */

public class StorageChooserViewHolder extends RecyclerView.ViewHolder {

    public ImageView icon;
    public TextView title,path;
    public ConstraintLayout constraintLayout;

    public StorageChooserViewHolder(View v){
        super(v);
        icon=v.findViewById(com.singh.multimeet.quicxplo.R.id.icon);
        title=v.findViewById(com.singh.multimeet.quicxplo.R.id.title);
        path=v.findViewById(com.singh.multimeet.quicxplo.R.id.path);
        constraintLayout=(ConstraintLayout) v;
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
