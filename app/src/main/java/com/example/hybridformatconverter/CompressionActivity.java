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

public class CompressionActivity extends AppCompatActivity {

    private MaterialCardView cardImages, cardDocuments, cardAudio, cardVideo;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compression);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardImages    = findViewById(R.id.cardImages);
        cardDocuments = findViewById(R.id.cardDocuments);
        cardAudio     = findViewById(R.id.cardAudio);
        cardVideo     = findViewById(R.id.cardVideo);
        bottomNav     = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        cardImages.setOnClickListener(v -> {
            Intent intent = new Intent(CompressionActivity.this, CompressFormatActivity.class);
            intent.putExtra("CATEGORY", "Images");
            startActivity(intent);
        });

        cardDocuments.setOnClickListener(v -> {
            Intent intent = new Intent(CompressionActivity.this, CompressFormatActivity.class);
            intent.putExtra("CATEGORY", "Documents");
            startActivity(intent);
        });

        cardAudio.setOnClickListener(v -> {
            Intent intent = new Intent(CompressionActivity.this, CompressFormatActivity.class);
            intent.putExtra("CATEGORY", "Audio");
            startActivity(intent);
        });

        cardVideo.setOnClickListener(v -> {
            Intent intent = new Intent(CompressionActivity.this, CompressFormatActivity.class);
            intent.putExtra("CATEGORY", "Video");
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
                    Toast.makeText(CompressionActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(CompressionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}