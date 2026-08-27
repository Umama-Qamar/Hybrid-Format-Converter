package com.example.hybridformatconverter;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.graphics.Paint;
import android.graphics.Canvas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;

public class HtmlToPdfConverter {

    // =========================================================
    // TXT -> PDF
    // =========================================================

    public static void convertTextFileToPdf(
            Context context,
            File input,
            File output
    ) throws Exception {

        StringBuilder text = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(input))) {

            String line;

            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        }

        createPdf(text.toString(), output);
    }

    // =========================================================
    // HTML -> PDF
    // =========================================================

    public static void convertHtmlFileToPdf(
            Context context,
            File input,
            File output
    ) throws Exception {

        StringBuilder html = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(input))) {

            String line;

            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }
        }

        String text = stripHtml(html.toString());

        createPdf(text, output);
    }

    // =========================================================
    // MARKDOWN -> PDF
    // =========================================================

    public static void convertMarkdownToPdf(
            Context context,
            File input,
            File output
    ) throws Exception {

        StringBuilder markdown = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(input))) {

            String line;

            while ((line = reader.readLine()) != null) {
                markdown.append(line).append("\n");
            }
        }

        String text = markdownToPlainText(
                markdown.toString()
        );

        createPdf(text, output);
    }

    // =========================================================
    // CREATE PDF
    // =========================================================

    private static void createPdf(
            String text,
            File output
    ) throws Exception {

        PdfDocument document =
                new PdfDocument();

        Paint paint =
                new Paint();

        paint.setTextSize(12f);

        paint.setColor(
                android.graphics.Color.BLACK
        );

        int pageWidth = 595;
        int pageHeight = 842;

        int margin = 40;

        int usableWidth =
                pageWidth - (margin * 2);

        int lineHeight = 18;

        String[] lines =
                text.split("\n", -1);

        PdfDocument.Page page = null;
        Canvas canvas = null;

        int y = margin;

        for (String originalLine : lines) {

            String line =
                    originalLine;

            if (line.isEmpty()) {
                line = " ";
            }

            // Wrap long lines
            while (paint.measureText(line) > usableWidth) {

                int breakIndex =
                        findBreakIndex(
                                line,
                                paint,
                                usableWidth
                        );

                String part =
                        line.substring(
                                0,
                                breakIndex
                        );

                if (page == null
                        || y + lineHeight >
                        pageHeight - margin) {

                    if (page != null) {
                        document.finishPage(page);
                    }

                    PdfDocument.PageInfo pageInfo =
                            new PdfDocument.PageInfo.Builder(
                                    pageWidth,
                                    pageHeight,
                                    document.getPages().size() + 1
                            ).create();

                    page =
                            document.startPage(
                                    pageInfo
                            );

                    canvas =
                            page.getCanvas();

                    y = margin;
                }

                canvas.drawText(
                        part,
                        margin,
                        y,
                        paint
                );

                y += lineHeight;

                line =
                        line.substring(
                                breakIndex
                        ).trim();
            }

            if (page == null
                    || y + lineHeight >
                    pageHeight - margin) {

                if (page != null) {
                    document.finishPage(page);
                }

                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(
                                pageWidth,
                                pageHeight,
                                document.getPages().size() + 1
                        ).create();

                page =
                        document.startPage(
                                pageInfo
                        );

                canvas =
                        page.getCanvas();

                y = margin;
            }

            canvas.drawText(
                    line,
                    margin,
                    y,
                    paint
            );

            y += lineHeight;
        }

        if (page != null) {
            document.finishPage(page);
        }

        try (FileOutputStream outputStream =
                     new FileOutputStream(output)) {

            document.writeTo(outputStream);
        }

        document.close();
    }

    // =========================================================
    // FIND TEXT WRAP POSITION
    // =========================================================

    private static int findBreakIndex(
            String text,
            Paint paint,
            int maxWidth
    ) {

        int low = 1;
        int high = text.length();

        int best = 1;

        while (low <= high) {

            int mid =
                    (low + high) / 2;

            String part =
                    text.substring(
                            0,
                            mid
                    );

            if (paint.measureText(part)
                    <= maxWidth) {

                best = mid;
                low = mid + 1;

            } else {

                high = mid - 1;
            }
        }

        int space =
                text.lastIndexOf(
                        ' ',
                        best
                );

        if (space > 0) {
            return space;
        }

        return best;
    }

    // =========================================================
    // REMOVE BASIC HTML
    // =========================================================

    private static String stripHtml(
            String html
    ) {

        String text = html;

        text =
                text.replaceAll(
                        "(?i)<br\\s*/?>",
                        "\n"
                );

        text =
                text.replaceAll(
                        "(?i)</p>",
                        "\n"
                );

        text =
                text.replaceAll(
                        "(?i)</div>",
                        "\n"
                );

        text =
                text.replaceAll(
                        "(?i)</h[1-6]>",
                        "\n"
                );

        text =
                text.replaceAll(
                        "<[^>]*>",
                        ""
                );

        text =
                text.replace(
                        "&nbsp;",
                        " "
                );

        text =
                text.replace(
                        "&amp;",
                        "&"
                );

        text =
                text.replace(
                        "&lt;",
                        "<"
                );

        text =
                text.replace(
                        "&gt;",
                        ">"
                );

        text =
                text.replace(
                        "&quot;",
                        "\""
                );

        text =
                text.replace(
                        "&#39;",
                        "'"
                );

        return text.trim();
    }

    // =========================================================
    // MARKDOWN -> PLAIN TEXT
    // =========================================================

    private static String markdownToPlainText(
            String markdown
    ) {

        String text =
                markdown;

        // Headings
        text =
                text.replaceAll(
                        "(?m)^#{1,6}\\s*",
                        ""
                );

        // Bold
        text =
                text.replaceAll(
                        "\\*\\*(.*?)\\*\\*",
                        "$1"
                );

        text =
                text.replaceAll(
                        "__(.*?)__",
                        "$1"
                );

        // Italic
        text =
                text.replaceAll(
                        "\\*(.*?)\\*",
                        "$1"
                );

        text =
                text.replaceAll(
                        "_(.*?)_",
                        "$1"
                );

        // Inline code
        text =
                text.replaceAll(
                        "`(.*?)`",
                        "$1"
                );

        // Links [text](url)
        text =
                text.replaceAll(
                        "\\[([^\\]]+)\\]\\([^\\)]+\\)",
                        "$1"
                );

        // Horizontal rules
        text =
                text.replaceAll(
                        "(?m)^[-*_]{3,}\\s*$",
                        ""
                );

        return text.trim();
    }
}