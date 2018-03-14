package com.singh.multimeet.quicxplo.ui.browse.quickAccess;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.singh.fileEx.FileEx;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.OnItemSelectedListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.R;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.MediaAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class QuickAccess extends AppCompatActivity implements OnRecyclerItemClickListener,OnItemSelectedListener {

    public static final int IMAGES=11;
    public static final int AUDIO=12;
    public static final int VIDEO=13;
    public static final int DOCUMENTS=14;
    private final static String TAG=QuickAccess.class.getSimpleName();
    FileEx fileEx;
    List<FileDirectory> fileList=new ArrayList<>();
    RecyclerView mediaRV;
    InterstitialAd interstitialAd;
    CollapsingToolbarLayout collapsingToolbarLayout;
    MediaAdapter mediaAdapter;
    ImageView headImage;
    FloatingActionButton sort
            ,share
            ,delete;
    int currentSortOrder=R.id.name;
    GridLayoutManager gridLayoutManager;
    ProgressBar progressBar;
    Subscription mediaSubscription, documentSubscription;
    List<FileDirectory> selectionList=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_access);
        setSupportActionBar(findViewById(R.id.toolbar));

        // creating ads
        MobileAds.initialize(this);
        interstitialAd=new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.UNIT_ID));
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                    Log.e(TAG,"failed to load ad...");
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.

               // onNewIntent(getIntent());
            }

        });
        interstitialAd.loadAd(new AdRequest.Builder().build());

        progressBar=findViewById(R.id.progress);

        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);
        fileEx=FileEx.newFileManager(Environment.getExternalStorageDirectory().toString(),this);
        mediaRV=findViewById(R.id.mediaRV);
        onNewIntent(getIntent());
        sort=findViewById(R.id.sort);
        share=findViewById(R.id.share);
        delete=findViewById(R.id.delete);
        sort.setVisibility(View.GONE);


        gridLayoutManager=new GridLayoutManager(this,3);
        mediaRV.setLayoutManager(gridLayoutManager);
        mediaRV.setHasFixedSize(true);
        setFilterButtonListener();
        setOptionsButtonListener();
        setDeleteButtonListener();

        //handleIntent(getIntent());
    }

    private void setMediaAdapter(int selectData){
        mediaAdapter=new MediaAdapter(fileList,QuickAccess.this,selectData, Glide.with(QuickAccess.this));
        mediaAdapter.setOnItemSelectedListener(QuickAccess.this);
        mediaAdapter.setOnRecyclerItemClickListener(QuickAccess.this);
        mediaRV.setAdapter(mediaAdapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(documentSubscription!=null)
            documentSubscription.cancel();
        if(mediaSubscription!=null)
            mediaSubscription.cancel();
    }

    // listener for delete button.
    public void setDeleteButtonListener(){

        delete.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setCancelable(false).setTitle("Delete")
                    .setMessage("Do you want to delete...?")
                    .setPositiveButton("Yup", (dialogInterface, i) -> {

                        for (FileDirectory fileDirectory : selectionList) {
                            fileEx.delete(fileDirectory.getPath());
                            fileList.remove(fileDirectory);
                        }

                        Toast.makeText(getApplicationContext(), "Files deleted.", Toast.LENGTH_SHORT).show();

                        mediaAdapter.disableSelection();
                        mediaAdapter.notifyDataSetChanged();

                    }).setNegativeButton("No", ((dialogInterface, i) -> {
            })).show();

        });
    }

    // listener for options button
    void setOptionsButtonListener(){
        // share button listener
        share.setOnClickListener(view -> {
            if(selectionList.size()>0){
                ArrayList<Uri> uris = new ArrayList<>();

                for (FileDirectory file : selectionList) {
                    uris.add(Uri.fromFile(new File(file.getPath())));
                }

                final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                startActivity(Intent.createChooser(intent, "Send"));
            }
        });


    }

    @Override
    public void onBackPressed() {
        if(selectionList.size()>0){
            mediaAdapter.disableSelection();
        }
        else{
            super.onBackPressed();
        }
    }

    void setFilterButtonListener(){
        sort.setOnClickListener((View view) -> {
            Dialog sortDialog=new Dialog(this);
            sortDialog.getWindow().setBackgroundDrawableResource(R.drawable.curved_back);
            sortDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            sortDialog.setContentView(R.layout.sort_dialog_view);

            RadioButton name=sortDialog.findViewById(R.id.name);
            RadioButton size=sortDialog.findViewById(R.id.size);
            RadioButton date=sortDialog.findViewById(R.id.date);

            switch (currentSortOrder){

                case  R.id.name:
                    name.setChecked(true);
                    break;
                case R.id.size:
                    size.setChecked(true);
                    break;
                case R.id.date:
                    date.setChecked(true);
            }

            RadioGroup group=sortDialog.findViewById(R.id.group);
            group.setOnCheckedChangeListener((radioGroup, i) -> {
                switch (radioGroup.getCheckedRadioButtonId()){

                    case R.id.name:
                        mediaAdapter.setFileDirectoryList(Util.sortBy(fileList,Util.NAME));

                        break;

                    case R.id.size:
                        mediaAdapter.setFileDirectoryList(Util.sortBy(fileList,Util.SIZE));
                        break;

                    case R.id.date:
                        mediaAdapter.setFileDirectoryList(Util.sortBy(fileList,Util.DATE));
                        break;
                }
                currentSortOrder=group.getCheckedRadioButtonId();
                sortDialog.dismiss();
            });
            Button cancel=sortDialog.findViewById(R.id.cancel);
            cancel.setOnClickListener(view1 -> sortDialog.dismiss());
            sortDialog.show();
        });
    }

    private void handleIntent(Intent intent){

        int selectData=intent.getExtras().getInt("select_data");
        if(selectData==QuickAccess.DOCUMENTS){
           loadDocuments();

        }else{

            loadMedia(selectData)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<FileDirectory>>() {

                        @Override
                        public void onSubscribe(Subscription s) {
                            mediaSubscription=s;
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(List<FileDirectory> fileDirectoryList) {

                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.e(TAG,"error: "+t.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            fileList=Util.sortBy(fileList,Util.NAME);
                            mediaAdapter=new MediaAdapter(fileList,QuickAccess.this,selectData, Glide.with(QuickAccess.this));
                            mediaAdapter.setOnItemSelectedListener(QuickAccess.this);
                            mediaAdapter.setOnRecyclerItemClickListener(QuickAccess.this);
                            mediaRV.setAdapter(mediaAdapter);
                            progressBar.setVisibility(View.GONE);
                            sort.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    Flowable<List<FileDirectory>> loadMedia(final int select){
        return Flowable.create(new FlowableOnSubscribe<List<FileDirectory>>() {
            @Override
            public void subscribe(FlowableEmitter<List<FileDirectory>> e) throws Exception {
                fileList=getAllMediaPath(QuickAccess.this,e,select);
                e.onComplete();
            }

        }, BackpressureStrategy.BUFFER);
    }

    void loadDocuments(){

        fileEx.find(Environment.getExternalStorageDirectory().toString(),FileEx.DOC_SELECTOR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<FileDirectory>>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        documentSubscription=s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(List<FileDirectory> fileDirectories) {
                        fileList=fileDirectories;
                        //Toast.makeText(getApplicationContext(),"search list size: "+fileDirectories.size(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("SearchActivity","Error message: "+t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        fileList=Util.sortBy(fileList,Util.NAME);
                        mediaAdapter=new MediaAdapter(fileList,QuickAccess.this,QuickAccess.DOCUMENTS,Glide.with(QuickAccess.this));
                        mediaAdapter.setOnItemSelectedListener(QuickAccess.this);
                        mediaAdapter.setOnRecyclerItemClickListener(QuickAccess.this);
                        mediaRV.setAdapter(mediaAdapter);
                        progressBar.setVisibility(View.GONE);
                        sort.setVisibility(View.VISIBLE);
                    }
                });
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        headImage=findViewById(R.id.image);
        int name=intent.getExtras().getInt("select_data");
        switch (name){

            case QuickAccess.IMAGES:
                collapsingToolbarLayout.setTitle("Images");
                headImage.setImageResource(R.drawable.picture);
                break;

            case AUDIO:
                collapsingToolbarLayout.setTitle("Music");
                headImage.setImageResource(R.drawable.music);
                break;

            case VIDEO:
                collapsingToolbarLayout.setTitle("Videos");
                headImage.setImageResource(R.drawable.video);
                break;

            case DOCUMENTS:
                collapsingToolbarLayout.setTitle("Documents");
                headImage.setImageResource(R.drawable.file);
                break;


        }
        collapsingToolbarLayout.setExpandedTitleGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        collapsingToolbarLayout.setExpandedTitleMargin(20,20,0,20);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedQuick);
        collapsingToolbarLayout.setExpandedTitleTypeface(AppController.getTypeface());
        handleIntent(intent);
    }


    private List<FileDirectory> getAllMediaPath(Activity activity, FlowableEmitter<List<FileDirectory>> e,int selection) {
        Uri uri=null;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        List<FileDirectory> list = new ArrayList<>();
        String absolutePathOfImage, fileName;
        String[] projection={};
        if(selection==AUDIO) {
            uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            projection=new String[]{ MediaStore.MediaColumns.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME};
            Log.e("Test","Audio");
        }else if(selection==IMAGES){
            uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{ MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME };
            Log.e("Test","Images");
        }else if(selection==VIDEO){
            uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            projection = new String[]{ MediaStore.Video.VideoColumns.DATA ,
                    MediaStore.Video.Media.DISPLAY_NAME};
            Log.e("Test","Video");
        }

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(projection[0]);
        column_index_folder_name = cursor.getColumnIndexOrThrow(projection[1]);
        cursor.moveToFirst();

        do{
            absolutePathOfImage = cursor.getString(column_index_data);
            fileName=cursor.getString(column_index_folder_name);
            if(!new File(absolutePathOfImage).exists())
                continue;
            list.add(new FileDirectory(fileName,FileDirectory.FILE,
                fileEx.getAbsoluteFileSize(absolutePathOfImage),
                    fileEx.getAbsoluteInfo(absolutePathOfImage),absolutePathOfImage));
            e.onNext(list);
        }while (cursor.moveToNext());

        return list;
    }

    @Override
    public void onClick(View view, int position) {
        Intent i=fileEx.getAbsoluteOpenableIntent(fileList.get(position).getPath());
        startActivity(i);
    }

    @Override
    public void onLongClick(View view, int position) {

    }

    @Override
    public void onItemListChanged(List<FileDirectory> list) {
        selectionList=list;
        if(selectionList.size()>0){
            share.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
        }else{
            share.setVisibility(View.GONE);
            delete.setVisibility(View.GONE);
        }
    }
}
