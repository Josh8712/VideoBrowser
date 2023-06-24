package com.jcomp.browser.viewer.video_loader;

import android.app.ProgressDialog;

import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.ViewerFragmentBase;

public abstract class ResourceLoader {
    final Post post;
    boolean active = true;
    ProgressDialog progressDialog;
    String title;
    String url;

    public ResourceLoader(Post post) {
        this.post = post;
        this.title = post.getTitle();
        this.url = post.url;
    }

    abstract public void start(ViewerFragmentBase postFragment);

    public boolean isActive() {
        return active;
    }

    abstract void showError();
}
