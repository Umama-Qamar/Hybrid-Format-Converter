package com.example.hybridformatconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class MarkdownConverter {

    // =========================================================
    // MD -> HTML
    // =========================================================

    public static void mdToHtml(
            File input,
            File output
    ) throws Exception {

        String markdown = readFile(input);

        String[] lines =
                markdown.split("\\r?\\n");

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("</head>\n");
        html.append("<body>\n");

        for (String line : lines) {

            if (line.startsWith("### ")) {

                html.append("<h3>")
                        .append(escape(
                                line.substring(4)))
                        .append("</h3>\n");

            } else if (line.startsWith("## ")) {

                html.append("<h2>")
                        .append(escape(
                                line.substring(3)))
                        .append("</h2>\n");

            } else if (line.startsWith("# ")) {

                html.append("<h1>")
                        .append(escape(
                                line.substring(2)))
                        .append("</h1>\n");

            } else if (line.trim().isEmpty()) {

                html.append("<br>\n");

            } else {

                String converted =
                        escape(line);

                converted =
                        converted.replaceAll(
                                "\\*\\*(.+?)\\*\\*",
                                "<strong>$1</strong>"
                        );

                converted =
                        converted.replaceAll(
                                "\\*(.+?)\\*",
                                "<em>$1</em>"
                        );

                html.append("<p>")
                        .append(converted)
                        .append("</p>\n");
            }
        }

        html.append("</body>\n");
        html.append("</html>");

        writeFile(
                output,
                html.toString()
        );
    }


    // =========================================================
    // HTML -> MD
    // =========================================================

    public static void htmlToMd(
            File input,
            File output
    ) throws Exception {

        String html =
                readFile(input);

        String markdown = html
                .replaceAll(
                        "(?i)<h1[^>]*>(.*?)</h1>",
                        "# $1\n"
                )
                .replaceAll(
                        "(?i)<h2[^>]*>(.*?)</h2>",
                        "## $1\n"
                )
                .replaceAll(
                        "(?i)<h3[^>]*>(.*?)</h3>",
                        "### $1\n"
                )
                .replaceAll(
                        "(?i)<strong>(.*?)</strong>",
                        "**$1**"
                )
                .replaceAll(
                        "(?i)<em>(.*?)</em>",
                        "*$1*"
                )
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

        markdown =
                markdown.replace(
                        "&nbsp;",
                        " "
                );

        markdown =
                markdown.replace(
                        "&amp;",
                        "&"
                );

        markdown =
                markdown.replace(
                        "&lt;",
                        "<"
                );

        markdown =
                markdown.replace(
                        "&gt;",
                        ">"
                );

        writeFile(
                output,
                markdown.trim()
        );
    }


    private static String readFile(
            File file
    ) throws Exception {

        try (FileInputStream fis =
                     new FileInputStream(file)) {

            byte[] data =
                    new byte[(int) file.length()];

            int length =
                    fis.read(data);

            return new String(
                    data,
                    0,
                    length,
                    StandardCharsets.UTF_8
            );
        }
    }


    private static void writeFile(
            File file,
            String text
    ) throws Exception {

        try (FileOutputStream fos =
                     new FileOutputStream(file)) {

            fos.write(
                    text.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }
    }


    private static String escape(
            String text) {

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}