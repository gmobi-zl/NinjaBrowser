package io.github.mthli.Ninja.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.mthli.Ninja.Database.Record;
import io.github.mthli.Ninja.Database.RecordAction;
import io.github.mthli.Ninja.R;

/**
 * Created by zl on 2017/8/16.
 */

public class BookmarksUtil {

    Context mContext;
    private static BookmarksUtil instance = null;

    private static final String BASE_GET_FAVICONS_URL = "http://www.google.com/s2/favicons?domain=";
    private static final String BASE_GET_FAVICONS_URL1 = "http://statics.dnspod.cn/proxy_favicon/_/favicon?domain=";
    private static final String FAVICONS_CACHE_FOLDER = "cfavicons";
    private HashMap<String, Boolean> downloadUrlList = null;
    private List<Record> defBookmarkList = null;

    public static BookmarksUtil getInstance(Context context){
        if (instance == null){
            instance = new BookmarksUtil(context);
        }
        return instance;
    }

    public BookmarksUtil(Context context){
        mContext = context;
        FileHelper.getCacheDir(context, FAVICONS_CACHE_FOLDER);
        downloadUrlList = new HashMap<String, Boolean>();
        defBookmarkList = new ArrayList<Record>();
        initDefaultBookmark();
    }

    private void initDefaultBookmark(){
        Record r1 = new Record();
        r1.setTitle("TrueMoveH");
        r1.setURL("http://www.truemove-h.com/");
        r1.setTime(System.currentTimeMillis());
        r1.setFaviconResId(R.drawable.truemove_h);
        defBookmarkList.add(r1);

        Record r2 = new Record();
        r2.setTitle("True iService");
        r2.setURL("http://www.truemove-h.com/iservice");
        r2.setTime(System.currentTimeMillis());
        r2.setFaviconResId(R.drawable.truemove_h);
        defBookmarkList.add(r2);

        Record r3 = new Record();
        r3.setTitle("TrueOnline");
        r3.setURL("http://trueonline.truecorp.co.th/");
        r3.setTime(System.currentTimeMillis());
        r3.setFaviconResId(R.drawable.trueonline_truecorp_co_th);
        defBookmarkList.add(r3);

        Record r4 = new Record();
        r4.setTitle("TrueVisions");
        r4.setURL("http://truevisionsgroup.truecorp.co.th/");
        r4.setTime(System.currentTimeMillis());
        r4.setFaviconResId(R.drawable.truevisionsgroup_truecorp_co_th);
        defBookmarkList.add(r4);

        Record r5 = new Record();
        r5.setTitle("TrueLife Plus");
        r5.setURL("http://truelifeplus.truecorp.co.th/");
        r5.setTime(System.currentTimeMillis());
        r5.setFaviconResId(R.drawable.truelifeplus_truecorp_co_th);
        defBookmarkList.add(r5);

        Record r6 = new Record();
        r6.setTitle("True You");
        r6.setURL("http://trueyou.truecorp.co.th/");
        r6.setTime(System.currentTimeMillis());
        r6.setFaviconResId(R.drawable.trueyou_truecorp_co_th);
        defBookmarkList.add(r6);

        Record r7 = new Record();
        r7.setTitle("TrueLife");
        r7.setURL("http://www.truelife.com/");
        r7.setTime(System.currentTimeMillis());
        r7.setFaviconResId(R.drawable.www_truelife_com);
        defBookmarkList.add(r7);

        Record r8 = new Record();
        r8.setTitle("TrueMoney");
        r8.setURL("http://www.truemoney.com/");
        r8.setTime(System.currentTimeMillis());
        r8.setFaviconResId(R.drawable.www_truemoney_com);
        defBookmarkList.add(r8);
    }

    public void checkDefBKInUserBookmark(){
        RecordAction action = new RecordAction(mContext);
        action.open(true);

        for (int i = 0; i < defBookmarkList.size(); i++){
            Record record = defBookmarkList.get(i);
            if (action.checkBookmark(record.getURL())) {
                continue;
            } else {
                String favFile = getFaviconsIconFile(record.getURL());
                record.setFaviconFile(favFile);
                action.addBookmark(record);
            }
        }
        action.close();
    }

    public boolean isDefBookmark(String url){
        if (url == null) return false;
        for (int i = 0; i < defBookmarkList.size(); i++){
            Record record = defBookmarkList.get(i);
            String defUrl = record.getURL();
            if (url.equals(defUrl))
                return true;
        }
        return false;
    }

    public String getUrlDomain(String url){
        try{
            URL u = new URL(url);
            return u.getHost();
        } catch (Exception e){
            return null;
        }
    }

    public String getFaviconsIconFile(String url){
        String domain = getUrlDomain(url);
        if (domain == null) return "";
        String md5Name = SystemHelper.getMd5(domain);

        File cacheFolder = FileHelper.getCacheDir(mContext, FAVICONS_CACHE_FOLDER);
        String favFile = cacheFolder.getAbsolutePath() + "/" + md5Name;

        if (false == FileHelper.fileIsExists(favFile) && false == downloadUrlList.containsKey(domain)){
            downloadFaviconsByUrl(domain, favFile);
        }

        return favFile;
    }

    public Bitmap getFaviconsBitmap(String filePath){
        if (filePath == null || filePath.equals("")) return null;
        try{
            FileInputStream fis = new FileInputStream(filePath);
            Bitmap bmp = BitmapFactory.decodeStream(fis);
            if (bmp != null){
                int bw = bmp.getWidth();
                int bh = bmp.getHeight();
                if (bw <= 0 || bh <= 0) {
                    FileHelper.delete(new File(filePath));
                    return null;
                }
            }

            return bmp;
        } catch (Exception e){
            return null;
        }
    }

    private void downloadFaviconsByUrl(final String domain, final String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = BASE_GET_FAVICONS_URL + domain;
                long ret = HttpHelper.download(url, filePath);
                if (ret < 0){
                    String url1 = BASE_GET_FAVICONS_URL1 + domain;
                    HttpHelper.download(url1, filePath);
                }
            }
        }).start();
    }
}
