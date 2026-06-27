package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageFormatConversionActivity extends AppCompatActivity {

    private Spinner spinnerFormat;
    private GridLayout gridConversions;
    private TextView tvSectionLabel;
    private BottomNavigationView bottomNav;

    private final String[] formats = {"JPG", "PNG", "WEBP", "GIF", "BMP", "TIFF", "HEIC", "SVG"};

    // Map of format -> list of conversion pairs
    private final Map<String, List<String[]>> conversionMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_format_conversion);

        buildConversionMap();
        initViews();
        setupSpinner();

        // Read which format was passed from previous screen
        String format = getIntent().getStringExtra("FORMAT");
        if (format != null) {
            for (int i = 0; i < formats.length; i++) {
                if (formats[i].equals(format)) {
                    spinnerFormat.setSelection(i);
                    break;
                }
            }
        } else {
            updateGrid("JPG");
        }

        setupBottomNavigation();
    }

    private void buildConversionMap() {
        // JPG
        conversionMap.put("JPG", Arrays.asList(
                new String[]{"JPG to PNG", "PNG to JPG"},
                new String[]{"JPG to BMP", "BMP to JPG"},
                new String[]{"JPG to WEBP", "WEBP to JPG"},
                new String[]{"JPG to GIF", "GIF to JPG"},
                new String[]{"JPG to TIFF", "TIFF to JPG"},
                new String[]{"JPG to SVG", "SVG to JPG"},
                new String[]{"JPG to HEIC", "HEIC to JPG"},
                new String[]{"JPG to PDF", "PDF to JPG"},
                new String[]{"JPG to HDR", "HDR to JPG"},
                new String[]{"JPG to EXR", "EXR to JPG"}
        ));

        // PNG
        conversionMap.put("PNG", Arrays.asList(
                new String[]{"PNG to JPG", "JPG to PNG"},
                new String[]{"PNG to WEBP", "WEBP to PNG"},
                new String[]{"PNG to BMP", "BMP to PNG"},
                new String[]{"PNG to GIF", "GIF to PNG"},
                new String[]{"PNG to TIFF", "TIFF to PNG"},
                new String[]{"PNG to HEIC", "HEIC to PNG"},
                new String[]{"PNG to SVG", "SVG to PNG"},
                new String[]{"PNG to PDF", "PDF to PNG"},
                new String[]{"PNG to HDR", "HDR to PNG"}
        ));

        // WEBP
        conversionMap.put("WEBP", Arrays.asList(
                new String[]{"WEBP to JPG", "JPG to WEBP"},
                new String[]{"WEBP to PNG", "PNG to WEBP"},
                new String[]{"WEBP to GIF", "GIF to WEBP"}
        ));

        // GIF
        conversionMap.put("GIF", Arrays.asList(
                new String[]{"GIF to JPG", "JPG to GIF"},
                new String[]{"GIF to PNG", "PNG to GIF"},
                new String[]{"GIF to WEBP", "WEBP to GIF"}
        ));

        // BMP
        conversionMap.put("BMP", Arrays.asList(
                new String[]{"BMP to JPG", "JPG to BMP"},
                new String[]{"BMP to PNG", "PNG to BMP"},
                new String[]{"BMP to PDF", "PDF to BMP"}
        ));

        // TIFF
        conversionMap.put("TIFF", Arrays.asList(
                new String[]{"TIFF to JPG", "JPG to TIFF"},
                new String[]{"TIFF to PNG", "PNG to TIFF"},
                new String[]{"TIFF to PDF", "PDF to TIFF"}
        ));

        // HEIC
        conversionMap.put("HEIC", Arrays.asList(
                new String[]{"HEIC to JPG", "JPG to HEIC"},
                new String[]{"HEIC to PNG", "PNG to HEIC"}
        ));

        // SVG
        conversionMap.put("SVG", Arrays.asList(
                new String[]{"SVG to JPG", "JPG to SVG"},
                new String[]{"SVG to PNG", "PNG to SVG"}
        ));
    }

    private void initViews() {
        spinnerFormat  = findViewById(R.id.spinnerFormat);
        gridConversions = findViewById(R.id.gridConversions);
        tvSectionLabel = findViewById(R.id.tvSectionLabel);
        bottomNav      = findViewById(R.id.bottomNav);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                formats
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormat.setAdapter(adapter);

        spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateGrid(formats[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateGrid(String format) {
        tvSectionLabel.setText(format + " CONVERSION");
        gridConversions.removeAllViews();

        List<String[]> pairs = conversionMap.get(format);
        if (pairs == null) return;

        // Flatten pairs into individual cards
        List<String> cards = new ArrayList<>();
        for (String[] pair : pairs) {
            cards.add(pair[0]);
            cards.add(pair[1]);
        }

        int dpSize = (int) (getResources().getDisplayMetrics().density * 130);
        int margin = (int) (getResources().getDisplayMetrics().density * 6);

        for (String label : cards) {
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

            // Inner layout
            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setGravity(android.view.Gravity.CENTER);
            inner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT));

            // Icon circle
            FrameLayout circle = new FrameLayout(this);
            int circleSize = (int) (getResources().getDisplayMetrics().density * 52);
            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(circleSize, circleSize);
            circle.setLayoutParams(circleParams);
            circle.setBackground(getResources().getDrawable(R.drawable.circle_icon_bg));

            ImageView icon = new ImageView(this);
            int iconSize = (int) (getResources().getDisplayMetrics().density * 26);
            FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
            iconParams.gravity = android.view.Gravity.CENTER;
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_swap_horiz);
            icon.setColorFilter(getResources().getColor(R.color.accent_blue));
            circle.addView(icon);

            // Label
            TextView tv = new TextView(this);
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParams.topMargin = (int) (getResources().getDisplayMetrics().density * 10);
            tv.setLayoutParams(tvParams);
            tv.setText(label);
            tv.setTextColor(getResources().getColor(R.color.text_primary));
            tv.setTextSize(13f);

            inner.addView(circle);
            inner.addView(tv);
            card.addView(inner);

            final String finalLabel = label;
            card.setOnClickListener(v ->
                    PermissionDialogHelper.checkAndShow(this, finalLabel, new PermissionDialogHelper.Callback() {
                        @Override
                        public void onProceed() {
                            Toast.makeText(ImageFormatConversionActivity.this, finalLabel + " coming soon!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onDenied() {
                            // user chose Don't allow - stay on screen
                        }
                    })
            );

            gridConversions.addView(card);
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(ImageFormatConversionActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_history) {
                    Toast.makeText(ImageFormatConversionActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(ImageFormatConversionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}