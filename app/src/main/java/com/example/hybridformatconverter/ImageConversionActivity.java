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

public class ImageConversionActivity extends AppCompatActivity {

    private MaterialCardView cardJpg, cardPng, cardBmp, cardTiff, cardSvg, cardWebp, cardGif, cardHeic;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_conversion);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardJpg  = findViewById(R.id.cardJpg);
        cardPng  = findViewById(R.id.cardPng);
        cardBmp  = findViewById(R.id.cardBmp);
        cardTiff = findViewById(R.id.cardTiff);
        cardSvg  = findViewById(R.id.cardSvg);
        cardWebp = findViewById(R.id.cardWebp);
        cardGif  = findViewById(R.id.cardGif);
        cardHeic = findViewById(R.id.cardHeic);
        bottomNav = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        cardJpg.setOnClickListener(v  -> openConversion("JPG"));
        cardPng.setOnClickListener(v  -> openConversion("PNG"));
        cardBmp.setOnClickListener(v  -> openConversion("BMP"));
        cardTiff.setOnClickListener(v -> openConversion("TIFF"));
        cardSvg.setOnClickListener(v  -> openConversion("SVG"));
        cardWebp.setOnClickListener(v -> openConversion("WEBP"));
        cardGif.setOnClickListener(v  -> openConversion("GIF"));
        cardHeic.setOnClickListener(v -> openConversion("HEIC"));
    }

    private void openConversion(String format) {
        Intent intent = new Intent(ImageConversionActivity.this, ImageFormatConversionActivity.class);
        intent.putExtra("FORMAT", format);
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    finish();
                    return true;
                } else if (id == R.id.nav_history) {
                    Toast.makeText(ImageConversionActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(ImageConversionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}