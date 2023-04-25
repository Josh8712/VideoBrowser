package com.jcomp.browser.splash;

import android.app.TaskStackBuilder;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.browser.BrowserActivity;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.welcome.Welcome;

public class DownloadPageAction extends BaseAction {
    String history;

    public DownloadPageAction(String history, AppCompatActivity activity) {
        super(activity);
        this.history = history;
    }

    @Override
    public void run() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra(Welcome.HISTORY_INTENT_KEY, history);
        startIntent(intent);
    }
}
