package com.example.hybridformatconverter;

import android.content.Context;
import android.net.Uri;

public class CompressionRouter {

    public interface Callback {
        void onSuccess(java.io.File outputFile, long originalSize, long newSize);
        void onError(String message);
    }

    public static void compress(Context ctx, String category, String format, Uri inputUri, Callback cb) {
        switch (category) {
            case "Images":
                ImageCompressor.compress(ctx, inputUri, format, 70, new ImageCompressor.Callback() {
                    public void onSuccess(java.io.File f, long o, long n) { cb.onSuccess(f, o, n); }
                    public void onError(String m) { cb.onError(m); }
                });
                break;
            case "Documents":
                DocumentCompressor.compress(ctx, inputUri, format, new DocumentCompressor.Callback() {
                    public void onSuccess(java.io.File f, long o, long n) { cb.onSuccess(f, o, n); }
                    public void onError(String m) { cb.onError(m); }
                });
                break;
            case "Audio":
                MediaCompressor.compressAudio(ctx, inputUri, format, new MediaCompressor.Callback() {
                    public void onSuccess(java.io.File f, long o, long n) { cb.onSuccess(f, o, n); }
                    public void onError(String m) { cb.onError(m); }
                });
                break;
            case "Video":
                MediaCompressor.compressVideo(ctx, inputUri, format, new MediaCompressor.Callback() {
                    public void onSuccess(java.io.File f, long o, long n) { cb.onSuccess(f, o, n); }
                    public void onError(String m) { cb.onError(m); }
                });
                break;
            default:
                cb.onError("Unknown category: " + category);
        }
    }
}