package com.jcomp.browser.download;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.common.util.concurrent.ListenableFuture;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.download.db.DownloadPost;
import com.jcomp.browser.download.db.DownloadPostDoa;
import com.jcomp.browser.player.PlayerInfo;
import com.jcomp.browser.player.VideoPlayerInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private static DownloadManager downloadManager;
    private final Context context;
    private final DownloadPostDoa db;

    public DownloadManager(Context context) {
        db = AppDatabase.getInstance(context).downloadPostDao();
        this.context = context;
    }

    @NonNull
    public static DownloadManager getInstance(Context context) {
        if (downloadManager == null) {
            downloadManager = new DownloadManager(context);
        }
        return downloadManager;
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public DownloadPost getRecord(PlayerInfo playerInfo) {
        return validateRecord(db.getByPath(playerInfo.getKey()));
    }

    public DownloadPost getRecord(DownloadPost downloadPost) {
        if (downloadPost == null)
            return null;
        return getRecord(downloadPost.uid);
    }

    public DownloadPost getRecord(long uid) {
        return validateRecord(db.getByUID(uid));
    }

    public List<DownloadPost> getRecord(String url) {
        List<DownloadPost> list = db.getAllByURL(url);
        list.replaceAll(this::validateRecord);
        return list;
    }

    private String getTag(long uid) {
        return String.valueOf(uid);
    }

    public DownloadPost validateRecord(DownloadPost downloadPost) {
        if (downloadPost == null)
            return null;
        switch (downloadPost.status) {
            case FINISHED:
            case DELETED:
            case FAILED:
            case PAUSE:
                return downloadPost;
        }
        String tag = getTag(downloadPost.uid);
        ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance(context).getWorkInfosByTag(tag);
        boolean running = false;
        boolean queuing = false;
        try {
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING;
                queuing = state == WorkInfo.State.ENQUEUED;
                if (running || queuing)
                    break;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if (!running && !queuing) {
            db.updateStatusByUID(downloadPost.uid, DownloadPost.Status.FAILED);
            return getRecord(downloadPost);
        }
        if (queuing) {
            downloadPost.status = DownloadPost.Status.PENDING;
        }
        return downloadPost;
    }

    public void download(PlayerInfo playerInfo) {
        new Thread(() -> {
            DownloadPost downloadPost = getRecord(playerInfo);
            if (downloadPost != null)
                return;
            downloadPost = createStorage(playerInfo);
            download(downloadPost);
        }).start();
    }

    public boolean download(DownloadPost downloadPost) {
        if (downloadPost == null)
            return false;
        if (!requireStorage(downloadPost)) {
            db.updateStatusByUID(downloadPost.uid, DownloadPost.Status.FAILED);
            return false;
        }
        db.updateStatusByUID(downloadPost.uid, DownloadPost.Status.PENDING);
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(DownloadTask.class)
                        .setInputData(new Data.Builder()
                                .putLong(DownloadTask.POST_ID_KEY, downloadPost.uid)
                                .build()
                        ).addTag(getTag(downloadPost.uid))
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS)
                        .build();
        WorkManager
                .getInstance(context)
                .enqueue(uploadWorkRequest);
        return true;
    }

    private boolean requireStorage(DownloadPost downloadPost) {
        File storage = new File(downloadPost.localPath);
        if (!storage.exists())
            storage.mkdir();
        return storage.exists();
    }

    public DownloadPost createStorage(PlayerInfo playerInfo) {
        if (getRecord(playerInfo) != null)
            return null;
        int v = 0;
        File target = null;
        int fileHash;
        while (target == null || target.exists()) {
            v += 1;
            fileHash = (playerInfo.getKey() + v).hashCode();
            target = new File(context.getFilesDir(), String.valueOf(fileHash));
        }
        String localPath = target.getAbsolutePath() + File.separator;
        DownloadPost dbPost = new DownloadPost(playerInfo, localPath);
        dbPost.uid = db.insert(dbPost);
        if (dbPost.uid > 0) {
            if (!target.mkdir()) {
                db.updateStatusByUID(dbPost.uid, DownloadPost.Status.FAILED);
                dbPost = null;
            }
        } else
            dbPost = null;
        return dbPost;
    }

    public boolean deleteStorage(DownloadPost downloadPost) {
        if (downloadPost == null || downloadPost.localPath == null)
            return false;
        File folder = new File(downloadPost.localPath);
        if (!folder.exists())
            return true;
        return deleteDirectory(folder);
    }

    public boolean deleteDirectory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (int i = 0; i < (files != null ? files.length : 0); i++) {
                    if (files[i].isDirectory()) {
                        if (!deleteDirectory(files[i]))
                            return false;
                    } else {
                        if (!files[i].delete())
                            return false;
                    }
                }
            }
            return file.delete();
        }
        return true;
    }

    public boolean deleteJob(DownloadPost downloadPost) {
        WorkManager.getInstance(context).cancelAllWorkByTag(getTag(downloadPost.uid));
        db.updateStatusByUID(downloadPost.uid, DownloadPost.Status.REMOVING);
        if (deleteStorage(downloadPost))
            return db.delete(downloadPost) > 0;
        return false;
    }

    public void pauseJob(DownloadPost downloadPost) {
        pauseJob(downloadPost.uid);
    }

    public void pauseJob(long uid) {
        db.updateStatusByUID(uid, DownloadPost.Status.PAUSE);
        WorkManager.getInstance(context).cancelAllWorkByTag(getTag(uid));
        db.updateStatusByUID(uid, DownloadPost.Status.PAUSE);
    }

    public void updateProgress(DownloadPost downloadPost, float progress) {
        if (downloadPost == null)
            return;
        db.updateProgressByUID(downloadPost.uid, progress);
    }

    public LiveData<List<WorkInfo>> getWorker(DownloadPost downloadPost, Context context) {
        return WorkManager.getInstance(context).getWorkInfosByTagLiveData(getTag(downloadPost.uid));
    }
}
