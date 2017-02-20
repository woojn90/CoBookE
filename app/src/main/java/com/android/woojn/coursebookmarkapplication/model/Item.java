package com.android.woojn.coursebookmarkapplication.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wjn on 2017-02-18.
 */

public class Item extends RealmObject {
    @PrimaryKey
    private int id;
    private String url;
    private String title;
    private String desc;
    private String imageUrl;
    private boolean visited;

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
