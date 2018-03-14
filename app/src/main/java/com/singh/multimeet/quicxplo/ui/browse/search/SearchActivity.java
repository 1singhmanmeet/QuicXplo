package com.singh.multimeet.quicxplo.ui.browse.search;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.R;
import com.singh.fileEx.FileEx;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;

import com.singh.multimeet.quicxplo.adapter.FilesAdapter;
import com.singh.multimeet.quicxplo.adapter.SearchListAdapter;
import com.singh.multimeet.quicxplo.ui.browse.Browse;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity implements OnRecyclerItemClickListener {

    FileEx fileEx;
    RecyclerView searchRecyclerView;
    List<FileDirectory> fileDirectoryList,searchResultList;
    FilesAdapter filesAdapter;
    SearchListAdapter searchListAdapter;
    int dirLevel=0;
    ProgressBar progressBar;
    Button back;
    TextView errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        progressBar=findViewById(R.id.progress);
        searchRecyclerView=findViewById(R.id.searchRecycler);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        back=findViewById(R.id.back);
        back.setOnClickListener(view -> {
            onBackPressed();
        });
        errorText=findViewById(R.id.errorText);
        fileEx=FileEx.newFileManager(Environment.getExternalStorageDirectory().toString(),this);
        handleIntent(getIntent());
    }

    List<FileDirectory> getFileList(List<String> fileList){

        fileDirectoryList.clear();
        for(String s : fileEx.listFiles()){
            if(fileEx.isFile(s))
                fileDirectoryList.add(new FileDirectory(s,FileDirectory.FILE
                        , fileEx.getFileSize(s),fileEx.getInfo(s),fileEx.getFilePath(s)));
            else
                fileDirectoryList.add(new FileDirectory(s,FileDirectory.DIR
                        , fileEx.getFileSize(s),fileEx.getInfo(s),fileEx.getFilePath(s)));
        }
        return fileDirectoryList;
    }

    Intent openFile(String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(path));

        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap
                .getFileExtensionFromUrl(uri.toString()));
        intent.setDataAndType(uri, type);
        if (intent.resolveActivity(getPackageManager()) == null)
            return null;
        return intent;
    }

    @Override
    public void onBackPressed() {

        if(dirLevel==1){
            searchListAdapter.setFileDirectoryList(searchResultList);
            searchListAdapter.notifyDataSetChanged();
            dirLevel--;
        }

        else if(dirLevel>1){
            fileEx.goUp();
            searchListAdapter.setFileDirectoryList(getFileList(fileEx.listFiles()));
            searchListAdapter.notifyDataSetChanged();
            dirLevel--;
        }

        else{
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        errorText.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            fileEx.find(Environment.getExternalStorageDirectory().toString(),query)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<List<FileDirectory>>() {

                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(List<FileDirectory> fileDirectories) {
                            searchResultList=fileDirectories;
                            fileDirectoryList=searchResultList;
                            //Toast.makeText(getApplicationContext(),"search list size: "+fileDirectories.size(),Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable t) {
                            Log.e("SearchActivity","Error message: "+t.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            if(searchResultList.size()==0) {
                                errorText.setVisibility(View.VISIBLE);
                                back.setVisibility(View.VISIBLE);
                            }
                            searchListAdapter=new SearchListAdapter(searchResultList,SearchActivity.this);
                            progressBar.setVisibility(View.GONE);
                            searchRecyclerView.setAdapter(searchListAdapter);
                            searchListAdapter.setOnRecyclerItemClickListener(SearchActivity.this);
                            //Toast.makeText(getApplicationContext(),"Completed ",Toast.LENGTH_SHORT).show();
                        }
                    });
            //Toast.makeText(getApplicationContext(), "Search is working", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view, int position) {
        if(fileDirectoryList.get(position).getFileOrDir()==FileDirectory.FILE){
            Intent i=openFile(fileDirectoryList.get(position).getPath());
            if(i!=null){
                startActivity(i);
            }
        }
        else{
            fileEx.setCurrentDir(fileDirectoryList.get(position).getPath());
            fileEx.openDir(fileDirectoryList.get(position).getName());
            dirLevel++;
            getFileList(fileEx.listFiles());
            searchListAdapter.setFileDirectoryList(fileDirectoryList);
            searchListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLongClick(View view, int position) {
        Dialog menuDialog=new Dialog(SearchActivity.this);
        menuDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        menuDialog.getWindow().setBackgroundDrawableResource(R.drawable.curved_back);
        menuDialog.setContentView(R.layout.options_layout);
        TextView open=menuDialog.findViewById(R.id.open);
        TextView share=menuDialog.findViewById(R.id.share);
        open.setTypeface(AppController.getTypeface());
        share.setTypeface(AppController.getTypeface());
        open.setOnClickListener(view1 -> {
            String parent_dir=new File(fileDirectoryList.get(position)
                    .getPath()).getParent();
            Intent i=new Intent(SearchActivity.this,Browse.class);
            //Log.e(TAG,"parent dir: "+parent_dir);
            i.putExtra(getResources().getString(R.string.dir_reference),parent_dir);
            menuDialog.dismiss();
            startActivity(i);
        });

        share.setOnClickListener(view1 -> {
            String path=fileDirectoryList.get(position).getPath();

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
}
