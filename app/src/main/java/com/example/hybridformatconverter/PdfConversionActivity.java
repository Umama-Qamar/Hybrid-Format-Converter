package com.example.hybridformatconverter;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import com.tom_roush.pdfbox.multipdf.Splitter;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfConversionActivity extends AppCompatActivity {

    public static final String EXTRA_ACTION = "PDF_ACTION";
    public static final String EXTRA_OUTPUT_IMAGE_FORMAT = "OUTPUT_IMAGE_FORMAT"; // "png" or "jpg"
    public static final String EXTRA_CONVERSION_LABEL = "CONVERSION_LABEL";       // optional display override

    public static final String ACTION_MERGE = "merge";
    public static final String ACTION_SPLIT = "split";
    public static final String ACTION_IMG_TO_PDF = "img_to_pdf";
    public static final String ACTION_PDF_TO_IMG = "pdf_to_img";

    private LinearLayout layoutFileSelected, layoutConverting, layoutDone, layoutError;
    private TextView tvConversionLabel, tvSelectedLabel, tvSelectedFileName, tvStatus, tvDoneMessage, tvError;
    private ImageView imgSelectedFile;
    private ProgressBar progressBar;
    private MaterialButton btnConvert, btnDownload, btnCompress, btnRetry;

    private String action;
    private String outputImageFormat = "png"; // used only for ACTION_PDF_TO_IMG
    private List<Uri> selectedUris = new ArrayList<>();
    private List<File> outputFiles = new ArrayList<>();

    private ActivityResultLauncher<String[]> multiFilePicker;
    private ActivityResultLauncher<String[]> singleFilePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_conversion);

        PDFBoxResourceLoader.init(getApplicationContext());

        action = getIntent().getStringExtra(EXTRA_ACTION);
        if (action == null) {
            finish();
            return;
        }

        String customFormat = getIntent().getStringExtra(EXTRA_OUTPUT_IMAGE_FORMAT);
        if (customFormat != null && !customFormat.isEmpty()) {
            outputImageFormat = customFormat.toLowerCase();
        }

        initViews();
        registerPickers();
        launchPicker();
    }

    private void initViews() {
        layoutFileSelected = findViewById(R.id.layoutFileSelected);
        layoutConverting = findViewById(R.id.layoutConverting);
        layoutDone = findViewById(R.id.layoutDone);
        layoutError = findViewById(R.id.layoutError);

        tvConversionLabel = findViewById(R.id.tvConversionLabel);
        tvSelectedLabel = findViewById(R.id.tvSelectedLabel);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        tvStatus = findViewById(R.id.tvStatus);
        tvDoneMessage = findViewById(R.id.tvDoneMessage);
        tvError = findViewById(R.id.tvError);

        imgSelectedFile = findViewById(R.id.imgSelectedFile);
        progressBar = findViewById(R.id.progressBar);

        btnConvert = findViewById(R.id.btnConvert);
        btnDownload = findViewById(R.id.btnDownload);
        btnCompress = findViewById(R.id.btnCompress);
        btnRetry = findViewById(R.id.btnRetry);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String customLabel = getIntent().getStringExtra(EXTRA_CONVERSION_LABEL);
        tvConversionLabel.setText(customLabel != null ? customLabel : titleForAction(action));

        btnConvert.setText(buttonLabelForAction(action));

        btnConvert.setOnClickListener(v -> startConversion());
        btnDownload.setOnClickListener(v -> downloadOutputs());
        btnCompress.setOnClickListener(v ->
                Toast.makeText(this, "Compression coming soon!", Toast.LENGTH_SHORT).show());
        btnRetry.setOnClickListener(v -> launchPicker());
    }

    private String titleForAction(String action) {
        switch (action) {
            case ACTION_MERGE: return "Merge PDF";
            case ACTION_SPLIT: return "Split PDF";
            case ACTION_IMG_TO_PDF: return "Image to PDF";
            case ACTION_PDF_TO_IMG: return "PDF to Image";
            default: return "Convert";
        }
    }

    private String buttonLabelForAction(String action) {
        switch (action) {
            case ACTION_MERGE: return "MERGE";
            case ACTION_SPLIT: return "SPLIT";
            default: return "CONVERT";
        }
    }

    private void registerPickers() {
        multiFilePicker = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris == null || uris.isEmpty()) { finish(); return; }
                    selectedUris = uris;
                    showFilesSelected();
                });

        singleFilePicker = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) { finish(); return; }
                    selectedUris = new ArrayList<>();
                    selectedUris.add(uri);
                    showFilesSelected();
                });
    }

    private void launchPicker() {
        if (action.equals(ACTION_MERGE)) {
            multiFilePicker.launch(new String[]{"application/pdf"});
        } else if (action.equals(ACTION_SPLIT) || action.equals(ACTION_PDF_TO_IMG)) {
            singleFilePicker.launch(new String[]{"application/pdf"});
        } else if (action.equals(ACTION_IMG_TO_PDF)) {
            multiFilePicker.launch(new String[]{"image/jpeg", "image/png"});
        }
    }

    private void showFilesSelected() {
        layoutFileSelected.setVisibility(View.VISIBLE);
        layoutConverting.setVisibility(View.GONE);
        layoutDone.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);

        tvSelectedLabel.setText(selectedUris.size() == 1 ? "File selected" : selectedUris.size() + " files selected");
        tvSelectedFileName.setText(getDisplayName(selectedUris.get(0)));

        if (action.equals(ACTION_IMG_TO_PDF)) {
            imgSelectedFile.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgSelectedFile.setPadding(0, 0, 0, 0);
            imgSelectedFile.setImageURI(selectedUris.get(0));
        } else {
            imgSelectedFile.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            int pad = (int) (getResources().getDisplayMetrics().density * 36);
            imgSelectedFile.setPadding(pad, pad, pad, pad);
            imgSelectedFile.setImageResource(R.drawable.ic_pdf);
        }
    }

    private String getDisplayName(Uri uri) {
        String name = uri.getLastPathSegment();
        return name != null ? name : "Selected file";
    }

    private void showConverting() {
        layoutFileSelected.setVisibility(View.GONE);
        layoutConverting.setVisibility(View.VISIBLE);
        layoutDone.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        tvStatus.setText("Converting...");
    }

    private void showDone(int count) {
        layoutFileSelected.setVisibility(View.GONE);
        layoutConverting.setVisibility(View.GONE);
        layoutDone.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        tvDoneMessage.setText(count == 1
                ? "Successfully converted!"
                : "Successfully converted " + count + " files!");
    }

    private void showError(String message) {
        layoutFileSelected.setVisibility(View.GONE);
        layoutConverting.setVisibility(View.GONE);
        layoutDone.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    // Thrown for user-facing validation problems (wrong format, multi-page PDF, unsupported output)
    private static class ConversionValidationException extends Exception {
        ConversionValidationException(String message) { super(message); }
    }

    private void startConversion() {
        showConverting();
        new Thread(() -> {
            try {
                outputFiles.clear();

                // ---- Pre-flight validation (runs before any real work) ----
                validateBeforeConversion();

                switch (action) {
                    case ACTION_MERGE: doMerge(); break;
                    case ACTION_SPLIT: doSplit(); break;
                    case ACTION_IMG_TO_PDF: doImagesToPdf(); break;
                    case ACTION_PDF_TO_IMG: doPdfToImages(); break;
                }
                runOnUiThread(() -> showDone(outputFiles.size()));
            } catch (ConversionValidationException ve) {
                String msg = ve.getMessage();
                runOnUiThread(() -> showError(msg));
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Conversion failed.";
                runOnUiThread(() -> showError(msg));
            }
        }).start();
    }

    // ---------- VALIDATION ----------
    private void validateBeforeConversion() throws Exception {
        boolean requiresPdfInput = action.equals(ACTION_MERGE)
                || action.equals(ACTION_SPLIT)
                || action.equals(ACTION_PDF_TO_IMG);

        if (requiresPdfInput) {
            for (Uri uri : selectedUris) {
                if (!isValidPdf(uri)) {
                    throw new ConversionValidationException(
                            "Wrong file format. Please select a valid PDF file.");
                }
            }
        }

        if (action.equals(ACTION_IMG_TO_PDF)) {
            for (Uri uri : selectedUris) {
                if (!isValidImage(uri)) {
                    throw new ConversionValidationException(
                            "Wrong file format. Please select a valid JPG or PNG image.");
                }
            }
        }

        if (action.equals(ACTION_PDF_TO_IMG)) {
            // Block unsupported output formats instead of silently falling back to PNG
            if (outputImageFormat.equals("tiff") || outputImageFormat.equals("tif")
                    || outputImageFormat.equals("bmp")) {
                throw new ConversionValidationException(
                        toFormatDisplayName(outputImageFormat) + " output isn't supported for offline conversion on this device. "
                                + "Please choose JPG or PNG instead.");
            }

            // Multi-page PDFs aren't allowed for single-image conversions
            InputStream is = getContentResolver().openInputStream(selectedUris.get(0));
            PDDocument document = PDDocument.load(is);
            int pageCount = document.getNumberOfPages();
            document.close();

            if (pageCount > 1) {
                throw new ConversionValidationException(
                        "Multiple page PDF is not allowed. Please select a single-page PDF, "
                                + "or use Split PDF first to separate the pages.");
            }
        }
    }

    private String toFormatDisplayName(String fmt) {
        if (fmt.equals("tif")) return "TIFF";
        return fmt.toUpperCase();
    }

    /** Checks the actual file bytes for a PDF signature, not just its extension/MIME. */
    private boolean isValidPdf(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return false;
            byte[] header = new byte[5];
            int read = is.read(header, 0, 5);
            if (read < 5) return false;
            String sig = new String(header, "US-ASCII");
            return sig.equals("%PDF-");
        } catch (Exception e) {
            return false;
        }
    }

    /** Confirms the file actually decodes as a bitmap image (catches mislabeled/corrupt files). */
    private boolean isValidImage(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return false;
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeStream(is, null, options);
            return options.outWidth > 0 && options.outHeight > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- MERGE ----------
    private void doMerge() throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        File outFile = new File(getCacheDir(), "merged_" + System.currentTimeMillis() + ".pdf");
        merger.setDestinationStream(new FileOutputStream(outFile));

        for (Uri uri : selectedUris) {
            InputStream is = getContentResolver().openInputStream(uri);
            merger.addSource(is);
        }
        merger.mergeDocuments(null);
        outputFiles.add(outFile);
    }

    // ---------- SPLIT ----------
    private void doSplit() throws Exception {
        InputStream is = getContentResolver().openInputStream(selectedUris.get(0));
        PDDocument document = PDDocument.load(is);

        Splitter splitter = new Splitter();
        splitter.setSplitAtPage(1);
        List<PDDocument> pages = splitter.split(document);

        int i = 1;
        for (PDDocument page : pages) {
            File pageFile = new File(getCacheDir(), "split_page_" + i + "_" + System.currentTimeMillis() + ".pdf");
            page.save(pageFile);
            page.close();
            outputFiles.add(pageFile);
            i++;
        }
        document.close();
    }

    // ---------- IMAGE(S) TO PDF ----------
    private void doImagesToPdf() throws Exception {
        PDDocument document = new PDDocument();
        for (Uri uri : selectedUris) {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(is);
            if (bitmap == null) continue;

            PDPage page = new PDPage(new PDRectangle(bitmap.getWidth(), bitmap.getHeight()));
            document.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bitmap);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.drawImage(pdImage, 0, 0, bitmap.getWidth(), bitmap.getHeight());
            contentStream.close();
        }

        File outFile = new File(getCacheDir(), "images_" + System.currentTimeMillis() + ".pdf");
        document.save(outFile);
        document.close();
        outputFiles.add(outFile);
    }

    // ---------- PDF TO IMAGE ----------
    private void doPdfToImages() throws Exception {
        boolean asJpg = outputImageFormat.equals("jpg") || outputImageFormat.equals("jpeg");
        String ext = asJpg ? "jpg" : "png";

        InputStream is = getContentResolver().openInputStream(selectedUris.get(0));
        PDDocument document = PDDocument.load(is);
        PDFRenderer renderer = new PDFRenderer(document);

        // Validated to be exactly 1 page before we got here
        Bitmap bitmap = renderer.renderImageWithDPI(0, 150);
        File imgFile = new File(getCacheDir(), "pdf_page_" + System.currentTimeMillis() + "." + ext);
        OutputStream out = new FileOutputStream(imgFile);
        if (asJpg) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out);
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        out.close();
        outputFiles.add(imgFile);

        document.close();
    }

    // ---------- DOWNLOAD ----------
    private void downloadOutputs() {
        if (outputFiles.isEmpty()) {
            Toast.makeText(this, "No output files found", Toast.LENGTH_SHORT).show();
            return;
        }

        int savedCount = 0;
        for (File file : outputFiles) {
            if (saveFileToDownloads(file)) savedCount++;
        }

        if (savedCount == outputFiles.size()) {
            Toast.makeText(this, savedCount + " file(s) saved to Downloads", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Saved " + savedCount + " of " + outputFiles.size() + " files", Toast.LENGTH_LONG).show();
        }
    }

    private boolean saveFileToDownloads(File file) {
        Uri destinationUri = null;
        try {
            String name = file.getName().toLowerCase();
            String mimeType;
            if (name.endsWith(".pdf")) mimeType = "application/pdf";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mimeType = "image/jpeg";
            else mimeType = "image/png";

            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            }

            destinationUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (destinationUri == null) return false;

            InputStream input = new FileInputStream(file);
            OutputStream output = getContentResolver().openOutputStream(destinationUri);
            if (output == null) { input.close(); return false; }

            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            input.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues completed = new ContentValues();
                completed.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(destinationUri, completed, null, null);
            }

            return true;
        } catch (Exception e) {
            if (destinationUri != null) {
                try { getContentResolver().delete(destinationUri, null, null); } catch (Exception ignored) {}
            }
            return false;
        }
    }
}