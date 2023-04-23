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
import com.jcomp.browser.parser.post.db.Playlist;
import com.jcomp.browser.parser.post.db.PlaylistDefault;
import com.jcomp.browser.parser.post.db.PlaylistDoa;
import com.jcomp.browser.parser.post.db.PlaylistRecord;
import com.jcomp.browser.parser.post.db.PlaylistWatched;
import com.jcomp.browser.parser.post.db.Post;
import com.jcomp.browser.welcome.Welcome;
import com.jcomp.browser.widget.BreathingAnim;

import java.util.List;

public class Splash extends AppCompatActivity implements Runnable {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Thread(this).start();
    }

    @Override
    public void run() {
        checkVersion();
//        WorkRequest uploadWorkRequest =
//                new OneTimeWorkRequest.Builder(TestTask.class)
//                        .addTag("Test")
//                        .build();
//        WorkManager
//                .getInstance(this)
//                .enqueue(uploadWorkRequest);
//
//
//
//        WorkManager
//                .getInstance(this).cancelAllWorkByTag("Test");

        //            Intent intent = new Intent(Splash.this, Test.class);;
//            startActivity(intent);
//            finish();
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
//                List<Post> data = db.getPostsTest();
//                List<Playlist> da = db.getPlayListTest();
//                List<Playlist> dd = db.getPlayListTest();
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
        }, 0);
    }


    private void done() {
        runOnUiThread(() -> {
            Intent intent = new Intent(Splash.this, Welcome.class);
            startActivity(intent);
            finish();
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
}
