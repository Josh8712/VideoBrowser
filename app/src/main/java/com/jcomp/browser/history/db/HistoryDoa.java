package com.jcomp.browser.history.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HistoryDoa {
    @Query("SELECT * FROM history")
    List<History> getAll();

    @Query("SELECT * FROM history WHERE url = :url")
    History getByPath(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(History history);

    @Update
    int update(History history);

    @Delete
    int delete(History history);

}
