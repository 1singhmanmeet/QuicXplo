package com.singh.multimeet.quicxplo.adapter.viewHolder;

import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.R;

import java.lang.reflect.Field;

/**
 * Created by multimeet on 15/1/18.
 */

public class QuickAccessViewHolder extends RecyclerView.ViewHolder {

    public ImageView icon;
    public TextView label;
    public CardView constraintLayout;

    public QuickAccessViewHolder(View v){
        super(v);
        icon=v.findViewById(R.id.icon);
        label=v.findViewById(R.id.label);
        constraintLayout=(CardView) v;
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
