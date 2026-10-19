package com.example.hybridformatconverter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ConvertAPIConverter {

    private static final String API_URL =
            "https://v2.convertapi.com/convert";

    /*
     * IMPORTANT:
     * Replace this with your ConvertAPI token.
     *
     * Do NOT send your real token to anyone.
     */
    private static final String API_TOKEN = "3RWP0kzsVaFP3eFF43vwxJFjXDQ9Prij";
    public static void convert(
            File input,
            File output,
            String from,
            String to
    ) throws Exception {
        if (input == null || !input.exists()) {
            throw new Exception("Input file does not exist.");
        }
        if (!input.isFile()) {
            throw new Exception("Input is not a valid file.");
        }

        if (output == null) {
            throw new Exception("Output file is invalid.");
        }

        if (API_TOKEN.equals("YOUR_CONVERT_API_TOKEN")
                || API_TOKEN.trim().isEmpty()) {

            throw new Exception(
                    "ConvertAPI token has not been configured."
            );
        }

        if (from == null || to == null) {
            throw new Exception(
                    "Conversion format is missing."
            );
        }

        from = from.trim().toLowerCase();
        to = to.trim().toLowerCase();

        String urlString =
                API_URL
                        + "/"
                        + URLEncoder.encode(from, "UTF-8")
                        + "/to/"
                        + URLEncoder.encode(to, "UTF-8")
                        + "?download=attachment";

        HttpURLConnection connection = null;

        try {

            URL url = new URL(urlString);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");

            connection.setDoOutput(true);

            connection.setConnectTimeout(30000);

            connection.setReadTimeout(180000);

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + API_TOKEN
            );

            connection.setRequestProperty(
                    "Accept",
                    "*/*"
            );

            String boundary =
                    "----ConvertAPIBoundary"
                            + System.currentTimeMillis();

            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary="
                            + boundary
            );

            uploadMultipartFile(
                    connection,
                    boundary,
                    input
            );

            int responseCode =
                    connection.getResponseCode();

            if (responseCode < 200
                    || responseCode >= 300) {

                String error =
                        readStream(
                                connection.getErrorStream()
                        );

                throw new Exception(
                        "ConvertAPI error "
                                + responseCode
                                + ": "
                                + error
                );
            }

            saveResponseToFile(
                    connection.getInputStream(),
                    output
            );

            if (!output.exists()
                    || output.length() == 0) {

                throw new Exception(
                        "ConvertAPI returned an empty file."
                );
            }

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    private static void uploadMultipartFile(
            HttpURLConnection connection,
            String boundary,
            File file
    ) throws Exception {

        DataOutputStream output =
                new DataOutputStream(
                        new BufferedOutputStream(
                                connection.getOutputStream()
                        )
                );

        /*
         * File parameter
         */
        output.writeBytes(
                "--"
                        + boundary
                        + "\r\n"
        );

        output.writeBytes(
                "Content-Disposition: form-data; "
                        + "name=\"File\"; filename=\""
                        + file.getName()
                        + "\"\r\n"
        );

        output.writeBytes(
                "Content-Type: application/octet-stream\r\n"
        );

        output.writeBytes("\r\n");

        try (FileInputStream input =
                     new FileInputStream(file)) {

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    input.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        length
                );
            }
        }

        output.writeBytes("\r\n");

        output.writeBytes(
                "--"
                        + boundary
                        + "--\r\n"
        );

        output.flush();
        output.close();
    }


    private static void saveResponseToFile(
            InputStream input,
            File output
    ) throws Exception {

        if (input == null) {
            throw new Exception(
                    "ConvertAPI returned no file."
            );
        }

        File parent =
                output.getParentFile();

        if (parent != null
                && !parent.exists()) {

            if (!parent.mkdirs()
                    && !parent.exists()) {

                throw new Exception(
                        "Could not create output directory."
                );
            }
        }

        try (
                BufferedInputStream bufferedInput =
                        new BufferedInputStream(input);

                FileOutputStream fileOutput =
                        new FileOutputStream(output);

                BufferedOutputStream bufferedOutput =
                        new BufferedOutputStream(
                                fileOutput
                        )
        ) {

            byte[] buffer =
                    new byte[8192];

            int length;

            while ((length =
                    bufferedInput.read(buffer)) != -1) {

                bufferedOutput.write(
                        buffer,
                        0,
                        length
                );
            }

            bufferedOutput.flush();
        }
    }


    private static String readStream(
            InputStream stream
    ) throws Exception {

        if (stream == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        byte[] buffer =
                new byte[8192];

        int length;

        try (InputStream input = stream) {

            while ((length =
                    input.read(buffer)) != -1) {

                result.append(
                        new String(
                                buffer,
                                0,
                                length,
                                "UTF-8"
                        )
                );
            }
        }

        return result.toString();
    }
}