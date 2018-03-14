package com.singh.multimeet.quicxplo.ui.browse.splash;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.ui.browse.home.Home;
import com.singh.multimeet.quicxplo.ui.browse.intro.Intro;
import com.singh.multimeet.quicxplo.ui.browse.quickAccess.QuickAccess;

import java.util.Arrays;

public class SplashScreen extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    ImageView logo;
    TextView textView;
    ShortcutManager shortcutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setAppShortcuts();
        logo=findViewById(R.id.logo);
        textView=findViewById(R.id.text);
        logo.setAlpha(0.0f);
        textView.setAlpha(0.0f);
        logo.animate().alpha(1.0f).setDuration(1000);
        textView.animate().alpha(1.0f).setDuration(2000).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {



                if(sharedPreferences.getString(Util.START_UP_FLAG,"").equals("")){
                    Intent i=new Intent(SplashScreen.this, Intro.class);
                    startActivity(i);
                    finish();
                }else{
                    Intent i=new Intent(SplashScreen.this,Home.class);
                    startActivity(i);
                    finish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(4000);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }).start();
        sharedPreferences=getSharedPreferences(Util.DIR_DATA, Context.MODE_PRIVATE);

    }

    void setAppShortcuts(){
        if(Build.VERSION.SDK_INT>=25) {
            shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager.getDynamicShortcuts().size() == 0) {
                Intent i = new Intent(SplashScreen.this, QuickAccess.class);
                i.setAction(Intent.ACTION_MAIN);
                i.putExtra("select_data", QuickAccess.IMAGES);
                ShortcutInfo imageShortcut = new ShortcutInfo.Builder(SplashScreen.this, "images")
                        .setShortLabel("Images")
                        .setLongLabel("Explore images")
                        .setIcon(Icon.createWithResource(SplashScreen.this, R.drawable.picture))
                        .setIntent(i)
                        .build();

                shortcutManager.setDynamicShortcuts(Arrays.asList(imageShortcut));

                i.putExtra("select_data", QuickAccess.AUDIO);
                ShortcutInfo musicShortcut = new ShortcutInfo.Builder(SplashScreen.this, "music")
                        .setShortLabel("Music")
                        .setLongLabel("Explore music")
                        .setIcon(Icon.createWithResource(SplashScreen.this, R.drawable.music))
                        .setIntent(i)
                        .build();
                shortcutManager.addDynamicShortcuts(Arrays.asList(musicShortcut));

                i.putExtra("select_data", QuickAccess.VIDEO);
                ShortcutInfo videoShortcut = new ShortcutInfo.Builder(SplashScreen.this, "video")
                        .setShortLabel("Videos")
                        .setLongLabel("Explore videos")
                        .setIcon(Icon.createWithResource(SplashScreen.this, R.drawable.video))
                        .setIntent(i)
                        .build();
                shortcutManager.addDynamicShortcuts(Arrays.asList(videoShortcut));

                i.putExtra("select_data", QuickAccess.DOCUMENTS);
                ShortcutInfo documentShortcut = new ShortcutInfo.Builder(SplashScreen.this, "document")
                        .setShortLabel("Documents")
                        .setLongLabel("Explore documents")
                        .setIcon(Icon.createWithResource(SplashScreen.this, R.drawable.file))
                        .setIntent(i)
                        .build();
                shortcutManager.addDynamicShortcuts(Arrays.asList(documentShortcut));
            }
        }
    }
}
