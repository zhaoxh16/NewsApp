package com.java.zxh.news;

public class NewsItemFavoriteControl {
    NewsItem newsItem;
    String action;
    long timestamp;

    public NewsItemFavoriteControl(NewsItem item, String action, long timestamp){
        this.newsItem = item;
        this.action = action;
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof NewsItemFavoriteControl)) return false;
        NewsItemFavoriteControl newsObj = (NewsItemFavoriteControl)obj;
        if(!this.newsItem.equals(newsObj.newsItem)) return false;
        return true;
    }
}
