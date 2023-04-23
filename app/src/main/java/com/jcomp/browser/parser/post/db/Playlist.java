package com.jcomp.browser.parser.post.db;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    public long uid;

    public String name;

    public boolean isDefault;
    public int tag;

    public Playlist() {
    }

    public Playlist(String name) {
        this.name = name;
        this.isDefault = false;
    }

    public String getName() {
        return name;
    }
}
