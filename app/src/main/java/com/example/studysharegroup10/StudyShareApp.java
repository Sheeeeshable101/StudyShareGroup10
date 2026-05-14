package com.example.studysharegroup10;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class StudyShareApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(ThemePreference.getNightMode(this));
    }
}
