package com.example.hybridformatconverter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextExtractFormatActivity extends AppCompatActivity {

    private GridLayout gridFormats;
    private BottomNavigationView bottomNav;

    private String selectedMimeType = "image/jpeg";

    private final Map<String, List<String>> formatsByCategory = new HashMap<>();
    private final Map<String, String> formatToMimeType = new HashMap<>();

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        Intent intent = new Intent(TextExtractFormatActivity.this, OcrResultActivity.class);
                        intent.putExtra("IMAGE_URI", imageUri.toString());
                        intent.putExtra("MIME_TYPE", selectedMimeType);
                        startActivity(intent);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_extract_format);

        buildFormatMap();
        buildMimeTypeMap();

        gridFormats = findViewById(R.id.gridFormats);
        bottomNav = findViewById(R.id.bottomNav);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String category = getIntent().getStringExtra("CATEGORY");
        if (category == null) category = "Images";

        TextView title = findViewById(R.id.toolbarTitle);
        title.setText(category + " Text Extraction");

        TextView sectionLabel = findViewById(R.id.tvSectionLabel);
        sectionLabel.setText(category.toUpperCase() + " TEXT EXTRACTION");

        buildGrid(category);
        setupBottomNavigation();
    }

    private void buildFormatMap() {
        formatsByCategory.put("Images",    Arrays.asList("JPG", "PNG", "BMP", "WEBP", "TIFF"));
    }

    private void buildMimeTypeMap() {
        formatToMimeType.put("JPG",     "image/jpeg");
        formatToMimeType.put("PNG",     "image/png");
        formatToMimeType.put("BMP",     "image/bmp");
        formatToMimeType.put("WEBP",    "image/webp");
        formatToMimeType.put("TIFF",    "image/tiff");
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
            icon.setImageResource(R.drawable.ic_text_extract);
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
            card.setOnClickListener(v -> {
                selectedMimeType = formatToMimeType.getOrDefault(finalFormat, "image/jpeg");
                PermissionDialogHelper.checkAndShow(
                        TextExtractFormatActivity.this,
                        "extract text from " + finalFormat,
                        new PermissionDialogHelper.Callback() {
                            @Override
                            public void onProceed() {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType(selectedMimeType);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                galleryLauncher.launch(intent);
                            }
                            @Override
                            public void onDenied() {
                                // stay on screen
                            }
                        }
                );
            });

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
                    Intent intent = new Intent(TextExtractFormatActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(TextExtractFormatActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(TextExtractFormatActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}