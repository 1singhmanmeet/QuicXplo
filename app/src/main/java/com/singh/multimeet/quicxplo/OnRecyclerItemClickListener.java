package com.singh.multimeet.quicxplo;

import android.view.View;

/**
 * Created by multimeet on 13/1/18.
 */

public interface OnRecyclerItemClickListener{
    void onClick(View view, int position);
    void onLongClick(View view,int position);
}
