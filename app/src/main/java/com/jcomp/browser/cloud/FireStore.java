package com.jcomp.browser.cloud;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.jcomp.browser.BuildConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FireStore {
    public static FireStore singleton = null;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    public FireStore() {
        db = FirebaseFirestore.getInstance();
    }

    public static FireStore getInstance() {
        if (singleton == null)
            singleton = new FireStore();
        return singleton;
    }

    private void storeTime(SharedPreferences sharedPreferences, Set<String> timeArray) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("time_array", timeArray);
        editor.apply();
    }

    public void insertLogin(Callback callback, int retry, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CONFIGS", Context.MODE_PRIVATE);
        long currentTime = new Date().getTime();
        Set<String> timeArray = new HashSet<>(sharedPreferences.getStringSet("time_array", new HashSet<String>()));
        long max_time = sharedPreferences.getLong("max_time", 0);
        timeArray.add(String.valueOf(currentTime));

        if (currentTime - max_time < 3600000 || retry > 3) {
            storeTime(sharedPreferences, timeArray);
            callback.onProceed();
            return;
        }

        if (currentUser == null) {
            auth(new Callback() {
                @Override
                public void onProceed() {
                    insertLogin(callback, retry + 1, context);
                }

                @Override
                public void onFailure() {
                    insertLogin(callback, retry + 1, context);
                }
            }, 0);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        Timestamp[] timestamps = new Timestamp[timeArray.size()];
        int i = 0;
        for (String time : timeArray)
            timestamps[i++] = new Timestamp(new Date(Long.parseLong(time)));
        data.put("timestamps", FieldValue.arrayUnion(timestamps));
        db.collection("users").document(currentUser.getUid())
                .set(data, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        sharedPreferences.edit().putLong("max_time", currentTime).remove("time_array").apply();
                    else
                        storeTime(sharedPreferences, timeArray);
                }).addOnFailureListener(e -> {
                    storeTime(sharedPreferences, timeArray);
                });
        callback.onProceed();
    }

    public void auth(Callback callback, int retry) {
        if (retry > 3) {
            callback.onFailure();
            return;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            mAuth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            callback.onProceed();
                        } else {
                            auth(callback, retry + 1);
                        }
                    })
                    .addOnFailureListener(e -> {
                        auth(callback, retry + 1);
                    });
        else
            callback.onProceed();
    }

    public void checkVersion(Callback callback) {
        db.collection("app").document("settings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Long min_version = task.getResult().getLong("min_version");
                        if (min_version != null && min_version > BuildConfig.VERSION_CODE)
                            callback.onFailure();
                        else
                            callback.onProceed();
                    }
                }).addOnFailureListener(e -> {
                    e.printStackTrace();
                    callback.onProceed();
                });
    }

    public void sendReport(String content, String email, int retry, Callback callback) {
        if (retry > 3) {
            callback.onFailure();
            return;
        }
        if (currentUser == null) {
            auth(new CallbackIgnore() {
                @Override
                public void onProceed() {
                    sendReport(content, email, retry + 1, callback);
                }
            }, 0);
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("email", email);
        data.put("status", 0);
        data.put("timestamp", FieldValue.serverTimestamp());
        db.collection("users").document(currentUser.getUid())
                .collection("reports").document()
                .set(data)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onProceed();
                    } else {
                        sendReport(content, email, retry + 1, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    sendReport(content, email, retry + 1, callback);
                });
    }

    public static abstract class Callback {
        public abstract void onProceed();

        public abstract void onFailure();
    }

    public static abstract class CallbackIgnore extends Callback {
        abstract public void onProceed();

        public void onFailure() {
            onProceed();
        }
    }
}
