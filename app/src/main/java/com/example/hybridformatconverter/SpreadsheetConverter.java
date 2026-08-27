package com.example.hybridformatconverter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SpreadsheetConverter {

    // =========================================================
    // XLSX -> CSV
    // =========================================================

    public static void xlsxToCsv(
            File input,
            File output
    ) throws Exception {

        try (
                FileInputStream fis =
                        new FileInputStream(input);

                Workbook workbook =
                        new XSSFWorkbook(fis);

                PrintWriter writer =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(output),
                                        StandardCharsets.UTF_8))
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            for (Row row : sheet) {

                List<String> values =
                        new ArrayList<>();

                for (int i = 0;
                     i < row.getLastCellNum();
                     i++) {

                    Cell cell =
                            row.getCell(i);

                    String value =
                            cell == null
                                    ? ""
                                    : formatter.formatCellValue(cell);

                    values.add(
                            csvEscape(value)
                    );
                }

                writer.println(
                        String.join(",", values)
                );
            }
        }
    }


    // =========================================================
    // CSV -> XLSX
    // =========================================================

    public static void csvToXlsx(
            File input,
            File output
    ) throws Exception {

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Sheet1");

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new FileInputStream(input),
                                        StandardCharsets.UTF_8))
        ) {

            String line;
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {

                Row row =
                        sheet.createRow(rowNumber++);

                String[] values =
                        parseCsvLine(line);

                for (int i = 0;
                     i < values.length;
                     i++) {

                    row.createCell(i)
                            .setCellValue(values[i]);
                }
            }
        }

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            workbook.write(fos);
        }

        workbook.close();
    }


    // =========================================================
    // XLSX -> JSON
    // =========================================================

    public static void xlsxToJson(
            File input,
            File output
    ) throws Exception {

        try (
                FileInputStream fis =
                        new FileInputStream(input);

                Workbook workbook =
                        new XSSFWorkbook(fis);

                PrintWriter writer =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(output),
                                        StandardCharsets.UTF_8))
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            Row header =
                    sheet.getRow(
                            sheet.getFirstRowNum()
                    );

            writer.println("[");

            boolean firstObject = true;

            for (int r = sheet.getFirstRowNum() + 1;
                 r <= sheet.getLastRowNum();
                 r++) {

                Row row =
                        sheet.getRow(r);

                if (row == null) {
                    continue;
                }

                if (!firstObject) {
                    writer.println(",");
                }

                writer.print("  {");

                for (int c = 0;
                     c < header.getLastCellNum();
                     c++) {

                    if (c > 0) {
                        writer.print(",");
                    }

                    String key =
                            formatter.formatCellValue(
                                    header.getCell(c)
                            );

                    String value =
                            formatter.formatCellValue(
                                    row.getCell(c)
                            );

                    writer.print(
                            "\"" + jsonEscape(key)
                                    + "\":\""
                                    + jsonEscape(value)
                                    + "\""
                    );
                }

                writer.print("}");

                firstObject = false;
            }

            writer.println();
            writer.println("]");
        }
    }


    // =========================================================
    // JSON -> XLSX
    // =========================================================

    public static void jsonToXlsx(
            File input,
            File output
    ) throws Exception {

        String json =
                readFile(input).trim();

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Sheet1");

        if (json.length() > 2) {

            json =
                    json.substring(
                            1,
                            json.length() - 1
                    ).trim();

            String[] objects =
                    json.split(
                            "\\},\\s*\\{"
                    );

            if (objects.length > 0) {

                objects[0] =
                        objects[0]
                                .replaceFirst("^\\{", "");

                objects[objects.length - 1] =
                        objects[objects.length - 1]
                                .replaceFirst("\\}$", "");

                Row header =
                        sheet.createRow(0);

                List<String> keys =
                        new ArrayList<>();

                String first =
                        objects[0];

                String[] pairs =
                        first.split(
                                ",(?=\\s*\\\")"
                        );

                for (int i = 0;
                     i < pairs.length;
                     i++) {

                    String[] kv =
                            pairs[i].split(
                                    ":",
                                    2
                            );

                    if (kv.length == 2) {

                        String key =
                                cleanJsonString(
                                        kv[0]
                                );

                        keys.add(key);

                        header.createCell(i)
                                .setCellValue(key);
                    }
                }

                for (int r = 0;
                     r < objects.length;
                     r++) {

                    Row row =
                            sheet.createRow(r + 1);

                    String[] values =
                            objects[r].split(
                                    ",(?=\\s*\\\")"
                            );

                    for (int c = 0;
                         c < values.length;
                         c++) {

                        String[] kv =
                                values[c].split(
                                        ":",
                                        2
                                );

                        if (kv.length == 2) {

                            row.createCell(c)
                                    .setCellValue(
                                            cleanJsonString(
                                                    kv[1]
                                            )
                                    );
                        }
                    }
                }
            }
        }

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            workbook.write(fos);
        }

        workbook.close();
    }


    // =========================================================
    // XLSX -> XML
    // =========================================================

    public static void xlsxToXml(
            File input,
            File output
    ) throws Exception {

        try (
                FileInputStream fis =
                        new FileInputStream(input);

                Workbook workbook =
                        new XSSFWorkbook(fis);

                PrintWriter writer =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(output),
                                        StandardCharsets.UTF_8))
        ) {

            Sheet sheet =
                    workbook.getSheetAt(0);

            DataFormatter formatter =
                    new DataFormatter();

            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<rows>");

            for (Row row : sheet) {

                writer.println("  <row>");

                for (Cell cell : row) {

                    String value =
                            formatter.formatCellValue(cell);

                    writer.println(
                            "    <cell>"
                                    + xmlEscape(value)
                                    + "</cell>"
                    );
                }

                writer.println("  </row>");
            }

            writer.println("</rows>");
        }
    }


    // =========================================================
    // XML -> XLSX
    // =========================================================

    public static void xmlToXlsx(
            File input,
            File output
    ) throws Exception {

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Sheet1");

        BufferedReader reader =
                new BufferedReader(
                        new FileReader(input)
                );

        String line;
        int rowNumber = 0;

        while ((line = reader.readLine()) != null) {

            if (!line.contains("<row>")) {
                continue;
            }

            Row row =
                    sheet.createRow(rowNumber++);

            while ((line = reader.readLine()) != null
                    && !line.contains("</row>")) {

                if (line.contains("<cell>")) {

                    String value =
                            line
                                    .replace("<cell>", "")
                                    .replace("</cell>", "")
                                    .trim();

                    row.createCell(
                            row.getLastCellNum() < 0
                                    ? 0
                                    : row.getLastCellNum()
                    ).setCellValue(value);
                }
            }
        }

        reader.close();

        try (FileOutputStream fos =
                     new FileOutputStream(output)) {

            workbook.write(fos);
        }

        workbook.close();
    }


    // =========================================================
    // CSV -> JSON
    // =========================================================

    public static void csvToJson(
            File input,
            File output
    ) throws Exception {

        List<String> lines =
                readLines(input);

        if (lines.isEmpty()) {
            writeFile(output, "[]");
            return;
        }

        String[] headers =
                parseCsvLine(lines.get(0));

        StringBuilder json =
                new StringBuilder("[\n");

        for (int i = 1; i < lines.size(); i++) {

            String[] values =
                    parseCsvLine(lines.get(i));

            if (i > 1) {
                json.append(",\n");
            }

            json.append("  {");

            for (int j = 0;
                 j < headers.length;
                 j++) {

                if (j > 0) {
                    json.append(",");
                }

                String value =
                        j < values.length
                                ? values[j]
                                : "";

                json.append("\"")
                        .append(
                                jsonEscape(headers[j])
                        )
                        .append("\":\"")
                        .append(
                                jsonEscape(value)
                        )
                        .append("\"");
            }

            json.append("}");
        }

        json.append("\n]");

        writeFile(
                output,
                json.toString()
        );
    }


    // =========================================================
    // JSON -> CSV
    // =========================================================

    public static void jsonToCsv(
            File input,
            File output
    ) throws Exception {

        String json =
                readFile(input).trim();

        if (json.length() < 2) {
            writeFile(output, "");
            return;
        }

        json =
                json.substring(
                        1,
                        json.length() - 1
                ).trim();

        String[] objects =
                json.split(
                        "\\},\\s*\\{"
                );

        if (objects.length == 0) {
            writeFile(output, "");
            return;
        }

        objects[0] =
                objects[0]
                        .replaceFirst("^\\{", "");

        objects[objects.length - 1] =
                objects[objects.length - 1]
                        .replaceFirst("\\}$", "");

        String[] firstPairs =
                objects[0].split(
                        ",(?=\\s*\\\")"
                );

        StringBuilder csv =
                new StringBuilder();

        List<String> headers =
                new ArrayList<>();

        for (String pair : firstPairs) {

            String[] kv =
                    pair.split(":", 2);

            if (kv.length == 2) {

                headers.add(
                        cleanJsonString(kv[0])
                );
            }
        }

        for (int i = 0;
             i < headers.size();
             i++) {

            if (i > 0) {
                csv.append(",");
            }

            csv.append(
                    csvEscape(headers.get(i))
            );
        }

        csv.append("\n");

        for (int r = 0;
             r < objects.length;
             r++) {

            String[] pairs =
                    objects[r].split(
                            ",(?=\\s*\\\")"
                    );

            for (int c = 0;
                 c < headers.size();
                 c++) {

                if (c > 0) {
                    csv.append(",");
                }

                String value = "";

                if (c < pairs.length) {

                    String[] kv =
                            pairs[c].split(
                                    ":",
                                    2
                            );

                    if (kv.length == 2) {
                        value =
                                cleanJsonString(
                                        kv[1]
                                );
                    }
                }

                csv.append(
                        csvEscape(value)
                );
            }

            csv.append("\n");
        }

        writeFile(
                output,
                csv.toString()
        );
    }


    // =========================================================
    // CSV -> XML
    // =========================================================

    public static void csvToXml(
            File input,
            File output
    ) throws Exception {

        List<String> lines =
                readLines(input);

        StringBuilder xml =
                new StringBuilder();

        xml.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        );

        xml.append("<rows>\n");

        for (String line : lines) {

            xml.append("  <row>\n");

            String[] values =
                    parseCsvLine(line);

            for (String value : values) {

                xml.append("    <cell>")
                        .append(xmlEscape(value))
                        .append("</cell>\n");
            }

            xml.append("  </row>\n");
        }

        xml.append("</rows>");

        writeFile(
                output,
                xml.toString()
        );
    }


    // =========================================================
    // XML -> CSV
    // =========================================================

    public static void xmlToCsv(
            File input,
            File output
    ) throws Exception {

        BufferedReader reader =
                new BufferedReader(
                        new FileReader(input)
                );

        StringBuilder csv =
                new StringBuilder();

        String line;
        boolean firstCell;

        while ((line = reader.readLine()) != null) {

            if (line.contains("<row>")) {

                firstCell = true;

                while ((line = reader.readLine()) != null
                        && !line.contains("</row>")) {

                    if (line.contains("<cell>")) {

                        String value =
                                line
                                        .replace(
                                                "<cell>",
                                                ""
                                        )
                                        .replace(
                                                "</cell>",
                                                ""
                                        )
                                        .trim();

                        if (!firstCell) {
                            csv.append(",");
                        }

                        csv.append(
                                csvEscape(value)
                        );

                        firstCell = false;
                    }
                }

                csv.append("\n");
            }
        }

        reader.close();

        writeFile(
                output,
                csv.toString()
        );
    }


    private static String[] parseCsvLine(
            String line) {

        List<String> result =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char ch = line.charAt(i);

            if (ch == '"') {

                if (quoted
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {

                    quoted = !quoted;
                }

            } else if (ch == ',' && !quoted) {

                result.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(ch);
            }
        }

        result.add(
                current.toString()
        );

        return result.toArray(
                new String[0]
        );
    }


    private static String csvEscape(
            String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")) {

            return "\""
                    + value.replace("\"", "\"\"")
                    + "\"";
        }

        return value;
    }


    private static String jsonEscape(
            String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }


    private static String cleanJsonString(
            String value) {

        value = value.trim();

        if (value.startsWith("\"")
                && value.endsWith("\"")) {

            value =
                    value.substring(
                            1,
                            value.length() - 1
                    );
        }

        return value
                .replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\\", "\\");
    }


    private static String xmlEscape(
            String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
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


    private static List<String> readLines(
            File file
    ) throws Exception {

        List<String> lines =
                new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     new FileInputStream(file),
                                     StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
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
}