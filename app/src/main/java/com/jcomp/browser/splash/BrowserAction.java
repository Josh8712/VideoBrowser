package com.jcomp.browser.splash;

import android.app.TaskStackBuilder;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jcomp.browser.browser.BrowserActivity;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.welcome.Welcome;

public class BrowserAction extends BaseAction {
    String url;

    public BrowserAction(String url, AppCompatActivity activity) {
        super(activity);
        this.url = url;
    }

    @Override
    public void run() {
        Intent intent = new Intent(activity, BrowserActivity.class);
        intent.putExtra(BrowserActivity.INTENT_URL_KEY, url);
        activity.startActivity(intent);
    }
}
