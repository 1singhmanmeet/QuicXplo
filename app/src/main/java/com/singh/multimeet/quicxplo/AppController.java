package com.singh.multimeet.quicxplo;

import android.app.Application;
import android.graphics.Typeface;
import android.os.StrictMode;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by multimeet on 10/12/17.
 */

public class AppController extends Application {

    static Typeface typeface;
    static RequestOptions options,circleOptions;
    static AppController INSTANCE=null;
    @Override
    public void onCreate() {
        super.onCreate();

        INSTANCE=this;
        typeface=Typeface.createFromAsset(getAssets(),"fonts/RobotoSlab.ttf");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        options=new RequestOptions();
        options.placeholder(R.drawable.progress_drawable)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .sizeMultiplier(0.8f)
                .useAnimationPool(true);
        circleOptions=new RequestOptions();
        circleOptions.placeholder(R.drawable.progress_drawable)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .sizeMultiplier(0.8f)
                .circleCrop()
                .useAnimationPool(true)
                ;

    }


    public static RequestOptions getCircleRequestOptions(){return circleOptions;}
    public static Typeface getTypeface(){return typeface;}
    public static RequestOptions getRequestOptions(){
        return options;
    }
    public static AppController getInstance(){

        return INSTANCE;
    }
}
