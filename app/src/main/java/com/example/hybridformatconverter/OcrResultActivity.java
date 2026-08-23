package com.example.hybridformatconverter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hybridformatconverter.data.HistoryRepository;

import com.google.android.material.button.MaterialButton;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OcrResultActivity extends AppCompatActivity {

    private LinearLayout layoutLoading;
    private LinearLayout layoutResult;
    private LinearLayout layoutError;

    private ProgressBar progressBar;

    private TextView tvProgressPercent;
    private TextView tvStatus;
    private TextView tvExtractedText;
    private TextView tvCharacterCount;
    private TextView tvError;

    private MaterialButton btnCopy;
    private MaterialButton btnDownload;
    private MaterialButton btnRetry;

    private ImageButton btnBack;

    private String extractedText = "";

    private Uri imageUri;
    private String mimeType;

    private Handler progressHandler = new Handler(Looper.getMainLooper());

    private int fakeProgress = 0;

    private boolean ocrFinished = false;

    private final ActivityResultLauncher<String> saveLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("text/plain"),
                    uri -> {
                        if (uri != null) {
                            saveText(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        initViews();

        String imageUriString =
                getIntent().getStringExtra("IMAGE_URI");

        mimeType =
                getIntent().getStringExtra("MIME_TYPE");

        if (imageUriString == null) {

            Toast.makeText(this,
                    "No image selected",
                    Toast.LENGTH_SHORT).show();

            finish();

            return;
        }

        imageUri = Uri.parse(imageUriString);

        btnBack.setOnClickListener(v -> finish());

        btnRetry.setOnClickListener(v -> startOcr());

        btnCopy.setOnClickListener(v -> copyText());

        btnDownload.setOnClickListener(v ->
                saveLauncher.launch("extracted_text.txt"));

        startOcr();
    }

    private void initViews() {

        layoutLoading = findViewById(R.id.layoutLoading);
        layoutResult = findViewById(R.id.layoutResult);
        layoutError = findViewById(R.id.layoutError);

        progressBar = findViewById(R.id.progressBar);

        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvStatus = findViewById(R.id.tvStatus);

        tvExtractedText = findViewById(R.id.tvExtractedText);
        tvCharacterCount = findViewById(R.id.tvCharacterCount);
        tvError = findViewById(R.id.tvError);

        btnCopy = findViewById(R.id.btnCopy);
        btnDownload = findViewById(R.id.btnDownload);
        btnRetry = findViewById(R.id.btnRetry);

        btnBack = findViewById(R.id.btnBack);
    }

    private void startOcr() {

        fakeProgress = 0;
        ocrFinished = false;

        showLoading();

        animateProgress();

        OcrHelper.extractTextFromImage(
                this,
                imageUri,
                mimeType,
                new OcrHelper.OcrCallback() {

                    @Override
                    public void onSuccess(String text) {

                        ocrFinished = true;

                        runOnUiThread(() -> {

                            progressBar.setProgress(100);

                            tvProgressPercent.setText("100%");

                            progressHandler.postDelayed(() -> {

                                extractedText = text;

                                showResult(text);

                            }, 300);

                        });

                    }

                    @Override
                    public void onFailure(String errorMessage) {

                        ocrFinished = true;

                        runOnUiThread(() -> {

                            showError(errorMessage);

                        });

                    }

                });

    }

    private void animateProgress() {

        progressHandler.post(new Runnable() {

            @Override
            public void run() {

                if (ocrFinished)
                    return;

                if (fakeProgress < 25) {

                    fakeProgress += 2;

                }

                else if (fakeProgress < 60) {

                    fakeProgress++;

                }

                else if (fakeProgress < 90) {

                    fakeProgress += 1;

                }

                else if (fakeProgress < 95) {

                    fakeProgress += 1;

                }

                progressBar.setProgress(fakeProgress);

                tvProgressPercent.setText(fakeProgress + "%");

                progressHandler.postDelayed(this, 120);

            }

        });

    }
    private void showLoading() {

        layoutLoading.setVisibility(View.VISIBLE);
        layoutResult.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);

        progressBar.setProgress(0);

        tvProgressPercent.setText("0%");

        tvStatus.setText("This usually takes 5–15 seconds");
    }

    private void showResult(String text) {

        layoutLoading.setVisibility(View.GONE);
        layoutResult.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);

        tvExtractedText.setText(text);

        tvCharacterCount.setText(text.length() + " characters");
    }

    private void showError(String message) {

        layoutLoading.setVisibility(View.GONE);
        layoutResult.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);

        tvError.setText(message);
    }

    private void copyText() {

        if (extractedText.isEmpty()) {

            Toast.makeText(this,
                    "Nothing to copy",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip =
                ClipData.newPlainText("Extracted Text", extractedText);

        clipboard.setPrimaryClip(clip);

        Toast.makeText(this,
                "Text copied successfully",
                Toast.LENGTH_SHORT).show();
    }

    private void saveText(Uri uri) {

        try {

            OutputStream outputStream =
                    getContentResolver().openOutputStream(uri);

            if (outputStream == null) {

                Toast.makeText(this,
                        "Unable to save file",
                        Toast.LENGTH_SHORT).show();

                return;
            }

            outputStream.write(
                    extractedText.getBytes(StandardCharsets.UTF_8));

            outputStream.flush();

            outputStream.close();

            new HistoryRepository(this).addDownload(
                    "extracted_text.txt",
                    uri.toString(),
                    "document"
            );

            Toast.makeText(this,
                    "File saved successfully",
                    Toast.LENGTH_LONG).show();

        }

        catch (Exception e) {

            Toast.makeText(this,
                    "Failed to save file",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        progressHandler.removeCallbacksAndMessages(null);

    }

}