package com.jcomp.browser.parser.tag;

import androidx.annotation.NonNull;

import com.jcomp.browser.parser.post.db.Post;

public class Tag extends Post {
    public Tag(String title, String img, @NonNull String url) {
        super(title, img, url);
        viewType = TYPE_TAG;
    }

    public Tag(String title, @NonNull String url) {
        this(title, null, url);
    }

    @Override
    public boolean isVideo() {
        return false;
    }
}
