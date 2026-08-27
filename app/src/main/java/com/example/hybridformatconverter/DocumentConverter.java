package com.example.hybridformatconverter;

import android.content.Context;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocumentConverter {

    public static void convert(
            Context context,
            File input,
            File output,
            String from,
            String to
    ) throws Exception {

        if (input == null || !input.exists()) {
            throw new Exception("Input file does not exist.");
        }

        if (output == null) {
            throw new Exception("Output file is invalid.");
        }

        if (from == null || to == null) {
            throw new Exception("Conversion format is missing.");
        }

        from = from.trim().toUpperCase();
        to = to.trim().toUpperCase();

        if (from.equals(to)) {
            FileUtils.copyFile(input, output);
            return;
        }

        if (from.equals("DOCX") && to.equals("TXT")) {
            DocxConverter.docxToTxt(input, output);
            return;
        }

        if (from.equals("TXT") && to.equals("DOCX")) {
            DocxConverter.txtToDocx(input, output);
            return;
        }

        if (from.equals("DOCX") && to.equals("HTML")) {
            DocxConverter.docxToHtml(input, output);
            return;
        }

        if (from.equals("DOCX") && to.equals("PDF")) {
            ConvertAPIConverter.convert(
                    input,
                    output,
                    "DOCX",
                    "PDF"
            );
            return;
        }

        if (from.equals("HTML") && to.equals("DOCX")) {
            HtmlConverter.htmlToDocx(input, output);
            return;
        }

        if (from.equals("TXT") && to.equals("HTML")) {
            HtmlConverter.txtToHtml(input, output);
            return;
        }

        if (from.equals("HTML") && to.equals("TXT")) {
            HtmlConverter.htmlToTxt(input, output);
            return;
        }

        if (from.equals("MD") && to.equals("HTML")) {
            MarkdownConverter.mdToHtml(input, output);
            return;
        }

        if (from.equals("HTML") && to.equals("MD")) {
            MarkdownConverter.htmlToMd(input, output);
            return;
        }

        if (from.equals("PDF") && to.equals("TXT")) {
            PdfConverter.pdfToTxt(input, output);
            return;
        }

        if (from.equals("PDF") && to.equals("JPG")) {
            PdfConverter.pdfToImage(
                    input,
                    output,
                    false
            );
            return;
        }

        if (from.equals("PDF") && to.equals("PNG")) {
            PdfConverter.pdfToImage(
                    input,
                    output,
                    true
            );
            return;
        }

        if (from.equals("PDF") && to.equals("DOCX")) {
            ConvertAPIConverter.convert(
                    input,
                    output,
                    "PDF",
                    "DOCX"
            );
            return;
        }

        if (from.equals("XLSX") && to.equals("CSV")) {
            SpreadsheetConverter.xlsxToCsv(input, output);
            return;
        }

        if (from.equals("CSV") && to.equals("XLSX")) {
            SpreadsheetConverter.csvToXlsx(input, output);
            return;
        }

        if (from.equals("XLSX") && to.equals("JSON")) {
            SpreadsheetConverter.xlsxToJson(input, output);
            return;
        }

        if (from.equals("JSON") && to.equals("XLSX")) {
            SpreadsheetConverter.jsonToXlsx(input, output);
            return;
        }

        if (from.equals("XLSX") && to.equals("XML")) {
            SpreadsheetConverter.xlsxToXml(input, output);
            return;
        }

        if (from.equals("XML") && to.equals("XLSX")) {
            SpreadsheetConverter.xmlToXlsx(input, output);
            return;
        }

        if (from.equals("CSV") && to.equals("JSON")) {
            SpreadsheetConverter.csvToJson(input, output);
            return;
        }

        if (from.equals("JSON") && to.equals("CSV")) {
            SpreadsheetConverter.jsonToCsv(input, output);
            return;
        }

        if (from.equals("CSV") && to.equals("XML")) {
            SpreadsheetConverter.csvToXml(input, output);
            return;
        }

        if (from.equals("XML") && to.equals("CSV")) {
            SpreadsheetConverter.xmlToCsv(input, output);
            return;
        }

        if (from.equals("EPUB") && to.equals("HTML")) {
            EpubConverter.epubToHtml(input, output);
            return;
        }

        if (from.equals("EPUB") && to.equals("TXT")) {
            EpubConverter.epubToTxt(input, output);
            return;
        }

        if (from.equals("TXT") && to.equals("PDF")) {
            txtToPdf(input, output);
            return;
        }

        if (from.equals("HTML") && to.equals("PDF")) {
            HtmlToPdfConverter.convertHtmlFileToPdf(
                    context,
                    input,
                    output
            );
            return;
        }

        if (from.equals("MD") && to.equals("PDF")) {
            HtmlToPdfConverter.convertMarkdownToPdf(
                    context,
                    input,
                    output
            );
            return;
        }

        throw new Exception(
                "Conversion "
                        + from
                        + " to "
                        + to
                        + " is not supported."
        );
    }

    private static void txtToPdf(
            File input,
            File output
    ) throws Exception {

        String text = readTextFile(input);
        createPdfFromText(text, output);
    }

    private static void createPdfFromText(
            String text,
            File output
    ) throws Exception {

        if (text == null) {
            text = "";
        }

        try (PDDocument document = new PDDocument()) {

            float margin = 50;
            float fontSize = 11;
            float leading = 16;

            List<String> lines =
                    wrapText(text, 90);

            PDPage page =
                    new PDPage(
                            PDRectangle.LETTER
                    );

            document.addPage(page);

            PDPageContentStream content =
                    new PDPageContentStream(
                            document,
                            page
                    );

            float pageHeight =
                    page.getMediaBox()
                            .getHeight();

            float currentY =
                    pageHeight - margin;

            content.beginText();

            content.setFont(
                    PDType1Font.HELVETICA,
                    fontSize
            );

            content.setLeading(leading);

            content.newLineAtOffset(
                    margin,
                    currentY
            );

            for (String line : lines) {

                if (currentY < margin + leading) {

                    content.endText();
                    content.close();

                    page =
                            new PDPage(
                                    PDRectangle.LETTER
                            );

                    document.addPage(page);

                    content =
                            new PDPageContentStream(
                                    document,
                                    page
                            );

                    currentY =
                            page.getMediaBox()
                                    .getHeight()
                                    - margin;

                    content.beginText();

                    content.setFont(
                            PDType1Font.HELVETICA,
                            fontSize
                    );

                    content.setLeading(leading);

                    content.newLineAtOffset(
                            margin,
                            currentY
                    );
                }

                content.showText(
                        makePdfSafe(line)
                );

                content.newLine();

                currentY -= leading;
            }

            content.endText();
            content.close();

            document.save(output);
        }
    }

    public static void createDocxFromText(
            String text,
            File output
    ) throws Exception {

        if (text == null) {
            text = "";
        }

        try (ZipOutputStream zip =
                     new ZipOutputStream(
                             new FileOutputStream(output)
                     )) {

            addZipEntry(
                    zip,
                    "[Content_Types].xml",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                            + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                            + "</Types>"
            );

            addZipEntry(
                    zip,
                    "_rels/.rels",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                            + "</Relationships>"
            );

            StringBuilder document =
                    new StringBuilder();

            document.append(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            );

            document.append(
                    "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
            );

            document.append("<w:body>");

            String[] paragraphs =
                    text.split("\\r?\\n", -1);

            for (String line : paragraphs) {

                document.append(
                        "<w:p><w:r><w:t xml:space=\"preserve\">"
                );

                document.append(
                        xmlEscape(line)
                );

                document.append(
                        "</w:t></w:r></w:p>"
                );
            }

            document.append(
                    "<w:sectPr>"
                            + "<w:pgSz w:w=\"12240\" w:h=\"15840\"/>"
                            + "<w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/>"
                            + "</w:sectPr>"
            );

            document.append("</w:body>");
            document.append("</w:document>");

            addZipEntry(
                    zip,
                    "word/document.xml",
                    document.toString()
            );
        }
    }

    private static void addZipEntry(
            ZipOutputStream zip,
            String name,
            String content
    ) throws Exception {

        ZipEntry entry =
                new ZipEntry(name);

        zip.putNextEntry(entry);

        byte[] data =
                content.getBytes(
                        StandardCharsets.UTF_8
                );

        zip.write(data);
        zip.closeEntry();
    }

    private static String xmlEscape(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String readTextFile(
            File file
    ) throws Exception {

        try (FileInputStream input =
                     new FileInputStream(file)) {

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

            return output.toString("UTF-8");
        }
    }

    private static String makePdfSafe(
            String text
    ) {

        if (text == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        for (int i = 0;
             i < text.length();
             i++) {

            char c =
                    text.charAt(i);

            if (c >= 32 && c <= 126) {
                result.append(c);
            } else if (c == '\t') {
                result.append("    ");
            } else {
                result.append("?");
            }
        }

        return result.toString();
    }

    private static List<String> wrapText(
            String text,
            int maxCharacters
    ) {

        List<String> result =
                new ArrayList<>();

        if (text == null) {
            result.add("");
            return result;
        }

        String[] paragraphs =
                text.split("\\r?\\n", -1);

        for (String paragraph : paragraphs) {

            if (paragraph.length() == 0) {
                result.add("");
                continue;
            }

            String remaining =
                    paragraph;

            while (remaining.length()
                    > maxCharacters) {

                int cut =
                        remaining.lastIndexOf(
                                " ",
                                maxCharacters
                        );

                if (cut <= 0) {
                    cut = maxCharacters;
                }

                result.add(
                        remaining.substring(
                                0,
                                cut
                        )
                );

                remaining =
                        remaining
                                .substring(cut)
                                .trim();
            }

            result.add(remaining);
        }

        return result;
    }
}