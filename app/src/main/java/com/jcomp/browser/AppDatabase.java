package com.jcomp.browser;

import android.content.Context;

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
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.Post;

@Database(entities = {DownloadPost.class, History.class, Post.class},
        version = 1)
@TypeConverters({Post.Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE post "
                    + " ADD COLUMN streamName TEXT");
            database.execSQL("ALTER TABLE DownloadPost "
                    + " ADD COLUMN streamName TEXT");
        }
    };
    static AppDatabase appDatabase;

    public static AppDatabase getInstance(Context context) {
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(context,
                    AppDatabase.class, "database").addMigrations(MIGRATION_5_6).build();
        return appDatabase;
    }

    public abstract DownloadPostDoa downloadPostDao();

    public abstract HistoryDoa historyDoa();

    public abstract PlaylistDoa playlistDoa();
}
