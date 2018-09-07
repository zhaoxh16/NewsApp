package com.java.zxh.news;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

public class NewsListLoader extends AsyncTaskLoader<List<NewsItem>> {
    String category;
    int number;

    public NewsListLoader(@NonNull Context context, String category, int number) {
        super(context);
        this.category = category;
        this.number = number;
    }

    @Nullable
    @Override
    protected void onStartLoading(){
        forceLoad();
    }

    @Nullable
    @Override
    public List<NewsItem> loadInBackground() {
        if(category.equals(NewsCategoryList.NewsCategory.FAVORITE.toString())){
            ((NewsApplication)getContext().getApplicationContext()).newsCategoryList.synchronizeFavorite();
        }
        NewsCategoryList newsCategoryList = ((NewsApplication)getContext().getApplicationContext()).newsCategoryList;
        newsCategoryList.updateNews(NewsCategoryList.NewsCategory.valueOf(category), number);
        return null;
    }
}
