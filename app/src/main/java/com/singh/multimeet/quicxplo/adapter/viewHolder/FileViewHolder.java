package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.AppController;


import java.lang.reflect.Field;

/**
 * Created by multimeet on 6/1/18.
 */
public class FileViewHolder extends RecyclerView.ViewHolder{

    public TextView name,info,size;
    public ImageView icon;
    public ConstraintLayout mainView;
    public FileViewHolder(View v){
        super(v);
        name=v.findViewById(R.id.name);
        icon=v.findViewById(R.id.icon);
        info=v.findViewById(R.id.info);
        size=v.findViewById(R.id.size);
        mainView=v.findViewById(R.id.main_view);
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
