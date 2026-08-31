package com.example.hybridformatconverter;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    // Copies a content:// Uri to a real temp file so native libs (ffmpeg, PDFBox, POI) can read a path.
    public static File copyUriToTempFile(Context ctx, Uri uri, String prefix, String suffix) throws Exception {
        File dir = new File(ctx.getCacheDir(), "compress_tmp");
        if (!dir.exists()) dir.mkdirs();
        File out = File.createTempFile(prefix, suffix, dir);
        try (InputStream in = ctx.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(out)) {
            if (in == null) throw new Exception("Could not open selected file");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) os.write(buf, 0, n);
        }
        return out;
    }

    public static String getDisplayName(Context ctx, Uri uri) {
        String name = "file";
        ContentResolver resolver = ctx.getContentResolver();
        try (android.database.Cursor cursor = resolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx != -1) name = cursor.getString(idx);
            }
        }
        return name;
    }

    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    public static File getOutputFile(Context ctx, String baseName, String extension) {
        File dir = new File(ctx.getExternalFilesDir(null), "Compressed");
        if (!dir.exists()) dir.mkdirs();
        String clean = baseName.contains(".") ? baseName.substring(0, baseName.lastIndexOf('.')) : baseName;
        return new File(dir, clean + "_compressed." + extension);
    }

    public static String humanReadableSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static void copyFile(File src, File dst) throws Exception {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
    }

    // Some inputs are already efficiently encoded — re-encoding can produce a LARGER file.
    // Rather than shipping a bigger "compressed" file, fall back to the original bytes.
    // Returns true if the fallback was used (i.e. original was kept because it was smaller/equal).
    public static boolean keepSmaller(File original, File candidateOutput) throws Exception {
        if (candidateOutput.length() >= original.length()) {
            copyFile(original, candidateOutput);
            return true;
        }
        return false;
    }
}