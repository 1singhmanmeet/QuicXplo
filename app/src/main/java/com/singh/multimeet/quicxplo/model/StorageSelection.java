package com.singh.multimeet.quicxplo.model;

/**
 * Created by multimeet on 22/1/18.
 */

public class StorageSelection {

    String path,title;

    public StorageSelection(String path, String title){
        this.path=path;
        this.title=title;

    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
