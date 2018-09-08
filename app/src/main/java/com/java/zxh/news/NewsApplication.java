package com.java.zxh.news;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.io.IOException;
import java.net.Socket;

public class NewsApplication extends Application {
    public DatabaseHelper databaseHelper;
    public NewsCategoryList newsCategoryList;
    public boolean loginStatus = false;
    private String serverIP = "59.66.130.36";
    private int serverPort = 8888;


    @Override
    public void onCreate() {
        super.onCreate();
        databaseHelper = new DatabaseHelper(getApplicationContext(), "NewsDatabase", null, 1);
        newsCategoryList = new NewsCategoryList(getApplicationContext());
    }

    public Socket getNewServerSocket(){
        try {
            return new Socket(serverIP, serverPort);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
