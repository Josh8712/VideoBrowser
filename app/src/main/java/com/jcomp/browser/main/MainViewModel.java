package com.jcomp.browser.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavDestination;

import com.jcomp.browser.parser.category.Category;
import com.jcomp.browser.parser.pager.Pager;
import com.jcomp.browser.parser.searcher.Searcher;

import java.util.ArrayList;
import java.util.HashMap;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Searcher> mSearcher;

    private final MutableLiveData<Pager> mPager;
    ArrayList<NavDestination> mDestinations = new ArrayList<>();
    HashMap<String, Category> mCategoryList = new HashMap<>();
    public MainViewModel() {
        this.mSearcher = new MutableLiveData<>();
        mPager = new MutableLiveData<>();
    }

    public ArrayList<NavDestination> getmDestinations() {
        return mDestinations;
    }

    public HashMap<String, Category> getmCategoryList() {
        return mCategoryList;
    }

    public MutableLiveData<Searcher> getmSearcher() {
        return mSearcher;
    }


    public MutableLiveData<Pager> getmPager() {
        return mPager;
    }
}
