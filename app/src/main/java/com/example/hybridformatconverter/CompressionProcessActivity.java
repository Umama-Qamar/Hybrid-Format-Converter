package com.example.hybridformatconverter;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.hybridformatconverter.data.HistoryRepository;
import java.util.Locale;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CompressionProcessActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "CATEGORY";
    public static final String EXTRA_FORMAT = "FORMAT";
    public static final String EXTRA_URI = "URI";

    private android.view.View stateSelected, stateProgress, stateResult, stateError;
    private ImageView ivThumbnail;
    private TextView tvFileName, tvProgressLabel, tvResultTitle, tvResultSubtitle, tvErrorMessage, toolbarTitle;
    private ProgressBar progressSpinner;
    private MaterialButton btnCompress, btnDownload;

    private String category;
    private String format;
    private Uri inputUri;
    private File resultFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compression_process);

        category = getIntent().getStringExtra(EXTRA_CATEGORY);
        format = getIntent().getStringExtra(EXTRA_FORMAT);
        inputUri = getIntent().getParcelableExtra(EXTRA_URI);

        if (category == null || format == null || inputUri == null) {
            Toast.makeText(this, "Missing file info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        toolbarTitle.setText(format + " Compression");
        tvFileName.setText(FileUtils.getDisplayName(this, inputUri));

        loadThumbnail();
        showState(stateSelected);

        btnCompress.setOnClickListener(v -> startCompression());
        btnDownload.setOnClickListener(v -> downloadResult());
    }

    private void bindViews() {
        stateSelected = findViewById(R.id.stateSelected);
        stateProgress = findViewById(R.id.stateProgress);
        stateResult = findViewById(R.id.stateResult);
        stateError = findViewById(R.id.stateError);

        ivThumbnail = findViewById(R.id.ivThumbnail);
        tvFileName = findViewById(R.id.tvFileName);
        tvProgressLabel = findViewById(R.id.tvProgressLabel);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvResultSubtitle = findViewById(R.id.tvResultSubtitle);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        progressSpinner = findViewById(R.id.progressSpinner);

        btnCompress = findViewById(R.id.btnCompress);
        btnDownload = findViewById(R.id.btnDownload);
    }

    // Only images get a real preview; everything else keeps the generic circle icon background.
    private void loadThumbnail() {
        if (!"Images".equals(category)) return;
        try (InputStream in = getContentResolver().openInputStream(inputUri)) {
            Bitmap bmp = BitmapFactory.decodeStream(in);
            if (bmp != null) ivThumbnail.setImageBitmap(bmp);
        } catch (Exception ignored) {
            // Falls back to the placeholder background — not fatal.
        }
    }

    private void showState(android.view.View toShow) {
        stateSelected.setVisibility(toShow == stateSelected ? android.view.View.VISIBLE : android.view.View.GONE);
        stateProgress.setVisibility(toShow == stateProgress ? android.view.View.VISIBLE : android.view.View.GONE);
        stateResult.setVisibility(toShow == stateResult ? android.view.View.VISIBLE : android.view.View.GONE);
        stateError.setVisibility(toShow == stateError ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void startCompression() {
        tvProgressLabel.setText("Compressing " + format + "...");
        showState(stateProgress);

        CompressionRouter.compress(this, category, format, inputUri, new CompressionRouter.Callback() {
            @Override
            public void onSuccess(File outputFile, long originalSize, long newSize) {
                runOnUiThread(() -> {
                    resultFile = outputFile;
                    int savedPercent = originalSize > 0
                            ? (int) (100 - ((newSize * 100L) / originalSize))
                            : 0;

                    tvResultTitle.setText("Successfully compressed!");
                    if (newSize >= originalSize) {
                        // Compressors fall back to the original file when re-encoding would
                        // make it bigger, so this means the file was already well optimized.
                        tvResultSubtitle.setText(String.format("%s  (already optimized)",
                                FileUtils.humanReadableSize(originalSize)));
                    } else {
                        tvResultSubtitle.setText(String.format("%s -> %s  (%d%% smaller)",
                                FileUtils.humanReadableSize(originalSize),
                                FileUtils.humanReadableSize(newSize),
                                savedPercent));
                    }
                    showState(stateResult);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    tvErrorMessage.setText(message != null && !message.trim().isEmpty()
                            ? message
                            : "Compression failed. This format may not be supported on your device.");
                    showState(stateError);
                });
            }
        });
    }

    // Saves the already-compressed file (in app-private external storage) into the
    // public Downloads collection via MediaStore, so it shows up in the Files/Downloads app.
    private void downloadResult() {
        if (resultFile == null || !resultFile.exists()) {
            Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDownload.setEnabled(false);
        new Thread(() -> {
            try {
                String extension = getExtension(resultFile.getName());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null) mimeType = "application/octet-stream";

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, resultFile.getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                Uri itemUri = getContentResolver().insert(collection, values);

                if (itemUri == null) throw new Exception("Could not create download entry");

                try (InputStream in = new FileInputStream(resultFile);
                     OutputStream out = getContentResolver().openOutputStream(itemUri)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                }

                Uri savedUri = itemUri;
                runOnUiThread(() -> {
                    btnDownload.setEnabled(true);
                    new HistoryRepository(this).addDownload(
                            resultFile.getName(),
                            savedUri.toString(),
                            category != null ? category.toLowerCase(Locale.ROOT) : "document"
                    );
                    Toast.makeText(this, "Saved to Downloads", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnDownload.setEnabled(true);
                    Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : "";
    }
}