package com.jcomp.browser.parser.category;

import androidx.annotation.NonNull;

import com.jcomp.browser.parser.post.db.Post;

public class Category extends Post {
    public int id = -1;

    public Category(String title, String img, @NonNull String url) {
        super(title, img, url);
    }

    public Category(String title, @NonNull String url) {
        super(title, null, url);
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVideo() {
        return false;
    }
}
