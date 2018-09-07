package com.java.zxh.news;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory cursorFactory, int version){
        super(context, name, cursorFactory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "create table if not exists NewsListTable "+
                "(title TEXT," +
                "link TEXT," +
                "author TEXT," +
                "description TEXT," +
                "category TEXT," +
                "timestamp INTEGER," +
                "read INTEGER," +
                "favorite INTEGER)";
        db.execSQL(createTable);
        createTable = "create table if not exists SelectedCategoryTable (name TEXT)";
        db.execSQL(createTable);
        Cursor c = db.rawQuery("select * from SelectedCategoryTable", null);
        int number=c.getCount();
        c.close();
        if(number == 0){
            db.execSQL("insert into SelectedCategoryTable(name) values ('NATIONAL')");
        }
        createTable = "create table if not exists NewsListFavoriteControlTable " +
                "(title TEXT, "+
                "timestamp INTEGER," +
                "category TEXT, " +
                "act TEXT, " +
                "addtimestamp INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("update SQLite database");
    }
}
