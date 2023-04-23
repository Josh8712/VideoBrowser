package com.jcomp.browser;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.history.db.HistoryDoa;
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistRecord;
import com.jcomp.browser.parser.post.db.Post;

@Database(entities = {DownloadPost.class, History.class, Post.class, Playlist.class, PlaylistRecord.class},
        version = 2,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        })
@TypeConverters({Post.Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "database").build();
        return appDatabase;
    }

    public abstract DownloadPostDoa downloadPostDao();

    public abstract HistoryDoa historyDoa();

    public abstract PlaylistDoa playlistDoa();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create the new table Playlist
//            database.execSQL("ALTER TABLE post "
//                    + " ADD COLUMN playlistID INTEGER");
//            database.execSQL("ALTER TABLE DownloadPost "
//                    + " ADD COLUMN playlistID INTEGER");
        }
    };

}
