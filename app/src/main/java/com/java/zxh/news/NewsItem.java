package com.java.zxh.news;

import java.io.Serializable;

public class NewsItem implements Serializable{
    public String title;
    public String link;
    public String author;
    public String description;
    public long timestamp;
    public boolean read;
    public boolean favorite;
    public NewsCategoryList.NewsCategory category;

    public NewsItem(String title, String link, String author, String description, long timestamp, boolean read, boolean favorite, NewsCategoryList.NewsCategory category){
        this.title = title;
        this.link = link;
        this.author = author;
        this.description = description;
        this.timestamp = timestamp;
        this.read = read;
        this.favorite = favorite;
        this.category = category;
    }


    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof NewsItem)) return false;
        NewsItem newsObj = (NewsItem)obj;
        if(!this.title.equals(newsObj.title)) return false;
        if(this.timestamp != newsObj.timestamp) return false;
        return true;
    }
}
