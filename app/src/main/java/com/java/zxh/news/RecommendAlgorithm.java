package com.java.zxh.news;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendAlgorithm {
    Map<NewsCategoryList.NewsCategory, List<NewsItem>> newsMap;
    List<NewsItem> recommendList;

    public RecommendAlgorithm(Map<NewsCategoryList.NewsCategory, List<NewsItem>> map){
        this.newsMap = map;
        this.recommendList = new ArrayList<NewsItem>();
    }

    public List<NewsItem> getRecommendList(){
        recommendList.clear();
        final Map<NewsCategoryList.NewsCategory, Integer> countMap = new HashMap<NewsCategoryList.NewsCategory, Integer>();
        for(NewsCategoryList.NewsCategory category: NewsCategoryList.NewsCategory.values()){
            if(category == NewsCategoryList.NewsCategory.RECOMMEND || category == NewsCategoryList.NewsCategory.FAVORITE
                    || category == NewsCategoryList.NewsCategory.SEARCH) {
                countMap.put(category, -10);
                continue;
            }
            int count = 0;
            List<NewsItem> newsList = newsMap.get(category);
            for(NewsItem newsItem: newsList){
                if(newsItem.favorite) count+=5;
                else if(newsItem.read) count+=1;
            }
            countMap.put(category, count);
        }

        class MyComparator implements java.util.Comparator<NewsCategoryList.NewsCategory>{
            @Override
            public int compare(NewsCategoryList.NewsCategory o1, NewsCategoryList.NewsCategory o2) {
                if(countMap.get(o1)>countMap.get(o2)) return -1;
                else if(countMap.get(o1)<countMap.get(o2)) return 1;
                else return 0;
            }
        }

        NewsCategoryList.NewsCategory[] categoryArray = NewsCategoryList.NewsCategory.values();
        Arrays.sort(categoryArray, new MyComparator());
        int recommendNumber = 5;
        for(NewsCategoryList.NewsCategory category: categoryArray){
            if(recommendNumber>0 && category != NewsCategoryList.NewsCategory.SEARCH &&
                    category != NewsCategoryList.NewsCategory.FAVORITE && category!= NewsCategoryList.NewsCategory.RECOMMEND){
                List<NewsItem> myList = newsMap.get(category);
                int alreadyRecommendNumber = 0;
                for(NewsItem item:myList){
                    if(alreadyRecommendNumber >= recommendNumber) break;
                    else if(item.read || item.favorite) continue;
                    else{
                        ++alreadyRecommendNumber;
                        recommendList.add(item);
                    }
                }
                --recommendNumber;
            }
        }
        Collections.shuffle(recommendList);
        return recommendList;
    }
}
