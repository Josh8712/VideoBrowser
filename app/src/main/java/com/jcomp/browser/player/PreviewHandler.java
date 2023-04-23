package com.jcomp.browser.player;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.jcomp.browser.player.preview.ImageMissHandler;
import com.jcomp.browser.player.preview.ImageTubeHandler;
import com.jcomp.browser.player.preview.TSHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

abstract public class PreviewHandler {

    private final Thread thread;
    protected String request;
    protected ArrayList<Cue> cues = new ArrayList<>();

    public PreviewHandler(String request) {
        this.request = request;
        thread = new Thread(() -> {
            try {
                download();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        });
        thread.start();
    }

    public static PreviewHandler parseRequest(String url) {
        PreviewSourceType type = checkRequestType(url);
        if (type == PreviewSourceType.Miss)
            return new ImageMissHandler(url);
        else if (type == PreviewSourceType.Jable) {
            return new TSHandler(url);
        }
        if (type == PreviewSourceType.Tube)
            return new ImageTubeHandler(url);
        return null;
    }

    public static PreviewSourceType checkRequestType(String url) {
        if (url == null)
            return null;
        if (url.startsWith("http"))
            return PreviewSourceType.Jable;
        else
            return parseJSONRequest(url);
    }

    private static PreviewSourceType parseJSONRequest(String url) {
        try {
            JSONObject json = new JSONObject(url);
            if (json.has("col") && json.has("row") && json.has("urls")) {
                return PreviewSourceType.Miss;
            } else if (json.has("timeline_screens_url"))
                return PreviewSourceType.Tube;
            return null;
        } catch (JSONException e) {
            return null;
        }
    }

    public void join() throws InterruptedException {
        thread.join();
    }

    public abstract void download() throws Exception;

    abstract public void setBitmap(Cue cue, ImageView imageView);

    abstract public Bitmap getRawBitmap(Cue cue);

    abstract public Cue getCue(long time, long totalTime);

    protected String getRoot(String root) {
        int pos = root.lastIndexOf('/');
        return root.substring(0, pos + 1);
    }

    public ArrayList<Cue> getCues() {
        return cues;
    }

    public void recycle() {

    }

    public enum PreviewSourceType {
        Jable, Miss, Tube
    }

    public abstract class Cue {
        public int start, end;

        public Cue(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
