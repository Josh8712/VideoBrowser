package com.jcomp.browser.viewer.video_loader;

import android.app.ProgressDialog;

import com.jcomp.browser.browser.Browser;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.ViewerFragmentBase;

public abstract class VideoResourceLoader extends ResourceLoader {
    String playerURL;

    public VideoResourceLoader(Post post) {
        super(post);
        this.playerURL = post.url;
    }

    abstract public void loadHLS(String hls, String preview);

    abstract public void loadPlayer(String[] playerList, Browser browser);

    abstract public void parseFinished();
}
