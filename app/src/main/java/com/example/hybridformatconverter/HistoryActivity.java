package com.example.hybridformatconverter;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hybridformatconverter.data.HistoryItem;
import com.example.hybridformatconverter.data.HistoryRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.io.OutputStream;
public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private HistoryRepository repo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.recyclerHistory);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);
        repo = new HistoryRepository(this);
        adapter.setActionListener(new HistoryAdapter.ActionListener() {
            @Override
            public void onItemClick(HistoryItem item) {
                openFile(item);}
            @Override
            public void onShare(HistoryItem item) {
                shareItem(item);}
            @Override
            public void onDownload(HistoryItem item) {
                redownloadItem(item);}
            @Override
            public void onRename(HistoryItem item) {
                showRenameDialog(item);}
            @Override
            public void onDelete(HistoryItem item) {
                showDeleteConfirmation(item);}});
        setupBottomNavigation();
        loadHistory();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();}
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav =
                findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(
                R.id.nav_history
        );
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_history) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(
                        new Intent(
                                HistoryActivity.this,
                                MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(
                        new Intent(
                                HistoryActivity.this,
                                SettingsActivity.class));
                return true;}
            return false;});}
    private void loadHistory() {
        repo.getHistory(items -> runOnUiThread(() -> {
            adapter.setItems(items);
            boolean empty = items.isEmpty();
            findViewById(R.id.emptyState)
                    .setVisibility(
                            empty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(
                    empty ? View.GONE : View.VISIBLE);}));}
    private void openFile(HistoryItem item) {
        try {
            Uri uri = Uri.parse(item.filePath);
            String mimeType =
                    getContentResolver().getType(uri);
            if (mimeType == null) {
                mimeType = getMimeTypeFromFileName(item.fileName);}
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show();}}
    private String getMimeTypeFromFileName(
            String fileName) {
        if (fileName == null) {
            return "*/*";}
        String extension = "";
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            extension = fileName.substring(dot + 1).toLowerCase();}
        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt": return "text/plain";
            case "html":
            case "htm":
                return "text/html";
            case "md": return "text/markdown";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv": return "text/csv";
            case "json": return "application/json";
            case "xml": return "application/xml";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "webp": return "image/webp";
            case "mp3": return "audio/mpeg";
            case "wav": return "audio/wav";
            case "ogg": return "audio/ogg";
            case "m4a": return "audio/mp4";
            case "mp4": return "video/mp4";
            case "mkv": return "video/x-matroska";
            case "avi": return "video/x-msvideo";
            default: return "*/*";}}
    private void shareItem(HistoryItem item) {
        try {
            Uri uri = Uri.parse(item.filePath);
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                mimeType = getMimeTypeFromFileName(item.fileName);}
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share " + item.fileName));}
        catch (Exception e) {
            Toast.makeText(this, "Unable to share this file", Toast.LENGTH_SHORT).show();}}
    private void redownloadItem(HistoryItem item) {
        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            Uri destinationUri = null;
            try {
                Uri sourceUri = Uri.parse(item.filePath);
                String mimeType = getContentResolver().getType(sourceUri);
                if (mimeType == null) {
                    mimeType = getMimeTypeFromFileName(item.fileName);}
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, item.fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                    values.put(MediaStore.MediaColumns.IS_PENDING, 1);}
                destinationUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (destinationUri == null) {
                    throw new Exception("Could not create download entry");}
                InputStream input = getContentResolver().openInputStream(sourceUri);
                OutputStream output = getContentResolver().openOutputStream(destinationUri);
                if (input == null || output == null) {
                    throw new Exception("Could not open file streams");}
                byte[] buffer = new byte[8192];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);}
                output.flush();
                output.close();
                input.close();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues completed = new ContentValues();
                    completed.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    getContentResolver().update(
                            destinationUri,
                            completed,
                            null,
                            null
                    );
                }

                Uri finalDestinationUri =
                        destinationUri;

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Saved to Downloads",
                            Toast.LENGTH_LONG
                    ).show();

                    new HistoryRepository(this)
                            .addDownload(
                                    item.fileName,
                                    finalDestinationUri.toString(),
                                    item.fileType
                            );

                    loadHistory();
                });

            } catch (Exception e) {

                if (destinationUri != null) {

                    try {

                        getContentResolver().delete(
                                destinationUri,
                                null,
                                null
                        );

                    } catch (Exception ignored) {
                    }
                }

                runOnUiThread(() ->
                        Toast.makeText(
                                this,
                                "Download failed. Original file may have been deleted.",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }

        }).start();
    }

    // =========================================================
    // RENAME
    // =========================================================

    private void showRenameDialog(
            HistoryItem item
    ) {

        String fullName =
                item.fileName;

        int dotIndex =
                fullName.lastIndexOf('.');

        String baseName =
                dotIndex > 0
                        ? fullName.substring(
                        0,
                        dotIndex
                )
                        : fullName;

        String extension =
                dotIndex > 0
                        ? fullName.substring(
                        dotIndex
                )
                        : "";

        int pad =
                (int) (
                        getResources()
                                .getDisplayMetrics()
                                .density * 20
                );

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.HORIZONTAL
        );

        container.setPadding(
                pad,
                pad / 2,
                pad,
                pad / 2
        );

        EditText input =
                new EditText(this);

        input.setText(baseName);

        input.setSelection(
                baseName.length()
        );

        input.setSingleLine(true);

        input.setFilters(
                new InputFilter[]{
                        (source, start, end,
                         dest, dstart, dend) -> {

                            for (int i = start;
                                 i < end;
                                 i++) {

                                char c =
                                        source.charAt(i);

                                if (c == '.' ||
                                        c == '/' ||
                                        c == '\\') {

                                    return "";
                                }
                            }

                            return null;
                        }
                }
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        input.setLayoutParams(
                inputParams
        );

        TextView extLabel =
                new TextView(this);

        extLabel.setText(extension);

        extLabel.setPadding(
                pad / 2,
                0,
                0,
                0
        );

        extLabel.setTextColor(
                getResources().getColor(
                        R.color.text_secondary
                )
        );

        container.addView(input);

        if (!extension.isEmpty()) {

            container.addView(
                    extLabel
            );
        }

        new AlertDialog.Builder(this)

                .setTitle("Rename file")

                .setView(container)

                .setNegativeButton(
                        "Cancel",
                        (dialog, which) ->
                                dialog.dismiss()
                )

                .setPositiveButton(
                        "Rename",
                        (dialog, which) -> {

                            String newBase =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (newBase.isEmpty()) {

                                Toast.makeText(
                                        this,
                                        "Name cannot be empty",
                                        Toast.LENGTH_SHORT
                                ).show();

                                return;
                            }

                            String newFullName =
                                    newBase + extension;

                            repo.renameItem(
                                    item.id,
                                    newFullName,
                                    success ->
                                            runOnUiThread(() -> {

                                                if (success) {

                                                    Toast.makeText(
                                                            this,
                                                            "Renamed",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    loadHistory();

                                                } else {

                                                    Toast.makeText(
                                                            this,
                                                            "Rename failed",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            })
                            );
                        }
                )

                .show();
    }

    // =========================================================
    // DELETE CONFIRMATION
    // =========================================================

    private void showDeleteConfirmation(
            HistoryItem item
    ) {

        new AlertDialog.Builder(this)

                .setTitle(
                        "Delete from history?"
                )

                .setMessage(
                        "\"" + item.fileName +
                                "\" will be removed from your history. " +
                                "The downloaded file itself will stay in your Downloads folder."
                )

                .setNegativeButton(
                        "Cancel",
                        (dialog, which) ->
                                dialog.dismiss()
                )

                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteItem(item)
                )

                .show();
    }

    // =========================================================
    // DELETE
    // =========================================================

    private void deleteItem(
            HistoryItem item
    ) {

        repo.deleteItem(
                item.id,
                success ->
                        runOnUiThread(() -> {

                            if (success) {

                                Toast.makeText(
                                        this,
                                        "Removed from history",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadHistory();

                            } else {

                                Toast.makeText(
                                        this,
                                        "Failed to delete",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        })
        );
    }
}