package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.R;

import java.lang.reflect.Field;

/**
 * Created by multimeet on 17/1/18.
 */

public class MediaViewHolder extends RecyclerView.ViewHolder {

    public ImageView thumbnail;
    public LinearLayout constraintLayout;
    public TextView name;
    public MediaViewHolder(View v){
        super(v);
        thumbnail=v.findViewById(R.id.thumbnail);
        name=v.findViewById(R.id.name);
        constraintLayout=(LinearLayout) v;
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
