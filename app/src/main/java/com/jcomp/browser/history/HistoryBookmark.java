package com.jcomp.browser.history;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;

public class HistoryBookmark extends History {
    public static final String BOOKMARK_URL = "BOOKMARK_URL";

    public HistoryBookmark(Context context) {
        super(context.getString(R.string.playlist), BOOKMARK_URL);
        this.removable = false;
        this.graph_id = R.id.playlist_graph;
    }

    @Override
    public void setIcon(ImageView button, TextView textView) {
        button.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        button.setImageResource(R.drawable.baseline_playlist_play_24);
    }
}
