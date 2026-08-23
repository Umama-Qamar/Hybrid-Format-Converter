package com.example.hybridformatconverter;

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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompressFormatActivity extends AppCompatActivity {

    private GridLayout gridFormats;
    private BottomNavigationView bottomNav;

    private final Map<String, List<String>> formatsByCategory = new HashMap<>();

    // Format label -> acceptable MIME types for the system file picker.
    // NOTE: deliberately broad ("image/*", "audio/*", "video/*") rather than exact MIME
    // strings. Devices/file managers report MIME types inconsistently — especially for
    // AMR, M4A, OGG/OPUS, WEBP — so an exact filter can make real files silently
    // disappear from the picker even though they're present. We validate the actual
    // file extension after selection instead (see EXTENSIONS_BY_FORMAT below).
    private static final Map<String, String[]> MIME_TYPES = new HashMap<>();
    static {
        MIME_TYPES.put("JPEG/JPG", new String[]{"image/*"});
        MIME_TYPES.put("PNG", new String[]{"image/*"});
        MIME_TYPES.put("WEBP", new String[]{"image/*"});
        MIME_TYPES.put("GIF", new String[]{"image/*"});

        MIME_TYPES.put("PDF", new String[]{"application/pdf"});
        MIME_TYPES.put("Word", new String[]{"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/*"});
        MIME_TYPES.put("Excel", new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/*"});
        MIME_TYPES.put("PPT", new String[]{"application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/*"});
        MIME_TYPES.put("TXT", new String[]{"text/*"});
        MIME_TYPES.put("CSV", new String[]{"text/*"});

        MIME_TYPES.put("MP3", new String[]{"audio/*"});
        MIME_TYPES.put("WAV", new String[]{"audio/*"});
        MIME_TYPES.put("AAC/M4A", new String[]{"audio/*"});
        MIME_TYPES.put("OGG/OPUS", new String[]{"audio/*"});
        MIME_TYPES.put("FLAC", new String[]{"audio/*"});

        MIME_TYPES.put("MP4", new String[]{"video/*"});
        MIME_TYPES.put("AVI", new String[]{"video/*"});
        MIME_TYPES.put("MOV", new String[]{"video/*"});
        MIME_TYPES.put("FLV", new String[]{"video/*"});
    }

    // Format label -> accepted file extensions, used to validate the file the user actually
    // picked (since the MIME filter above is intentionally loose).
    private static final Map<String, String[]> EXTENSIONS_BY_FORMAT = new HashMap<>();
    static {
        EXTENSIONS_BY_FORMAT.put("JPEG/JPG", new String[]{"jpg", "jpeg"});
        EXTENSIONS_BY_FORMAT.put("PNG", new String[]{"png"});
        EXTENSIONS_BY_FORMAT.put("WEBP", new String[]{"webp"});
        EXTENSIONS_BY_FORMAT.put("GIF", new String[]{"gif"});

        EXTENSIONS_BY_FORMAT.put("PDF", new String[]{"pdf"});
        EXTENSIONS_BY_FORMAT.put("Word", new String[]{"docx", "doc"});
        EXTENSIONS_BY_FORMAT.put("Excel", new String[]{"xlsx", "xls"});
        EXTENSIONS_BY_FORMAT.put("PPT", new String[]{"pptx", "ppt"});
        EXTENSIONS_BY_FORMAT.put("TXT", new String[]{"txt"});
        EXTENSIONS_BY_FORMAT.put("CSV", new String[]{"csv"});

        EXTENSIONS_BY_FORMAT.put("MP3", new String[]{"mp3"});
        EXTENSIONS_BY_FORMAT.put("WAV", new String[]{"wav"});
        EXTENSIONS_BY_FORMAT.put("AAC/M4A", new String[]{"aac", "m4a"});
        EXTENSIONS_BY_FORMAT.put("OGG/OPUS", new String[]{"ogg", "opus"});
        EXTENSIONS_BY_FORMAT.put("FLAC", new String[]{"flac"});

        EXTENSIONS_BY_FORMAT.put("MP4", new String[]{"mp4"});
        EXTENSIONS_BY_FORMAT.put("AVI", new String[]{"avi"});
        EXTENSIONS_BY_FORMAT.put("MOV", new String[]{"mov"});
        EXTENSIONS_BY_FORMAT.put("FLV", new String[]{"flv"});
    }

    // Set right before launching the picker, read back in the picker's callback.
    private String pendingFormat;
    private ActivityResultLauncher<String[]> filePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress_format);

        buildFormatMap();

        gridFormats = findViewById(R.id.gridFormats);
        bottomNav = findViewById(R.id.bottomNav);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        filePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return; // user cancelled the picker

                    String pickedName = FileUtils.getDisplayName(this, uri);
                    String pickedExt = FileUtils.getExtension(pickedName);
                    String[] allowedExts = EXTENSIONS_BY_FORMAT.get(pendingFormat);
                    if (allowedExts != null && !Arrays.asList(allowedExts).contains(pickedExt)) {
                        Toast.makeText(this,
                                "Wrong file format selected. Please select a " + pendingFormat + " file.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String category = getIntent().getStringExtra("CATEGORY");
                    if (category == null) category = "Images";

                    Intent intent = new Intent(CompressFormatActivity.this, CompressionProcessActivity.class);
                    intent.putExtra(CompressionProcessActivity.EXTRA_CATEGORY, category);
                    intent.putExtra(CompressionProcessActivity.EXTRA_FORMAT, pendingFormat);
                    intent.putExtra(CompressionProcessActivity.EXTRA_URI, uri);
                    // Persist read access so the new activity/background thread can still open this content Uri.
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
        );

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
        formatsByCategory.put("Images", Arrays.asList("JPEG/JPG", "PNG", "WEBP", "GIF"));
        formatsByCategory.put("Documents", Arrays.asList("PDF", "Word", "Excel", "PPT", "TXT", "CSV"));
        formatsByCategory.put("Audio", Arrays.asList("MP3", "WAV", "AAC/M4A", "OGG/OPUS", "FLAC"));
        formatsByCategory.put("Video", Arrays.asList("MP4", "AVI", "MOV", "FLV"));
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
                            pendingFormat = finalFormat;
                            String[] mimes = MIME_TYPES.get(finalFormat);
                            if (mimes == null) mimes = new String[]{"*/*"};
                            filePicker.launch(mimes);
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
                    Intent intent = new Intent(CompressFormatActivity.this, HistoryActivity.class);
                    startActivity(intent);
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