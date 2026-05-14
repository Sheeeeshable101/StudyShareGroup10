package com.example.studysharegroup10;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_NAV = "state_bottom_nav";

    private BottomNavigationView bottomNav;
    private int currentNavId = R.id.navigation_home;
    private OnBackPressedCallback backToHomeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePalettePreference.applyActivityTheme(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_nav);
        backToHomeCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                navigateHome();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backToHomeCallback);

        NavigationBarView.OnItemSelectedListener listener = item -> {
            currentNavId = item.getItemId();
            Fragment dest;
            int id = item.getItemId();
            if (id == R.id.navigation_account) {
                dest = new AccountFragment();
            } else if (id == R.id.navigation_settings) {
                dest = new SettingsFragment();
            } else {
                dest = new HomeFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, dest)
                    .commit();
            updateBackToHomeEnabled();
            return true;
        };

        if (savedInstanceState != null) {
            currentNavId = savedInstanceState.getInt(STATE_NAV, R.id.navigation_home);
            bottomNav.setOnItemSelectedListener(null);
            bottomNav.setSelectedItemId(currentNavId);
            bottomNav.setOnItemSelectedListener(listener);
        } else {
            bottomNav.setOnItemSelectedListener(listener);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
        updateBackToHomeEnabled();
    }

    private void updateBackToHomeEnabled() {
        if (backToHomeCallback != null) {
            backToHomeCallback.setEnabled(currentNavId != R.id.navigation_home);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        signInAnonymouslyIfNeeded();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_NAV, currentNavId);
    }

    private void signInAnonymouslyIfNeeded() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            notifyHomeAuthChanged();
            return;
        }
        auth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Exception e = task.getException();
                        if (e != null) {
                            android.widget.Toast.makeText(this, e.getMessage(), android.widget.Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                    notifyHomeAuthChanged();
                });
    }

    public void onUserSignedOut() {
        signInAnonymouslyIfNeeded();
    }

    public void navigateHome() {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
    }

    void notifyHomeAuthChanged() {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof HomeFragment) {
            ((HomeFragment) f).refreshAuthStatus();
        }
    }
}
