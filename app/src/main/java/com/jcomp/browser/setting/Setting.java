package com.jcomp.browser.setting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jcomp.browser.R;
import com.jcomp.browser.cloud.FireStore;
import com.jcomp.browser.databinding.ActivitySettingBinding;
import com.jcomp.browser.tools.HelperFunc;

import java.io.File;
import java.util.Objects;

public class Setting extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivitySettingBinding binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.clearCache.setOnClickListener(v -> {
            deleteFiles(getCacheDir());
            deleteFiles(getExternalCacheDir());
            updateCacheSize(binding.cacheSize);
        });
        binding.report.setOnClickListener(v -> {
            // create a popup form dialog to report a bug
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report, null);

            final EditText inputEditText = dialogView.findViewById(R.id.edit_text_input);
            final EditText emailEditText = dialogView.findViewById(R.id.edit_text_email);
            builder.setTitle(R.string.report);
            builder.setView(dialogView);
            builder.setPositiveButton(R.string.submit, null);
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
                dialog.cancel();
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                String userInput = inputEditText.getText().toString().trim();
                String email = emailEditText.getText().toString();
                if (userInput.isEmpty()) {
                    inputEditText.requestFocus();
                    inputEditText.setError(getText(R.string.enter_report_advice));
                    return;
                }
                sendReport(userInput, email, new FireStore.Callback() {
                    @Override
                    public void onProceed() {
                        alertDialog.dismiss();
                    }

                    @Override
                    public void onFailure() {

                    }
                });
            });
        });

        updateCacheSize(binding.cacheSize);
    }

    private void sendReport(String userInput, String email, FireStore.Callback callback) {
        // show a progress dialog
        ProgressDialog dialog = ProgressDialog.show(this, "",
                getText(R.string.loading), true);
        dialog.setCancelable(true);
        FireStore.getInstance().sendReport(userInput, email, 0, new FireStore.Callback() {
            @Override
            public void onProceed() {
                dialog.dismiss();
                HelperFunc.showToast(Setting.this, R.string.report_success, Toast.LENGTH_LONG);
                callback.onProceed();
            }

            @Override
            public void onFailure() {
                dialog.dismiss();
                HelperFunc.showToast(Setting.this, R.string.something_went_wrong, Toast.LENGTH_LONG);
                callback.onFailure();
            }
        });
        dialog.show();
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    private void updateCacheSize(TextView cacheSize) {
        float size = getDirSize(getCacheDir());
        size += getDirSize(getExternalCacheDir());
        String sizeStr;
        if (size < 1024) {
            sizeStr = String.format("%.2f", size) + " B";
        } else if (size < 1024 * 1024) {
            sizeStr = String.format("%.2f", size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            sizeStr = String.format("%.2f", size / (1024 * 1024)) + " MB";
        } else {
            sizeStr = String.format("%.2f", size / (1024 * 1024 * 1024)) + " GB";
        }
        cacheSize.setText(sizeStr);
    }

    public void deleteFiles(File file) {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                deleteFiles(f);
            }
        }
        file.delete();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
