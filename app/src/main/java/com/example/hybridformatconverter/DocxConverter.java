package com.example.hybridformatconverter;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
public class DocxConverter {
    // DOCX -> TXT (paragraphs + tables)
    public static void docxToTxt(
            File input,
            File output
    ) throws Exception {
        try (
                FileInputStream fis =
                        new FileInputStream(input);

                XWPFDocument document =
                        new XWPFDocument(fis);

                PrintWriter writer =
                        new PrintWriter(
                                new FileOutputStream(output)
                        )
        ) {

            for (IBodyElement element : document.getBodyElements()) {

                if (element instanceof XWPFParagraph) {

                    writer.println(
                            ((XWPFParagraph) element).getText()
                    );

                } else if (element instanceof XWPFTable) {

                    XWPFTable table = (XWPFTable) element;

                    for (XWPFTableRow row : table.getRows()) {

                        List<String> cells = new ArrayList<>();

                        for (XWPFTableCell cell : row.getTableCells()) {
                            cells.add(
                                    cell.getText()
                                            .replace("\n", " ")
                                            .trim()
                            );
                        }

                        writer.println(
                                String.join(" | ", cells)
                        );
                    }

                    writer.println();
                }
            }
        }
    }


    // =========================================================
    // TXT -> DOCX
    // =========================================================

    public static void txtToDocx(
            File input,
            File output
    ) throws Exception {

        XWPFDocument document =
                new XWPFDocument();

        try (
                FileInputStream fis =
                        new FileInputStream(input)
        ) {

            StringBuilder text =
                    new StringBuilder();

            byte[] buffer =
                    new byte[8192];

            int length;

            while (
                    (length = fis.read(buffer))
                            != -1
            ) {

                text.append(
                        new String(
                                buffer,
                                0,
                                length,
                                java.nio.charset.StandardCharsets.UTF_8
                        )
                );
            }

            String[] lines =
                    text.toString().split(
                            "\\r?\\n",
                            -1
                    );

            for (String line : lines) {

                XWPFParagraph paragraph =
                        document.createParagraph();

                paragraph.createRun()
                        .setText(line);
            }
        }

        try (
                FileOutputStream fos =
                        new FileOutputStream(output)
        ) {

            document.write(fos);
        }

        document.close();
    }


    // =========================================================
    // DOCX -> HTML (paragraphs + real <table>)
    // =========================================================

    public static void docxToHtml(
            File input,
            File output
    ) throws Exception {

        try (
                FileInputStream fis =
                        new FileInputStream(input);

                XWPFDocument document =
                        new XWPFDocument(fis);

                PrintWriter writer =
                        new PrintWriter(
                                new FileWriter(output)
                        )
        ) {

            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset=\"UTF-8\">");
            writer.println("<title>Converted Document</title>");
            writer.println(
                    "<style>table{border-collapse:collapse;} "
                            + "td,th{border:1px solid #999;padding:4px;}</style>"
            );
            writer.println("</head>");
            writer.println("<body>");

            for (IBodyElement element : document.getBodyElements()) {

                if (element instanceof XWPFParagraph) {

                    String text =
                            ((XWPFParagraph) element).getText();

                    if (text == null) {
                        text = "";
                    }

                    writer.println(
                            "<p>" + escapeHtml(text) + "</p>"
                    );

                } else if (element instanceof XWPFTable) {

                    writer.println("<table>");

                    for (
                            XWPFTableRow row
                            : ((XWPFTable) element).getRows()
                    ) {

                        writer.println("<tr>");

                        for (XWPFTableCell cell : row.getTableCells()) {

                            writer.println(
                                    "<td>"
                                            + escapeHtml(cell.getText())
                                            + "</td>"
                            );
                        }

                        writer.println("</tr>");
                    }

                    writer.println("</table>");
                }
            }

            writer.println("</body>");
            writer.println("</html>");
        }
    }


    // =========================================================
    // HTML ESCAPING
    // =========================================================

    private static String escapeHtml(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}