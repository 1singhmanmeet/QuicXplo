package com.singh.multimeet.quicxplo.model;

/**
 * Created by multimeet on 13/1/18.
 */

public class Storage {
    private String total,free,used,title,path;
    private int percentage;

    public Storage(String title,String total,String free,String used,int percentage,String path){
        this.free=free;
        this.path=path;
        this.title=title;
        this.total=total;
        this.used=used;
        this.percentage=percentage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Storage(){}

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getFree() {
        return free;
    }

    public void setFree(String free) {
        this.free = free;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
