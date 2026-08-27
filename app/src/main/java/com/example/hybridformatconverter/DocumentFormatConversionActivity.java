package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridLayout;
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

public class DocumentFormatConversionActivity extends AppCompatActivity {

    private Spinner spinnerFormat;
    private GridLayout gridConversions;
    private TextView tvSectionLabel;
    private BottomNavigationView bottomNav;

    /*
     * Formats that are actually supported by our
     * document conversion engine.
     */
    private final String[] formats = {
            "DOCX",
            "PDF",
            "TXT",
            "HTML",
            "XLSX",
            "EPUB",
            "CSV",
            "MD",
            "JSON",
            "XML"
    };

    private final Map<String, List<String>> conversionMap =
            new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_document_format_conversion
        );

        buildConversionMap();

        initViews();

        setupSpinner();

        String format =
                getIntent().getStringExtra("FORMAT");

        if (format != null) {

            format =
                    format.toUpperCase();

            for (int i = 0; i < formats.length; i++) {

                if (formats[i].equals(format)) {

                    spinnerFormat.setSelection(i);

                    break;
                }
            }

        } else {

            updateGrid("DOCX");
        }

        setupBottomNavigation();
    }

    // =========================================================
    // CONVERSION MAP
    // =========================================================

    private void buildConversionMap() {

        /*
         * Only conversions that our custom document engine
         * can actually perform are shown.
         */

        conversionMap.put(
                "DOCX",
                Arrays.asList(
                        "PDF",
                        "TXT",
                        "HTML"
                )
        );

        conversionMap.put(
                "PDF",
                Arrays.asList(
                        "DOCX",
                        "TXT",
                        "JPG",
                        "PNG"
                )
        );

        conversionMap.put(
                "TXT",
                Arrays.asList(
                        "DOCX",
                        "PDF"
                )
        );

        conversionMap.put(
                "HTML",
                Arrays.asList(
                        "DOCX",
                        "PDF",
                        "MD"
                )
        );
        conversionMap.put(
                "XLSX",
                Arrays.asList(
                        "CSV",
                        "JSON",
                        "XML"
                )
        );

        conversionMap.put(
                "EPUB",
                Arrays.asList(
                        "HTML",
                        "TXT"
                )
        );

        conversionMap.put(
                "CSV",
                Arrays.asList(
                        "XLSX",
                        "JSON",
                        "XML"
                )
        );

        conversionMap.put(
                "MD",
                Arrays.asList(
                        "HTML",
                        "PDF"
                )
        );

        conversionMap.put(
                "JSON",
                Arrays.asList(
                        "XLSX",
                        "CSV"
                )
        );

        conversionMap.put(
                "XML",
                Arrays.asList(
                        "XLSX",
                        "CSV"
                )
        );
    }

    // =========================================================
    // INITIALIZE
    // =========================================================

    private void initViews() {

        spinnerFormat =
                findViewById(R.id.spinnerFormat);

        gridConversions =
                findViewById(R.id.gridConversions);

        tvSectionLabel =
                findViewById(R.id.tvSectionLabel);

        bottomNav =
                findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack)
                .setOnClickListener(
                        v -> finish()
                );
    }

    // =========================================================
    // SPINNER
    // =========================================================

    private void setupSpinner() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        formats
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerFormat.setAdapter(adapter);

        spinnerFormat.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        updateGrid(
                                formats[position]
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            AdapterView<?> parent) {
                    }
                }
        );
    }

    // =========================================================
    // BUILD CONVERSION GRID
    // =========================================================

    private void updateGrid(String format) {

        tvSectionLabel.setText(
                format + " CONVERSION"
        );

        gridConversions.removeAllViews();

        List<String> targets =
                conversionMap.get(format);

        if (targets == null) {
            return;
        }

        List<String> cards = new ArrayList<>();

// =========================================================
// CONVERSIONS FROM SELECTED FORMAT
// =========================================================

        for (String target : targets) {

            cards.add(
                    format + " to " + target
            );
        }

// =========================================================
// CONVERSIONS TO SELECTED FORMAT
// =========================================================

        for (Map.Entry<String, List<String>> entry
                : conversionMap.entrySet()) {

            String sourceFormat = entry.getKey();

            List<String> sourceTargets =
                    entry.getValue();

            // Don't add A → A
            if (sourceFormat.equals(format)) {
                continue;
            }

            // If sourceFormat can convert to selected format,
            // show that reverse direction as well.
            if (sourceTargets.contains(format)) {

                cards.add(
                        sourceFormat + " to " + format
                );
            }
        }

        int dpSize =
                (int)
                        (
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                                        * 130
                        );

        int margin =
                (int)
                        (
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                                        * 6
                        );

        for (String label : cards) {

            MaterialCardView card =
                    new MaterialCardView(this);

            GridLayout.LayoutParams params =
                    new GridLayout.LayoutParams();

            params.width = 0;

            params.height = dpSize;

            params.columnSpec =
                    GridLayout.spec(
                            GridLayout.UNDEFINED,
                            1f
                    );

            params.rowSpec =
                    GridLayout.spec(
                            GridLayout.UNDEFINED,
                            1f
                    );

            params.setMargins(
                    margin,
                    margin,
                    margin,
                    margin
            );

            card.setLayoutParams(params);

            card.setRadius(40f);

            card.setCardBackgroundColor(
                    getResources()
                            .getColor(R.color.card_bg)
            );

            card.setCardElevation(0f);

            card.setClickable(true);

            card.setFocusable(true);

            // =================================================
            // INNER LAYOUT
            // =================================================

            LinearLayout inner =
                    new LinearLayout(this);

            inner.setOrientation(
                    LinearLayout.VERTICAL
            );

            inner.setGravity(
                    android.view.Gravity.CENTER
            );

            inner.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    )
            );

            // =================================================
            // CIRCLE
            // =================================================

            FrameLayout circle =
                    new FrameLayout(this);

            int circleSize =
                    (int)
                            (
                                    getResources()
                                            .getDisplayMetrics()
                                            .density
                                            * 52
                            );

            LinearLayout.LayoutParams circleParams =
                    new LinearLayout.LayoutParams(
                            circleSize,
                            circleSize
                    );

            circle.setLayoutParams(
                    circleParams
            );

            circle.setBackground(
                    getResources()
                            .getDrawable(
                                    R.drawable.circle_icon_bg
                            )
            );

            // =================================================
            // ICON
            // =================================================

            ImageView icon =
                    new ImageView(this);

            int iconSize =
                    (int)
                            (
                                    getResources()
                                            .getDisplayMetrics()
                                            .density
                                            * 26
                            );

            FrameLayout.LayoutParams iconParams =
                    new FrameLayout.LayoutParams(
                            iconSize,
                            iconSize
                    );

            iconParams.gravity =
                    android.view.Gravity.CENTER;

            icon.setLayoutParams(
                    iconParams
            );

            icon.setImageResource(
                    R.drawable.ic_swap_horiz
            );

            icon.setColorFilter(
                    getResources()
                            .getColor(
                                    R.color.accent_blue
                            )
            );

            circle.addView(icon);

            // =================================================
            // LABEL
            // =================================================

            TextView tv =
                    new TextView(this);

            LinearLayout.LayoutParams tvParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            tvParams.topMargin =
                    (int)
                            (
                                    getResources()
                                            .getDisplayMetrics()
                                            .density
                                            * 10
                            );

            tv.setLayoutParams(tvParams);

            tv.setText(label);

            tv.setTextColor(
                    getResources()
                            .getColor(
                                    R.color.text_primary
                            )
            );

            tv.setTextSize(13f);

            tv.setGravity(
                    android.view.Gravity.CENTER
            );

            inner.addView(circle);

            inner.addView(tv);

            card.addView(inner);

            // =================================================
            // CLICK
            // =================================================

            final String finalLabel =
                    label;

            card.setOnClickListener(
                    v -> {

                        PermissionDialogHelper.checkAndShow(
                                this,
                                finalLabel,
                                new PermissionDialogHelper.Callback() {

                                    @Override
                                    public void onProceed() {

                                        openDocumentConversion(
                                                finalLabel
                                        );
                                    }

                                    @Override
                                    public void onDenied() {
                                    }
                                }
                        );
                    }
            );

            gridConversions.addView(card);
        }
    }

    // =========================================================
    // OPEN DOCUMENT CONVERSION
    // =========================================================

    private void openDocumentConversion(
            String conversion) {

        String[] parts =
                conversion.split(" to ");

        if (parts.length != 2) {

            Toast.makeText(
                    this,
                    "Invalid conversion.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String from =
                parts[0].trim().toUpperCase();

        String to =
                parts[1].trim().toUpperCase();

        Intent intent =
                new Intent(
                        this,
                        DocumentFileConversionActivity.class
                );

        intent.putExtra(
                "FROM_FORMAT",
                from
        );

        intent.putExtra(
                "TO_FORMAT",
                to
        );

        intent.putExtra(
                "CONVERSION_LABEL",
                from + " to " + to
        );

        startActivity(intent);
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void setupBottomNavigation() {

        bottomNav.setSelectedItemId(
                R.id.nav_home
        );

        bottomNav.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(
                            @NonNull MenuItem item) {

                        int id =
                                item.getItemId();

                        if (id == R.id.nav_home) {

                            Intent intent =
                                    new Intent(
                                            DocumentFormatConversionActivity.this,
                                            MainActivity.class
                                    );

                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            );

                            startActivity(intent);

                            return true;

                        } else if (id == R.id.nav_history) {
                            Intent intent = new Intent(DocumentFormatConversionActivity.this, HistoryActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (id == R.id.nav_settings) {

                            Intent intent =
                                    new Intent(
                                            DocumentFormatConversionActivity.this,
                                            SettingsActivity.class
                                    );

                            startActivity(intent);

                            return true;
                        }

                        return false;
                    }
                }
        );
    }
}