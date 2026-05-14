package com.example.studysharegroup10;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_settings);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateHome();
            }
        });

        MaterialButtonToggleGroup themeToggle = view.findViewById(R.id.theme_toggle);
        int saved = ThemePreference.getNightMode(requireContext());
        themeToggle.check(ThemePreference.buttonIdForMode(saved));
        themeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            int newMode = ThemePreference.modeForButtonId(checkedId);
            if (newMode == ThemePreference.getNightMode(requireContext())) {
                return;
            }
            ThemePreference.setNightMode(requireContext(), newMode);
            AppCompatDelegate.setDefaultNightMode(newMode);
            requireActivity().recreate();
        });

        ChipGroup paletteGroup = view.findViewById(R.id.palette_chip_group);
        paletteGroup.check(ThemePalettePreference.chipIdForPalette(ThemePalettePreference.getPalette(requireContext())));
        paletteGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                return;
            }
            String palette = ThemePalettePreference.paletteForChipId(checkedId);
            if (palette.equals(ThemePalettePreference.getPalette(requireContext()))) {
                return;
            }
            ThemePalettePreference.setPalette(requireContext(), palette);
            requireActivity().recreate();
        });
    }
}
