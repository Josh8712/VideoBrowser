package com.jcomp.browser.viewer.adapter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.jcomp.browser.parser.pager.Pager;
import com.jcomp.browser.parser.post.db.Post;

import java.util.LinkedHashMap;

public class PostFragmentModelView extends ViewModel {
    private final MutableLiveData<LinkedHashMap<String, Post>> mPost;
    private final MutableLiveData<Pager> mPager;

    public PostFragmentModelView() {
        super();
        mPost = new MutableLiveData<>();
        mPager = new MutableLiveData<>();
    }

    public MutableLiveData<LinkedHashMap<String, Post>> getmPost() {
        return mPost;
    }

    public MutableLiveData<Pager> getmPager() {
        return mPager;
    }
}
