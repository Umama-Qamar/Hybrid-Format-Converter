package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.content.SharedPreferences themePrefs =
                getSharedPreferences("settings_prefs", MODE_PRIVATE);

        String themeMode =
                themePrefs.getString("appearance_mode", "auto");

        if (themeMode.equals("light")) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            );
        } else if (themeMode.equals("dark")) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            );
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            );
        }

        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.splashProgressBar);

        startLoading();
    }

    private void startLoading() {

        handler.postDelayed(() -> updateProgress(15), 150);
        handler.postDelayed(() -> updateProgress(30), 350);
        handler.postDelayed(() -> updateProgress(45), 550);
        handler.postDelayed(() -> updateProgress(60), 750);
        handler.postDelayed(() -> updateProgress(75), 950);
        handler.postDelayed(() -> updateProgress(90), 1150);
        handler.postDelayed(() -> updateProgress(100), 1350);

        handler.postDelayed(() -> {
            Intent intent = new Intent(
                    SplashActivity.this,
                    MainActivity.class
            );

            startActivity(intent);
            finish();
        }, 1550);
    }

    private void updateProgress(int value) {
        progressBar.setProgress(value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}