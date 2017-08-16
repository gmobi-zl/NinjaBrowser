package io.github.mthli.Ninja.Database;

public class Record {
    private String title;
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    private String url;
    public String getURL() {
        return url;
    }
    public void setURL(String url) {
        this.url = url;
    }

    private long time;
    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    private String faviconFile;
    public String getFaviconFile(){
        return faviconFile;
    }
    public void setFaviconFile(String favFile){
        faviconFile = favFile;
    }

    private int faviconResId;
    public int getFaviconResId(){
        return faviconResId;
    }
    public void setFaviconResId(int resId){
        faviconResId = resId;
    }

    public Record() {
        this.title = null;
        this.url = null;
        this.time = 0l;
        this.faviconFile = "";
        this.faviconResId = 0;
    }

    public Record(String title, String url, long time) {
        this.title = title;
        this.url = url;
        this.time = time;
    }

    public Record(String title, String url, long time, String favFile, int favResId) {
        this.title = title;
        this.url = url;
        this.time = time;
        this.faviconFile = favFile;
        this.faviconResId = favResId;
    }
}
