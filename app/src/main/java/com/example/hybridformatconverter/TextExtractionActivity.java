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

public class TextExtractionActivity extends AppCompatActivity {

    private MaterialCardView cardImages;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_extraction);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardImages    = findViewById(R.id.cardImages);
        bottomNav     = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        cardImages.setOnClickListener(v -> {
            Intent intent = new Intent(
                    TextExtractionActivity.this,
                    TextExtractFormatActivity.class
            );

            intent.putExtra("CATEGORY", "Images");
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
                    finish();
                    return true;
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(TextExtractionActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(TextExtractionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}