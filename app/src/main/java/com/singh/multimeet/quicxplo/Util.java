package com.singh.multimeet.quicxplo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.annimon.stream.Stream;
import com.singh.fileEx.model.FileDirectory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

/**
 * Created by multimeet on 10/12/17.
 */

public class Util {

    public static final String BASE_URI="base_uri";
    public static final String DIR_DATA="dir_data";
    private static SimpleDateFormat completeDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",Locale.US);
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    static final String TAG=Util.class.getSimpleName();
    static List<String> list=new ArrayList<>();
    public static final int MUSIC_ART=1;
    public static final int VIDEO_ART=2;
    public static final String UNIT_ID="ca-app-pub-9697270530960431/1910084296";
    private static final String PDF="pdf";
    private static final String MP3="mp3";
    private static final String MP4="mp4";
    private static final String FLV="flv";
    private static final String _3GP="3gp";
    private static final String acc="acc";
    private static final String aax="aax";
    private static final String m4a="m4a";
    private static final String wav="wav";
    private static final String webm="webm";
    private static final String ogg="ogg";
    private static final String ZIP="zip";
    public static final String START_UP_FLAG="flag";
    public static final int NAME=0;
    public static final int SIZE=1;
    public static final int DATE=2;
    static Calendar calendar=Calendar.getInstance();

    //  function to get trimmed String
    public static String getTrimmed(String name){
        if(name.length()>16){
            return name.substring(0,15)+"...";
        }
        return name;
    }

    public static String getCompleteDate(String date){
        String finalDate="";
        try {
            Date d = completeDateFormat.parse(date);
            finalDate=completeDateFormat.format(d);
            //finalDate=calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(Calendar.)+" "+calendar.get(Calendar.YEAR);
        }catch (Exception e){
            Log.e(TAG,"date error: "+e.getMessage());
            e.printStackTrace();
        }
        return finalDate;
    }

