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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import com.example.hybridformatconverter.data.HistoryRepository;
public class ConversionActivity extends AppCompatActivity {
    private static final String TAG = "ConversionActivity";
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
    private ImageView imgSelectedFile;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private MaterialButton btnConvert;
    private MaterialButton btnDownload;
    private MaterialButton btnRetry;
    private String conversionLabel;
    private String fromFormat;
    private String toFormat;
    private String inputMimeType;
    private String category;
    private Uri selectedFileUri;
    private File outputFile;
    private final ActivityResultLauncher<Intent> filePicker =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                            finish();
                            return;}
                        selectedFileUri = result.getData().getData();
                        if (selectedFileUri == null) {
                            finish();
                            return;}
                        String fileName = getFileName(selectedFileUri);
                        if (isCorrectFormat(fileName)) {showFileSelected(fileName);
                        } else {
                            selectedFileUri = null;
                            Toast.makeText(this, "Wrong file format selected. Please select a " + fromFormat.toUpperCase(Locale.ROOT) + " file.", Toast.LENGTH_LONG).show();
                            openFilePicker();}});
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);
        conversionLabel = getIntent().getStringExtra("CONVERSION_LABEL");
        fromFormat = getIntent().getStringExtra("FROM_FORMAT");
        toFormat = getIntent().getStringExtra("TO_FORMAT");
        inputMimeType = getIntent().getStringExtra("MIME_TYPE");
        category = getIntent().getStringExtra("CATEGORY");
        if (conversionLabel == null) {conversionLabel = "Convert";}
        if (fromFormat == null) {fromFormat = "";}
        if (toFormat == null) {toFormat = "";}
        if (inputMimeType == null) {inputMimeType = "*/*";}
        if (category == null) {category = "";}
        fromFormat = fromFormat.trim().toLowerCase(Locale.ROOT);
        toFormat = toFormat.trim().toLowerCase(Locale.ROOT);
        initViews();
        openFilePicker();}
    private void initViews() {
        layoutFileSelected = findViewById(R.id.layoutFileSelected);
        layoutConverting = findViewById(R.id.layoutConverting);
        layoutDone = findViewById(R.id.layoutDone);
        layoutError = findViewById(R.id.layoutError);
        tvConversionLabel = findViewById(R.id.tvConversionLabel);
        tvSelectedFileName = findViewById(R.id.tvSelectedFileName);
        tvSelectedLabel = findViewById(R.id.tvSelectedLabel);
        tvStatus = findViewById(R.id.tvStatus);
        tvDoneMessage = findViewById(R.id.tvDoneMessage);
        tvError = findViewById(R.id.tvError);
        imgSelectedFile = findViewById(R.id.imgSelectedFile);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
        btnConvert = findViewById(R.id.btnConvert);
        btnDownload = findViewById(R.id.btnDownload);
        btnRetry = findViewById(R.id.btnRetry);
        if (tvConversionLabel != null) {tvConversionLabel.setText(conversionLabel);}
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());}
        if (btnConvert != null) {
            btnConvert.setOnClickListener(v -> {
                        if (selectedFileUri == null) {
                            Toast.makeText(this, "Please select a file first.", Toast.LENGTH_SHORT).show();
                            return;}
                        String fileName = getFileName(selectedFileUri);
                        if (!isCorrectFormat(fileName)) {
                            Toast.makeText(this, "Wrong file format selected.", Toast.LENGTH_LONG).show();
                            return;}
                        startConversion(selectedFileUri);}
            );}
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> downloadOutputFile());}
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> openFilePicker());}
        if (layoutFileSelected != null) {layoutFileSelected.setVisibility(View.GONE);}
        if (layoutConverting != null) {layoutConverting.setVisibility(View.GONE);}
        if (layoutDone != null) {layoutDone.setVisibility(View.GONE);}
        if (layoutError != null) {layoutError.setVisibility(View.GONE);}
        if (btnDownload != null) {btnDownload.setVisibility(View.GONE);}}
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String mimeType = getMimeTypeForFormat(fromFormat);
        if (isImageFormat(fromFormat)) {
            intent.setType("image/*");
        } else if (isAudioFormat(fromFormat)) {
            intent.setType("audio/*");
        } else if (isVideoFormat(fromFormat)) {
            intent.setType("video/*");
        } else {intent.setType(mimeType);}
        filePicker.launch(intent);}
    private String getMimeTypeForFormat(String format) {
        if (format == null) {return "*/*";}
        switch (format.toLowerCase(Locale.ROOT)) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png": return "image/png";
            case "webp": return "image/webp";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "tiff":
            case "tif":
                return "image/tiff";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "aac": return "audio/aac";
            case "m4a": return "audio/mp4";
            case "ogg": return "audio/ogg";
            case "opus": return "audio/ogg";
            case "flac": return "audio/flac";
            case "wma": return "audio/x-ms-wma";
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "flv": return "video/x-flv";
            default: return "*/*";}}
    private boolean isImageFormat(String format) {
        if (format == null) {
            return false;}
        return format.equalsIgnoreCase("jpg")
                || format.equalsIgnoreCase("jpeg")
                || format.equalsIgnoreCase("png")
                || format.equalsIgnoreCase("webp")
                || format.equalsIgnoreCase("gif")
                || format.equalsIgnoreCase("bmp")
                || format.equalsIgnoreCase("tiff")
                || format.equalsIgnoreCase("tif");}
    private boolean isAudioFormat(String format) {
        if (format == null) {return false;}
        return format.equalsIgnoreCase("mp3")
                || format.equalsIgnoreCase("wav")
                || format.equalsIgnoreCase("aac")
                || format.equalsIgnoreCase("m4a")
                || format.equalsIgnoreCase("ogg")
                || format.equalsIgnoreCase("opus")
                || format.equalsIgnoreCase("flac")
                || format.equalsIgnoreCase("wma");}
    private boolean isVideoFormat(String format) {
        if (format == null) {return false;}
        return format.equalsIgnoreCase("mp4")
                || format.equalsIgnoreCase("avi")
                || format.equalsIgnoreCase("mov")
                || format.equalsIgnoreCase("flv");}
    private boolean isCorrectFormat(String fileName) {
        if (fileName == null || fromFormat == null || fromFormat.isEmpty()) {return false;}
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        String requiredExtension = "." + fromFormat.toLowerCase(Locale.ROOT);
        return lowerName.endsWith(requiredExtension);}
    private void startConversion(Uri inputUri) {
        showConverting();
        new Thread(() -> {File inputFile = null;
            try {inputFile = new File(getCacheDir(), "conversion_input." + fromFormat);
                if (inputFile.exists()) {inputFile.delete();}
                InputStream inputStream = getContentResolver().openInputStream(inputUri);
                if (inputStream == null) {runOnUiThread(() -> showError("Unable to open selected file."));
                    return;}
                FileOutputStream fileOutputStream = new FileOutputStream(inputFile);
                byte[] buffer = new byte[8192];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, length);}
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                if (!inputFile.exists() || inputFile.length() <= 0) {
                    runOnUiThread(() -> showError("Selected file is empty or could not be copied."));
                    return;}
                outputFile = new File(getCacheDir(), "converted." + toFormat);
                if (outputFile.exists()) {outputFile.delete();}
                String inputPath = inputFile.getAbsolutePath();
                String outputPath = outputFile.getAbsolutePath();
                String command;
                if (isVideoFormat(fromFormat) && isVideoFormat(toFormat)) {
                    command = buildVideoCommand(inputPath, outputPath);}
                else if (isImageFormat(fromFormat) && isImageFormat(toFormat)) {
                    command = buildImageCommand(inputPath, outputPath);}
                else if (isAudioFormat(fromFormat) && isAudioFormat(toFormat)) {
                    command = buildAudioCommand(inputPath, outputPath);}
                else {
                    command = "-y " + "-i \"" + inputPath + "\" " + "\"" + outputPath + "\"";}
                Log.d(TAG, "FROM: " + fromFormat + " TO: " + toFormat);
                Log.d(TAG, "FFmpeg command: " + command);
                FFmpegSession session = FFmpegKit.execute(command);
                ReturnCode returnCode = session.getReturnCode();
                String ffmpegOutput = session.getOutput();
                Log.d(TAG, "Return code: " + returnCode);
                Log.d(TAG, "FFmpeg output:\n" + ffmpegOutput);
                if (ReturnCode.isSuccess(returnCode) && outputFile.exists() && outputFile.length() > 0) {
                    runOnUiThread(this::showDone);}
                else {
                    Log.e(TAG, "Conversion failed.");
                    Log.e(TAG, ffmpegOutput != null ? ffmpegOutput : "No FFmpeg output.");
                    runOnUiThread(() -> showError("FFmpeg could not convert this file."));}
            } catch (Exception e) {
                Log.e(TAG, "Conversion exception", e);
                String error = e.getMessage();
                if (error == null || error.trim().isEmpty()) {
                    error = "Unknown conversion error.";}
                String finalError = "Error: " + error;
                runOnUiThread(() -> showError(finalError));}
        }).start();}
    private String buildVideoCommand(
            String inputPath,
            String outputPath) {
        if (toFormat.equalsIgnoreCase("mp4")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-map 0:v:0 " + "-map 0:a? " + "-c:v mpeg4 " + "-q:v 3 " + "-c:a aac " + "-b:a 128k " + "-pix_fmt yuv420p " + "-movflags +faststart " + "-f mp4 " + "\"" + outputPath + "\"";}
        if (fromFormat.equalsIgnoreCase("mp4") && toFormat.equalsIgnoreCase("mov")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-map 0:v:0 " + "-map 0:a? " + "-c:v mpeg4 " + "-q:v 3 " + "-c:a aac " + "-b:a 128k " + "-pix_fmt yuv420p " + "-movflags +faststart " + "-f mov " + "\"" + outputPath + "\"";}
        if (fromFormat.equalsIgnoreCase("mp4") && toFormat.equalsIgnoreCase("avi")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-map 0:v:0 " + "-map 0:a? " + "-c:v mpeg4 " + "-q:v 3 " + "-c:a mp3 " + "-b:a 128k " + "-pix_fmt yuv420p " + "-f avi " + "\"" + outputPath + "\"";
        }
        if (fromFormat.equalsIgnoreCase("mp4") && toFormat.equalsIgnoreCase("flv")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-map 0:v:0 " + "-map 0:a? " + "-c:v flv " + "-q:v 5 " + "-c:a aac " + "-b:a 128k " + "-pix_fmt yuv420p " + "-f flv " + "\"" + outputPath + "\"";}
        return "-y " + "-i \"" + inputPath + "\" " + "-map 0:v:0 " + "-map 0:a? " + "-c:v mpeg4 " + "-q:v 3 " + "-c:a aac " + "-b:a 128k " + "-pix_fmt yuv420p " + "\"" + outputPath + "\"";}
    private String buildImageCommand(
            String inputPath,
            String outputPath) {
        String format = toFormat.toLowerCase(Locale.ROOT);
        if (format.equals("jpg") || format.equals("jpeg")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-frames:v 1 " + "-q:v 2 " + "-f image2 " + "\"" + outputPath + "\"";}
        if (format.equals("png")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-frames:v 1 " + "-f image2 " + "\"" + outputPath + "\"";}
        if (format.equals("webp")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-frames:v 1 " + "-c:v libwebp " + "-q:v 80 " + "-f image2 " + "\"" + outputPath + "\"";}
        if (format.equals("bmp")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-frames:v 1 " + "-f image2 " + "\"" + outputPath + "\"";}
        if (format.equals("gif")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-f gif " + "\"" + outputPath + "\"";}
        return "-y " + "-i \"" + inputPath + "\" " + "-frames:v 1 " + "\"" + outputPath + "\"";}
    private String buildAudioCommand(String inputPath, String outputPath) {
        String format = toFormat.toLowerCase(Locale.ROOT);
        if (format.equals("mp3")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a libmp3lame " + "-b:a 192k " + "-f mp3 " + "\"" + outputPath + "\"";}
        if (format.equals("wav")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a pcm_s16le " + "-f wav " + "\"" + outputPath + "\"";}
        if (format.equals("aac")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a aac " + "-b:a 192k " + "-f adts " + "\"" + outputPath + "\"";}
        if (format.equals("m4a")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a aac " + "-b:a 192k " + "-movflags +faststart " + "-f mp4 " + "\"" + outputPath + "\"";}
        if (format.equals("ogg")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a libvorbis " + "-q:a 5 " + "-f ogg " + "\"" + outputPath + "\"";}
        if (format.equals("opus")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a libopus " + "-b:a 128k " + "-f opus " + "\"" + outputPath + "\"";}
        if (format.equals("flac")) {
            return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a flac " + "-f flac " + "\"" + outputPath + "\"";}
        return "-y " + "-i \"" + inputPath + "\" " + "-vn " + "-c:a aac " + "-b:a 192k " + "\"" + outputPath + "\"";}
    private void downloadOutputFile() {
        if (outputFile == null || !outputFile.exists() || outputFile.length() <= 0) {
            Toast.makeText(this, "Output file not found", Toast.LENGTH_SHORT).show();
            return;}
        Uri destinationUri = null;
        try {String fileName = "converted." + toFormat.toLowerCase(Locale.ROOT
        );
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, getMimeTypeForFormat(toFormat));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                destinationUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            } else {
                destinationUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);}
            if (destinationUri == null) {
                Toast.makeText(this, "Could not create download file.", Toast.LENGTH_LONG).show();
                return;}
            InputStream input = new FileInputStream(outputFile);
            OutputStream output = getContentResolver().openOutputStream(destinationUri);
            if (output == null) {
                input.close();
                getContentResolver().delete(destinationUri, null, null);
                Toast.makeText(this, "Could not open output file.", Toast.LENGTH_LONG).show();
                return;}
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1
            ) {
                output.write(buffer, 0, length);}
            output.flush();
            output.close();
            input.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues completed = new ContentValues();
                completed.put(MediaStore.MediaColumns.IS_PENDING, 0);
                getContentResolver().update(destinationUri, completed, null, null);}
            String downloadedName = "converted." + toFormat.toLowerCase(Locale.ROOT);
            new HistoryRepository(this).addDownload(
                    downloadedName,
                    destinationUri.toString(),
                    isImageFormat(toFormat) ? "image" : isAudioFormat(toFormat) ? "audio" : isVideoFormat(toFormat) ? "video" : "document");
            Toast.makeText(this, "File downloaded successfully", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "File downloaded successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Download error", e);
            if (destinationUri != null) {
                try {
                    getContentResolver().delete(destinationUri, null, null);
                } catch (Exception ignored) {}}
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();}}
    private String getFileName(Uri uri) {
        String result = "Selected file";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        String name = cursor.getString(index);
                        if (name != null && !name.isEmpty()) {
                            result = name;}}}
            } finally {cursor.close();}}
        return result;}
    private void showFileSelected(
            String fileName) {
        if (layoutFileSelected != null) {layoutFileSelected.setVisibility(View.VISIBLE);}
        if (layoutConverting != null) {layoutConverting.setVisibility(View.GONE);}
        if (layoutDone != null) {layoutDone.setVisibility(View.GONE);}
        if (layoutError != null) {layoutError.setVisibility(View.GONE);}
        if (tvSelectedFileName != null) {tvSelectedFileName.setText(fileName);}
        if (tvSelectedLabel != null) {tvSelectedLabel.setText("File selected");}
        if (isImageFormat(fromFormat) && selectedFileUri != null && imgSelectedFile != null) {
            imgSelectedFile.setVisibility(View.VISIBLE);
            try {
                imgSelectedFile.setImageURI(selectedFileUri);
            } catch (Exception e) {
                imgSelectedFile.setVisibility(View.GONE);}
        } else if (imgSelectedFile != null) {
            imgSelectedFile.setVisibility(View.GONE);}
        if (btnConvert != null) {btnConvert.setVisibility(View.VISIBLE);}}
    private void showConverting() {
        if (layoutFileSelected != null) {layoutFileSelected.setVisibility(View.GONE);}
        if (layoutConverting != null) {layoutConverting.setVisibility(View.VISIBLE);}
        if (layoutDone != null) {layoutDone.setVisibility(View.GONE);}
        if (layoutError != null) {layoutError.setVisibility(View.GONE);}
        if (tvStatus != null) {
            tvStatus.setText("Converting " + fromFormat.toUpperCase(Locale.ROOT) + " to " + toFormat.toUpperCase(Locale.ROOT) + "...");}
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);}
        if (btnConvert != null) {
            btnConvert.setVisibility(View.GONE);}}
    private void showDone() {
        if (layoutFileSelected != null) {layoutFileSelected.setVisibility(View.GONE);}
        if (layoutConverting != null) {layoutConverting.setVisibility(View.GONE);}
        if (layoutDone != null) {layoutDone.setVisibility(View.VISIBLE);}
        if (layoutError != null) {layoutError.setVisibility(View.GONE);}
        if (btnConvert != null) {btnConvert.setVisibility(View.GONE);}
        if (btnDownload != null) {btnDownload.setVisibility(View.VISIBLE);}
        if (tvDoneMessage != null) {tvDoneMessage.setText("Successfully converted to " + toFormat.toUpperCase(Locale.ROOT) + "!");
        }}
    private void showError(
            String message) {
        if (layoutFileSelected != null) {layoutFileSelected.setVisibility(View.GONE);}
        if (layoutConverting != null) {layoutConverting.setVisibility(View.GONE);}
        if (layoutDone != null) {layoutDone.setVisibility(View.GONE);}
        if (layoutError != null) {layoutError.setVisibility(View.VISIBLE);}
        if (tvError != null) {tvError.setText(message);}
        if (btnConvert != null) {btnConvert.setVisibility(View.VISIBLE);}
        if (btnDownload != null) {btnDownload.setVisibility(View.GONE);}
    }}