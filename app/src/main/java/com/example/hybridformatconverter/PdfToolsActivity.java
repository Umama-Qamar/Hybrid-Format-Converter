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

public class PdfToolsActivity extends AppCompatActivity {

    private MaterialCardView cardMerge, cardSplit, cardImageToPdf, cardPdfToImage;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_tools);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardMerge = findViewById(R.id.cardMerge);
        cardSplit = findViewById(R.id.cardSplit);
        cardImageToPdf = findViewById(R.id.cardImageToPdf);
        cardPdfToImage = findViewById(R.id.cardPdfToImage);
        bottomNav = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        cardMerge.setOnClickListener(v ->
                PermissionDialogHelper.checkAndShow(this, "merge PDF files", new PermissionDialogHelper.Callback() {
                    @Override
                    public void onProceed() {
                        launchPdfConversion(PdfConversionActivity.ACTION_MERGE);
                    }
                    @Override
                    public void onDenied() {
                        // user chose Don't allow - stay on screen
                    }
                }));

        cardSplit.setOnClickListener(v ->
                PermissionDialogHelper.checkAndShow(this, "split PDF files", new PermissionDialogHelper.Callback() {
                    @Override
                    public void onProceed() {
                        launchPdfConversion(PdfConversionActivity.ACTION_SPLIT);
                    }
                    @Override
                    public void onDenied() {
                        // user chose Don't allow - stay on screen
                    }
                }));

        cardImageToPdf.setOnClickListener(v ->
                PermissionDialogHelper.checkAndShow(this, "convert images to PDF", new PermissionDialogHelper.Callback() {
                    @Override
                    public void onProceed() {
                        launchPdfConversion(PdfConversionActivity.ACTION_IMG_TO_PDF);
                    }
                    @Override
                    public void onDenied() {
                        // user chose Don't allow - stay on screen
                    }
                }));

        cardPdfToImage.setOnClickListener(v ->
                PermissionDialogHelper.checkAndShow(this, "convert PDF to images", new PermissionDialogHelper.Callback() {
                    @Override
                    public void onProceed() {
                        launchPdfConversion(PdfConversionActivity.ACTION_PDF_TO_IMG);
                    }
                    @Override
                    public void onDenied() {
                        // user chose Don't allow - stay on screen
                    }
                }));
    }

    private void launchPdfConversion(String action) {
        Intent intent = new Intent(PdfToolsActivity.this, PdfConversionActivity.class);
        intent.putExtra(PdfConversionActivity.EXTRA_ACTION, action);
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
                    Toast.makeText(PdfToolsActivity.this, "History", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(PdfToolsActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}