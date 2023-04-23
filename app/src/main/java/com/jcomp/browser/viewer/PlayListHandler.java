package com.jcomp.browser.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.cloud.FireStore;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistRecord;
import com.jcomp.browser.parser.post.db.PlaylistWithCount;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.parser.post.db.PostWithPlayList;

import java.util.HashMap;
import java.util.List;

public class PlayListHandler {
    public static long defaultPlaylistID = -1;
    public static long defaultWatchlistID = -1;
    public static void updatePlaylistID(Context context) {
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        if(defaultPlaylistID == -1)
            defaultPlaylistID = db.getPlayListIdByTag(PlaylistDoa.DEFAULT_PLAYLIST_TAG);
        if(defaultWatchlistID == -1)
            defaultWatchlistID = db.getPlayListIdByTag(PlaylistDoa.HISTORY_TAG);
    }

    public static void insert(Post post, long playlistID, Context context) {
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        long postID = db.insert(new Post(post));
        db.insertRecord(new PlaylistRecord(postID, playlistID));
    }


    public static void remove(PostWithPlayList post, Context context) {
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        db.delete(post.uid, post.playlistID);
        db.delete(post);
    }

    public static void clearPlaylist(long playlistID, Context context) {
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        db.clearPlaylist(playlistID);
        db.clearRecord(playlistID);
    }

    public static void insertToDefaultPlayList(Post post, Context context) {
        updatePlaylistID(context);
        insert(post, defaultPlaylistID, context);
    }

    public static void insertToDefaultHistoryList(Post post, Context context) {
        updatePlaylistID(context);
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        Post latestPost = db.getLatestInPlaylist(defaultWatchlistID);
        if(latestPost != null && latestPost.url.equals(post.url))
            return;
        insert(post, defaultWatchlistID, context);
    }

    public static List<PostWithPlayList> checkRecordByURL(String url, Context context) {
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        return db.getPostInPlayListByPath(url);
    }

    public static void deletePlaylist(long playlistID, Context context) {
        clearPlaylist(playlistID, context);
        PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
        db.deletePlaylist(playlistID);
        db.clearRecord(playlistID);
    }

    public static void addPlayList(Context context, CallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.add_playlist);

        final EditText input = new EditText(context);

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.rightMargin = 40;
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        builder.setPositiveButton(R.string.add, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            String m_Text = input.getText().toString().trim();
            if (m_Text.isEmpty()) {
                input.setError(context.getText(R.string.empty_name_error));
                input.requestFocus();
                return;
            }
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Thread(()-> {
                PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
                long result = db.addPlaylist(new Playlist(m_Text));
                input.post(() -> {
                    progressDialog.dismiss();
                    if(result > 0) {
                        alertDialog.dismiss();
                        callBack.onDone();
                    }
                    else
                        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                });
            }).start();
        });
    }

    public static void managePlayList(Context context, Post post, CallBack callBack) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getText(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(()-> {
            PlaylistDoa db = AppDatabase.getInstance(context).playlistDoa();
            List<PlaylistWithCount> playlists = db.getEditablePlayList();
            List<PostWithPlayList> result = db.getPostInPlayListByPath(post.url);
            HashMap<Long, PostWithPlayList> map = new HashMap<>();
            for (PostWithPlayList postWithPlayList : result) {
                map.put(postWithPlayList.playlistID, postWithPlayList);
            }

            ((Activity)context).runOnUiThread(() -> {
                progressDialog.dismiss();
                // Set up the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.change_playlist);

                String[] playlistNames = new String[playlists.size()];
                boolean[] checkedItems = new boolean[playlistNames.length];
                boolean[] oriItems = new boolean[playlistNames.length];

                for (int i = 0; i < playlistNames.length; i++) {
                    playlistNames[i] = playlists.get(i).getName();
                    if(map.containsKey(playlists.get(i).uid)) {
                        checkedItems[i] = true;
                        oriItems[i] = true;
                    }
                }

                builder.setMultiChoiceItems(playlistNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                });

                builder.setPositiveButton(R.string.ok, null);
                builder.setNegativeButton(R.string.cancel,null);

                AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    progressDialog.show();
                    new Thread(()->{
                        for(int i = 0; i < oriItems.length; i++) {
                            if(oriItems[i] != checkedItems[i]) {
                                if(checkedItems[i]) {
                                    insert(post, playlists.get(i).uid, context);
                                } else {
                                    remove(map.get(playlists.get(i).uid), context);
                                }
                            }
                        }
                        ((Activity)context).runOnUiThread(() -> {
                            progressDialog.dismiss();
                            dialog.dismiss();
                            callBack.onDone();
                        });
                    }).start();
                });
            });
        }).start();
    }

    public interface CallBack {
        void onDone();
    }
}
