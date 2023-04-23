package com.jcomp.browser.player.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.jcomp.browser.player.PreviewHandler;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ImageMissHandler extends PreviewHandler {
    Bitmap cache;
    String cacheCue = null;
    private int width, height, col, row;
    private Picasso picasso;

    public ImageMissHandler(String request) {
        super(request);
    }

    public void download() throws Exception {
        JSONObject json = new JSONObject(request);
        col = json.getInt("col");
        row = json.getInt("row");
        width = json.getInt("width");
        height = json.getInt("height");
        int pic_num = json.getInt("pic_num");

        JSONArray urls = json.getJSONArray("urls");
        int idx = 0;
        for (int t = 0; t < urls.length(); t++) {
            for (int h = 0; h < row; h++)
                for (int w = 0; w < col; w++) {
                    cues.add(new Cue(-idx, -(idx + 1), w * width, h * height, urls.getString(t)));
                    idx += 1;
                    if (idx >= pic_num)
                        return;
                }
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
        if (cue.url.equals(cacheCue)) {
            return Bitmap.createBitmap(cache, cue.x, cue.y, width, height);
        }
        try {
            URL url = new URL(cue.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("referer", cue.url);
            connection.setRequestMethod("GET");
            connection.connect();
            cache = BitmapFactory.decodeStream(connection.getInputStream());
            cacheCue = cue.url;
            return Bitmap.createBitmap(cache, cue.x, cue.y, width, height);
        } catch (IOException ignore) {
        }
        return null;
    }

    protected void getBitmap(Cue cue, ImageView imageView) {
        if (picasso == null) {
            Context context = imageView.getContext();
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("referer", cue.url)
                                .build();
                        return chain.proceed(newRequest);
                    })
                    .build();
            picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(client)).build();
        }
        picasso.load(cue.url).transform(new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap newOne = Bitmap.createBitmap(source, cue.x, cue.y, width, height);
                source.recycle();
                return newOne;
            }

            @Override
            public String key() {
                return cue.x + "-" + cue.y;
            }
        }).into(imageView);
    }

    public PreviewHandler.Cue getCue(long time, long totalTime) {
        time /= 1000;
        totalTime /= 1000;
        float interval = (float) totalTime / cues.size();
        int thumbNum = (int) Math.floor(((float) time / interval));
        if (thumbNum >= cues.size())
            return null;
        return cues.get(thumbNum);
    }

    @Override
    public void recycle() {
        if (cache != null) {
            cache.recycle();
            cacheCue = null;
            cache = null;
        }
    }

    public class Cue extends PreviewHandler.Cue {
        public String url;
        int x, y;

        public Cue(int start, int end, int x, int y, String url) {
            super(start, end);
            this.x = x;
            this.y = y;
            this.url = url;
        }
    }
}
