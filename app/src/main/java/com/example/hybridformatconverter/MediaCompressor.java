package com.example.hybridformatconverter;

import android.content.Context;
import android.net.Uri;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Session;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MediaCompressor {

    public interface Callback {
        void onSuccess(File outputFile, long originalSize, long newSize);

        void onError(String message);
    }


    /*
     * format -> {extension, FFmpeg arguments}
     *
     * We store the FFmpeg arguments without the input/output paths.
     * The paths are added separately using executeWithArguments().
     */
    private static final Map<String, String[]> AUDIO_CMDS =
            new HashMap<>();

    private static final Map<String, String[]> VIDEO_CMDS =
            new HashMap<>();


    static {

        // ---------------- AUDIO ----------------

        AUDIO_CMDS.put(
                "MP3",
                new String[]{
                        "mp3",
                        "-codec:a",
                        "libmp3lame",
                        "-b:a",
                        "128k"
                }
        );

        AUDIO_CMDS.put(
                "WAV",
                new String[]{
                        "wav",
                        "-ar",
                        "22050",
                        "-ac",
                        "1"
                }
        );

        AUDIO_CMDS.put(
                "AAC/M4A",
                new String[]{
                        "m4a",
                        "-codec:a",
                        "aac",
                        "-b:a",
                        "96k"
                }
        );

        AUDIO_CMDS.put(
                "FLAC",
                new String[]{
                        "flac",
                        "-compression_level",
                        "8"
                }
        );

        AUDIO_CMDS.put(
                "OGG/OPUS",
                new String[]{
                        "ogg",
                        "-codec:a",
                        "libopus",
                        "-b:a",
                        "64k"
                }
        );


        // ---------------- VIDEO ----------------

        AUDIO_CMDS.size();

        VIDEO_CMDS.put(
                "MP4",
                new String[]{
                        "mp4",
                        "-c:v",
                        "mpeg4",
                        "-qscale:v",
                        "6",
                        "-c:a",
                        "aac",
                        "-b:a",
                        "96k"
                }
        );

        VIDEO_CMDS.put(
                "AVI",
                new String[]{
                        "avi", "-c:v", "mpeg4", "-qscale:v", "6", "-c:a", "mp3"
                }
        );

        VIDEO_CMDS.put(
                "MOV",
                new String[]{
                        "mov", "-c:v", "mpeg4", "-qscale:v", "6", "-c:a", "aac", "-b:a", "96k"
                }
        );
        VIDEO_CMDS.put(
                "FLV",
                new String[]{
                        "flv", "-c:v", "flv", "-c:a", "mp3", "-q:v", "6"
                }
        );
    }
    public static void compressAudio(
            Context ctx, Uri inputUri, String format, Callback cb
    ) {

        run(
                ctx, inputUri, format, AUDIO_CMDS, cb
        );
    }
    public static void compressVideo(
            Context ctx, Uri inputUri, String format, Callback cb
    ) {

        run(
                ctx, inputUri, format, VIDEO_CMDS, cb
        );
    }
    private static void run(
            Context ctx,
            Uri inputUri,
            String format,
            Map<String, String[]> cmds,
            Callback cb
    ) {
        if (format == null || format.trim().isEmpty()) {
            cb.onError("Media format was not specified");
            return;
        }
        String key = format.trim().toUpperCase();
        String[] spec = cmds.get(key);
        if (spec == null) {
            cb.onError("Unsupported format: " + format);
            return;
        }
        new Thread(() -> {
            File inputFile = null;
            File outputFile = null;
            try {
                String extension = spec[0];
                /*
                 * IMPORTANT:
                 * The extension comes from our internal map,
                 * not directly from the UI label.
                 */
                inputFile = FileUtils.copyUriToTempFile(
                        ctx, inputUri, "media_in", "." + extension
                );
                if (inputFile == null || !inputFile.exists()) {
                    cb.onError(
                            "Unable to create temporary media file"
                    );
                    return;
                }
                long originalSize = inputFile.length();
                if (originalSize <= 0) {
                    cb.onError(
                            "Input media file is empty or could not be read"
                    );
                    return;
                }
                String displayName =
                        FileUtils.getDisplayName(
                                ctx, inputUri
                        );

                if (displayName == null ||
                        displayName.trim().isEmpty()) {

                    displayName = "compressed_media";
                }
                outputFile = FileUtils.getOutputFile(
                        ctx, displayName, extension
                );
                if (outputFile == null) {
                    cb.onError(
                            "Unable to create output media file"
                    );
                    return;
                }
                /*
                 * Build FFmpeg arguments safely.
                 *
                 * This is much safer than:
                 *
                 * String.format(...)
                 * FFmpegKit.execute(command)
                 */
                String[] args = new String[
                        spec.length + 3
                        ];
                int index = 0;
                args[index++] = "-i";
                args[index++] = inputFile.getAbsolutePath();
                /*
                 * spec[0] is the extension, so start at index 1.
                 */
                for (int i = 1; i < spec.length; i++) {
                    args[index++] = spec[i];
                }
                args[index++] = "-y";
                args[index] = outputFile.getAbsolutePath();
                Session session =
                        FFmpegKit.executeWithArguments(args);
                if (ReturnCode.isSuccess(
                        session.getReturnCode()
                )
                        && outputFile.exists()
                        && outputFile.length() > 0) {
                    /*
                     * If compression actually made the file bigger,
                     * keepSmaller() can keep the original.
                     */
                    FileUtils.keepSmaller(
                            inputFile,
                            outputFile
                    );
                    File finalFile =
                            outputFile.exists()
                                    ? outputFile
                                    : inputFile;
                    cb.onSuccess(
                            finalFile,
                            originalSize,
                            finalFile.length()
                    );
                } else {
                    cb.onError(
                            ImageCompressor
                                    .readableFfmpegError(session)
                    );
                }

            } catch (Exception e) {

                String message = e.getMessage();

                if (message == null ||
                        message.trim().isEmpty()) {

                    message = e.getClass().getSimpleName();
                }
                cb.onError(
                        "Media compression failed: " + message
                );
            }
        }).start();
    }
}