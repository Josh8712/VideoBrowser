package com.jcomp.browser.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.AppDatabase;
import com.jcomp.browser.databinding.ActivityTestBinding;
import com.jcomp.browser.download.DownloadManager;
import com.jcomp.browser.welcome.Welcome;

public class Test extends AppCompatActivity implements Runnable {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityTestBinding binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void run() {
        AppDatabase.getInstance(getApplicationContext());
        DownloadManager.getInstance(getApplicationContext());


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

        runOnUiThread(() -> {
//            Intent intent = new Intent(Splash.this, Welcome.class);;
//            startActivity(intent);
//            finish();

            Intent intent = new Intent(Test.this, Welcome.class);
            startActivity(intent);
            finish();
        });
    }
}
