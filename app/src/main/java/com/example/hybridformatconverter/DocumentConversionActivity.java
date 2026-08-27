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

public class DocumentConversionActivity extends AppCompatActivity {

    private MaterialCardView cardDocx, cardPdf, cardTxt, cardHtml, cardXlsx, cardEpub, cardCsv, cardMd, cardJson, cardXml;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_conversion);

        initViews();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        cardDocx  = findViewById(R.id.cardDocx);
        cardPdf   = findViewById(R.id.cardPdf);
        cardTxt   = findViewById(R.id.cardTxt);
        cardHtml  = findViewById(R.id.cardHtml);
        cardXlsx  = findViewById(R.id.cardXlsx);
        cardEpub  = findViewById(R.id.cardEpub);
        cardCsv   = findViewById(R.id.cardCsv);
        cardMd    = findViewById(R.id.cardMd);
        cardJson  = findViewById(R.id.cardJson);
        cardXml   = findViewById(R.id.cardXml);
        bottomNav = findViewById(R.id.bottomNav);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        cardDocx.setOnClickListener(v -> openConversion("DOCX"));
        cardPdf.setOnClickListener(v  -> openConversion("PDF"));
        cardTxt.setOnClickListener(v  -> openConversion("TXT"));
        cardHtml.setOnClickListener(v -> openConversion("HTML"));
        cardXlsx.setOnClickListener(v -> openConversion("XLSX"));
        cardEpub.setOnClickListener(v -> openConversion("EPUB"));
        cardCsv.setOnClickListener(v  -> openConversion("CSV"));
        cardMd.setOnClickListener(v   -> openConversion("MD"));
        cardJson.setOnClickListener(v -> openConversion("JSON"));
        cardXml.setOnClickListener(v  -> openConversion("XML"));
    }

    private void openConversion(String format) {
        Intent intent = new Intent(DocumentConversionActivity.this, DocumentFormatConversionActivity.class);
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
                    Intent intent = new Intent(DocumentConversionActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_settings) {
                    Intent intent = new Intent(DocumentConversionActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}