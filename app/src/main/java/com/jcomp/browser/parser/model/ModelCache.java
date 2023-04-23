package com.jcomp.browser.parser.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

public class ModelCache {
    static ModelCache singleton;
    final int CACHE_SIZE = 1024;
    final String KEY;
    LruCache<String, Model[]> modelCache;
    SharedPreferences sharedPref;
    Map<String, String[]> playerCache;
    Map<String, String> hlsCache;

    private ModelCache(Context context) {
        KEY = "root_model";
        modelCache = new LruCache<String, Model[]>(CACHE_SIZE) {
            private int sizeOf(String key, Model value) {
                return 1;
            }
        };
        sharedPref = context.getSharedPreferences(
                KEY, Context.MODE_PRIVATE);
        String saved = sharedPref.getString(KEY, null);
        Map<String, Model[]> map = new Gson().fromJson(saved, new TypeToken<Map<String, Model[]>>() {
        }.getType());
        if (saved != null)
            for (String key : map.keySet())
                modelCache.put(key, map.get(key));

        playerCache = new HashMap<>();
        hlsCache = new HashMap<>();
    }

    public static ModelCache getSingleton(Context context) {
        if (singleton == null)
            singleton = new ModelCache(context);
        return singleton;
    }

    public void putModel(String key, Model[] value) {
        modelCache.put(key, value);
        sharedPref.edit().putString(KEY, new Gson().toJson(modelCache.snapshot())).apply();
    }

    public Model[] getModel(String key) {
        return modelCache.get(key);
    }

    public void putPlayer(String key, String[] player) {
        playerCache.put(key, player);
    }

    public String[] getPlayer(String key) {
        return playerCache.getOrDefault(key, null);
    }

    public void putHLS(String key, String hls) {
        hlsCache.put(key, hls);
    }

    public String getHLS(String key) {
        return hlsCache.getOrDefault(key, null);
    }

    public void removeHLS(String urlKey) {
        hlsCache.remove(urlKey);
    }
}
