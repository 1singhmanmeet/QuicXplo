package com.singh.multimeet.quicxplo.adapter;



import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.singh.multimeet.quicxplo.fragment.IntroFragment;

/**
 * Created by multimeet on 17/2/18.
 */

public class IntroAdapter extends FragmentPagerAdapter {

    public IntroAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
            case 1:
            case 2:
                return IntroFragment.newInstance(Color.parseColor("#007DD6"), position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

}
