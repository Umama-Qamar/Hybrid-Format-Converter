package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardImages, cardDocuments, cardAudio, cardVideo;
    private MaterialCardView cardCompression, cardExtractText, cardPdfTools;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before anything else
        android.content.SharedPreferences themePrefs = getSharedPreferences("settings_prefs", MODE_PRIVATE);
        String themeMode = themePrefs.getString("appearance_mode", "auto");
        if (themeMode.equals("light")) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        } else if (themeMode.equals("dark")) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardImages      = findViewById(R.id.cardImages);
        cardDocuments   = findViewById(R.id.cardDocuments);
        cardAudio       = findViewById(R.id.cardAudio);
        cardVideo       = findViewById(R.id.cardVideo);
        cardCompression = findViewById(R.id.cardCompression);
        cardExtractText = findViewById(R.id.cardExtractText);
        cardPdfTools    = findViewById(R.id.cardPdfTools);
        bottomNav       = findViewById(R.id.bottomNav);
    }

    private void setupClickListeners() {
        cardImages.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ImageConversionActivity.class);
            startActivity(intent);
        });

        cardDocuments.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DocumentConversionActivity.class);
            startActivity(intent);
        });

        cardAudio.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AudioConversionActivity.class);
            startActivity(intent);
        });

        cardVideo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, VideoConversionActivity.class);
            startActivity(intent);
        });

        cardCompression.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CompressionActivity.class);
            startActivity(intent);
        });

        cardExtractText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TextExtractionActivity.class);
            startActivity(intent);
        });

        cardPdfTools.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PdfToolsActivity.class);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Already on home — scroll to top if needed
                    return true;
                } else if (id == R.id.nav_history) {
                    // TODO: navigate to HistoryActivity
                    Toast.makeText(MainActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}