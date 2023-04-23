package com.jcomp.browser.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class DownloadTaskReceiver extends BroadcastReceiver {
    public static final String ID_KEY = "ID_KEY";
    public static final String TASK_ID = "TASK_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        long uid = (long) intent.getExtras().get(ID_KEY);
        Task taskID = (Task) intent.getExtras().get(TASK_ID);
        new Thread(() -> {
            switch (taskID) {
                case STOP:
                    DownloadManager.getInstance(context).pauseJob(uid);
                    break;
            }
        }).start();
    }

    enum Task {
        STOP
    }
}
