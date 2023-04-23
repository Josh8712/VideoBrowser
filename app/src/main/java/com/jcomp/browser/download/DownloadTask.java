package com.jcomp.browser.download;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.download.video.DownloadHandlerBase;
import com.jcomp.browser.download.video.HLSDownloadHandler;
import com.jcomp.browser.download.video.Mp4DownloadHandler;
import com.jcomp.browser.history.HistoryDownload;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.tools.HelperFunc;
import com.jcomp.browser.tools.Notification;
import com.jcomp.browser.viewer.video_loader.LocalLoader;
import com.jcomp.browser.welcome.Welcome;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;

public class DownloadTask extends Worker {
    public static final String POST_ID_KEY = "POST_ID";
    public static final String DOWNLOAD_INFO_FILENAME = "info.txt";
    DownloadPost downloadPost;
    StorageInfo storageInfo;
    long taskID;
    NotificationCompat.Builder notificationBuilder;
    long lastTrigger = 0;
    PendingIntent playIntent = null;
    private DownloadPostDoa db;
    private long jobCounter;
    private boolean finished = false;

    public DownloadTask(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        db = AppDatabase.getInstance(getApplicationContext()).downloadPostDao();
        downloadPost = db.getByUID(getInputData().getLong(POST_ID_KEY, -1));
        if (downloadPost == null)
            return Result.failure();
        db.updateStatusByUID(downloadPost.uid, DownloadPost.Status.RUNNING);
        downloadPost = db.getByUID(getInputData().getLong(POST_ID_KEY, -1));
        if (downloadPost == null || downloadPost.status != DownloadPost.Status.RUNNING)
            return Result.failure();

        jobCounter = downloadPost.jobCounter;
        taskID = downloadPost.uid;
        setForegroundAsync(createForegroundInfo());
        try {
            start();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            failed();
            return Result.retry();
        } finally {
            finished = true;
        }

        return Result.success();
    }

    public void start() throws Exception {
        VideoType type = checkType();
        DownloadHandlerBase handler;
        switch (type) {
            case HLS:
                handler = new HLSDownloadHandler(this);
                break;
            case MP4:
                handler = new Mp4DownloadHandler(this);
                break;
            default:
                throw new Exception();
        }
        storageInfo = new StorageInfo(type, handler.getFileIndex());
        storeInfo();
        handler.start();
    }

