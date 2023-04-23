package com.jcomp.browser.parser.post.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.jcomp.browser.history.db.History;

import java.util.List;

@Dao
public interface PlaylistDoa {
    static final int HISTORY_TAG = 0;
    static final int DEFAULT_PLAYLIST_TAG = 1;
//    @Query("SELECT * FROM playlistrecord")
//    List<PlaylistRecord> getRecordsTest();
//
//    @Query("SELECT * FROM post")
//    List<Post> getPostsTest();
//
//    @Query("SELECT * FROM playlist")
//    List<Playlist> getPlayListTest();

    // Post
    @Query("SELECT * FROM playlistrecord JOIN post ON playlistrecord.postID = post.uid WHERE playlistrecord.playlistID = :playlistID ORDER BY playlistrecord.uid DESC")
    List<Post> getAllInPlaylist(int playlistID);

    @Query("SELECT * FROM playlistrecord JOIN post ON playlistrecord.postID = post.uid WHERE playlistrecord.playlistID = :playlistID ORDER BY playlistrecord.uid DESC LIMIT 1")
    Post getLatestInPlaylist(long playlistID);


    @Query("SELECT post.*, playlist.uid AS playlistID FROM post JOIN playlistrecord ON playlistrecord.postID = post.uid JOIN playlist ON playlistrecord.playlistID = playlist.uid  WHERE url = :url AND (playlist.tag != " + HISTORY_TAG + " OR NOT playlist.isDefault)")
    List<PostWithPlayList> getPostInPlayListByPath(String url);

    @Insert
    long insert(Post post);

    @Delete
    int delete(Post post);

    // Playlist
    @Query("SELECT playlist.*, count(post.uid) AS count, MIN(post.img) AS preview FROM playlist LEFT JOIN playlistrecord ON playlist.uid = playlistrecord.playlistID LEFT JOIN post ON post.uid = playlistrecord.postID GROUP BY playlist.uid")
    List<PlaylistWithCount> getPlayList();

    @Query("SELECT playlist.*, count(post.uid) AS count, MIN(post.img) AS preview FROM playlist LEFT JOIN playlistrecord ON playlist.uid = playlistrecord.playlistID LEFT JOIN post ON post.uid = playlistrecord.postID WHERE (playlist.tag != " + HISTORY_TAG + " OR NOT playlist.isDefault) GROUP BY playlist.uid")
    List<PlaylistWithCount> getEditablePlayList();

    @Query("SELECT uid FROM playlist WHERE tag = :tag")
    long getPlayListIdByTag(int tag);

    @Insert
    long addPlaylist(Playlist playlist);

    // PlaylistRecord
    @Insert
    void insertRecord(PlaylistRecord record);

    @Query("DELETE FROM playlistrecord WHERE postID = :postID AND playlistID = :playlistID")
    int delete(long postID, long playlistID);

    @Query("DELETE FROM post WHERE post.uid IN (SELECT postID FROM playlistrecord WHERE playlistID = :playlistID)")
    int clearPlaylist(long playlistID);

    @Query("DELETE FROM playlistrecord WHERE playlistID = :playlistID")
    void clearRecord(long playlistID);

    @Query("DELETE FROM playlist WHERE uid = :playlistID")
    void deletePlaylist(long playlistID);
}
