package com.jcomp.browser.viewer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jcomp.browser.R;
import com.jcomp.browser.parser.model.Model;
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistWithCount;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.viewer.video_loader.ResourceLoader;

import java.util.LinkedHashMap;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<PlaylistWithCount> playlists;
    CallBack openCallBack;

    public PlayListAdapter(List<PlaylistWithCount> playlists, CallBack openCallBack) {
        this.playlists = playlists;
        this.openCallBack = openCallBack;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PlaylistWithCount playlist = playlists.get(position);
        PlayListViewHolder viewHolder = (PlayListViewHolder) holder;
        viewHolder.bind(playlist);
        holder.itemView.setOnClickListener(v -> {
            openCallBack.onClick(playlist);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void reset(List<PlaylistWithCount> playlists) {
        this.playlists.clear();
        this.playlists.addAll(playlists);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if(position < 0 || position >= playlists.size())
            return;
        playlists.remove(position);
        notifyItemRemoved(position);
    }

    public interface CallBack {
        void onClick(PlaylistWithCount playlist);
    }
}