    public static Date getDateFromPath(String path){
        try{
            String format=completeDateFormat.format(new File(path).lastModified());
            return completeDateFormat.parse(format);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // sorting function for file directory.
    public static List<FileDirectory> sortBy(List<FileDirectory> list,int selection) {
        switch (selection) {

            case NAME:
                Collections.sort(list, (FileDirectory f1, FileDirectory f2) -> {
                    return f1.getName().compareTo(f2.getName());
                    }
                );
                break;

            case SIZE:
                Collections.sort(list, (FileDirectory f1, FileDirectory f2) -> {
                            return f1.getSize().compareTo(f2.getSize());
                        }
                );
                break;

            case DATE:

                Collections.sort(list, new Comparator<FileDirectory>() {
                            @Override
                            public int compare(FileDirectory f1, FileDirectory f2) {
                                Date d1=null,d2=null;
                                try {

                                    d1 = simpleDateFormat.parse(f1.getDate());
                                    d2= simpleDateFormat.parse(f2.getDate());
                                    Log.e(TAG,"d1: "+d1+" d2: "+d2);
                                    return d1.compareTo(d2);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        }
                );
                break;


        }
        return list;
    }


    public static int getImageResIdFromExension(String name){
        String ext= MimeTypeMap.getFileExtensionFromUrl(name.replace(" ",""));
        switch (ext.toLowerCase()){
            case PDF:
                return R.drawable.pdf;

            case ZIP:
                return R.drawable.zip;

            case MP3:
            case acc:
            case aax:
            case m4a:
            case wav:
            case ogg:
            case webm:
                return R.drawable.music;

            case MP4:
            case FLV:
            case _3GP:
                return R.drawable.video;

        }
        return R.drawable.file;
    }

    public static String getParentDirName(String path){
        String[] dir=path.split("/");
        return dir[dir.length-1];
    }

    // Load albumArt.
    public static Flowable<Bitmap> loadAlbumArt(String path,int selection){

        return Flowable.create(new FlowableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(FlowableEmitter<Bitmap> e) throws Exception {
                android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                //Log.e(TAG,"Path: "+path);
                mmr.setDataSource(path);
                byte [] data=null;
                Bitmap bitmap=null;
                if(selection==MUSIC_ART) {
                    data = mmr.getEmbeddedPicture();
                    if(data != null)
                    {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 8;
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
                        e.onNext(bitmap);
                    }
                }
                else if(selection==VIDEO_ART){
                    bitmap=mmr.getFrameAtTime();
                    e.onNext(bitmap);
                }


                e.onComplete();
            }
        },BackpressureStrategy.BUFFER);

    }

    public static Map<String,List<FileDirectory>> listToMap(List<Map.Entry<String,
            List<FileDirectory>>> list){
        Map<String,List<FileDirectory>> resultMap=new HashMap<>();
        for (Map.Entry<String, List<FileDirectory>> ls : list) {
            String key=(String)ls.getKey();
            resultMap.put(ls.getKey(),ls.getValue());
        }
        return resultMap;
    }

    public static Flowable<Map<String,List<FileDirectory>>> getRecentlyAddedFiles(final List<FileDirectory> recentList){
        List<FileDirectory> resultList=new ArrayList<>();
        return Flowable.create(new FlowableOnSubscribe<Map<String,List<FileDirectory>>>() {
            @Override
            public void subscribe(FlowableEmitter<Map<String,List<FileDirectory>>> e) throws Exception {
                Collections.sort(recentList, (l1, l2) -> {
                    try {
                        Date d1 = simpleDateFormat.parse(l1.getDate());
                        Date d2=simpleDateFormat.parse(l2.getDate());
                        return d2.compareTo(d1);
                    }catch (ParseException exp){
                        exp.printStackTrace();
                        return 1;
                    }
                });
                // Collections.sort(recentList,Collections.reverseOrder());
                List<FileDirectory> recentListTmp= Util.timeRemovedList(recentList);
                List<Map.Entry<String,List<FileDirectory>>> finalList=Stream
                        .of(recentListTmp)
                        .groupBy(FileDirectory::getDate)
                        .toList();
                list.clear();
                list=Util.getSortedFileList(finalList);
                Map<String,List<FileDirectory>> resultMap=Util.listToMap(finalList);
                e.onNext(resultMap);
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
    }

    public static List<String> getRecentItemKeys(){
        return list;
    }

    public static List<String> getSortedFileList(List<Map.Entry<String,
            List<FileDirectory>>> list){
        List<Date> dateList=new ArrayList<>();
        List<String> resultList=new ArrayList<>();
        try {
            //Date date = simpleDateFormat.parse("01/01/1980");
            for (Map.Entry<String, List<FileDirectory>> ls : list) {
                String key=(String)ls.getKey();
                dateList.add(simpleDateFormat.parse(key));
            }
            Collections.sort(dateList,Collections.reverseOrder());
            for(Date d:dateList){
                resultList.add(simpleDateFormat.format(d));
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
        return resultList;
    }

    public static DocumentFile getDocumentFile(Context c,String path,String name,Uri baseTreeUri){

        DocumentFile documentFile=DocumentFile.fromTreeUri(c,baseTreeUri);
        Log.e(TAG,"base tree uri: "+baseTreeUri.toString());
        if(path.equals(""))
            return documentFile;
        String[] dirs=path.split("/");
        for(String dir:dirs){
            documentFile=documentFile.findFile(dir);
        }
        if(name==null)
            return documentFile;
        return documentFile.findFile(name);
    }

    /**
     *
     * @param fileDirectoryList
     * List to process
     * @return
     */
    public static List<FileDirectory> timeRemovedList(List<FileDirectory> fileDirectoryList){
        for(int i=0;i<fileDirectoryList.size();i++){
            FileDirectory fileDirectory=fileDirectoryList.get(i);
            fileDirectoryList.get(i).setDate(fileDirectory.getDate().split(" ")[0]);
        }
        return fileDirectoryList;
    }

    public static String getProcessedPath(String path){
        StringBuilder processedPath=new StringBuilder("");
        String[] dirs=path.split("/");
        if(dirs.length<=3){
            return "";
        }
        for(int i=0;i<dirs.length;i++){
            if(i>2){
                processedPath.append(dirs[i]+"/");
            }
        }
        return processedPath.substring(0,processedPath.length()-1);
    }

}
