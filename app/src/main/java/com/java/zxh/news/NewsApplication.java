package com.java.zxh.news;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class NewsApplication extends Application {
    public DatabaseHelper databaseHelper;
    public NewsCategoryList newsCategoryList;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(getApplicationContext(), "NewsDatabase", null, 1);
        newsCategoryList = new NewsCategoryList(getApplicationContext());
    }

}
