package com.singh.multimeet.quicxplo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.singh.multimeet.quicxplo.BreadCrumbsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by multimeet on 4/2/18.
 */

public class BreadCrumbsAdapter extends RecyclerView.Adapter<BreadCrumbsAdapter.BreadCrumbViewHolder> {

    List<String> crumbList=new ArrayList<>();
    StringBuilder completePath=new StringBuilder("");
    String[] INVALID_PATHS=new String[]{"/storage","/storage/emulated"};
    BreadCrumbsListener breadCrumbsListener;
    RecyclerView recyclerView;

    public BreadCrumbsAdapter(String path,RecyclerView recyclerView){
        getCrumbList(path);
        completePath.append(path);
        notifyDataSetChanged();
        this.recyclerView=recyclerView;
    }

    void getCrumbList(String path){
        path=path.substring(1,path.length());
        String[] paths=path.split("/");
        for(String crumb:paths)
            crumbList.add(crumb);
    }

    public void setBreadCrumbsListener(BreadCrumbsListener breadCrumbsListener){
        this.breadCrumbsListener=breadCrumbsListener;
    }
    public void setCrumbList(String path){
        crumbList.clear();
        completePath.delete(0,completePath.length());
        completePath.append(path);
        recyclerView.scrollToPosition(crumbList.size());
        getCrumbList(path);
        notifyDataSetChanged();
    }

    @Override
    public BreadCrumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(com.singh.multimeet.quicxplo.R.layout.bread_crumb_view,parent,false);
        return new BreadCrumbViewHolder(v);
    }

    String getRequiredPath(String name){
        int index=completePath.indexOf(name);
        return completePath.substring(0,index+name.length());
    }

    @Override
    public void onBindViewHolder(BreadCrumbViewHolder holder, int position) {
        holder.name.setText(crumbList.get(position));

        if(breadCrumbsListener!=null){
            holder.root.setOnClickListener(view -> {
                String requiredPath=getRequiredPath(crumbList.get(position));
                if(requiredPath.equals(INVALID_PATHS[0]) || requiredPath.equals(INVALID_PATHS[1]))
                    breadCrumbsListener.onCrumbSelected(null);
                else
                    breadCrumbsListener.onCrumbSelected(getRequiredPath(crumbList.get(position)));
            });
        }
    }



    @Override
    public int getItemCount() {
        return crumbList.size();
    }

    class BreadCrumbViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        ImageView separator;
        RelativeLayout root;
        public BreadCrumbViewHolder(View v){
            super(v);
            name=v.findViewById(com.singh.multimeet.quicxplo.R.id.name);
            separator=v.findViewById(com.singh.multimeet.quicxplo.R.id.separator);
            root=(RelativeLayout)v;
        }
    }
}
