package com.example.studysharegroup10;

import android.content.Context;

final class ProfileRepository {

    private static final String PREFS = "profile_prefs";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_PHOTO_PATH = "photo_path";

    private ProfileRepository() {
    }

    static String getDisplayName(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_DISPLAY_NAME, "");
    }

    static void setDisplayName(Context context, String name) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DISPLAY_NAME, name != null ? name.trim() : "")
                .apply();
    }

    static String getPhotoPath(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PHOTO_PATH, "");
    }

    static void setPhotoPath(Context context, String path) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PHOTO_PATH, path != null ? path : "")
                .apply();
    }
}
