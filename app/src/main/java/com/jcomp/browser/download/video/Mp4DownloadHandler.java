package com.jcomp.browser.download.video;

import com.jcomp.browser.download.DownloadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

public class Mp4DownloadHandler extends DownloadHandlerBase {

    private static final String STATUS_FILENAME = "status";
    private static final int BLOCK_SIZE = 4 * 1024 * 1024;
    RandomAccessFile output;
    private byte[] downloadStatus;

    public Mp4DownloadHandler(DownloadTask downloadTask) {
        super(downloadTask);
    }

    @Override
    public void _start() throws Exception {
        String filename = downloadTask.getDownloadPost().localPath + getFileIndex();
        restoreStatus();
        output = new RandomAccessFile(filename, "rw");
        downloadRoot();
    }

    private void restoreStatus() throws IOException {
        File file = new File(downloadTask.getDownloadPost().localPath + STATUS_FILENAME);
        if (file.exists() && file.length() > 0) {
            downloadStatus = new byte[(int) file.length()];
            FileInputStream stream = new FileInputStream(file);
            if (stream.read(downloadStatus) < downloadStatus.length)
                downloadStatus = null;
        }
        HttpURLConnection connection = DownloadHandlerBase.createConnection(downloadTask.getDownloadPost().videoPath, downloadTask.getDownloadPost());
        System.out.println(connection.getResponseCode());
        long bytes = connection.getHeaderFieldLong("Content-Length", -1);
        if (bytes <= 0)
            throw new IOException();
        int length = (int) Math.ceil((double) bytes / BLOCK_SIZE);
        if (downloadStatus == null || length != downloadStatus.length)
            downloadStatus = new byte[length];
        incrementJobs(length);
    }

    private synchronized void setStatus(int taskID) throws IOException {
        downloadStatus[taskID] = 1;
        if (taskID % 10 == 0) {
            FileOutputStream file = new FileOutputStream(downloadTask.getDownloadPost().localPath + STATUS_FILENAME);
            file.write(downloadStatus);
            file.flush();
            file.close();
        }
    }

    @Override
    public String getFileIndex() {
        return "index.mp4";
    }

    private synchronized void write(byte[] data, int len, long offset) throws IOException {
        output.seek(offset);
        output.write(data, 0, len);
    }

    private void downloadRoot() throws Exception {
        for (int i = 0; i < downloadStatus.length; i++) {
            while (true) {
                if (downloadTask.isStopped()) {
                    downloadTask.cancel();
                    return;
                }
                if (shouldAddNewJob())
                    break;
                Thread.sleep(1000);
            }
            if (downloadStatus[i] == 1) {
                incrementProgress();
                continue;
            }
            int finalI = i;
            addJob(() -> {
                download(finalI);
            });
        }
    }

    private void download(int taskID) {
        int retry = 0;
        while (retry < maxRetry) {
            try {
                HttpURLConnection connection = DownloadHandlerBase.createRawConnection(downloadTask.getDownloadPost().videoPath, downloadTask.getDownloadPost());
                connection.addRequestProperty("range", "bytes=" + ((long) taskID * BLOCK_SIZE) + "-" + ((long) (taskID + 1) * BLOCK_SIZE));
                connection.connect();
                if (!validProgress(connection, taskID))
                    throw new IOException();
                InputStream stream = connection.getInputStream();
                byte[] data = new byte[BLOCK_SIZE];
                int offset = 0, count;
                while ((count = stream.read(data, offset, BLOCK_SIZE - offset)) > 0) {
                    offset += count;
                    addRecord(count);
                }
                if (taskID != downloadStatus.length - 1 && offset != BLOCK_SIZE)
                    throw new IOException();
                write(data, offset, (long) taskID * BLOCK_SIZE);
                setStatus(taskID);
                incrementProgress();
                return;
            } catch (Exception e) {
                retry += 1;
            }
        }
        failedQueue.add(() -> {
            download((taskID));
        });
    }

    private boolean validProgress(HttpURLConnection connection, int taskID) {
        try {
            String rangeResponse = connection.getHeaderField("Content-Range");
            if (rangeResponse == null)
                return false;
            String[] rangePair = rangeResponse.split(" ")[1].split("-");
            if (Long.parseLong(rangePair[0]) != ((long) taskID * BLOCK_SIZE))
                return false;
            rangePair = rangePair[1].split("/");
            if (taskID == downloadStatus.length - 1) {
                return Long.parseLong(rangePair[0]) + 1 == Long.parseLong(rangePair[1]);
            } else {
                return Long.parseLong(rangePair[0]) + 1 >= (long) (taskID + 1) * BLOCK_SIZE;
            }
        } catch (Exception exception) {
            return false;
        }
    }
}
