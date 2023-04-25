package com.jcomp.browser.browser;

import android.app.ActivityManager;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.ParserJumpCallback;
import com.jcomp.browser.welcome.Welcome;

import java.util.List;

public class BrowserActivity extends AppCompatActivity {
    public static final String SAVED_STATE_KEY = "SAVED_STATE_KEY";
    public static final String SAVED_STATE_OUTSIDE = "SAVED_STATE_OUTSIDE";
    public static final String INTENT_KEYWORD_KEY = "INTENT_KEYWORD_KEY";
    public static final String INTENT_URL_KEY = "INTENT_URL_KEY";
    static final int CHECK_INTERVAL = 1000;
    Browser browser;
    long lastCheck = 0;
    private Handler mHandler;
    boolean fromOutside = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        browser = this.findViewById(R.id.browser);
        browser.init(new Handler());

        String url = null;
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(SAVED_STATE_KEY, null);
            fromOutside = savedInstanceState.getBoolean(SAVED_STATE_OUTSIDE, false);
        }
        if (url == null) {
            Intent intent = getIntent();
            if (intent != null) {
                url = intent.getStringExtra(INTENT_KEYWORD_KEY);
                if (url == null) {
                    url = intent.getStringExtra(INTENT_URL_KEY);
                    fromOutside = true;
                }
            }
        }
        browser.setFragmentReady(true);
        browser.setHomeButtonCallback((view) -> {
            onBackPressed();
        });

        browser.registerCallback(history -> runOnUiThread(() -> {
            if (isDestroyed())
                return;
            if (fromOutside) {
                Intent intent = new Intent(BrowserActivity.this, MainActivity.class);
                intent.putExtra(Welcome.HISTORY_INTENT_KEY, new Gson().toJson(history));
                startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.putExtra(Welcome.HISTORY_INTENT_KEY, new Gson().toJson(history));
                setResult(RESULT_OK, intent);
            }
            finish();
        }));

        mHandler = new Handler();
        browser.setFragmentReady(true);
        if(url != null)
            browser.loadUrl(url, true);
//        parseWebsite(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        browser.destroy();
    }

    private void parseWebsite(boolean force) {
        if (isDestroyed())
            return;
        if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL) {
            if (force) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        parseWebsite(true);
                    }
                }, CHECK_INTERVAL);
            }
            return;
        }
        lastCheck = System.currentTimeMillis();
        browser.loadScript("javascript:window.HtmlViewer.getHTML(window.location.href " +
                ", '<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>')");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                parseWebsite(force);
            }
        }, CHECK_INTERVAL);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_STATE_KEY, browser.getUrl());
        outState.putBoolean(SAVED_STATE_OUTSIDE, fromOutside);

    }
    @Override
    public void onBackPressed() {
        if (browser.getBrowser().canGoBack())
            browser.getBrowser().goBack();
        else {
            super.onBackPressed();
        }
    }

    //
//    @JavascriptInterface
//    public void getHTML(String location, String html) {
//        new Parser(location, html).isTarget(new Parser.TargetCallbackInterface() {
//            @Override
//            public void onFinished() {
//                if (isDestroyed())
//                    return;
//                mHandler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(isDestroyed())
//                            return;
//                        parseWebsite(false);
//                    }
//                }, CHECK_INTERVAL);
//            }
//
//            @Override
//            public void onTarget() {
//                System.out.println(isTaskRoot());
//                mHandler.post(() -> {
//                    if(isDestroyed())
//                        return;
//                    History history = new History(browser.getBrowser().getTitle(), browser.getUrl());
//                    if(fromOutside) {
//                        Intent intent = new Intent(BrowserActivity.this, MainActivity.class);
//                        intent.putExtra(Welcome.HISTORY_INTENT_KEY, new Gson().toJson(history));
//                        startActivity(intent);
//                    } else {
//                        Intent intent = new Intent();
//                        intent.putExtra(Welcome.HISTORY_INTENT_KEY, new Gson().toJson(history));
//                        setResult(RESULT_OK, intent);
//                    }
//                    finish();
//                });
//
//            }
//        });
//    }
}