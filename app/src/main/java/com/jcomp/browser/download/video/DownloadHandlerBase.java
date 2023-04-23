package com.jcomp.browser.download.video;

import android.util.Pair;

import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.download.DownloadTask;
import com.jcomp.browser.download.PreviewDownloadHandler;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.player.PreviewHandler;
import com.jcomp.browser.player.preview.ImageMissHandler;
import com.jcomp.browser.player.preview.ImageTubeHandler;
import com.jcomp.browser.player.preview.TSHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

abstract public class DownloadHandlerBase {
    private final static int THREAD_POOL_COUNT = 4;
    protected final int maxRetry = 3;
    private final List<Future<?>> futures = new ArrayList<>();
    DownloadTask downloadTask;
    long accumBytes = 0;
    Deque<Pair<Long, Long>> speed = new LinkedList<>();
    int checkPointProgress = -1;
    Deque<Runnable> failedQueue = new LinkedList<>();
    private int jobCount = 0;
    private int jobFinishedCount = 0;
    private ThreadPoolExecutor mFixedThreadPool;


    public DownloadHandlerBase(DownloadTask downloadTask) {
        if (downloadTask == null)
            return;
        this.downloadTask = downloadTask;
        mFixedThreadPool = new ThreadPoolExecutor(THREAD_POOL_COUNT, THREAD_POOL_COUNT, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        new Thread(() -> {
            while (downloadTask != null && !downloadTask.isStopped() && !downloadTask.isFinished()) {
                checkUpdate();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static HttpURLConnection createConnection(String url, DownloadPost post) throws IOException {
        HttpURLConnection connection = createRawConnection(url, post);
        connection.connect();
        return connection;
    }

    public static HttpURLConnection createRawConnection(String url, DownloadPost post) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        Map<String, String> requestHeaders = createHeader(post);
        for (Map.Entry<String, String> property : requestHeaders.entrySet()) {
            connection.setRequestProperty(property.getKey(), property.getValue());
        }
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        return connection;
    }

    public static Map<String, String> createHeader(DownloadPost downloadPost) {
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("referer", downloadPost.playerPath);
        return requestHeaders;
    }

    protected boolean shouldAddNewJob() {
        if (mFixedThreadPool.getQueue().size() < THREAD_POOL_COUNT) {
            while (mFixedThreadPool.getQueue().size() < THREAD_POOL_COUNT) {
                if (failedQueue.isEmpty())
                    return true;
                addJob(failedQueue.pop());

            }
            return false;
        }
        return false;
    }

    synchronized protected void incrementProgress() {
        jobFinishedCount += 1;
        checkUpdate();
    }

    synchronized protected void incrementJobs(int jobCount) {
        this.jobCount += jobCount;
    }

    protected void checkUpdate() {
        if (downloadTask == null)
            return;
        float progress = (float) jobFinishedCount / (jobCount + 1) * 100;
        boolean update = false;
        if (checkPointProgress < (int) progress) {
            checkPointProgress = (int) progress;
            update = true;
        }
        if (checkPointProgress % 5 == 0)
            new Thread(() -> {
                if (downloadTask != null)
                    DownloadManager.getInstance(downloadTask.getApplicationContext()).updateProgress(downloadTask.getDownloadPost(), progress);
            }).start();
        if (!update)
            update = downloadTask.shouldUpdate();
        if (update)
            downloadTask.setForegroundAsync(downloadTask.createForegroundInfo(progress, getSpeed()));
    }

    public void start() throws Exception {
        addRecord(0);
        addJob(this::downloadPreview);
        _start();
        int i = 0;
        while (i < futures.size()) {
            futures.get(i).get();
            i += 1;
        }
        finish();
        recycle();
    }

    public abstract void _start() throws Exception;

    void downloadPreview() {
        String request = downloadTask.getDownloadPost().previewPath;
        if (request == null || request.isEmpty())
            return;
        int retry = 0;
        int jobCount = 0;
        while (retry < maxRetry) {
            if (Thread.interrupted())
                return;
            try {
                PreviewDownloadHandler handler = null;
                PreviewHandler.PreviewSourceType type = PreviewHandler.checkRequestType(request);
                if (type == PreviewHandler.PreviewSourceType.Jable)
                    handler = new PreviewDownloadHandler(new TSHandler(request), DownloadTask.PreviewType.Jable);
                else if (type == PreviewHandler.PreviewSourceType.Miss)
                    handler = new PreviewDownloadHandler(new ImageMissHandler(request), DownloadTask.PreviewType.MISS);
                else if (type == PreviewHandler.PreviewSourceType.Tube)
                    handler = new PreviewDownloadHandler(new ImageTubeHandler(request), DownloadTask.PreviewType.TUBE);

                if (handler == null)
                    return;
                int _jobCount = handler.getJobCount();
                incrementJobs(_jobCount - jobCount);
                jobCount = _jobCount;
                handler.download(downloadTask.getDownloadPost().localPath, new PreviewDownloadHandler.CallBack() {
                    @Override
                    public void call() {
                        incrementProgress();
                    }
                });
                downloadTask.setPreviewType(handler.getType());
                incrementProgress();
                return;
            } catch (Exception e) {
                retry += 1;
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }
        failedQueue.add(this::downloadPreview);
    }

    protected int getSpeed() {
        if (speed.size() <= 1)
            return 0;
        return (int) ((speed.getLast().first - speed.getFirst().first) / (System.currentTimeMillis() - speed.getFirst().second) * 1000);
    }

    protected synchronized void addRecord(long bytes) {
        if (bytes == 0 && !speed.isEmpty())
            return;
        accumBytes += bytes;
        if (speed.isEmpty() || (System.currentTimeMillis() - speed.getLast().second > 1000))
            speed.add(new Pair<>(accumBytes, System.currentTimeMillis()));
        if (speed.size() > 30)
            speed.removeFirst();
    }

    protected void finish() {
        downloadTask = null;
    }

    private void recycle() {
        if (mFixedThreadPool != null) {
            mFixedThreadPool.shutdownNow();
        }
    }

    public void addJob(Runnable job) {
        futures.add(mFixedThreadPool.submit(job));
    }

    abstract public String getFileIndex();
}
