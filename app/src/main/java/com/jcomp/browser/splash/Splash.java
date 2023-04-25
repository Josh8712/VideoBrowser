package com.jcomp.browser.splash;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.R;
import com.jcomp.browser.cloud.FireStore;
import com.jcomp.browser.databinding.ActivitySplashBinding;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.parser.post.db.PlaylistDefault;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistWatched;
import com.jcomp.browser.player.Player;
import com.jcomp.browser.welcome.Welcome;


public class Splash extends AppCompatActivity implements Runnable {

    BaseAction action = new BaseAction(this);
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(!wasLaunchedFromRecent()) {
            Intent intent = getIntent();
            if (intent != null) {
                if(intent.hasExtra(Welcome.HISTORY_INTENT_KEY)) {
                    action = new DownloadPageAction(intent.getStringExtra(Welcome.HISTORY_INTENT_KEY), this);
                } else if(intent.hasExtra(Player.PLAYER_INFO_KEY)) {
                    action = new PlayerAction(intent.getStringExtra(Player.PLAYER_INFO_KEY), this);
                } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                    String url = intent.getData().toString();
                    action = new BrowserAction(url, this);
                }
            }
        }
        new Thread(this).start();
    }
    @Override
    public void run() {
        checkVersion();
    }

    private void checkVersion() {
        FireStore.getInstance().checkVersion(new FireStore.Callback() {
            @Override
            public void onProceed() {
                initDB();
            }

            @Override
            public void onFailure() {
                // show force update dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
                builder.setTitle(R.string.update_required);
                builder.setMessage(R.string.force_update_message);
                builder.setPositiveButton(R.string.update, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.jcomp.browser"));
                    startActivity(intent);
                    finish();
                });
                builder.setCancelable(false);
                builder.show();
            }
        });
    }

    private void initDB() {
        new Thread(() -> {
            try {
                PlaylistDoa db = AppDatabase.getInstance(getApplicationContext()).playlistDoa();
                if(db.getPlayList().isEmpty()) {
                    db.addPlaylist(new PlaylistWatched(this));
                    db.addPlaylist(new PlaylistDefault(this));
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage(R.string.error_init_db);
                return;
            }
            DownloadManager.getInstance(getApplicationContext());
            initAD();
        }).start();
    }

    private void initAD() {
        MobileAds.initialize(Splash.this, initializationStatus -> initCloud());
    }

    private void initCloud() {
        FireStore.getInstance().insertLogin(new FireStore.CallbackIgnore() {
            @Override
            public void onProceed() {
                done();
            }
        }, 0, this);
    }


    private void done() {
        runOnUiThread(() -> {
            if(action != null) {
                action.run();
            } else {
                Intent intent = new Intent(Splash.this, Welcome.class);
                startActivity(intent);
            }
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    public void showErrorMessage(int message) {
        runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
            builder.setTitle(R.string.error);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.retry_later, (dialog, which) -> {
                finish();
            });
            builder.setCancelable(false);
            builder.show();
        });
    }

    protected boolean wasLaunchedFromRecent() {
        if(getIntent() == null)
            return true;
        return (getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
    }
}
