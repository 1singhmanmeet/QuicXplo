package com.singh.multimeet.quicxplo.ui.browse.intro;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.singh.multimeet.quicxplo.IntroPageTransformer;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.adapter.IntroAdapter;

public class Intro extends AppCompatActivity {

    ViewPager viewPager;
    static final int READ_WRITE=22;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        viewPager = findViewById(R.id.viewPager);

        // Set an Adapter on the ViewPager
        viewPager.setAdapter(new IntroAdapter(getSupportFragmentManager()));

        // Set a PageTransformer
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        checkPermissions();
    }
    public void checkPermissions(){
        if(Build.VERSION.SDK_INT<23)
            return;
        if(ContextCompat.checkSelfPermission(this
                , Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this
                            ,Manifest.permission.READ_EXTERNAL_STORAGE)){

            }
            else{
                ActivityCompat.requestPermissions(this
                        ,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.READ_EXTERNAL_STORAGE},READ_WRITE);

            }

        }
    }
}
