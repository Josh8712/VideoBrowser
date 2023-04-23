package com.jcomp.browser.download;

import android.graphics.Bitmap;

import com.jcomp.browser.player.PreviewHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class PreviewDownloadHandler {
    public static final String PREVIEW_FILENAME = "preview.txt";
    public static final String PREVIEW_FILE_PREFIX = "cue-";
    public static final String SEP = " ";
    public static final String NEWLINE = "\r\n";
    PreviewHandler handler;
    DownloadTask.PreviewType type;

    public PreviewDownloadHandler(PreviewHandler handler, DownloadTask.PreviewType type) {
        this.handler = handler;
        this.type = type;
    }

    public int getJobCount() throws Exception {
        handler.join();
        return handler.getCues().size();
    }

    public void download(String dir, CallBack callBack) throws Exception {
        ArrayList<PreviewHandler.Cue> cues = handler.getCues();
        int idx = 0;
        FileWriter writer = new FileWriter(dir + PREVIEW_FILENAME);
        for (PreviewHandler.Cue cue : cues) {
            writer.write(String.valueOf(cue.start));
            writer.write(SEP);
            writer.write(String.valueOf(cue.end));
            writer.write(SEP);
            writer.write(dir + PREVIEW_FILE_PREFIX + idx);
            writer.write(NEWLINE);
            idx += 1;
        }
        writer.close();
        idx = 0;
        try {
            for (PreviewHandler.Cue cue : cues) {
                File file = new File(dir + PREVIEW_FILE_PREFIX + idx);
                if (file.exists())
                    continue;
                FileOutputStream out = new FileOutputStream(file);
                Bitmap bitmap = handler.getRawBitmap(cue);
                if (bitmap == null)
                    throw new Exception();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                bitmap.recycle();
                out.flush();
                out.close();
                idx += 1;
                callBack.call();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            handler.recycle();
        }

    }

    public DownloadTask.PreviewType getType() {
        return type;
    }

    public abstract static class CallBack {
        abstract public void call();
    }
}
