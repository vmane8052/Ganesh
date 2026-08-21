package com.ganeshmandal.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.lang.reflect.Type;

public class CacheManager {
    private static final String PREF_NAME = "OfflineCachePrefs";
    private static final Gson gson = new Gson();

    public static void saveCache(Context context, String key, Object data) {
        if (context == null || key == null || data == null) return;
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = gson.toJson(data);
            prefs.edit().putString(key, json).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T getCache(Context context, String key, Type typeOfT) {
        if (context == null || key == null || typeOfT == null) return null;
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(key, null);
            if (json != null && !json.trim().isEmpty()) {
                return gson.fromJson(json, typeOfT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearCache(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
