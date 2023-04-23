package com.jcomp.browser.parser.post.db;

import android.content.Context;

import com.jcomp.browser.R;

public class PlaylistDefault extends Playlist {

    public PlaylistDefault(Context context) {
        super(context.getString(R.string.my_playlist));
        this.tag = PlaylistDoa.DEFAULT_PLAYLIST_TAG;
        this.isDefault = true;
    }
}
