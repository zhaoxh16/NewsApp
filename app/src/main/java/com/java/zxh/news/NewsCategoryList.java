package com.java.zxh.news;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewsCategoryList{
    private HashMap<NewsCategory, List<NewsItem>> newsMap;
    private HashMap<NewsCategory, List<NewsItem>> loadNewsMap;
    private NewsFetcher newsFetcher;
    private Context context;
    public List<NewsCategory> categoryList;
    private List<NewsItem> newFavoriteNewsList;
    private List<NewsItem> removeFavoriteNewsList;
    private String searchWord;
    private String lastSearchWord;
    private List<NewsItemFavoriteControl> favoriteControlList;

    public void setCategoryList(List<NewsCategory> categoryList){
        this.categoryList = categoryList;
        if(categoryList.contains(NewsCategory.FAVORITE))
            categoryList.remove(NewsCategory.FAVORITE);
        if(categoryList.contains(NewsCategory.SEARCH))
            categoryList.remove(NewsCategory.SEARCH);
        this.categoryList.add(0,NewsCategory.FAVORITE);
        this.categoryList.add(1,NewsCategory.SEARCH);
        DatabaseHelper helper = ((NewsApplication)context).databaseHelper;
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from SelectedCategoryTable");
        for(NewsCategory category: categoryList){
            if(category == NewsCategory.FAVORITE||category == NewsCategory.SEARCH) continue;
            String categoryString = category.toString();
            db.execSQL("insert into SelectedCategoryTable(name) values (?)",new Object[]{categoryString});
        }
    }

    private void setFavoriteControlListToDB(NewsItem item, SQLiteDatabase db){
        System.out.println("setFavoriteControListToDB"+item.title);
        Date date = new Date();
        if(item.favorite){
            NewsItemFavoriteControl favoriteControlItem = new NewsItemFavoriteControl(item, "add", date.getTime());
            if(favoriteControlList.contains(favoriteControlItem)){
                favoriteControlList.remove(favoriteControlItem);
            }
            favoriteControlList.add(favoriteControlItem);
            Cursor cursor = db.rawQuery("select * from NewsListFavoriteControlTable where title = ? and timestamp = ?",
                    new String[]{item.title,((Long)item.timestamp).toString()});
            if(cursor.getCount() == 0){
                db.execSQL("insert into NewsListFavoriteControlTable(title, timestamp, category, act, addtimestamp) values(?,?,?,?,?)",
                        new Object[]{item.title, item.timestamp, item.category.toString(), "add", date.getTime()});
            }else {
                db.execSQL("update NewsListFavoriteControlTable set act = ?, addtimestamp = ? where title = ? and timestamp = ?",
                        new Object[]{"add", date.getTime(), item.title, item.timestamp});
            }
        }else{
            NewsItemFavoriteControl favoriteControlItem = new NewsItemFavoriteControl(item, "remove", date.getTime());
            if(favoriteControlList.contains(favoriteControlItem)){
                favoriteControlList.remove(favoriteControlItem);
            }
            favoriteControlList.add(favoriteControlItem);
            Cursor cursor = db.rawQuery("select * from NewsListFavoriteControlTable where title = ? and timestamp = ?",
                    new String[]{item.title,((Long)item.timestamp).toString()});
            if(cursor.getCount() == 0){
                db.execSQL("insert into NewsListFavoriteControlTable(title, timestamp, category,  act, addtimestamp) values(?,?,?,?,?)",
                        new Object[]{item.title, item.timestamp, item.category.toString(), "remove", date.getTime()});
            }else {
                db.execSQL("update NewsListFavoriteControlTable set act = ?, addtimestamp = ? where title = ? and timestamp = ?",
                        new Object[]{"remove", date.getTime(), item.title, item.timestamp});
            }
        }
    }

    public void setFavorite(NewsItem item){
        System.out.println("setFavorite"+item.title);
        SQLiteDatabase db = ((NewsApplication)context).databaseHelper.getWritableDatabase();
        if(item.favorite) {
            db.execSQL("update NewsListTable set favorite = 0 where title = ? and timestamp = ?",
                    new Object[]{item.title, item.timestamp});
            item.favorite = false;
            if(newFavoriteNewsList.contains(item)) newFavoriteNewsList.remove(item);
            else removeFavoriteNewsList.add(item);
        }else{
            db.execSQL("update NewsListTable set favorite = 1 where title = ? and timestamp = ?",
                    new Object[]{item.title, item.timestamp});
            item.favorite = true;
            if(removeFavoriteNewsList.contains(item)) removeFavoriteNewsList.remove(item);
            else newFavoriteNewsList.add(item);
        }
        setFavoriteControlListToDB(item, db);
    }

    public NewsCategoryList(Context context){
        newFavoriteNewsList = new ArrayList<NewsItem>();
        removeFavoriteNewsList = new ArrayList<NewsItem>();
        this.favoriteControlList = new ArrayList<NewsItemFavoriteControl>();
        searchWord = "";
        lastSearchWord = "";
        newsMap = new HashMap<NewsCategory, List<NewsItem>>();
        for(NewsCategory category: NewsCategory.values()){
            newsMap.put(category, new ArrayList<NewsItem>());
        }
        loadNewsMap = new HashMap<NewsCategory, List<NewsItem>>();
        for(NewsCategory category:NewsCategory.values()){
            loadNewsMap.put(category, new ArrayList<NewsItem>());
        }
        newsFetcher = new NewsFetcher();
        this.context = context;
        AsyncTask<Void, Void, Void> newTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                getNewsFromLocal();
                getNewsFromInternet();
                return null;
            }
        };
        newTask.execute();
    }

    public boolean setSearchWord(String word){
        this.searchWord = word;
        if(searchWord.equals(lastSearchWord)) return false;
        return true;
    }

    private void searchNews(NewsCategory category, int number){
        newsMap.get(category).clear();
        loadNewsMap.get(category).clear();
        if(searchWord.equals("")) return;
        List<NewsItem> tempList = new ArrayList<NewsItem>();
        for(List<NewsItem> newsList:newsMap.values()){
            for(NewsItem newsItem:newsList){
                if(newsItem.title.contains(searchWord)||newsItem.author.contains(searchWord)
                        || newsItem.description.contains(searchWord)||newsItem.category.toString().contains(searchWord)
                        ||newsItem.link.contains(searchWord)||new Timestamp(newsItem.timestamp).toString().contains(searchWord)){
                    tempList.add(newsItem);
                }
            }
        }
        newsMap.get(category).addAll(tempList);
        if(number<newsMap.get(category).size())
            loadNewsMap.get(category).addAll(newsMap.get(category).subList(0,number));
        else
            loadNewsMap.get(category).addAll(newsMap.get(category));
        lastSearchWord = searchWord;
    }

    public void updateNews(NewsCategory category, int number){
        if(category == NewsCategory.FAVORITE){
            newsMap.get(category).removeAll(removeFavoriteNewsList);
            removeFavoriteNewsList.clear();
            Collections.reverse(newsMap.get(category));
            newsMap.get(category).addAll(newFavoriteNewsList);
            Set<NewsItem> tempSet = new HashSet<NewsItem>();
            tempSet.addAll(newsMap.get(category));
            newsMap.get(category).clear();
            newsMap.get(category).addAll(tempSet);
            Collections.reverse(newsMap.get(category));
            newFavoriteNewsList.clear();
            loadNewsMap.get(category).clear();
            if(number<newsMap.get(category).size())
                loadNewsMap.get(category).addAll(newsMap.get(category).subList(0,number));
            else
                loadNewsMap.get(category).addAll(newsMap.get(category));
            return;
        }
        else if(category == NewsCategory.SEARCH){
            if(lastSearchWord.equals(searchWord)){
                loadNewsMap.get(category).clear();
                if(number<newsMap.get(category).size())
                    loadNewsMap.get(category).addAll(newsMap.get(category).subList(0,number));
                else
                    loadNewsMap.get(category).addAll(newsMap.get(category));
                return;
            }
            searchNews(category, number);
            return;
        }
        ArrayList<ArrayList> newsArray = newsFetcher.getCategoryNews(category, context);
        if(newsArray == null) return;
        List<NewsItem> localItemsList = newsMap.get(category);
        List<NewsItem> newItemsList = new ArrayList<NewsItem>();
        for(int i=0;i<newsArray.size();++i){
            try{
                ArrayList obj = newsArray.get(i);
                NewsItem item = new NewsItem((String)obj.get(0),(String)obj.get(1),
                        (String)obj.get(2),(String)obj.get(3), (Long)obj.get(4),
                        false,false,NewsCategory.valueOf((String)obj.get(5)));
                if(localItemsList.contains(item)) continue;
                newItemsList.add(item);
            }catch(Exception e){
                System.out.println(e);
            }
        }
        Collections.reverse(localItemsList);
        Collections.reverse(newItemsList);
        localItemsList.addAll(newItemsList);
        Collections.reverse(localItemsList);
        saveNews(newItemsList);
        loadNewsMap.get(category).clear();
        if(number<newsMap.get(category).size())
            loadNewsMap.get(category).addAll(newsMap.get(category).subList(0,number));
        else
            loadNewsMap.get(category).addAll(newsMap.get(category));
    }

    private void saveNews(List<NewsItem> newsList){
        DatabaseHelper helper = ((NewsApplication)context).databaseHelper;
        SQLiteDatabase db = helper.getWritableDatabase();
        for(NewsItem item: newsList){
            int read = 0;
            if(item.read) read = 1;
            int favorite = 0;
            if(item.favorite) favorite = 1;
            db.execSQL("insert into NewsListTable(title, link, author, description, category, " +
                    "timestamp, read, favorite) values (?,?,?,?,?,?,?,?)", new Object[]{
                    item.title, item.link, item.author, item.description, item.category.toString(),
                    item.timestamp, read, favorite});
        }
    }

    public void synchronizeFavorite(){
        System.out.println("synchronizeFavorite");
        try {
            String host = "59.66.130.36";
            int port = 8888;
            Socket socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            String mac = new MACAddressFetcher().getAdresseMAC(context);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("macAddress", mac);
            map.put("activity", "synchronizeFavorite");
            List<Map<String, Object>> favoriteListUser = new ArrayList<Map<String, Object>>();
            for(NewsItemFavoriteControl itemFavoriteControl: favoriteControlList){
                Map<String,Object> favoriteMap = new HashMap<String,Object>();
                favoriteMap.put("title",itemFavoriteControl.newsItem.title);
                favoriteMap.put("timestamp",itemFavoriteControl.newsItem.timestamp);
                favoriteMap.put("category",itemFavoriteControl.newsItem.category.toString());
                favoriteMap.put("act",itemFavoriteControl.action);
                favoriteMap.put("addtimestamp",itemFavoriteControl.timestamp);
                favoriteListUser.add(favoriteMap);
            }
            map.put("param", favoriteListUser);
            output.writeObject(map);
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            Object inputObject = input.readObject();
            socket.close();
            if(inputObject == null) return;
            else{
                List<Map<String, Object>> favoriteListSynchronized = (List<Map<String, Object>>)inputObject;
                SQLiteDatabase db = ((NewsApplication)context).databaseHelper.getWritableDatabase();
                db.execSQL("delete from NewsListFavoriteControlTable");
                List<NewsItemFavoriteControl> newList = new ArrayList<NewsItemFavoriteControl>();
                for(Map<String,Object> m:favoriteListSynchronized){
                    NewsCategory category = NewsCategory.valueOf((String)m.get("category"));
                    NewsItem newItem = new NewsItem((String)m.get("title"),null,null,null,(Long)m.get("timestamp"),false,false,category);
                    int index = newsMap.get(category).indexOf(newItem);
                    NewsItem item = newsMap.get(category).get(index);
                    String action = (String)m.get("act");
                    NewsItemFavoriteControl newsItemFavoriteControl = new NewsItemFavoriteControl(item, action, (Long)m.get("addtimestamp"));
                    newList.add(newsItemFavoriteControl);
                    if(!item.favorite && action.equals("remove")) {
                        setFavoriteControlListToDB(item, db);
                    }
                    else if(item.favorite && action.equals("add")){
                        setFavoriteControlListToDB(item, db);
                    }
                    else{
                        setFavorite(item);
                    }
                }
                favoriteControlList = newList;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void getNewsFromInternet(){
        //将服务器上的收藏下载下来
        try {
            String host = "59.66.130.36";
            int port = 8888;
            Socket socket = new Socket(host, port);
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            String mac = new MACAddressFetcher().getAdresseMAC(context);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("macAddress", mac);
            map.put("activity", "getFavorite");
            map.put("param", null);
            output.writeObject(map);
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            Object inputObject = input.readObject();
            socket.close();
            if(inputObject == null) return;
            else{
                List<Map<String, Object>> favoriteListServer = (List<Map<String, Object>>)inputObject;
                for(Map<String,Object> m:favoriteListServer){
                    String title = (String)m.get("title");
                    String link = (String)m.get("link");
                    String author = (String)m.get("author");
                    String description = (String)m.get("description");
                    Long timestamp = (Long)m.get("timestamp");
                    boolean read = false;
                    boolean favorite = false;
                    NewsCategory category = NewsCategory.valueOf((String)m.get("category"));
                    NewsItem item = new NewsItem(title, link, author, description, timestamp, read, favorite, category);
                    newsMap.get(category).add(item);
                }
            }
            socket.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

        //同步收藏
        synchronizeFavorite();
    }

    private void getNewsFromLocal(){
        categoryList = new ArrayList<NewsCategory>();
        DatabaseHelper helper = ((NewsApplication)context).databaseHelper;
        SQLiteDatabase db = helper.getReadableDatabase();

        //加载所选择的新闻列
        String querySQL = "select * from SelectedCategoryTable";
        Cursor cursor = db.rawQuery(querySQL, null);
        while(cursor.moveToNext()){
            String categoryString = cursor.getString(0);
            NewsCategory category = NewsCategory.valueOf(categoryString);
            categoryList.add(category);
        }
        cursor.close();
        categoryList.add(0,NewsCategory.FAVORITE);
        categoryList.add(1,NewsCategory.SEARCH);

        // 加载已保存的新闻列表
        querySQL = "select * from NewsListTable order by timestamp desc";
        cursor = db.rawQuery(querySQL, null);
        while(cursor.moveToNext()){
            String title = cursor.getString(0);
            String link = cursor.getString(1);
            String author = cursor.getString(2);
            String description = cursor.getString(3);
            String categoryString = cursor.getString(4);
            NewsCategory category = NewsCategory.valueOf(categoryString);
            long timestamp = cursor.getLong(5);
            int readInt = cursor.getInt(6);
            boolean read;
            if(readInt != 0) read = true;
            else read = false;
            int favoriteInt = cursor.getInt(7);
            boolean favorite;
            if(favoriteInt != 0) favorite = true;
            else favorite = false;
            NewsItem newsItem = new NewsItem(title, link, author, description, timestamp, read, favorite, category);
            newsMap.get(category).add(newsItem);
            if(favorite == true)
                newsMap.get(NewsCategory.FAVORITE).add(newsItem);
        }
        for(NewsCategory category: NewsCategory.values()){
            if(10<newsMap.get(category).size())
                loadNewsMap.get(category).addAll(newsMap.get(category).subList(0,10));
            else
                loadNewsMap.get(category).addAll(newsMap.get(category));
        }
        cursor.close();

        //加载已保存的favoriteControl
        querySQL = "select * from NewsListFavoriteControlTable";
        cursor = db.rawQuery(querySQL, null);
        while(cursor.moveToNext()) {
            String title = cursor.getString(0);
            Long timestamp = cursor.getLong(1);
            String category = cursor.getString(2);
            String act = cursor.getString(3);
            Long addtimestamp = cursor.getLong(4);
            NewsItem newsItem = new NewsItem(title, null, null, null, timestamp, false, false, NewsCategory.valueOf(category));
            int index = newsMap.get(NewsCategory.valueOf(category)).indexOf(newsItem);
            if(index==-1) continue;
            NewsItem item = newsMap.get(NewsCategory.valueOf(category)).get(index);
            NewsItemFavoriteControl itemFavoriteControl = new NewsItemFavoriteControl(item, act, addtimestamp);
            favoriteControlList.add(itemFavoriteControl);
        }
        cursor.close();
    }

    public List<NewsItem> getNews(String category){
        return loadNewsMap.get(NewsCategory.valueOf(category));
    }

    public List<NewsItem> getAllNews(String category){
        return newsMap.get(NewsCategory.valueOf(category));
    }

    public enum NewsCategory {
        NATIONAL, MOVIE, FINANCE, TECHNOLOGY, GAME, EDUCATION,
        CONSTELLATION, ANIME, FASHION, JOKE, CHILDREN, SPORT,
        FAVORITE, SEARCH
        //PARENT, WEATHER, SECURITIES, CAR, VIDEO, BOOK, PHONE, FEMALE
    }
}