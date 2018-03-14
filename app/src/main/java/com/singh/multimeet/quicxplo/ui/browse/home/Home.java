package com.singh.multimeet.quicxplo.ui.browse.home;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.singh.fileEx.FileEx;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.OnDataLoadListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.QuickAccessAdapter;
import com.singh.multimeet.quicxplo.adapter.RecentFilesAdapter;
import com.singh.multimeet.quicxplo.adapter.StorageAdapter;
import com.singh.multimeet.quicxplo.model.Storage;
import com.singh.multimeet.quicxplo.ui.browse.Browse;
import com.singh.multimeet.quicxplo.ui.browse.quickAccess.QuickAccess;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Home extends AppCompatActivity implements OnRecyclerItemClickListener {



    Intent intent;
    TextView storage,recents;
    FileEx fileEx;
    RecyclerView  storageDevices,quickAccess,recentFilesView;
    List<Storage> storageList=new ArrayList<>();
    Map<String,File> mountedDevices=null;
    double total,used;
    StorageAdapter storageAdapter;
    List<String> quickAccessList=new ArrayList<>();
    List<FileDirectory> recentList=new ArrayList<>();
    Map<String,List<FileDirectory>> recentItemsMap=new LinkedHashMap<>();
    QuickAccessAdapter quickAccessAdapter;
    ProgressBar recentsProgressBar;
    LinearLayoutManager recentListLayoutManager;

    public static final String IMAGES="Images";
    public static final String AUDIO="Audio";
    public static final String VIDEO="Videos";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
    public static final String DOCUMENTS="Documents";
    RecentFilesAdapter recentFilesAdapter;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    private static final String TAG=Home.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setTitle(getResources().getString(R.string.app_name));
        collapsingToolbarLayout.setExpandedTitleMarginBottom(20);
        collapsingToolbarLayout.setExpandedTitleMargin(20,20,0,20);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedQuick);
        collapsingToolbarLayout.setExpandedTitleTypeface(AppController.getTypeface());

        // checking permissions
        storage=findViewById(R.id.storage);
        recents=findViewById(R.id.recents);
        storageDevices=findViewById(R.id.storageRV);
        storageDevices.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        fileEx=FileEx.newFileManager(Environment.getExternalStorageDirectory().toString(),this);
        quickAccess=findViewById(R.id.quick);
        quickAccess.setLayoutManager(new GridLayoutManager(this,2));
        recentFilesView=findViewById(R.id.recentList);
        recentListLayoutManager=new LinearLayoutManager(this);
        recentFilesView.setHasFixedSize(true);
        recentFilesView.setLayoutManager(recentListLayoutManager);
        recentsProgressBar=findViewById(R.id.progressBar);
        setRecentList();
        setTypeFace();
        mountStorage();
        setQuickAccessList();

    }


    public void setRecentList(){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -7);
           // Log.d(TAG,simpleDateFormat.format(calendar.getTime())+"");
            fileEx.findwithDate(Environment.getExternalStorageDirectory().toString(), simpleDateFormat.parse(simpleDateFormat
                    .format(calendar.getTime())))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<List<FileDirectory>>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(List<FileDirectory> fileDirectoryList) {
                            recentList=fileDirectoryList;

                            Log.e(TAG,"name: "+recentList.size());
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG,t.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            setOrRefreshRecentItems(recentList);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void setOrRefreshRecentItems(List<FileDirectory> recentItemList){
        Util.getRecentlyAddedFiles(recentItemList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<Map<String,List<FileDirectory>>>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Map<String,List<FileDirectory>> fileDirectoryMap) {
                        recentItemsMap=fileDirectoryMap;
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG,"Recent List Error: "+t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        List<String> keyList=Util.getRecentItemKeys();
                        recentFilesAdapter=new RecentFilesAdapter(Home.this
                                ,recentItemsMap,keyList, Glide.with(Home.this));
                        setRecentItemListener();
                        recentFilesAdapter.setOnDataLoadListener(new OnDataLoadListener() {
                            @Override
                            public void onDataLoaded(boolean loaded) {
                                if(recentsProgressBar.getVisibility()==View.VISIBLE)
                                    recentsProgressBar.setVisibility(View.GONE);
                            }
                        });
                        recentFilesView.setAdapter(recentFilesAdapter);
                    }
                });
    }

    void setRecentItemListener(){
        recentFilesAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent i=fileEx.getAbsoluteOpenableIntent(recentFilesAdapter.getTotalList()
                        .get(position).getPath());
                startActivity(i);
            }

            @Override
            public void onLongClick(View view, int position) {
                Dialog menuDialog=new Dialog(Home.this);
                menuDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                menuDialog.getWindow().setBackgroundDrawableResource(R.drawable.curved_back);
                menuDialog.setContentView(R.layout.options_layout);
                TextView open=menuDialog.findViewById(R.id.open);
                TextView share=menuDialog.findViewById(R.id.share);
                open.setTypeface(AppController.getTypeface());
                share.setTypeface(AppController.getTypeface());
                open.setOnClickListener(view1 -> {
                    String parent_dir=new File(recentFilesAdapter.getTotalList().get(position)
                            .getPath()).getParent();
                    Intent i=new Intent(Home.this,Browse.class);
                  //  Log.e(TAG,"parent dir: "+parent_dir);
                    i.putExtra(getResources().getString(R.string.dir_reference),parent_dir);
                    menuDialog.dismiss();
                    startActivity(i);
                    //menuDialog.dismiss();
                });

                share.setOnClickListener(view1 -> {
                    String path=recentFilesAdapter.getTotalList().get(position).getPath();

                    ArrayList<Uri> uris=new ArrayList<>(1);
                    uris.add(Uri.fromFile(new File(path)));

                    final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    intent.setType("*/*");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    menuDialog.dismiss();
                    startActivity(Intent.createChooser(intent,"Send"));

                });
                menuDialog.show();
            }
        });
    }

    void setQuickAccessList(){
        quickAccessList.add(IMAGES);
        quickAccessList.add(AUDIO);
        quickAccessList.add(VIDEO);
        quickAccessList.add(DOCUMENTS);
        quickAccessAdapter=new QuickAccessAdapter(quickAccessList);
        quickAccess.setAdapter(quickAccessAdapter);
        Intent i=new Intent(this,QuickAccess.class);
        quickAccessAdapter.setOnRecyclerItemClickListener(new OnRecyclerItemClickListener() {

            @Override
            public void onClick(View view, int position) {
                int selection=0;
                switch (quickAccessList.get(position)){
                    case IMAGES:
                        selection= QuickAccess.IMAGES;
                        break;

                    case AUDIO:
                        selection=QuickAccess.AUDIO;
                        break;

                    case VIDEO:
                        selection=QuickAccess.VIDEO;
                        break;

                    case DOCUMENTS:
                        selection=QuickAccess.DOCUMENTS;
                        break;

                    default:
                        selection=QuickAccess.IMAGES;
                }

                i.putExtra("select_data",selection);
                startActivity(i);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        });

    }

    void mountStorage(){
        mountedDevices=FileEx.getAllStorageLocations();
        Iterator iterator=mountedDevices.keySet().iterator();
        String key="";
        while(iterator.hasNext()){
            key=""+iterator.next();
            fileEx.changeRootDirectory(mountedDevices.get(key).getAbsolutePath());
            total=Double.parseDouble(fileEx.getTotalRootSpace().split(" ")[0]);
            used=Double.parseDouble(fileEx.getUsedRootSpace().split(" ")[0]);
            storageList.add(new Storage(key,String.format(getResources().getString(R.string.default_total_text),
                    fileEx.getTotalRootSpace()),String.format(getResources().getString(R.string.default_free_text),
                    fileEx.getFreeRootSpace()),String.format(getResources().getString(R.string.default_used_text),
                    fileEx.getUsedRootSpace()),getPercentage(total,used),mountedDevices.get(key).getAbsolutePath()));
        }
        storageAdapter=new StorageAdapter(storageList,this);
        storageDevices.setAdapter(storageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        storageList.clear();
        mountStorage();

    }

    @Override
    protected void onResume() {
        super.onResume();
        storageList.clear();
        mountStorage();
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

    public int getPercentage(double total,double used){
        return (int)((used/total)*100);
    }


    @Override
    public void onClick(View view, int position) {
        intent=new Intent(this,Browse.class);
        fileEx.changeRootDirectory(storageList.get(position).getPath());
        fileEx.setCurrentDir(storageList.get(position).getPath());
        intent.putExtra(getResources().getString(R.string.dir_reference),
                storageList.get(position).getPath());
        startActivity(intent);
    }

    @Override
    public void onLongClick(View view, int position) {

    }
}
