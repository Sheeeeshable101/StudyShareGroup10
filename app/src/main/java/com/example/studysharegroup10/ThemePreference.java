package com.example.studysharegroup10;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemePreference {

    private static final String PREFS = "study_share_prefs";
    private static final String KEY_NIGHT_MODE = "night_mode";

    private ThemePreference() {
    }

    public static int getNightMode(Context context) {
        int mode = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (mode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                && mode != AppCompatDelegate.MODE_NIGHT_NO
                && mode != AppCompatDelegate.MODE_NIGHT_YES) {
            return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        return mode;
    }

    public static void setNightMode(Context context, int mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_NIGHT_MODE, mode)
                .apply();
    }

    static int buttonIdForMode(int mode) {
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                return R.id.theme_light;
            case AppCompatDelegate.MODE_NIGHT_YES:
                return R.id.theme_dark;
            default:
                return R.id.theme_follow_system;
        }
    }

    static int modeForButtonId(int buttonId) {
        if (buttonId == R.id.theme_light) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if (buttonId == R.id.theme_dark) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }
}
