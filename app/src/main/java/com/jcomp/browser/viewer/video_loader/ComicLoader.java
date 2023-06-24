package com.jcomp.browser.viewer.video_loader;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.jcomp.browser.comic.ComicPlayer;
import com.jcomp.browser.comic.ComicPlayerInfo;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.player.VideoPlayerInfo;
import com.jcomp.browser.viewer.ViewerFragmentBase;

public class ComicLoader extends ResourceLoader {

    private ViewerFragmentBase postFragment;

    public ComicLoader(Post post) {
        super(post);
    }

    Context getContext() {
        return postFragment.getContext();
    }

    @Override
    public void start(ViewerFragmentBase postFragment) {
        this.postFragment = postFragment;
        Intent intent = new Intent(postFragment.getContext(), ComicPlayer.class);
        intent.putExtra(ComicPlayer.COMIC_INFO_KEY, new Gson().toJson(new PlayerInfo(post, VideoPlayerInfo.PlayerType.ONLINE)));
        getContext().startActivity(intent);
    }

    @Override
    void showError() {

    }
}
