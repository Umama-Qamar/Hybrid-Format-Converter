package com.example.hybridformatconverter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "settings_prefs";
    private static final String KEY_APPEARANCE = "appearance_mode"; // "auto", "light", "dark"
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";

    private MaterialCardView cardAppearance, cardRateApp, cardFeedback, cardAbout, cardShare;
    private SwitchMaterial switchNotifications;
    private TextView tvAppearanceValue;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSavedSettings();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardAppearance       = findViewById(R.id.cardAppearance);
        cardRateApp          = findViewById(R.id.cardRateApp);
        cardFeedback         = findViewById(R.id.cardFeedback);
        cardAbout            = findViewById(R.id.cardAbout);
        cardShare            = findViewById(R.id.cardShare);
        switchNotifications  = findViewById(R.id.switchNotifications);
        tvAppearanceValue    = findViewById(R.id.tvAppearanceValue);
        bottomNav            = findViewById(R.id.bottomNav);
    }

    private void loadSavedSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String appearance = prefs.getString(KEY_APPEARANCE, "auto");
        tvAppearanceValue.setText(capitalize(appearance));

        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void setupClickListeners() {
        cardAppearance.setOnClickListener(v -> showAppearanceDialog());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Notifications enabled" : "Notifications disabled",
                    Toast.LENGTH_SHORT).show();
        });

        cardRateApp.setOnClickListener(v -> RateAppDialogHelper.show(this));
        cardFeedback.setOnClickListener(v -> FeedbackDialogHelper.show(this));
        cardShare.setOnClickListener(v -> openShareApp());

        cardAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void showAppearanceDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_appearance);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroupAppearance);
        RadioButton radioAuto = dialog.findViewById(R.id.radioAuto);
        RadioButton radioLight = dialog.findViewById(R.id.radioLight);
        RadioButton radioDark = dialog.findViewById(R.id.radioDark);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentMode = prefs.getString(KEY_APPEARANCE, "auto");

        switch (currentMode) {
            case "light":
                radioLight.setChecked(true);
                break;
            case "dark":
                radioDark.setChecked(true);
                break;
            default:
                radioAuto.setChecked(true);
                break;
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedMode;
            int nightMode;

            if (checkedId == R.id.radioLight) {
                selectedMode = "light";
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.radioDark) {
                selectedMode = "dark";
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                selectedMode = "auto";
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            prefs.edit().putString(KEY_APPEARANCE, selectedMode).apply();
            AppCompatDelegate.setDefaultNightMode(nightMode);
            tvAppearanceValue.setText(capitalize(selectedMode));

            dialog.dismiss();
        });

        dialog.show();
    }

    private void openShareApp() {
        String packageName = getPackageName();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hybrid Format Converter");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Check out Hybrid Format Converter! Convert, compress, and extract text from your files easily.\n\n" +
                        "https://play.google.com/store/apps/details?id=" + packageName);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(SettingsActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_settings) {
                    return true;
                }
                return false;
            }
        });
    }
}