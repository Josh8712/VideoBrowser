package com.jcomp.browser.download.video;

import com.jcomp.browser.download.DownloadTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HLSDownloadHandler extends DownloadHandlerBase {

    private String root;

    public HLSDownloadHandler(DownloadTask downloadTask) {
        super(downloadTask);
        root = downloadTask.getDownloadPost().videoPath;
    }

    public static String getFilenameFromURL(String line) {
        return line.substring(line.lastIndexOf("/") + 1).split("\\?")[0];
    }

    @Override
    public void _start() throws Exception {
        incrementJobs(1);
        downloadRoot(downloadTask.getDownloadPost().videoPath);
        downloadTask.setForegroundAsync(downloadTask.createForegroundInfo(100f, getSpeed()));
    }

    @Override
    public String getFileIndex() {
        return "index.m3u8";
    }

    private void downloadRoot(String url) throws Exception {
        ArrayList<String> lines = readLines(url);
        if (lines.isEmpty() || !lines.get(0).startsWith("#EXTM3U"))
            throw new Exception();
        incrementJobs(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            while (true) {
                if (downloadTask.isStopped()) {
                    downloadTask.cancel();
                    return;
                }
                if (shouldAddNewJob())
                    break;
                Thread.sleep(1000);
            }
            String line = lines.get(i);
            if (line.startsWith("#EXT-X-KEY")) {
                String key = extractKeyURL(line);
                if (key == null)
                    throw new Exception();
                addJob(() -> download(key));
                incrementProgress();
            } else if (!line.startsWith("#") && line.trim().length() > 0) {
                String filename = getFilenameFromURL(line);
                String filepath = downloadTask.getDownloadPost().localPath + filename;
                File f = new File(filepath);
                if (f.exists() && !f.isDirectory()) {
                    incrementProgress();
                    continue;
                }
                addJob(() -> {
                    download(line);
                });
            } else if (line.startsWith("#EXT-X-STREAM-INF")) {
                incrementJobs(-lines.size() + i);
                url = lines.get(i + 1);
                if (!url.startsWith("http"))
                    url = getRoot() + url;
                root = url;
                downloadRoot(url);
                return;
            } else
                incrementProgress();
        }
    }

    private ArrayList<String> readLines(String url) throws IOException {
        if (!url.startsWith("http"))
            url = getRoot() + url;
        HttpURLConnection connection = null;
        FileWriter output = null;
        ArrayList<String> lines = new ArrayList<>();
        try {
            connection = createConnection(url, downloadTask.getDownloadPost());
            output = new FileWriter(downloadTask.getDownloadPost().localPath + getFileIndex());
            BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = buffer.readLine()) != null) {
                lines.add(line);
                if (line.startsWith("http"))
                    line = getFilenameFromURL(line);
                output.write(line);
                output.write("\r\n");
            }
            output.flush();
            output.close();
            connection.disconnect();
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null)
                connection.disconnect();
            if (output != null)
                output.close();
        }
        return lines;
    }

    private String extractKeyURL(String line) {
        Pattern p = Pattern.compile("URI=\"(.*)\"");
        Matcher m = p.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private String getRoot(String url) {
        int pos = url.lastIndexOf('/');
        return url.substring(0, pos + 1);
    }

    private String getRoot() {
        return getRoot(root);
    }


    public void download(String url) {
        int retry = 0;
        if (!url.startsWith("http"))
            url = getRoot() + url;
        String filename = getFilenameFromURL(url);
        String filepath = downloadTask.getDownloadPost().localPath + filename;
        File f = new File(filepath);
        if (f.exists() && !f.isDirectory())
            return;
        while (retry < maxRetry) {
            HttpURLConnection connection = null;
            try {
                if (Thread.interrupted())
                    return;
                connection = createConnection(url, downloadTask.getDownloadPost());
                try (InputStream is = connection.getInputStream();
                     BufferedInputStream input = new BufferedInputStream(is);
                     OutputStream output = new FileOutputStream(filepath)) {
                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                        addRecord(count);
                    }
                    output.flush();
                    incrementProgress();
                }
                return;
            } catch (Exception exception) {
                if (f.exists() && !f.isDirectory())
                    f.delete();
                retry += 1;
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        }
        String finalUrl = url;
        failedQueue.add(() -> download(finalUrl));
    }


}
