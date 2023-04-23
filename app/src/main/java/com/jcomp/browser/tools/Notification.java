package com.jcomp.browser.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class Notification {
    public static final int DOWNLOAD_PAGE_INTENT_ID = 1 << 31;
    public static final String DOWNLOAD_CHANNEL_ID = "download";
    public static final String DOWNLOAD_STATUS_CHANNEL_ID = "download status";


    private static void createChannel(Context context, String name, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(name, name, importance);
            channel.setDescription(name);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void createDownloadChannel(Context context) {
        createChannel(context, DOWNLOAD_CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
    }

    public static void createDownloadStatusChannel(Context context) {
        createChannel(context, DOWNLOAD_STATUS_CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
    }
}
