package com.java.zxh.news;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;


public class NewsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<List<NewsItem>>{
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    public int loaderID;
    private String category;
    private Context mContext;
    public int loadNumber;
    private NewsListFragment thisFragment = this;
    private boolean loading;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle){
        loadNumber = 10;
        loading = false;
        loaderID = getArguments().getInt("loaderID");
        category = getArguments().getString("category");
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView = (RecyclerView)view.findViewById(R.id.news_list_recycler_view);
        assert recyclerView != null;
        setupRecyclerView(recyclerView, category);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                        DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        final LinearLayoutManager linearLayoutManager =
                (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalCount = linearLayoutManager.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                if(!loading && getActivity()!=null && totalCount<=lastVisiblePosition+1){
                    loadNumber+=10;
                    loading = true;
                    getLoaderManager().restartLoader(loaderID, null, thisFragment);
                }
            }
        });

        getLoaderManager().restartLoader(loaderID, null, this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(loaderID);
        try {
            SwipeRefreshLayout refreshLayout =
                    (SwipeRefreshLayout)getView().findViewById(R.id.swipe_refresh_layout);
            refreshLayout.setRefreshing(false);
            refreshLayout.setVisibility(View.INVISIBLE);
            RecyclerView recyclerView = (RecyclerView)getView().findViewById(R.id.news_list_recycler_view);
            recyclerView.setVisibility(View.INVISIBLE);
            getView().setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (getView() == null) {
            return;
        }
        SwipeRefreshLayout refreshLayout =
                (SwipeRefreshLayout)getView().findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setRefreshing(true);
        loading = true;
        getLoaderManager().restartLoader(loaderID, null, this);
    }

    @NonNull
    @Override
    public Loader<List<NewsItem>> onCreateLoader(int i, @Nullable Bundle bundle) {
        return new NewsListLoader(mContext, category, loadNumber);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<NewsItem>> loader, List<NewsItem> newsItem) {
        updateNews();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<NewsItem>> loader) {

    }

    void setupRecyclerView(RecyclerView recyclerView, String category){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new NewsItemRecyclerViewAdapter(category));
    }

    void updateNews(){
        if (getContext() == null || getActivity() == null) {
            return;
        }
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout)getView().findViewById(R.id.swipe_refresh_layout);
        refreshLayout.setRefreshing(false);
        RecyclerView newsList = (RecyclerView)refreshLayout.findViewById(R.id.news_list_recycler_view);
        loading = false;
        newsList.getAdapter().notifyDataSetChanged();
    }

    public class NewsItemRecyclerViewAdapter
            extends RecyclerView.Adapter<NewsItemRecyclerViewAdapter.ViewHolder> {
        private final List<NewsItem> newsItemList;

        public NewsItemRecyclerViewAdapter(String category){
            this.newsItemList = ((NewsApplication)getContext().getApplicationContext()).newsCategoryList.getNews(category);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.news_list_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final NewsItemRecyclerViewAdapter.ViewHolder holder, int position) {
            if (getActivity() == null) {
                return;
            }
            holder.mItem = newsItemList.get(position);
            holder.mSourceView.setText(newsItemList.get(position).author);
            long nowTimeLong=newsItemList.get(position).timestamp;
            DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeString = ymdhmsFormat.format(nowTimeLong);
            holder.mDatetimeView.setText(timeString);
            holder.mTitleView.setText(newsItemList.get(position).title);
            if (!newsItemList.get(position).read) {
                holder.mTitleView.setTextColor(getResources().getColor(R.color.primaryTextDark));
            } else {
                holder.mTitleView.setTextColor(getResources().getColor(R.color.newsTitleRead));
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.mItem.read = true;
                    notifyItemChanged(holder.getAdapterPosition());
                    Context context = v.getContext();
                    setRead(context, holder.mItem);
                    Intent intent = new Intent(context, NewsDetailsActivity.class);
                    intent.putExtra("item",holder.mItem);
                    context.startActivity(intent);
                }

                private void setRead(Context context, NewsItem item){
                    NewsApplication app = (NewsApplication)context.getApplicationContext();
                    DatabaseHelper databaseHelper = app.databaseHelper;
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    db.execSQL("update NewsListTable set read = 1 where title = ? and timestamp = ?",
                            new Object[]{item.title, item.timestamp});
                }
            });
        }

        @Override
        public int getItemCount() {
            return newsItemList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mSourceView, mDatetimeView;
            public NewsItem mItem;


            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.title);
                mSourceView = (TextView) view.findViewById(R.id.source);
                mDatetimeView = (TextView) view.findViewById(R.id.datetime);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTitleView.getText() + "'";
            }
        }
    }
}