    public VideoType checkType() {
        try {
            HttpURLConnection connection = DownloadHandlerBase.createConnection(downloadPost.videoPath, downloadPost);
            System.out.println(connection.getResponseCode());
            String type = connection.getHeaderField("content-type");
            connection.disconnect();
            if (type.contains("video"))
                return VideoType.MP4;
            else
                return VideoType.HLS;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return VideoType.ERROR;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setPreviewType(PreviewType previewType) throws IOException {
        storageInfo.previewType = previewType;
        storeInfo();
    }

    private void storeInfo() throws IOException {
        FileWriter file = new FileWriter(downloadPost.localPath + DOWNLOAD_INFO_FILENAME);
        file.write(new Gson().toJson(storageInfo));
        file.flush();
        file.close();
    }

    protected ForegroundInfo createForegroundInfo() {
        return createForegroundInfo(-1, -1);
    }

    private PendingIntent getCancelIntent() {
        Intent intent = new Intent(getApplicationContext(), DownloadTaskReceiver.class);
        intent.putExtra(DownloadTaskReceiver.ID_KEY, getDownloadPost().uid);
        intent.putExtra(DownloadTaskReceiver.TASK_ID, DownloadTaskReceiver.Task.STOP);
        return PendingIntent.getBroadcast(
                getApplicationContext(),
                (int) getDownloadPost().uid, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Intent getDownloadPageIntent() {
        Intent downloadIntent = new Intent(getApplicationContext(), MainActivity.class);
        downloadIntent.putExtra(Welcome.HISTORY_INTENT_KEY, new Gson().toJson(new HistoryDownload(getApplicationContext())));
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return downloadIntent;
    }

    private PendingIntent getPlayIntent() {
        Intent intent = LocalLoader.getPlayDownloadedIntent(getApplicationContext(), downloadPost);
        if (intent == null)
            return null;
        Intent downloadIntent = getDownloadPageIntent();
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(downloadIntent);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent((int) getDownloadPost().uid,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public ForegroundInfo createForegroundInfo(float progress, int speed) {
        setProgressAsync(new Data.Builder().putFloat("progress", progress).putInt("speed", speed).build());
        lastTrigger = System.currentTimeMillis();
        String progressTitle = " " + HelperFunc.humanReadableByteCountBin(speed) + "/s";
        String title = getApplicationContext().getString(com.google.android.exoplayer2.core.R.string.exo_download_downloading);
        String pause = getApplicationContext().getString(R.string.pause);
        String play = getApplicationContext().getString(R.string.play);
        if (notificationBuilder == null) {

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addNextIntentWithParentStack(getDownloadPageIntent());

            Notification.createDownloadChannel(getApplicationContext());
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), Notification.DOWNLOAD_CHANNEL_ID)
                    .setContentTitle(title)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setOngoing(true)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(downloadPost.getTitle()))
                    .setContentIntent(stackBuilder.getPendingIntent(Notification.DOWNLOAD_PAGE_INTENT_ID, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                    .addAction(android.R.drawable.ic_delete, pause, getCancelIntent());
        }
        if (playIntent == null) {
            playIntent = getPlayIntent();
            if (playIntent != null) {
                notificationBuilder.addAction(android.R.drawable.ic_media_play, play, playIntent);
            }
        }
        if (progress == -1) {
            notificationBuilder = notificationBuilder.setProgress(100, (int) progress, true)
                    .setContentText(title);
        } else {
            notificationBuilder = notificationBuilder.setProgress(100, (int) progress, false)
                    .setContentText(progressTitle);
        }
        return new ForegroundInfo((int) taskID, notificationBuilder.build());
    }

    public boolean shouldUpdate() {
        return System.currentTimeMillis() - lastTrigger > 1000;
    }

    public void cancel() {
        if (!shouldHandle())
            return;
        DownloadManager.getInstance(getApplicationContext()).pauseJob(downloadPost);
    }

    public void finish() throws IOException {
        if (!shouldHandle())
            return;
        storeInfo();
        db.updateStatusByUID(taskID, DownloadPost.Status.FINISHED);

        com.jcomp.browser.tools.Notification.createDownloadStatusChannel(getApplicationContext());
        String title = getApplicationContext().getString(com.google.android.exoplayer2.core.R.string.exo_download_completed);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), Notification.DOWNLOAD_STATUS_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(downloadPost.getTitle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(R.drawable.baseline_check_24)
                .setAutoCancel(true);
        PendingIntent intent = getPlayIntent();
        if (intent != null)
            builder.setContentIntent(intent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify((int) -taskID, builder.build());
    }

    public void failed() {
        setProgressAsync(new Data.Builder().putBoolean("failed", true).build());
        if (!shouldHandle())
            return;
        db.updateStatusByUID(taskID, DownloadPost.Status.FAILED);
    }

    private boolean shouldHandle() {
        if (downloadPost == null || downloadPost.jobCounter != jobCounter)
            return false;
        downloadPost = DownloadManager.getInstance(getApplicationContext()).getRecord(downloadPost);
        return downloadPost != null && downloadPost.status == DownloadPost.Status.RUNNING;
    }

    public DownloadPost getDownloadPost() {
        return downloadPost;
    }

    enum VideoType {
        HLS, MP4, ERROR
    }

    public enum PreviewType {
        Jable, MISS, TUBE, ERROR
    }

    public static class StorageInfo {
        public VideoType videoType;
        public PreviewType previewType;
        public String indexName;

        public StorageInfo(VideoType videoType, String indexName) {
            this.videoType = videoType;
            this.indexName = indexName;
            this.previewType = PreviewType.ERROR;
        }
    }
}
