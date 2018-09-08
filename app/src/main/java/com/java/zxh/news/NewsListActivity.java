package com.java.zxh.news;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewsListActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_news_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_category_choose){
            chooseCategory();
            return true;
        }
        else if(id == R.id.action_search){
            searchNews();
            return true;
        }else if(id == R.id.action_login){
            login();
            return true;
        }else if(id == R.id.action_register){
            register();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void register(){
        Intent intent = new Intent(NewsListActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void login(){
        Intent intent = new Intent(NewsListActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void searchNews(){
        final Context context = NewsListActivity.this;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("搜索");    //设置对话框标题
        final EditText edit = new EditText(context);
        builder.setView(edit);
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "你输入的是: " + edit.getText().toString(), Toast.LENGTH_SHORT).show();
                boolean flag = ((NewsApplication)getApplicationContext()).newsCategoryList.setSearchWord(edit.getText().toString());
                mViewPager.setCurrentItem(2);
                PagerAdapter f = mViewPager.getAdapter();
                NewsListFragment fragment = (NewsListFragment) f.instantiateItem(mViewPager, 2);
                fragment.loadNumber = 10;
                fragment.getLoaderManager().restartLoader(fragment.loaderID, null, fragment);
            }
        });
        builder.setCancelable(true);    //设置按钮是否可以按返回键取消,false则不可以取消
        AlertDialog dialog = builder.create();  //创建对话框
        dialog.show();
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private final List<NewsCategoryList.NewsCategory> categoryList;

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            categoryList = ((NewsApplication)getApplicationContext()).newsCategoryList.categoryList;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return categoryList.get(position).toString();
        }

        @Override
        public Fragment getItem(int position) {
            NewsListFragment fragment = new NewsListFragment();
            Bundle args = new Bundle();
            args.putString("category", categoryList.get(position).toString());
            int loaderID = Arrays.asList(NewsCategoryList.NewsCategory.values()).indexOf(categoryList.get(position));
            args.putInt("loaderID", loaderID);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return categoryList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return PagerAdapter.POSITION_NONE;
        }
    }

    private void chooseCategory(){
        AlertDialog.Builder builder = new AlertDialog.Builder(NewsListActivity.this);
        builder.setTitle("请选择你想要的栏目");
        final String[] allCategories = new String[NewsCategoryList.NewsCategory.values().length];
        for(int i=0;i<allCategories.length;++i){
            allCategories[i] = NewsCategoryList.NewsCategory.values()[i].toString();
        }

        final List<NewsCategoryList.NewsCategory> selectedCategories = ((NewsApplication)getApplicationContext()).newsCategoryList.categoryList;
        boolean[] chooseCategories = new boolean[allCategories.length];
        for(int i=0;i<chooseCategories.length;++i){
            if(selectedCategories.contains(NewsCategoryList.NewsCategory.valueOf(allCategories[i])))
                chooseCategories[i] = true;
            else chooseCategories[i] = false;
        }

        builder.setMultiChoiceItems(allCategories, chooseCategories, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked){
                    selectedCategories.add(NewsCategoryList.NewsCategory.valueOf(allCategories[which]));
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }else{
                    selectedCategories.remove(NewsCategoryList.NewsCategory.valueOf(allCategories[which]));
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }
            }
        });

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((NewsApplication)getApplicationContext()).newsCategoryList.setCategoryList(selectedCategories);
                mSectionsPagerAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
