package com.jcomp.browser.welcome;


import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.browser.BrowserActivity;
import com.jcomp.browser.databinding.ActivityWelcomeBinding;
import com.jcomp.browser.history.HistorySetting;
import com.jcomp.browser.history.db.History;
import com.jcomp.browser.main.MainActivity;
import com.jcomp.browser.setting.Setting;

import java.util.List;

public class Welcome extends AppCompatActivity {
    public static final String HISTORY_INTENT_KEY = "HISTORY_INTENT_KEY";
    private EditText keyword;
    private HistoryAdapter history_adapter;
    private ActivityResultLauncher<Intent> mGetBrowser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityWelcomeBinding binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        keyword = binding.keyword;

        new Thread(this::fakeHistory).start();

        setupKeywordEdit(binding.query, binding.clearInputButton);
        binding.query.setOnClickListener(view -> open_browser());
        binding.clearInputButton.setOnClickListener(view -> clearInput());
        new Thread(() -> {
            initHistory(binding.history);
            updateHistory();
        }).start();
        Bundle called_bundle = getIntent().getExtras();
        if (called_bundle != null && called_bundle.getString(HISTORY_INTENT_KEY, null) != null)
            putHistoryAndOpen(new Gson().fromJson(called_bundle.getString(HISTORY_INTENT_KEY), History.class));
        mGetBrowser =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                                Intent data = result.getData();
                                Bundle bundle = data.getExtras();
                                if (bundle == null)
                                    return;
                                try {
                                    putHistoryAndOpen(new Gson().fromJson(bundle.getString(HISTORY_INTENT_KEY), History.class));
                                } catch (Exception ignore) {
                                }
                            }
                        });

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setupKeywordEdit(View query, View clearInputButton) {
        keyword.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                query.performClick();
            }
            return false;
        });
        keyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0)
                    clearInputButton.setVisibility(View.GONE);
                else
                    clearInputButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initHistory(RecyclerView historyView) {
        runOnUiThread(() -> {
            history_adapter = new HistoryAdapter(history -> {
                if (history instanceof HistorySetting)
                    startActivity(new Intent(Welcome.this, Setting.class));
                else {
                    if (history.isRemovable()) {
                        history.setTimestamp();
                        putHistoryAndOpen(history);
                    } else {
                        openTab(history);
                    }
                }
            });
            historyView.setAdapter(history_adapter);
            historyView.setLayoutManager(new GridLayoutManager(this, 3));
        });
    }

    private void updateHistory() {
        new Thread(() -> {
            List<History> historyList = AppDatabase.getInstance(this).historyDoa().getAll();
            runOnUiThread(() -> {
                if (history_adapter != null)
                    history_adapter.setData(historyList, Welcome.this);
            });
        }).start();
    }

    private void clearInput() {
        keyword.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHistory();
    }

    public void putHistoryAndOpen(History history) {
        new Thread(() -> {
            AppDatabase.getInstance(this).historyDoa().insert(history);
        }).start();
        clearInput();
        openTab(history);
    }

    private void open_browser() {
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.putExtra(BrowserActivity.INTENT_KEYWORD_KEY, keyword.getText().toString());
        mGetBrowser.launch(intent);
    }

    private void openTab(History history) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(HISTORY_INTENT_KEY, new Gson().toJson(history));
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (getCurrentFocus() != null && getCurrentFocus() instanceof EditText) {
                Rect outRect = new Rect();
                getCurrentFocus().getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    getCurrentFocus().clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void fakeHistory() {
        if (!AppDatabase.getInstance(this).historyDoa().getAll().isEmpty())
            return;
        String[] url = {
                "https://jable.tv/",
                "https://5278.cc/forum-23-1.html",
                "https://avple.tv/",
                "https://www.netflav.com/",
                "https://85tube.com/",
                "https://www.seselah.com/",
                "https://159i.com/video/",
                "https://twdvd.com/amateur/",
                "https://missav.com/"
        };
        for (String s : url) {
            AppDatabase.getInstance(this).historyDoa().insert(new History(null, s));
        }
        updateHistory();
    }
}
