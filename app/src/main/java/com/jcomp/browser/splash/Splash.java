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
import com.jcomp.browser.welcome.Welcome;
import com.jcomp.browser.widget.BreathingAnim;

public class Splash extends AppCompatActivity implements Runnable {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BreathingAnim.breath(findViewById(R.id.icon), 20, 100, 1000);
        new Thread(this).start();
    }

    @Override
    public void run() {
        FireStore.getInstance().checkVersion(new FireStore.Callback() {
            @Override
            public void onProceed() {
                AppDatabase.getInstance(getApplicationContext());
                DownloadManager.getInstance(getApplicationContext());
                MobileAds.initialize(Splash.this, initializationStatus -> {
                    FireStore.getInstance().insertLogin(new FireStore.CallbackIgnore() {
                        @Override
                        public void onProceed() {
                            runOnUiThread(() -> {
                                Intent intent = new Intent(Splash.this, Welcome.class);
                                startActivity(intent);
                                finish();
                            });
                        }
                    }, 0);
                });
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
}
