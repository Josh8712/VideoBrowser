package com.jcomp.browser.parser.post.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

public class PlaylistWithCount extends Playlist {
    public int count;
    public String preview;

    public PlaylistWithCount() {
    }

    public int getCount() {
        return count;
    }

    public String getPreview() {
        return preview;
    }

    public void clear() {
        count = 0;
        preview = null;
    }
}
