package com.jcomp.browser.download.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DownloadPostDoa {
    @Query("SELECT * FROM downloadpost ORDER BY uid DESC")
    List<DownloadPost> getAll();

    @Query("SELECT * FROM downloadpost WHERE uid = :uid")
    DownloadPost getByUID(long uid);

    @Query("SELECT * FROM downloadpost WHERE playerPath = :playerPath")
    DownloadPost getByPath(String playerPath);

    @Query("SELECT * FROM downloadpost WHERE url = :url")
    DownloadPost getByURL(String url);

    @Query("SELECT * FROM downloadpost WHERE url = :url")
    List<DownloadPost> getAllByURL(String url);

    @Query("SELECT status FROM downloadpost WHERE uid = :uid")
    DownloadPost.Status getStatusByUID(long uid);

    @Query("UPDATE downloadpost SET status = :status, jobCounter = jobCounter + 1 WHERE uid = :uid")
    int updateStatusByUID(long uid, DownloadPost.Status status);

    @Query("UPDATE downloadpost SET progress = :progress WHERE uid = :uid")
    void updateProgressByUID(long uid, float progress);

    @Insert
    long insert(DownloadPost posts);

    @Delete
    int delete(DownloadPost post);

}
