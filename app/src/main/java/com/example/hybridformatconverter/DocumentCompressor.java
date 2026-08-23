package com.example.hybridformatconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.graphics.PDXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.cos.COSName;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class DocumentCompressor {

    public interface Callback {
        void onSuccess(File outputFile, long originalSize, long newSize);
        void onError(String message);
    }

    public static void compress(Context ctx, Uri inputUri, String format, Callback cb) {
        new Thread(() -> {
            try {
                PDFBoxResourceLoader.init(ctx.getApplicationContext());
                String displayName = FileUtils.getDisplayName(ctx, inputUri);

                switch (format.toUpperCase()) {
                    case "PDF": {
                        File inputFile = FileUtils.copyUriToTempFile(ctx, inputUri, "doc_in", ".pdf");
                        long originalSize = inputFile.length();
                        File out = FileUtils.getOutputFile(ctx, displayName, "pdf");
                        compressPdf(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    case "WORD":
                    case "EXCEL":
                    case "PPT": {
                        String ext = format.equalsIgnoreCase("WORD") ? "docx"
                                : format.equalsIgnoreCase("EXCEL") ? "xlsx" : "pptx";
                        File inputFile = FileUtils.copyUriToTempFile(ctx, inputUri, "doc_in", "." + ext);
                        long originalSize = inputFile.length();
                        File out = FileUtils.getOutputFile(ctx, displayName, ext);
                        compressOfficeDocument(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    case "TXT":
                    case "CSV": {
                        String ext = format.equalsIgnoreCase("TXT") ? "txt" : "csv";
                        File inputFile = FileUtils.copyUriToTempFile(ctx, inputUri, "doc_in", "." + ext);
                        long originalSize = inputFile.length();
                        File out = FileUtils.getOutputFile(ctx, displayName, ext + ".gz");
                        gzipFile(inputFile, out);
                        cb.onSuccess(out, originalSize, out.length());
                        break;
                    }
                    default:
                        cb.onError("Unsupported document format: " + format);
                }
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    // Downsamples/recompresses embedded images in a PDF's XObjects — the actual source of size.
    private static void compressPdf(File input, File output) throws Exception {
        try (PDDocument doc = PDDocument.load(input)) {
            for (PDPage page : doc.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobj = resources.getXObject(name);
                    if (xobj instanceof PDImageXObject) {
                        PDImageXObject image = (PDImageXObject) xobj;
                        Bitmap bmp = image.getImage();
                        if (bmp != null) {
                            PDImageXObject recompressed = JPEGFactory.createFromImage(doc, bmp, 0.5f);
                            resources.put(name, recompressed);
                        }
                    }
                }
            }
            doc.save(output);
        }
    }

    // .docx/.xlsx/.pptx are OPC (zip) packages — recompress embedded images in place, then re-save.
    // Note: converting embedded PNGs to JPEG drops transparency; acceptable tradeoff for size wins,
    // but skip this if the document relies on transparent images.
    private static void compressOfficeDocument(File input, File output) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(input.getAbsolutePath())) {
            for (PackagePart part : pkg.getParts()) {
                String contentType = part.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) continue;
                if (contentType.contains("wmf") || contentType.contains("emf")) continue; // vector, skip

                byte[] original = readAllBytes(part.getInputStream());
                Bitmap bmp = BitmapFactory.decodeByteArray(original, 0, original.length);
                if (bmp == null) continue;

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 65, baos);
                byte[] compressed = baos.toByteArray();
                bmp.recycle();

                if (compressed.length < original.length) {
                    try (OutputStream os = part.getOutputStream()) {
                        os.write(compressed);
                    }
                }
            }
            pkg.save(output);
        }
    }

    private static void gzipFile(File input, File output) throws Exception {
        try (InputStream in = new java.io.FileInputStream(input);
             GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(output)) {
                 { def.setLevel(Deflater.BEST_COMPRESSION); }
             }) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) gz.write(buf, 0, n);
        }
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) baos.write(buf, 0, n);
        return baos.toByteArray();
    }
}