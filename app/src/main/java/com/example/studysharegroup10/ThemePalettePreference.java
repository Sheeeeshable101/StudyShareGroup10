package com.example.studysharegroup10;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.StyleRes;

public final class ThemePalettePreference {

    static final String PALETTE_CLASSROOM = "classroom";
    static final String PALETTE_OCEAN = "ocean";
    static final String PALETTE_SUNSET = "sunset";
    static final String PALETTE_LAVENDER = "lavender";

    private static final String PREFS = "study_share_prefs";
    private static final String KEY_PALETTE = "theme_palette";

    private ThemePalettePreference() {
    }

    public static String getPalette(Context context) {
        String v = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PALETTE, PALETTE_CLASSROOM);
        if (PALETTE_OCEAN.equals(v) || PALETTE_SUNSET.equals(v) || PALETTE_LAVENDER.equals(v)) {
            return v;
        }
        return PALETTE_CLASSROOM;
    }

    public static void setPalette(Context context, String palette) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PALETTE, palette)
                .apply();
    }

    static void applyActivityTheme(Activity activity) {
        activity.setTheme(themeResId(getPalette(activity)));
    }

    @StyleRes
    static int themeResId(String palette) {
        switch (palette) {
            case PALETTE_OCEAN:
                return R.style.Theme_StudyShareGroup10_Ocean;
            case PALETTE_SUNSET:
                return R.style.Theme_StudyShareGroup10_Sunset;
            case PALETTE_LAVENDER:
                return R.style.Theme_StudyShareGroup10_Lavender;
            default:
                return R.style.Theme_StudyShareGroup10_Classroom;
        }
    }

    static int chipIdForPalette(String palette) {
        switch (palette) {
            case PALETTE_OCEAN:
                return R.id.palette_ocean;
            case PALETTE_SUNSET:
                return R.id.palette_sunset;
            case PALETTE_LAVENDER:
                return R.id.palette_lavender;
            default:
                return R.id.palette_classroom;
        }
    }

    static String paletteForChipId(int chipId) {
        if (chipId == R.id.palette_ocean) {
            return PALETTE_OCEAN;
        }
        if (chipId == R.id.palette_sunset) {
            return PALETTE_SUNSET;
        }
        if (chipId == R.id.palette_lavender) {
            return PALETTE_LAVENDER;
        }
        return PALETTE_CLASSROOM;
    }
}
