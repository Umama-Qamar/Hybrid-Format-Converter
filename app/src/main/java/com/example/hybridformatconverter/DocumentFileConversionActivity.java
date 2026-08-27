package com.example.hybridformatconverter;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.example.hybridformatconverter.data.HistoryRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
public class DocumentFileConversionActivity
        extends AppCompatActivity {

    private LinearLayout layoutFileSelected;
    private LinearLayout layoutConverting;
    private LinearLayout layoutDone;
    private LinearLayout layoutError;

    private TextView tvConversionLabel;
    private TextView tvSelectedFileName;
    private TextView tvSelectedLabel;
    private TextView tvStatus;
    private TextView tvDoneMessage;
    private TextView tvError;

    private ProgressBar progressBar;

    private ImageButton btnBack;

    private MaterialButton btnConvert;
    private MaterialButton btnDownload;
    private MaterialButton btnRetry;

    private String fromFormat;
    private String toFormat;
    private String conversionLabel;

    private Uri selectedFileUri;

    private File outputFile;

    // =========================================================
    // FILE PICKER
    // =========================================================

    private final ActivityResultLauncher<Intent> filePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != Activity.RESULT_OK
                                || result.getData() == null) {

                            finish();
                            return;
                        }

                        selectedFileUri =
                                result.getData().getData();

                        if (selectedFileUri == null) {

                            finish();
                            return;
                        }

                        /*
                         * Keep permission to read the selected
                         * document after this activity continues.
                         */
                        try {

                            getContentResolver()
                                    .takePersistableUriPermission(
                                            selectedFileUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    );

                        } catch (Exception ignored) {
                            // Some providers do not support
                            // persistable permissions.
                        }

                        String fileName =
                                getFileName(selectedFileUri);

                        if (isCorrectFormat(fileName)) {

                            showFileSelected(fileName);

                        } else {

                            selectedFileUri = null;

                            Toast.makeText(
                                    this,
                                    "Wrong file format selected. Please select a "
                                            + fromFormat
                                            + " file.",
                                    Toast.LENGTH_LONG
                            ).show();

                            openFilePicker();
                        }
                    }
            );


    // =========================================================
    // ON CREATE
    // =========================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_document_file_conversion
        );

        /*
         * IMPORTANT:
         *
         * PDFBox Android requires its resource loader
         * to be initialized before PDFBox is used.
         *
         * This fixes errors such as:
         *
         * GlyphList
         * 'com/tom_roush/pdfbox/resources/glyphlist/glyphlist.txt'
         * not found
         */
        try {

            PDFBoxResourceLoader.init(
                    getApplicationContext()
            );

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "PDF library initialization failed.",
                    Toast.LENGTH_LONG
            ).show();
        }


        // =====================================================
        // GET CONVERSION INFORMATION
        // =====================================================

        fromFormat =
                getIntent()
                        .getStringExtra(
                                "FROM_FORMAT"
                        );

        toFormat =
                getIntent()
                        .getStringExtra(
                                "TO_FORMAT"
                        );

        conversionLabel =
                getIntent()
                        .getStringExtra(
                                "CONVERSION_LABEL"
                        );


        if (fromFormat == null) {
            fromFormat = "";
        }

        if (toFormat == null) {
            toFormat = "";
        }

        if (conversionLabel == null) {

            conversionLabel =
                    fromFormat
                            + " to "
                            + toFormat;
        }


        fromFormat =
                fromFormat
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        toFormat =
                toFormat
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        initViews();

        openFilePicker();
    }


    // =========================================================
    // INITIALIZE VIEWS
    // =========================================================

    private void initViews() {

        layoutFileSelected =
                findViewById(
                        R.id.layoutFileSelected
                );

        layoutConverting =
                findViewById(
                        R.id.layoutConverting
                );

        layoutDone =
                findViewById(
                        R.id.layoutDone
                );

        layoutError =
                findViewById(
                        R.id.layoutError
                );


        tvConversionLabel =
                findViewById(
                        R.id.tvConversionLabel
                );

        tvSelectedFileName =
                findViewById(
                        R.id.tvSelectedFileName
                );

        tvSelectedLabel =
                findViewById(
                        R.id.tvSelectedLabel
                );

        tvStatus =
                findViewById(
                        R.id.tvStatus
                );

        tvDoneMessage =
                findViewById(
                        R.id.tvDoneMessage
                );

        tvError =
                findViewById(
                        R.id.tvError
                );


        progressBar =
                findViewById(
                        R.id.progressBar
                );


        btnBack =
                findViewById(
                        R.id.btnBack
                );

        btnConvert =
                findViewById(
                        R.id.btnConvert
                );

        btnDownload =
                findViewById(
                        R.id.btnDownload
                );

        btnRetry =
                findViewById(
                        R.id.btnRetry
                );


        tvConversionLabel.setText(
                conversionLabel
        );


        // =====================================================
        // BUTTONS
        // =====================================================

        btnBack.setOnClickListener(
                v -> finish()
        );


        btnConvert.setOnClickListener(
                v -> {

                    if (selectedFileUri == null) {

                        Toast.makeText(
                                this,
                                "Please select a file first.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    startConversion(
                            selectedFileUri
                    );
                }
        );


        btnDownload.setOnClickListener(
                v -> downloadOutputFile()
        );


        btnRetry.setOnClickListener(
                v -> openFilePicker()
        );


        // =====================================================
        // INITIAL VISIBILITY
        // =====================================================

        layoutFileSelected.setVisibility(
                View.GONE
        );

        layoutConverting.setVisibility(
                View.GONE
        );

        layoutDone.setVisibility(
                View.GONE
        );

        layoutError.setVisibility(
                View.GONE
        );

        btnDownload.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // FILE PICKER
    // =========================================================

    private void openFilePicker() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        // Use a broad base type; narrow it with EXTRA_MIME_TYPES.
        // Some providers mislabel CSV/XLSX/etc, so a single strict
        // MIME string can hide files that are actually present.
        intent.setType("*/*");

        String[] acceptableMimeTypes =
                getAcceptableMimeTypes(fromFormat);

        if (acceptableMimeTypes != null
                && acceptableMimeTypes.length > 0) {

            intent.putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    acceptableMimeTypes
            );
        }

        filePicker.launch(intent);
    }
    private String[] getAcceptableMimeTypes(String format) {

        if (format == null) {
            return null;
        }

        if (format.equalsIgnoreCase("CSV")) {

            return new String[] {
                    "text/csv",
                    "text/comma-separated-values",
                    "application/csv",
                    "application/vnd.ms-excel",
                    "text/plain"
            };
        }

        String mime =
                getMimeTypeForFormat(format);

        if (mime == null || mime.equals("*/*")) {
            return null;
        }

        return new String[] { mime };
    }


    // =========================================================
    // MIME TYPE
    // =========================================================

    private String getMimeTypeForFormat(
            String format) {

        if (format == null) {
            return "*/*";
        }

        switch (
                format.toUpperCase(
                        Locale.ROOT
                )
        ) {

            case "DOCX":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            case "PDF":
                return "application/pdf";

            case "TXT":
                return "text/plain";

            case "HTML":
                return "text/html";
            case "XLSX":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            case "CSV":
                return "text/csv";

            case "JSON":
                return "application/json";

            case "XML":
                return "application/xml";

            case "MD":
                return "text/markdown";

            case "EPUB":
                return "application/epub+zip";

            default:
                return "*/*";
        }
    }


    // =========================================================
    // FORMAT CHECK
    // =========================================================

    private boolean isCorrectFormat(
            String fileName) {

        if (fileName == null
                || fromFormat == null
                || fromFormat.isEmpty()) {

            return false;
        }

        String lower =
                fileName.toLowerCase(
                        Locale.ROOT
                );

        return lower.endsWith(
                "."
                        + fromFormat.toLowerCase(
                        Locale.ROOT
                )
        );
    }


    // =========================================================
    // START CONVERSION
    // =========================================================

    private void startConversion(
            Uri inputUri) {

        showConverting();

        new Thread(() -> {

            File inputFile = null;

            try {

                // =================================================
                // INITIALIZE PDFBOX FIRST
                // =================================================

                PdfConverter.initialize(
                        getApplicationContext()
                );


                // =================================================
                // FORMATS
                // =================================================

                String inputExtension =
                        fromFormat.toLowerCase(
                                Locale.ROOT
                        );

                String outputExtension =
                        toFormat.toLowerCase(
                                Locale.ROOT
                        );


                // =================================================
                // COPY INPUT TO CACHE
                // =================================================

                inputFile =
                        FileUtils.copyUriToTempFile(
                                this,
                                inputUri,
                                "document_input_",
                                "." + inputExtension
                        );

                if (inputFile == null
                        || !inputFile.exists()
                        || inputFile.length() == 0) {

                    throw new Exception(
                            "Could not read selected file."
                    );
                }


                // =================================================
                // CREATE OUTPUT
                // =================================================

                outputFile =
                        new File(
                                getCacheDir(),
                                "converted_document."
                                        + outputExtension
                        );

                if (outputFile.exists()) {
                    outputFile.delete();
                }


                // =================================================
                // CONVERT
                // =================================================

                DocumentConverter.convert(
                        getApplicationContext(),
                        inputFile,
                        outputFile,
                        fromFormat,
                        toFormat
                );


                // =================================================
                // CHECK OUTPUT
                // =================================================

                if (!outputFile.exists()
                        || outputFile.length() <= 0) {

                    throw new Exception(
                            "Conversion produced an empty file."
                    );
                }


                // =================================================
                // SUCCESS
                // =================================================

                runOnUiThread(
                        this::showDone
                );


            } catch (Exception e) {

                e.printStackTrace();

                String message =
                        e.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {

                    message =
                            "Document conversion failed.";
                }

                String finalMessage =
                        message;

                runOnUiThread(
                        () ->
                                showError(
                                        finalMessage
                                )
                );

            } finally {

                // =================================================
                // DELETE TEMP INPUT
                // =================================================

                if (inputFile != null
                        && inputFile.exists()) {

                    try {
                        inputFile.delete();
                    } catch (Exception ignored) {
                    }
                }
            }

        }).start();
    }


    // =========================================================
    // GET FILE NAME
    // =========================================================

    private String getFileName(
            Uri uri) {

        String result =
                "Selected file";


        Cursor cursor =
                getContentResolver()
                        .query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );


        if (cursor != null) {

            try {

                if (cursor.moveToFirst()) {

                    int index =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );


                    if (index != -1) {

                        String name =
                                cursor.getString(
                                        index
                                );


                        if (name != null
                                && !name.isEmpty()) {

                            result =
                                    name;
                        }
                    }
                }

            } finally {

                cursor.close();
            }
        }


        return result;
    }


    // =========================================================
    // FILE SELECTED
    // =========================================================

    private void showFileSelected(
            String fileName) {

        layoutFileSelected.setVisibility(
                View.VISIBLE
        );

        layoutConverting.setVisibility(
                View.GONE
        );

        layoutDone.setVisibility(
                View.GONE
        );

        layoutError.setVisibility(
                View.GONE
        );


        tvSelectedFileName.setText(
                fileName
        );


        tvSelectedLabel.setText(
                fromFormat
                        + " file selected"
        );


        btnConvert.setVisibility(
                View.VISIBLE
        );

        btnDownload.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // CONVERTING
    // =========================================================

    private void showConverting() {

        layoutFileSelected.setVisibility(
                View.GONE
        );

        layoutConverting.setVisibility(
                View.VISIBLE
        );

        layoutDone.setVisibility(
                View.GONE
        );

        layoutError.setVisibility(
                View.GONE
        );


        tvStatus.setText(
                "Converting "
                        + fromFormat
                        + " to "
                        + toFormat
                        + "..."
        );


        progressBar.setIndeterminate(
                true
        );


        btnConvert.setVisibility(
                View.GONE
        );

        btnDownload.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // DONE
    // =========================================================

    private void showDone() {

        layoutFileSelected.setVisibility(
                View.GONE
        );

        layoutConverting.setVisibility(
                View.GONE
        );

        layoutDone.setVisibility(
                View.VISIBLE
        );

        layoutError.setVisibility(
                View.GONE
        );


        btnDownload.setVisibility(
                View.VISIBLE
        );


        tvDoneMessage.setText(
                "Successfully converted to "
                        + toFormat
                        + "!"
        );
    }


    // =========================================================
    // ERROR
    // =========================================================

    private void showError(
            String message) {

        layoutFileSelected.setVisibility(
                View.GONE
        );

        layoutConverting.setVisibility(
                View.GONE
        );

        layoutDone.setVisibility(
                View.GONE
        );

        layoutError.setVisibility(
                View.VISIBLE
        );


        if (message == null
                || message.trim().isEmpty()) {

            message =
                    "Document conversion failed.";
        }


        tvError.setText(
                message
        );


        btnDownload.setVisibility(
                View.GONE
        );
    }


    // =========================================================
    // DOWNLOAD
    // =========================================================

    private void downloadOutputFile() {

        if (outputFile == null
                || !outputFile.exists()
                || outputFile.length() <= 0) {

            Toast.makeText(
                    this,
                    "Output file not found.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Uri destinationUri =
                null;


        try {

            String fileName =
                    "converted."
                            + toFormat.toLowerCase(
                            Locale.ROOT
                    );


            ContentValues values =
                    new ContentValues();


            values.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    fileName
            );


            values.put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    getMimeTypeForFormat(
                            toFormat
                    )
            );


            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.Q) {

                values.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                );


                values.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        1
                );
            }


            destinationUri =
                    getContentResolver()
                            .insert(
                                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                    values
                            );


            if (destinationUri == null) {

                throw new Exception(
                        "Could not create download file."
                );
            }


            try (
                    InputStream input =
                            new FileInputStream(
                                    outputFile
                            );

                    OutputStream output =
                            getContentResolver()
                                    .openOutputStream(
                                            destinationUri
                                    )
            ) {

                if (output == null) {

                    throw new Exception(
                            "Could not open download output."
                    );
                }


                byte[] buffer =
                        new byte[8192];


                int length;


                while (
                        (
                                length =
                                        input.read(buffer)
                        ) != -1
                ) {

                    output.write(
                            buffer,
                            0,
                            length
                    );
                }


                output.flush();
            }


            if (Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.Q) {

                ContentValues completed =
                        new ContentValues();


                completed.put(
                        MediaStore.MediaColumns.IS_PENDING,
                        0
                );


                getContentResolver()
                        .update(
                                destinationUri,
                                completed,
                                null,
                                null
                        );
            }


            String downloadedName = "converted." + toFormat.toLowerCase(Locale.ROOT);
            new HistoryRepository(this).addDownload(
                    downloadedName,
                    destinationUri.toString(),
                    "document"
            );

            Toast.makeText(
                    this,
                    "File downloaded successfully.",
                    Toast.LENGTH_SHORT
            ).show();
        } catch (Exception e) {
            e.printStackTrace();
            if (destinationUri != null) {

                try {

                    getContentResolver()
                            .delete(
                                    destinationUri,
                                    null,
                                    null
                            );

                } catch (Exception ignored) {
                }
            }
            String message =
                    e.getMessage();
            if (message == null
                    || message.trim().isEmpty()) {

                message =
                        "Download failed.";
            }
            Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}