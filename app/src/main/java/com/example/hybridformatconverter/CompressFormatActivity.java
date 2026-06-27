package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompressFormatActivity extends AppCompatActivity {

    private GridLayout gridFormats;
    private BottomNavigationView bottomNav;

    private final Map<String, List<String>> formatsByCategory = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_format);

        buildFormatMap();

        gridFormats = findViewById(R.id.gridFormats);
        bottomNav = findViewById(R.id.bottomNav);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra("CATEGORY");
        if (category == null) category = "Images";

        TextView title = findViewById(R.id.toolbarTitle);
        title.setText(category + " Compression");

        TextView sectionLabel = findViewById(R.id.tvSectionLabel);
        sectionLabel.setText(category.toUpperCase() + " COMPRESSION");

        buildGrid(category);
        setupBottomNavigation();
    }

    private void buildFormatMap() {
        formatsByCategory.put("Images", Arrays.asList("JPEG/JPG", "PNG", "WEBP", "HEIC/HEIF", "GIF"));
        formatsByCategory.put("Documents", Arrays.asList("PDF", "Word", "Excel", "PPT", "TXT", "CSV"));
        formatsByCategory.put("Audio", Arrays.asList("MP3", "WAV", "AAC/M4A", "OGG/OPUS", "FLAC", "AMR"));
        formatsByCategory.put("Video", Arrays.asList("MP4", "AVI", "MKV", "MOV", "WEBM", "FLV"));
    }

    private void buildGrid(String category) {
        gridFormats.removeAllViews();
        List<String> formats = formatsByCategory.get(category);
        if (formats == null) return;

        int dpSize = (int) (getResources().getDisplayMetrics().density * 130);
        int margin = (int) (getResources().getDisplayMetrics().density * 6);

        for (String format : formats) {
            MaterialCardView card = new MaterialCardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpSize;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(params);
            card.setRadius(40f);
            card.setCardBackgroundColor(getResources().getColor(R.color.card_bg));
            card.setCardElevation(0f);
            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setGravity(Gravity.CENTER);
            inner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            FrameLayout circle = new FrameLayout(this);
            int circleSize = (int) (getResources().getDisplayMetrics().density * 52);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(circleSize, circleSize);
            circle.setLayoutParams(circleParams);
            circle.setBackground(getResources().getDrawable(R.drawable.circle_icon_bg));

            ImageView icon = new ImageView(this);
            int iconSize = (int) (getResources().getDisplayMetrics().density * 26);
            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
            iconParams.gravity = Gravity.CENTER;
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_compress);
            icon.setColorFilter(getResources().getColor(R.color.accent_blue));
            circle.addView(icon);

            TextView tv = new TextView(this);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.topMargin = (int) (getResources().getDisplayMetrics().density * 10);
            tv.setLayoutParams(tvParams);
            tv.setText(format);
            tv.setTextColor(getResources().getColor(R.color.text_primary));
            tv.setTextSize(13f);
            tv.setGravity(Gravity.CENTER);

            inner.addView(circle);
            inner.addView(tv);
            card.addView(inner);

            final String finalFormat = format;
            card.setOnClickListener(v ->
                    PermissionDialogHelper.checkAndShow(this, "compress " + finalFormat, new PermissionDialogHelper.Callback() {
                        @Override
                        public void onProceed() {
                            Toast.makeText(CompressFormatActivity.this, finalFormat + " compression coming soon!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onDenied() {
                            // user chose Don't allow - stay on screen
                        }
                    })
            );

            gridFormats.addView(card);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(CompressFormatActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_history) {
                    Toast.makeText(CompressFormatActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(CompressFormatActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}