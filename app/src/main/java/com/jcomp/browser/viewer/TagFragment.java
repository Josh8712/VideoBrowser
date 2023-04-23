package com.jcomp.browser.viewer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.tag.Tag;

import java.util.LinkedHashMap;
import java.util.List;

public class TagFragment extends VideoParserFragment {
    protected int getSpanCount() {
        return 4;
    }

    @Override
    protected boolean addToTop() {
        return false;
    }

    @Override
    public void load(String url) {
        LinkedHashMap<String, Post> posts = new LinkedHashMap<>();
        List<Post> tagList = new Gson().fromJson((String) getArguments().get(MainActivity.CLASS_LIST_KEY), new TypeToken<List<Tag>>() {
        }.getType());
        for (Post tag : tagList)
            posts.put(tag.getKey(), tag);
        addPost(posts);
        parseFinished();
    }


}
