package com.jcomp.browser.browser;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jcomp.browser.R;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.parser.ParserJumpCallback;
import com.jcomp.browser.welcome.Welcome;

public class BrowserActivity extends AppCompatActivity {
    public static final String SAVED_STATE_KEY = "SAVED_STATE_KEY";
    public static final String INTENT_KEYWORD_KEY = "INTENT_KEYWORD_KEY";
    static final int CHECK_INTERVAL = 1000;
    Browser browser;
    boolean fromOutside = false;
    long lastCheck = 0;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        browser = this.findViewById(R.id.browser);
        browser.init(new Handler());

        String url = null;
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(SAVED_STATE_KEY, null);
        }
        if (url == null) {
            Intent intent = getIntent();
            if (intent != null) {
                if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    url = intent.getData().toString();
                    fromOutside = true;
                } else {
                    String keyword = intent.getStringExtra(INTENT_KEYWORD_KEY);
                    if (keyword != null)
                        url = browser.buildQueryURL(keyword);
                }
            }
        }
        browser.setFragmentReady(true);
        browser.setHomeButtonCallback((view) -> {
            finish();
        });

        browser.registerCallback(new ParserJumpCallback() {
            @Override
            public void navigateToList(History history) {
                runOnUiThread(() -> {
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
                });

            }
        });

        mHandler = new Handler();
        browser.loadUrl(url, false);
        browser.setFragmentReady(true);
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
    }

    @Override
    public void onBackPressed() {
        if (browser.getBrowser().canGoBack())
            browser.getBrowser().goBack();
        else
            super.onBackPressed();
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