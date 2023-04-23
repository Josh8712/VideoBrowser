package com.jcomp.browser.history;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;

public class HistoryDownload extends History {
    public static final String DOWNLOAD_URL = "DOWNLOAD_URL";

    public HistoryDownload(Context context) {
        super(context.getString(R.string.download), DOWNLOAD_URL);
        this.removable = false;
        this.graph_id = R.id.download_graph;
    }

    @Override
    public void setIcon(ImageView button, TextView textView) {
        button.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        button.setImageResource(R.drawable.ic_baseline_download_24);
    }
}
