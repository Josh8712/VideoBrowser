package com.jcomp.browser.player.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.jcomp.browser.player.PreviewHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

public class ImageTubeHandler extends PreviewHandler {
    public ImageTubeHandler(String request) {
        super(request);
    }

    public void download() throws Exception {
        JSONObject json = new JSONObject(request);
        String template = json.getString("timeline_screens_url");
        template = template.replace("{time}", "{0}");
        int count = Integer.parseInt(json.getString("timeline_screens_count"));
        int interval = Integer.parseInt(json.getString("timeline_screens_interval"));

        for (int t = 0; t < count; t++) {
            cues.add(new Cue(t * interval, (t + 1) * interval, MessageFormat.format(template, t + 1)));
        }
    }

    @Override
    public void setBitmap(PreviewHandler.Cue _cue, ImageView imageView) {
        if (_cue == null)
            return;
        Cue cue = (Cue) _cue;
        getBitmap(cue, imageView);
    }

    @Override
    public Bitmap getRawBitmap(PreviewHandler.Cue _cue) {
        Cue cue = (Cue) _cue;
        try {
            URL url = new URL(cue.url);
            return BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException ignore) {
        }
        return null;
    }

    protected void getBitmap(Cue cue, ImageView imageView) {
        Picasso.get().load(cue.url).into(imageView);
    }

    public PreviewHandler.Cue getCue(long time, long totalTime) {
        time /= 1000;
        if (cues.isEmpty())
            return null;
        for (PreviewHandler.Cue cue : cues) {
            if (cue.start >= time) {
                return cue;
            }
        }
        return null;
    }

    public class Cue extends PreviewHandler.Cue {
        public String url;

        public Cue(int start, int end, String url) {
            super(start, end);
            this.url = url;
        }
    }
}
