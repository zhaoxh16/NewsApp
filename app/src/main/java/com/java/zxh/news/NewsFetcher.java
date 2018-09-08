package com.java.zxh.news;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.*;

public class NewsFetcher {

    public ArrayList<ArrayList> getCategoryNews(String category, Context context){
        NewsCategoryList.NewsCategory c = NewsCategoryList.NewsCategory.valueOf(category);
        return getCategoryNews(c, context);
    }

    public ArrayList<ArrayList> getCategoryNews(NewsCategoryList.NewsCategory category, Context context){
        try {
            Socket socket = ((NewsApplication)context.getApplicationContext()).getNewServerSocket();
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            String mac = new MACAddressFetcher().getAdresseMAC(context);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("macAddress", mac);
            map.put("activity", "update");
            map.put("param", category.toString());
            output.writeObject(map);
            output.flush();

            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            ArrayList<ArrayList> newsObject =  (ArrayList<ArrayList>)input.readObject();
            socket.close();
            return newsObject;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
