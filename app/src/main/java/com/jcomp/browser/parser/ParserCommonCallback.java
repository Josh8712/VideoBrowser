package com.jcomp.browser.parser;

import com.jcomp.browser.parser.post.db.Post;

import java.util.LinkedHashMap;

public interface ParserCommonCallback {
    void addPost(LinkedHashMap<String, Post> postList);

    void parseFinished();
}
