package com.example.hybridformatconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;

import java.io.File;
import java.io.FileOutputStream;

public class ImageCompressor {

    public interface Callback {
        void onSuccess(File outputFile, long originalSize, long newSize);
        void onError(String message);
    }

    // quality: 0-100. Reasonable default for "compress" is 60-75.
    public static void compress(Context ctx, Uri inputUri, String format, int quality, Callback cb) {
        new Thread(() -> {
            try {
                String displayName = FileUtils.getDisplayName(ctx, inputUri);
                // NOTE: format is a UI label like "JPEG/JPG" — never use it directly as a file
                // extension (the "/" makes Android treat it as a path and file creation fails).
                // Derive the real extension from the actual selected file instead, with a safe
                // fallback per category if the filename somehow has none.
                String realExt = FileUtils.getExtension(displayName);
                if (realExt.isEmpty()) {
                    switch (format.toUpperCase()) {
                        case "JPEG/JPG": realExt = "jpg"; break;
                        case "PNG": realExt = "png"; break;
                        case "WEBP": realExt = "webp"; break;
                        case "GIF": realExt = "gif"; break;
                        default: realExt = "tmp";
                    }
                }
                File inputFile = FileUtils.copyUriToTempFile(ctx, inputUri, "img_in", "." + realExt);
                long originalSize = inputFile.length();

                switch (format.toUpperCase()) {
                    case "JPEG/JPG":
                    case "JPG":
                    case "JPEG": {
                        Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
                        if (bmp == null) { cb.onError("Could not decode image — file may be corrupted"); return; }
                        File out = FileUtils.getOutputFile(ctx, displayName, "jpg");
                        try (FileOutputStream fos = new FileOutputStream(out)) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                        }
                        bmp.recycle();
                        FileUtils.keepSmaller(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    case "PNG": {
                        Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
                        if (bmp == null) { cb.onError("Could not decode image — file may be corrupted"); return; }
                        File out = FileUtils.getOutputFile(ctx, displayName, "png");
                        try (FileOutputStream fos = new FileOutputStream(out)) {
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        }
                        bmp.recycle();
                        FileUtils.keepSmaller(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    case "WEBP": {
                        Bitmap bmp = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
                        if (bmp == null) { cb.onError("Could not decode image — file may be corrupted"); return; }
                        File out = FileUtils.getOutputFile(ctx, displayName, "webp");
                        try (FileOutputStream fos = new FileOutputStream(out)) {
                            Bitmap.CompressFormat webpFormat =
                                    android.os.Build.VERSION.SDK_INT >= 30
                                            ? Bitmap.CompressFormat.WEBP_LOSSY
                                            : Bitmap.CompressFormat.WEBP;
                            bmp.compress(webpFormat, quality, fos);
                        }
                        bmp.recycle();
                        FileUtils.keepSmaller(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    case "GIF": {
                        File out = FileUtils.getOutputFile(ctx, displayName, "gif");
                        String cmd = String.format(
                                "-i %s -vf \"fps=12,scale=iw*0.85:ih*0.85:flags=lanczos,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse\" -y %s",
                                inputFile.getAbsolutePath(), out.getAbsolutePath());
                        Session session = FFmpegKit.execute(cmd);
                        if (ReturnCode.isSuccess(session.getReturnCode())) {
                            FileUtils.keepSmaller(inputFile, out);
                            cb.onSuccess(out, originalSize, out.length());
                        } else {
                            cb.onError(readableFfmpegError(session));
                        }
                        break;
                    }
                    default:
                        cb.onError("Unsupported image format: " + format);
                }
            } catch (Exception e) {
                cb.onError(e.getMessage() != null ? e.getMessage() : "Unknown error while compressing image");
            }
        }).start();
    }

    // session.getFailStackTrace() is frequently null even on real failures — the actual
    // reason lives in the ffmpeg logs, so surface that instead of a bare "null" message.
    static String readableFfmpegError(Session session) {
        String logs = session.getAllLogsAsString();
        if (logs != null && !logs.trim().isEmpty()) {
            String[] lines = logs.trim().split("\n");
            return lines[lines.length - 1]; // last log line usually has the actual failure reason
        }
        return "Compression failed (return code " + session.getReturnCode() + ")";
    }
}