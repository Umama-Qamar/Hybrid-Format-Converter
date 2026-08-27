package com.example.hybridformatconverter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
public class PdfConverter {
    private static boolean initialized = false;
    /**
     * Initialize PDFBox resources.
     *
     * Call this once before using any PDFBox operation.
     */
    public static synchronized void initialize(Context context) {
        if (initialized) {return;}
        if (context == null) {throw new IllegalArgumentException("Context cannot be null.");}
        PDFBoxResourceLoader.init(context.getApplicationContext());
        initialized = true;}
    public static void pdfToTxt(File input, File output) throws Exception {
        validateInput(input);
        validateOutput(output);
        try (PDDocument document = PDDocument.load(input);
             PrintWriter writer = new PrintWriter(output)
        ) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            writer.print(normalizeWhitespace(text));
        }}
    public static void pdfToImage(File input, File output, boolean png
    ) throws Exception {
        validateInput(input);
        validateOutput(output);
        ParcelFileDescriptor descriptor = null;
        PdfRenderer renderer = null;
        PdfRenderer.Page page = null;
        Bitmap bitmap = null;
        try {
            descriptor = ParcelFileDescriptor.open(
                    input,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(descriptor);
            if (renderer.getPageCount() == 0) {
                throw new Exception("PDF contains no pages."
                );}
            page = renderer.openPage(0);
            int width = page.getWidth() * 2;
            int height = page.getHeight() * 2;
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.WHITE);
            page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            try (FileOutputStream fos = new FileOutputStream(output)
            ) {
                boolean success = bitmap.compress(png
                                ? Bitmap.CompressFormat.PNG
                                : Bitmap.CompressFormat.JPEG,
                        95,
                        fos);
                if (!success) {
                    throw new Exception("Could not create image from PDF.");
                }}
        } finally {
            if (bitmap != null
                    && !bitmap.isRecycled()) {

                bitmap.recycle();
            }

            if (page != null) {
                page.close();
            }

            if (renderer != null) {
                renderer.close();
            }

            if (descriptor != null) {
                descriptor.close();
            }
        }
    }


    // =========================================================
    // EXTRACT TEXT FROM PDF
    // =========================================================

    public static String extractText(
            File input
    ) throws Exception {

        validateInput(input);

        try (
                PDDocument document =
                        PDDocument.load(input)
        ) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            String text =
                    stripper.getText(document);

            return normalizeWhitespace(text);
        }
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private static void validateInput(
            File input
    ) throws Exception {

        if (input == null) {

            throw new Exception(
                    "Input file is null."
            );
        }

        if (!input.exists()) {

            throw new Exception(
                    "Input PDF does not exist."
            );
        }

        if (!input.isFile()) {

            throw new Exception(
                    "Input PDF is not a valid file."
            );
        }
    }


    private static void validateOutput(
            File output
    ) throws Exception {

        if (output == null) {

            throw new Exception(
                    "Output file is invalid."
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
    }


    // =========================================================
    // NORMALIZE WHITESPACE
    // =========================================================

    public static String normalizeWhitespace(
            String text
    ) {

        if (text == null) {
            return "";
        }

        String collapsed =
                text.replaceAll(
                        "[ \\t]{2,}",
                        " "
                );

        collapsed =
                collapsed.replaceAll(
                        "(?m)[ \\t]+$",
                        ""
                );

        return collapsed;
    }
}