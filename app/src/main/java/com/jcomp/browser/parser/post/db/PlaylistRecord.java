package com.jcomp.browser.parser.post.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class PlaylistRecord {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public long postID;
    public long playlistID;

    public PlaylistRecord() {
    }

    public PlaylistRecord(long postID, long playlistID) {
        this.postID = postID;
        this.playlistID = playlistID;
    }
}
