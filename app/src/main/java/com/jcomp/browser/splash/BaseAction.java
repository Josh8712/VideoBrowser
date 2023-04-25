package com.jcomp.browser.splash;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.browser.BrowserActivity;
import com.jcomp.browser.welcome.Welcome;

public class BaseAction implements Runnable {
    AppCompatActivity activity;

    public BaseAction(AppCompatActivity activity) {
        this.activity = activity;
    }

    public static boolean shouldAddWelcome(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int numActivities = manager.getAppTasks().get(0).getTaskInfo().numActivities;
        if (numActivities == 1) {
            return true;
        }
        return false;
    }

    public void startIntent(Intent intent) {
        if(shouldAddWelcome(activity)) {
            Intent[] intents = {new Intent(activity, Welcome.class), intent};
            activity.startActivities(intents);
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    public void run() {
        if (shouldAddWelcome(activity)) {
            Intent intent = new Intent(activity, Welcome.class);
            activity.startActivity(intent);
        }
    }
}
