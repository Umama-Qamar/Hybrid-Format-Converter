package com.example.hybridformatconverter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EpubConverter {

    public static void epubToHtml(
            File input,
            File output
    ) throws Exception {

        StringBuilder html =
                new StringBuilder();

        try (ZipFile zip =
                     new ZipFile(input)) {

            zip.stream()
                    .filter(entry ->
                            !entry.isDirectory()
                                    && (
                                    entry.getName()
                                            .toLowerCase()
                                            .endsWith(".html")
                                            ||
                                            entry.getName()
                                                    .toLowerCase()
                                                    .endsWith(".xhtml")
                            ))
                    .forEach(entry -> {

                        try (InputStream in =
                                     zip.getInputStream(entry)) {

                            byte[] data =
                                    readAll(in);

                            html.append(
                                    new String(
                                            data,
                                            StandardCharsets.UTF_8
                                    )
                            );

                            html.append("\n");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            fos.write(
                    html.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );
        }
    }


    public static void epubToTxt(
            File input,
            File output
    ) throws Exception {

        StringBuilder text =
                new StringBuilder();

        try (ZipFile zip =
                     new ZipFile(input)) {

            zip.stream()
                    .filter(entry ->
                            !entry.isDirectory()
                                    && (
                                    entry.getName()
                                            .toLowerCase()
                                            .endsWith(".html")
                                            ||
                                            entry.getName()
                                                    .toLowerCase()
                                                    .endsWith(".xhtml")
                            ))
                    .forEach(entry -> {

                        try (InputStream in =
                                     zip.getInputStream(entry)) {

                            String html =
                                    new String(
                                            readAll(in),
                                            StandardCharsets.UTF_8
                                    );

                            String clean =
                                    html
                                            .replaceAll(
                                                    "(?i)<br\\s*/?>",
                                                    "\n"
                                            )
                                            .replaceAll(
                                                    "(?i)</p>",
                                                    "\n"
                                            )
                                            .replaceAll(
                                                    "<[^>]*>",
                                                    ""
                                            );

                            text.append(
                                    clean
                            ).append("\n");

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            fos.write(
                    text.toString()
                            .getBytes(
                                    StandardCharsets.UTF_8
                            )
            );
        }
    }


    private static byte[] readAll(
            InputStream input
    ) throws Exception {

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

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

        return output.toByteArray();
    }
}