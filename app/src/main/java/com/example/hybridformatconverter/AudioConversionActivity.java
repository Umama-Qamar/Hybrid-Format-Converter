package com.example.hybridformatconverter;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioConversionActivity extends AppCompatActivity {

    private GridLayout gridConversions;
    private BottomNavigationView bottomNav;


    // =========================================================
    // SUPPORTED AUDIO FORMATS
    // =========================================================

    private final List<String> targets =
            Arrays.asList(
                    "WAV",
                    "AAC",
                    "M4A",
                    "OGG",
                    "OPUS",
                    "FLAC",
                    "WMA"
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_audio_conversion
        );


        gridConversions =
                findViewById(
                        R.id.gridConversions
                );


        bottomNav =
                findViewById(
                        R.id.bottomNav
                );


        findViewById(
                R.id.btnBack
        ).setOnClickListener(
                v -> finish()
        );


        buildGrid();

        setupBottomNavigation();
    }


    // =========================================================
    // BUILD GRID
    // =========================================================

    private void buildGrid() {

        gridConversions.removeAllViews();


        List<String> cards =
                new ArrayList<>();


        for (String target : targets) {

            cards.add(
                    "MP3 to " + target
            );


            cards.add(
                    target + " to MP3"
            );
        }


        int dpSize =
                (int) (
                        getResources()
                                .getDisplayMetrics()
                                .density * 130
                );


        int margin =
                (int) (
                        getResources()
                                .getDisplayMetrics()
                                .density * 6
                );


        // =====================================================
        // CREATE CARDS
        // =====================================================

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


            card.setLayoutParams(
                    params
            );


            card.setRadius(40f);


            card.setCardBackgroundColor(
                    getResources()
                            .getColor(
                                    R.color.card_bg
                            )
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
            // ICON CIRCLE
            // =================================================

            FrameLayout circle =
                    new FrameLayout(this);


            int circleSize =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .density * 52
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
            // SWAP ICON
            // =================================================

            ImageView icon =
                    new ImageView(this);


            int iconSize =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .density * 26
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
            // CARD TEXT
            // =================================================

            TextView tv =
                    new TextView(this);


            LinearLayout.LayoutParams tvParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );


            tvParams.topMargin =
                    (int) (
                            getResources()
                                    .getDisplayMetrics()
                                    .density * 10
                    );


            tv.setLayoutParams(
                    tvParams
            );


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
                                AudioConversionActivity.this,
                                finalLabel,

                                new PermissionDialogHelper.Callback() {

                                    @Override
                                    public void onProceed() {

                                        openConversion(
                                                finalLabel
                                        );
                                    }


                                    @Override
                                    public void onDenied() {

                                        // Stay here
                                    }
                                }
                        );
                    }
            );


            gridConversions.addView(card);
        }
    }


    // =========================================================
    // OPEN CONVERSION
    // =========================================================

    private void openConversion(String conversion) {

        String[] parts =
                conversion.split(" to ");


        if (parts.length != 2) {

            Toast.makeText(
                    this,
                    "Invalid conversion",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        String fromFormat =
                parts[0]
                        .trim()
                        .toLowerCase();


        String toFormat =
                parts[1]
                        .trim()
                        .toLowerCase();


        Intent intent =
                new Intent(
                        AudioConversionActivity.this,
                        ConversionActivity.class
                );


        intent.putExtra(
                "CONVERSION_LABEL",
                conversion
        );


        intent.putExtra(
                "FROM_FORMAT",
                fromFormat
        );


        intent.putExtra(
                "TO_FORMAT",
                toFormat
        );


        // =====================================================
        // IMPORTANT
        // AUDIO ONLY
        // =====================================================

        intent.putExtra(
                "MIME_TYPE",
                "audio/*"
        );


        intent.putExtra(
                "CATEGORY",
                "audio"
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
                            @NonNull MenuItem item
                    ) {

                        int id =
                                item.getItemId();


                        // =================================================
                        // HOME
                        // =================================================

                        if (id == R.id.nav_home) {

                            Intent intent =
                                    new Intent(
                                            AudioConversionActivity.this,
                                            MainActivity.class
                                    );


                            intent.addFlags(
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            );
                            startActivity(intent);
                            return true;}
                        if (id == R.id.nav_history) {
                            Toast.makeText(
                                    AudioConversionActivity.this,
                                    "History",
                                    Toast.LENGTH_SHORT).show();
                            return true;}
                        if (id == R.id.nav_settings) {
                            Intent intent =
                                    new Intent(AudioConversionActivity.this,
                                            SettingsActivity.class);
                            startActivity(intent);
                            return true;}
                        return false;
                    }});}}