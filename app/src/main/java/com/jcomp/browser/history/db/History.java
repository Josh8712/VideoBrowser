package com.jcomp.browser.history.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.net.MalformedURLException;
import java.net.URL;

@Entity
public class History {
    protected int graph_id;
    protected boolean removable = true;
    @PrimaryKey
    @NonNull
    String url;
    String title, favicon, customName;
    long timestamp;

    public History(int graph_id, String title, String customName, @NonNull String url, String favicon, long timestamp) {
        this.graph_id = graph_id;
        this.title = title;
        this.customName = customName;
        this.url = url;
        this.favicon = favicon;
        this.timestamp = timestamp;
    }

    @Ignore
    public History(String title, @NonNull String url, String favicon) {
        if (title == null)
            title = "";
        this.title = title;
        this.url = url;
        this.timestamp = System.currentTimeMillis();
        this.graph_id = -1;
        this.favicon = favicon;
    }

    @Ignore
    public History(String title, String url) {
        this(title, url, null);
    }

    public String getTitle() {
        return title;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public String getFavicon() {
        return favicon;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSpecialTab() {
        return graph_id != -1;
    }

    public int getId() {
        return graph_id;
    }

    public void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public void setIcon(ImageView button, TextView placeholder) {
        if (favicon != null) {
            try {
                byte[] bytes = Base64.decode(favicon, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap == null)
                    throw new Exception();
                button.setVisibility(View.VISIBLE);
                placeholder.setVisibility(View.GONE);
                button.setImageBitmap(bitmap);
            } catch (Exception e) {
                favicon = null;
            }
        }
        if (favicon == null) {
            button.setVisibility(View.GONE);
            placeholder.setVisibility(View.VISIBLE);
            String domain = getDomainName();
            if (domain == null || domain.isEmpty())
                placeholder.setText(null);
            else
                placeholder.setText(String.valueOf(domain.toUpperCase().charAt(0)));
        }
    }

    public void update(String favicon, String title) {
        if (favicon != null)
            this.favicon = favicon;
        if (title != null && !title.isEmpty())
            this.title = title;
    }

    private String getDomainName() {
        try {
            String[] cap = new URL(url).getHost().split("\\.");
            if (cap[0].equals("www"))
                return cap[1];
            return cap[0];
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public String getDisplayName() {
        String domain = customName;
        if (domain == null || domain.isEmpty()) {
            domain = getTitle();
            if (domain == null || domain.isEmpty()) {
                domain = getDomainName();
                if (domain == null)
                    domain = getUrl();
            }
        }
        return domain;
    }

    public boolean isRemovable() {
        return removable;
    }
}
