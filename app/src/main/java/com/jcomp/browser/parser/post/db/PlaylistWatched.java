package com.jcomp.browser.parser.post.db;

import android.content.Context;

import com.jcomp.browser.R;

public class PlaylistWatched extends Playlist {

    public PlaylistWatched(Context context) {
        super(context.getString(R.string.watch_history));
        this.tag = PlaylistDoa.HISTORY_TAG;
        this.isDefault = true;
    }
}
