package com.jcomp.browser.parser.post.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaylistDoa {
    @Query("SELECT * FROM post ORDER BY uid DESC")
    List<Post> getAll();

    @Query("SELECT * FROM post WHERE url = :url")
    Post getByPath(String url);

    @Insert
    long insert(Post post);

    @Update
    int update(Post post);

    @Delete
    int delete(Post post);

}
