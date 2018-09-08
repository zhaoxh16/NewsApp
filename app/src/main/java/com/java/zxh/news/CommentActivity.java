package com.java.zxh.news;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    List<Map<String, Object>> mCommentList;
    NewsItem mItem;//注意！此处的mItem是独立创建的，修改该item不会修改newsMap中的item
    CommentRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentList = new ArrayList<Map<String, Object>>();
        mItem = (NewsItem)getIntent().getSerializableExtra("item");
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Comment");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = CommentActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("评论");    //设置对话框标题
                final EditText edit = new EditText(context);
                builder.setView(edit);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "你输入的是: " + edit.getText().toString(), Toast.LENGTH_SHORT).show();
                        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,Void> setCommentTask = new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected Void doInBackground(Void... voids) {
                                try {
                                    Socket socket = ((NewsApplication) getApplicationContext()).getNewServerSocket();
                                    ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                                    String mac = new MACAddressFetcher().getAdresseMAC(CommentActivity.this);
                                    HashMap<String, Object> map = new HashMap<String, Object>();
                                    map.put("macAddress", mac);
                                    map.put("activity", "setComment");
                                    Map<String, Object> paramMap = new HashMap<String, Object>();
                                    String title = mItem.title;
                                    Long timestamp = mItem.timestamp;
                                    Map<String, Object> newsItemMap = new HashMap<String, Object>();
                                    newsItemMap.put("title",title);
                                    newsItemMap.put("timestamp",timestamp);
                                    paramMap.put("newsItem",newsItemMap);
                                    String content = edit.getText().toString();
                                    String author = mac;
                                    Long commenttime = new Date().getTime();
                                    Map<String, Object> commentMap = new HashMap<String, Object>();
                                    commentMap.put("content",content);
                                    commentMap.put("author",author);
                                    commentMap.put("timestamp",commenttime);
                                    paramMap.put("comment",commentMap);
                                    map.put("param",paramMap);
                                    output.writeObject(map);
                                    output.flush();
                                    ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                                    Object inputObject = input.readObject();
                                    if(inputObject != null){
                                        List<Map<String,Object>>commentList = (List<Map<String,Object>>)inputObject;
                                        Collections.reverse(commentList);
                                        mCommentList.clear();
                                        mCommentList.addAll(commentList);
                                    }
                                    socket.close();
                                    System.out.println(mCommentList);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                super.onPostExecute(aVoid);
                                mRecyclerView.getAdapter().notifyDataSetChanged();
                            }
                        };
                        setCommentTask.execute();
                    }
                });
                builder.setCancelable(true);    //设置按钮是否可以按返回键取消,false则不可以取消
                AlertDialog dialog = builder.create();  //创建对话框
                dialog.show();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new CommentRecyclerViewAdapter(this, mCommentList);
        mRecyclerView.setAdapter(mAdapter);

        @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void,Void> getCommentTask = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Socket socket = ((NewsApplication) getApplicationContext()).getNewServerSocket();
                    ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    String mac = new MACAddressFetcher().getAdresseMAC(CommentActivity.this);
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("macAddress", mac);
                    map.put("activity", "getComment");
                    String title = mItem.title;
                    Long timestamp = mItem.timestamp;
                    Map<String, Object> insideMap = new HashMap<String, Object>();
                    insideMap.put("title",title);
                    insideMap.put("timestamp",timestamp);
                    map.put("param",insideMap);
                    output.writeObject(map);
                    output.flush();
                    ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
                    Object inputObject = input.readObject();
                    if(inputObject != null){
                        List<Map<String,Object>>commentList = (List<Map<String,Object>>)inputObject;
                        mCommentList.clear();
                        Collections.reverse(commentList);
                        mCommentList.addAll(commentList);
                    }
                    socket.close();
                    System.out.println(mCommentList);
                }catch(Exception e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        };
        getCommentTask.execute();

    }

    public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {
        private final List<Map<String, Object>> commentList;

        public CommentRecyclerViewAdapter(Context context, List<Map<String, Object>> commentList){
            this.commentList = commentList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_list_item, parent, false);
            return new CommentRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CommentRecyclerViewAdapter.ViewHolder holder, int position) {
            holder.mContentView.setText((String)commentList.get(position).get("content"));
            long nowTimeLong= (Long)commentList.get(position).get("timestamp");
            DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeString = ymdhmsFormat.format(nowTimeLong);
            holder.mDatetimeView.setText(timeString);
            holder.mAuthorView.setText((String)commentList.get(position).get("author"));
        }

        @Override
        public int getItemCount() {
            System.out.println(commentList.size());
            return commentList==null?0:commentList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mContentView, mDatetimeView, mAuthorView;
            public Map<String, Object> mItem;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.comment_content);
                mAuthorView = (TextView) view.findViewById(R.id.comment_author);
                mDatetimeView = (TextView) view.findViewById(R.id.comment_datetime);
            }
        }
    }

}
