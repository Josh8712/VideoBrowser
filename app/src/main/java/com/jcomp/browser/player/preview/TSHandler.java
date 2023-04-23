package com.jcomp.browser.player.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.jcomp.browser.player.PreviewHandler;

import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TSHandler extends PreviewHandler {
    Bitmap bitmap = null;

    public TSHandler(String request) {
        super(request);
    }

    public void download() throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(this.request)
                .build();
        Response response = client.newCall(request).execute();
        String body = response.body().string();
        String[] lines = body.split("\n");
        String filename = parseCue(lines);

        URL imageURL = new URL(getRoot(this.request) + filename);
        bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
    }

    @Override
    public void setBitmap(PreviewHandler.Cue _cue, ImageView imageView) {
        if (bitmap == null || cues.isEmpty() || _cue == null)
            return;
        Cue cue = (Cue) _cue;
        imageView.post(() -> {
            imageView.setImageBitmap(Bitmap.createBitmap(bitmap, cue.x, cue.y, cue.w, cue.h));
        });
    }

    public Bitmap getRawBitmap(PreviewHandler.Cue _cue) {
        if (bitmap == null || cues.isEmpty() || _cue == null)
            return null;
        Cue cue = (Cue) _cue;
        return Bitmap.createBitmap(bitmap, cue.x, cue.y, cue.w, cue.h);
    }

    public PreviewHandler.Cue getCue(long time, long totalTime) {
        time /= 1000;
        if (bitmap == null || cues.isEmpty())
            return null;
        for (PreviewHandler.Cue cue : cues) {
            if (cue.start >= time) {
                return cue;
            }
        }
        return null;
    }

    protected int parseTimeCode(String timeCodeString) {
        int hour = Integer.parseInt(timeCodeString.substring(0, 2));
        int minute = Integer.parseInt(timeCodeString.substring(3, 5));
        int second = Integer.parseInt(timeCodeString.substring(6, 8));
        return (hour * 60 + minute) * 60 + second;
    }


    public String parseCue(String[] lines) {
        int start = 2;
        String filename = "";
        while (start + 1 < lines.length) {
            int s = parseTimeCode(lines[start].substring(0, 12));
            int e = parseTimeCode(lines[start].substring(17));
            filename = lines[start + 1].split("#")[0];
            String[] pos = lines[start + 1].split("=")[1].split(",");
            cues.add(new Cue(s, e, Integer.valueOf(pos[0]), Integer.valueOf(pos[1]), Integer.valueOf(pos[2]), Integer.valueOf(pos[3])));
            start += 3;
        }
        return filename;
    }

    public class Cue extends PreviewHandler.Cue {
        int x, y, w, h;

        public Cue(int start, int end, int x, int y, int w, int h) {
            super(start, end);
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
