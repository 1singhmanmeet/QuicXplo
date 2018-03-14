package com.singh.multimeet.quicxplo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.SectionIndexer;
import android.widget.Toast;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.singh.fileEx.FileEx;
import com.singh.multimeet.quicxplo.AppController;
import com.singh.fileEx.model.FileDirectory;
import com.singh.multimeet.quicxplo.OnDirectoryChangeListener;
import com.singh.multimeet.quicxplo.OnItemSelectedListener;
import com.singh.multimeet.quicxplo.OnRecyclerItemClickListener;
import com.singh.multimeet.quicxplo.Util;
import com.singh.multimeet.quicxplo.adapter.viewHolder.FileViewHolder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by multimeet on 6/12/17.
 *
 * This adapter is used to display files and directories in browse activity.
 * Here we are handling the Onclick events and the multiple item selection logic is
 * also inside the onBindViewHolder.
 */

public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> implements SectionIndexer {

    List<FileDirectory> fileDirectories=new ArrayList<>();
    List<FileDirectory> selectedList=new ArrayList<>();
    FileEx fileEx;
    OnRecyclerItemClickListener onRecyclerItemClickListener;
    boolean isCopied=false;

    OnDirectoryChangeListener onAdpaterDirectoryChangeListener;
    // Listener to listen for on directory changes.
    OnDirectoryChangeListener onDirectoryChangeListener;
    // Listener to listen for file selection.
    OnItemSelectedListener onItemSelectedListener;
    Context context;
    String[] IMG_TYPES=new String[]{"jpg","png","gif"};
    String[] scrollList=null;
    String selectedColor="";
    Calendar calendar;
    Date date;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    public FilesAdapter(Context context,List<FileDirectory> fileDirectories, FileEx fileEx, OnItemSelectedListener onItemSelectedListener){
        this.fileDirectories=fileDirectories;
        this.fileEx = fileEx;
        this.onItemSelectedListener=onItemSelectedListener;
        onDirectoryChangeListener=new OnDirectoryChangeListener() {
            @Override
            public void onDirectoryChange(List<?> newList) {
                setFileDirectories((List<FileDirectory>)newList);
            }
        };
        this.context=context;
        Arrays.sort(IMG_TYPES);
        selectedColor=this.context.getString(com.singh.multimeet.quicxplo.R.string.selectedColor);
        createScrollList();
        calendar=Calendar.getInstance();
    }

    void createScrollList(){
        scrollList=new String[fileDirectories.size()];
        for(int i=0;i<fileDirectories.size();i++){
            scrollList[i]=fileDirectories.get(i).getName().toUpperCase().charAt(0)+"";
        }
    }

   public void setOnAdpaterDirectoryChangeListener(OnDirectoryChangeListener
                                                           onAdpaterDirectoryChangeListener) {
       this.onAdpaterDirectoryChangeListener = onAdpaterDirectoryChangeListener;
   }

    public void setContext(Context context){
        this.context=context;
    }

    //Method to disable selection mode.

    // Notify Adapter to disable the selection.
    public void disableSelection() {

        this.isCopied=true;
        notifyDataSetChanged();
    }

    // Clear the selected list.
    public void clearSelectedList(){ this.selectedList.clear();}

    void setFileDirectories(List<FileDirectory> list){
        this.fileDirectories=list;
        notifyDataSetChanged();
     }


    // listener to tell activity when file structure is changed with
    // some user operations like rename, delete, cut, copy .etc
    public OnDirectoryChangeListener getOnDirectoryChangeListener(){
        return onDirectoryChangeListener;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(com.singh.multimeet.quicxplo.R.layout.file_directory_view,parent,false);
        return new FileViewHolder(v);
    }



    @Override
    public void onBindViewHolder(FileViewHolder holder, int position) {
        holder.name.setText(Util.getTrimmed(fileDirectories.get(position).getName()));
        holder.info.setText(fileDirectories.get(position).getDate());
        if(selectedList.size()>0 && selectedList.contains(fileDirectories.get(position))){
            holder.mainView.setBackground(new ColorDrawable(Color.parseColor(selectedColor)));
        }else{
            holder.mainView.setBackground(new ColorDrawable(Color.WHITE));
        }

        // when user long clicks on any list item add it to selection list and notify activity.
        holder.mainView.setOnLongClickListener((view)->{
                selectedList.add(fileDirectories.get(position));
                onItemSelectedListener.onItemListChanged(selectedList);
                holder.mainView.setBackground(new ColorDrawable(Color.parseColor(selectedColor)));
            return true;
        });

        // on clicking item
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorDrawable colorDrawable=(ColorDrawable) holder.mainView.getBackground();

                try {
                    if(colorDrawable!=null) {

                        // Checking whether the clicked item is present in selected list.
                        // Remove if yes
                        if (colorDrawable.getColor() == Color.parseColor(selectedColor) && !isCopied) {
                            selectedList.remove(fileDirectories.get(position));
                            onItemSelectedListener.onItemListChanged(selectedList);
                            holder.mainView.setBackground(null);
                            return;
                        }
                    }

                    // If selection mode is enabled add item to list notify the activity.
                    if(!isCopied &&  selectedList.size()>0){

                        selectedList.add(fileDirectories.get(position));
                        onItemSelectedListener.onItemListChanged(selectedList);
                        holder.mainView.setBackground(new ColorDrawable(Color.parseColor(selectedColor)));
                        return;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                // if clicked item is a directory or folder open it and list files.
                if(fileDirectories.get(position).getFileOrDir()== FileDirectory.DIR){
                    fileEx.openDir(fileDirectories.get(position).getName());
                    loadFiles();
                    // call back when opening new dir to update breadcrumbs
                    onAdpaterDirectoryChangeListener.onDirectoryChange(fileDirectories);
                    notifyDataSetChanged();
                }

                // if its a file then open it.
                else{
                    Intent i=fileEx.getOpenableIntent(fileDirectories.get(position).getName());
                    if(i!=null)
                        context.startActivity(i);
                    else
                        Toast.makeText(context.getApplicationContext(),context
                                .getResources()
                                .getString(com.singh.multimeet.quicxplo.R.string.no_app_available),Toast.LENGTH_SHORT).show();
                }
            }
        });

        String name=fileDirectories.get(position).getName();

        // getting tye of file to be opened.
        String ext=MimeTypeMap.getFileExtensionFromUrl(name);
        if(fileDirectories.get(position).getFileOrDir()== FileDirectory.DIR){
            holder.size.setText("");
            holder.icon.setImageResource(com.singh.multimeet.quicxplo.R.drawable.folder);
            return;
        }

        // If current item is an image.
        else if(ext.length() <= 3 && (ext.equals(IMG_TYPES[0]) || ext.equals(IMG_TYPES[1])
                || ext.equals(IMG_TYPES[2]))){
            holder.size.setText(fileDirectories.get(position).getSize());
            Glide.with(context).load(Uri.fromFile(new File(fileDirectories
                    .get(position).getPath()))).apply(AppController.getRequestOptions())
                    .transition(GenericTransitionOptions.with(com.singh.multimeet.quicxplo.R.anim.item_fall_down))
                    .into(holder.icon);
        }


        // If current item is not a directory.
        else {
            holder.size.setText(fileDirectories.get(position).getSize());
            holder.icon.setImageResource(Util.getImageResIdFromExension(fileDirectories
                    .get(position).getName()));
        }

    }

    void loadFiles(){
        fileDirectories.clear();
        for(String s : fileEx.listFiles()){
            if(fileEx.isFile(s))
                fileDirectories.add(new FileDirectory(s,FileDirectory.FILE
                        , fileEx.getFileSize(s),fileEx.getInfo(s),fileEx.getFilePath(s)));
            else
                fileDirectories.add(new FileDirectory(s,FileDirectory.DIR
                        , fileEx.getFileSize(s),fileEx.getInfo(s),fileEx.getFilePath(s)));
        }

    }

    @Override
    public int getItemCount()
    {

        return fileDirectories.size();
    }

    @Override
    public Object[] getSections() {
        return scrollList;
    }

    @Override
    public int getPositionForSection(int i) {
        return i;
    }

    @Override
    public int getSectionForPosition(int i) {
        return i;
    }
}
