package com.java.zxh.news;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class NewsDetailsActivity extends AppCompatActivity {
    private NewsItem mItem;
    private Uri imageURI;
    private boolean hasImage = false;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_news_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        NewsItem item = (NewsItem)intent.getSerializableExtra("item");
        int index = ((NewsApplication)getApplicationContext()).newsCategoryList.getAllNews(item.category.toString()).indexOf(item);
        mItem = ((NewsApplication)getApplicationContext()).newsCategoryList.getAllNews(item.category.toString()).get(index);

        String url = mItem.link;
        String title = mItem.title;
        toolbar.setTitle(title);
        WebView webView = findViewById(R.id.wb_content);
        webView.setWebViewClient(new WebViewClient(){
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
                Uri uri = request.getUrl();
                if(uri.toString().endsWith(".jpg") || uri.toString().endsWith(".jpeg")
                        ||uri.toString().endsWith(".png")||uri.toString().endsWith(".gif")||uri.toString().contains("gtimg")){
                    File file = saveImage(getImageInputStream(uri.toString()));
                    if(file != null){
                        imageURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", file);
                        hasImage = true;
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if(url.endsWith(".jpg") || url.endsWith(".jpeg")
                        ||url.endsWith(".png")||url.endsWith(".gif")||url.contains("gtimg")){
                    File file = saveImage(getImageInputStream(url));
                    if(file != null){
                        imageURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", file);
                        hasImage = true;
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }

            private File saveImage(Bitmap bitmap){
                String path = Environment.getExternalStorageDirectory().getPath()+"/testCachedImage";
                File file=new File(path);
                FileOutputStream fileOutputStream=null;
                //文件夹不存在，则创建它
                if(!file.exists()){
                    System.out.println("create fileDir");
                    file.mkdir();
                }
                try {
                    File imageOut = new File(path+"/test.png");
                    if(!hasImage || !imageOut.exists()){
                        fileOutputStream=new FileOutputStream(imageOut);
                        if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream))
                            System.out.println("Compress failed");
                    }else{
                        Bitmap bitmap2 = BitmapFactory.decodeFile(path+"/test.png");
                        if(bitmap.getByteCount()>bitmap2.getByteCount()) {
                            fileOutputStream = new FileOutputStream(imageOut);
                            if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream))
                                System.out.println("Compress failed");
                        }
                    }
                    fileOutputStream.close();
                    return imageOut;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private Bitmap getImageInputStream(String imageUrl){
                URL url;
                HttpURLConnection connection=null;
                Bitmap bitmap=null;
                try {
                    url = new URL(imageUrl);
                    connection=(HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(6000); //超时设置
                    connection.setDoInput(true);
                    connection.setUseCaches(false); //设置不使用缓存
                    InputStream inputStream=connection.getInputStream();
                    bitmap= BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }

        });

        //获取webview设置属性
        WebSettings webSettings = webView.getSettings();
        //显示大小设置
        webSettings.setBuiltInZoomControls(true); // 显示放大缩小
        webSettings.setSupportZoom(true); // 可以缩放
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //缓存设置
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()+"/webcache";
        webSettings.setDatabasePath(cacheDirPath);
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        if(connectInternet()) webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        else webSettings.setCacheMode(WebSettings.LOAD_CACHE_ONLY);

        webView.loadUrl(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news_details_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if(mItem.favorite)
            menu.findItem(R.id.action_favorite).setTitle("delete from favorite");
        else
            menu.findItem(R.id.action_favorite).setTitle("add to favorite");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            addToFavorite(item);
            return true;
        }
        else if(id == R.id.action_share){
            share();
            return true;
        }else if(id == R.id.action_comment){
            comment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void comment(){
        Intent commentIntent = new Intent(NewsDetailsActivity.this, CommentActivity.class);
        commentIntent.putExtra("item",mItem);
        startActivity(commentIntent);
    }

    private void share(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri uri;
        if(hasImage) uri = imageURI;
        else uri=null;
        String content = "["+mItem.title+"]\n"+mItem.description+"\n(详情请见："+mItem.link+")";

        if(uri!=null){
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            //当用户选择短信时使用sms_body取得文字
            shareIntent.putExtra("sms_body", content);
            shareIntent.putExtra("Kdescription",content);
        }else{
            shareIntent.setType("text/plain");
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);
        startActivity(Intent.createChooser(shareIntent, "分享"));
    }

    private void addToFavorite(MenuItem item){
        if(mItem.favorite){
            item.setTitle("add to favorite");
            ((NewsApplication)getApplicationContext()).newsCategoryList.setFavorite(mItem);
        }
        else{
            item.setTitle("delete from favorite");
            ((NewsApplication)getApplicationContext()).newsCategoryList.setFavorite(mItem);
        }
    }

    private boolean connectInternet(){

        ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

}
