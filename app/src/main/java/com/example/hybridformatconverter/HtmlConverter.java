package com.example.hybridformatconverter;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class HtmlConverter {

    // =========================================================
    // TXT -> HTML
    // =========================================================

    public static void txtToHtml(
            File input,
            File output
    ) throws Exception {

        StringBuilder text = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(input,
                                     StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                text.append(escapeHtml(line))
                        .append("<br>\n");
            }
        }

        try (PrintWriter writer =
                     new PrintWriter(
                             new FileWriter(
                                     output,
                                     StandardCharsets.UTF_8))) {

            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<title>Converted Document</title>");
            writer.println("</head>");
            writer.println("<body>");
            writer.print(text);
            writer.println("</body>");
            writer.println("</html>");
        }
    }


    // =========================================================
    // HTML -> TXT
    // =========================================================

    public static void htmlToTxt(
            File input,
            File output
    ) throws Exception {

        String html;

        try (FileInputStream fis =
                     new FileInputStream(input)) {

            byte[] data = new byte[(int) input.length()];
            int length = fis.read(data);

            html = new String(
                    data,
                    0,
                    length,
                    StandardCharsets.UTF_8
            );
        }

        String text = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</h[1-6]>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        try (PrintWriter writer =
                     new PrintWriter(
                             new FileWriter(
                                     output,
                                     StandardCharsets.UTF_8))) {

            writer.print(text.trim());
        }
    }


    // =========================================================
    // HTML -> DOCX
    // =========================================================

    public static void htmlToDocx(
            File input,
            File output
    ) throws Exception {

        String html;

        try (FileInputStream fis =
                     new FileInputStream(input)) {

            byte[] data = new byte[(int) input.length()];
            int length = fis.read(data);

            html = new String(
                    data,
                    0,
                    length,
                    StandardCharsets.UTF_8
            );
        }

        String text = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)</div>", "\n")
                .replaceAll("(?i)</h[1-6]>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        XWPFDocument document =
                new XWPFDocument();

        String[] lines =
                text.split("\\r?\\n");

        for (String line : lines) {

            document.createParagraph()
                    .createRun()
                    .setText(line);
        }

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            document.write(fos);
        }

        document.close();
    }


    // =========================================================
    // HTML ESCAPE
    // =========================================================

    private static String escapeHtml(
            String text) {

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}