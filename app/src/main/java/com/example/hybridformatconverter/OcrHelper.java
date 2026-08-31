package com.example.hybridformatconverter;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class OcrHelper {
    private static final String API_KEY = "K87420498188957";
    private static final String API_URL =
            "https://api.ocr.space/parse/image";
    public interface OcrCallback {
        void onSuccess(String extractedText);
        void onFailure(String errorMessage);
    }
    public static void extractTextFromImage(
            Context context,
            Uri imageUri,
            String mimeType,
            OcrCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                InputStream inputStream =
                        context.getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    handler.post(() ->
                            callback.onFailure("Unable to read image."));
                    return;
                }
                byte[] imageBytes = readBytes(inputStream);
                inputStream.close();
                String extension = getExtension(mimeType);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
                RequestBody requestBody =
                        new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("apikey", API_KEY)
                                .addFormDataPart("language", "eng")
                                .addFormDataPart("isOverlayRequired", "false")
                                .addFormDataPart("OCREngine", "2")
                                .addFormDataPart("detectOrientation", "true")
                                .addFormDataPart(
                                        "file",
                                        "image." + extension,
                                        RequestBody.create(
                                                imageBytes,
                                                MediaType.parse(mimeType)
                                        )
                                )
                                .build();
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(requestBody)
                        .build();
                Response response = client.newCall(request).execute();
                System.out.println("HTTP Code = " + response.code());
                System.out.println("HTTP Message = " + response.message());
                if (!response.isSuccessful()) {
                    handler.post(() ->
                            callback.onFailure(
                                    "Server Error (" +
                                            response.code() +
                                            ")"));
                    return;
                }
                if (response.body() == null) {
                    handler.post(() ->
                            callback.onFailure(
                                    "Empty server response"));
                    return;
                }
                String responseBody = response.body().string();
                System.out.println(responseBody);
                JSONObject json = new JSONObject(responseBody);
                boolean errored = json.optBoolean(
                        "IsErroredOnProcessing",
                        false);
                if (errored) {
                    final String errorMessage;
                    Object error = json.opt("ErrorMessage");
                    if (error instanceof JSONArray) {
                        JSONArray array = (JSONArray) error;
                        errorMessage = array.length() > 0 ? array.getString(0) : "Unable to extract text.";
                    } else if (error instanceof String) {errorMessage = (String) error;
                    } else {
                        errorMessage = "Unable to extract text.";}
                    handler.post(() -> callback.onFailure(errorMessage));
                    return;}
                JSONArray results = json.optJSONArray("ParsedResults");
                if (results == null || results.length() == 0) {
                    handler.post(() ->
                            callback.onFailure("No text detected in the image."));
                    return;}
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    builder.append(object.optString("ParsedText", ""));
                    if (i != results.length() - 1) {
                        builder.append("\n");}}
                String extracted = builder.toString().trim();
                if (extracted.isEmpty()) {
                    handler.post(() -> callback.onFailure("No readable text found."));
                } else {
                    handler.post(() -> callback.onSuccess(extracted));}}
            catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage() == null ? "" : e.getMessage();
                if (message.contains("Failed to connect") || message.contains("timeout")) {
                    handler.post(() -> callback.onFailure("Couldn't connect to the OCR server.\nPlease check your internet connection and try again."));
                } else {
                    handler.post(() -> callback.onFailure("Something went wrong while extracting text."));
                }}});}
    private static byte[] readBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);}
        buffer.flush();
        return buffer.toByteArray();}
    private static String getExtension(
            String mimeType) {if (mimeType == null) return "jpg";
        switch (mimeType) {
            case "image/png": return "png";
            case "image/bmp": return "bmp";
            case "image/gif": return "gif";
            case "image/webp": return "webp";
            default: return "jpg";
        }}}