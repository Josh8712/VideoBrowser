package com.jcomp.browser.player.preview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.jcomp.browser.download.PreviewDownloadHandler;
import com.jcomp.browser.player.PreviewHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class GenerateHandler extends PreviewHandler {
    ArrayList<Cue> cues = new ArrayList<>();

    public GenerateHandler(String request) {
        super(request);
    }

    public void download() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(Uri.parse(request).getPath()));
        ArrayList<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line);
        String[] arr = new String[lines.size()];
        lines.toArray(arr);

        parseCue(arr);
    }

    @Override
    public void setBitmap(PreviewHandler.Cue _cue, ImageView imageView) {
        if (cues.isEmpty() || _cue == null)
            return;
        GenerateHandler.Cue cue = (Cue) _cue;
        imageView.post(() -> {
            imageView.setImageBitmap(BitmapFactory.decodeFile(cue.path));
        });
    }

    @Override
    public Bitmap getRawBitmap(PreviewHandler.Cue _cue) {
        if (cues.isEmpty() || _cue == null)
            return null;
        GenerateHandler.Cue cue = (Cue) _cue;
        return BitmapFactory.decodeFile(cue.path);
    }

    @Override
    public PreviewHandler.Cue getCue(long time, long totalTime) {
        time /= 1000;
        totalTime /= 1000;
        if (cues.isEmpty())
            return null;
        for (PreviewHandler.Cue cue : cues) {
            float start = cue.start;
            if (cue.start <= 0)
                start *= -((float) totalTime / cues.size());
            if (start >= time)
                return cue;
        }
        return null;
    }

    private void parseCue(String[] arr) {
        for (String c : arr) {
            String[] segs = c.split(PreviewDownloadHandler.SEP);
            cues.add(new Cue(Integer.parseInt(segs[0]), Integer.parseInt(segs[1]), segs[2]));
        }
    }

    class Cue extends PreviewHandler.Cue {
        String path;

        public Cue(int start, int end, String path) {
            super(start, end);
            this.path = path;
        }
    }
}
