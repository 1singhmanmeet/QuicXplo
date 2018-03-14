package com.singh.multimeet.quicxplo.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.ui.browse.home.Home;

/**
 * Created by multimeet on 17/2/18.
 */
public class IntroFragment extends Fragment {

    private static final String BACKGROUND_COLOR = "backgroundColor";
    private static final String PAGE = "page";

    private int mBackgroundColor, mPage;

    public static IntroFragment newInstance(int backgroundColor, int page) {
        IntroFragment frag = new IntroFragment();
        Bundle b = new Bundle();
        b.putInt(BACKGROUND_COLOR, backgroundColor);
        b.putInt(PAGE, page);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().containsKey(BACKGROUND_COLOR))
            throw new RuntimeException("Fragment must contain a \"" + BACKGROUND_COLOR + "\" argument!");
        mBackgroundColor = getArguments().getInt(BACKGROUND_COLOR);

        if (!getArguments().containsKey(PAGE))
            throw new RuntimeException("Fragment must contain a \"" + PAGE + "\" argument!");
        mPage = getArguments().getInt(PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Select a layout based on the current page
        int layoutResId=0;
        switch (mPage) {
            case 0:
                layoutResId = R.layout.intro_frag_1;
                break;

            case 1:
                layoutResId= com.singh.multimeet.quicxplo.R.layout.intro_frag_2;
                break;

            case 2:
                layoutResId = com.singh.multimeet.quicxplo.R.layout.intro_frag_3;
                break;

        }

        // Inflate the layout resource file
        View view = getActivity().getLayoutInflater().inflate(layoutResId, container, false);

        // Set the current page index as the View's tag (useful in the PageTransformer)
        view.setTag(mPage);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the background color of the root view to the color specified in newInstance()
        View background = view.findViewById(com.singh.multimeet.quicxplo.R.id.intro_background);
        background.setBackgroundColor(mBackgroundColor);
        TextView title=view.findViewById(com.singh.multimeet.quicxplo.R.id.title);
        title.setTypeface(AppController.getTypeface());

        if(mPage==2&& getActivity()!=null){
            Button finish=view.findViewById(com.singh.multimeet.quicxplo.R.id.description);
            finish.setTypeface(AppController.getTypeface());
            finish.setOnClickListener(view1 -> {
                getActivity().getSharedPreferences(Util.DIR_DATA, Context.MODE_PRIVATE)
                    .edit().putString(Util.START_UP_FLAG,"1").apply();
                Intent i=new Intent(getActivity(),Home.class);
                getActivity().startActivity(i);
                getActivity().finish();
            });
        }else{
            TextView des=view.findViewById(com.singh.multimeet.quicxplo.R.id.description);
            des.setTypeface(AppController.getTypeface());

        }
    }

}

