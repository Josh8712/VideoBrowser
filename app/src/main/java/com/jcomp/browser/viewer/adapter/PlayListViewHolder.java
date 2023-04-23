package com.jcomp.browser.viewer.adapter;

import android.app.ProgressDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.menu.Popup;
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistWithCount;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.viewer.PlayListHandler;
import com.jcomp.browser.viewer.video_loader.DownloaderLoader;
import com.jcomp.browser.viewer.video_loader.LocalListLoader;
import com.jcomp.browser.widget.BreathingAnim;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayListViewHolder extends RecyclerView.ViewHolder {

    final ImageView preview;
    final TextView name, count;

    public PlayListViewHolder(@NonNull View view) {
        super(view);
        preview = view.findViewById(R.id.preview);
        name = view.findViewById(R.id.name);
        count = view.findViewById(R.id.count);
    }

    private void openPopup(View view, PlaylistWithCount playlist, int position) {
        PopupMenu menu = new PopupMenu(view.getContext(), view, Gravity.END);
        menu.getMenu().add(Menu.NONE, 1, 1, R.string.open);
        menu.getMenu().add(Menu.NONE, 2, 1, R.string.clear_playlist);
        if(!playlist.isDefault)
            menu.getMenu().add(Menu.NONE, 3, 1, R.string.delete_playlist);
        menu.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == 1)
                view.performClick();
            else {
                ProgressDialog dialog = new ProgressDialog(view.getContext());
                dialog.setCancelable(false);
                dialog.show();
                switch (item.getItemId()) {
                    case 2:
                        new Thread(() -> {
                            PlayListHandler.clearPlaylist(playlist.uid, view.getContext());
                            view.post(() -> {
                                playlist.clear();
                                dialog.dismiss();
                                if (getBindingAdapter() != null) {
                                    getBindingAdapter().notifyItemChanged(position);
                                }
                            });
                        }).start();
                        break;
                    case 3:
                        new Thread(() -> {
                            PlayListHandler.deletePlaylist(playlist.uid, view.getContext());
                            view.post(() -> {
                                if (getBindingAdapter() != null) {
                                    ((PlayListAdapter)getBindingAdapter()).removeItem(position);
                                }
                                dialog.dismiss();
                            });
                        }).start();
                        break;
                }
            }
            return true;
        });
        menu.show();
    }

    public void bind(PlaylistWithCount playlist) {
        count.setText(String.valueOf(playlist.getCount()));
        name.setText(playlist.getName());
        String preview_url = playlist.getPreview();
        Picasso.get().load(preview_url).placeholder(R.drawable.gallery_placeholder).into(preview);
        itemView.setOnLongClickListener(view -> {
            openPopup(view, playlist, getAbsoluteAdapterPosition());
            return true;
        });
    }
}

