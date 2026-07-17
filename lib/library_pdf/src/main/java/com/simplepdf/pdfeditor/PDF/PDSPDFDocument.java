package com.simplepdf.pdfeditor.PDF;

import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.tom_roush.pdfbox.pdmodel.common.PDPageLabelRange;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;


public class PDSPDFDocument {
    private static final transient Object sLockObject = new Object();
    Context context;
    private HashMap<Integer, PDSPDFPage> mPages;
    Uri pdfDocument;
    public InputStream stream;
    private int mNumPages = -1;
    private transient PdfRenderer mRenderer = null;

    public PDSPDFDocument(Context context, Uri uri) throws FileNotFoundException {
        this.pdfDocument = null;
        this.context = null;
        this.mPages = null;
        this.mPages = new HashMap<>();
        this.pdfDocument = uri;
        this.context = context;
        this.stream = context.getContentResolver().openInputStream(uri);
    }

    public void open() throws IOException {
        ParcelFileDescriptor openFileDescriptor = this.context.getContentResolver().openFileDescriptor(this.pdfDocument, PDPageLabelRange.STYLE_ROMAN_LOWER);
        if (isValidPDF(openFileDescriptor)) {
            synchronized (sLockObject) {
                try {
                    PdfRenderer pdfRenderer = new PdfRenderer(openFileDescriptor);
                    this.mRenderer = pdfRenderer;
                    this.mNumPages = pdfRenderer.getPageCount();
                } catch (Exception unused) {
                    if (openFileDescriptor != null) {
                        openFileDescriptor.close();
                    }
                    throw new IOException();
                } catch (Throwable unused2) {
                }
            }
            return;
        }
        openFileDescriptor.close();
        throw new IOException();
    }

    public void close() {
        PdfRenderer pdfRenderer = this.mRenderer;
        if (pdfRenderer != null) {
            pdfRenderer.close();
            this.mRenderer = null;
        }
    }

    public PDSPDFPage getPage(int i) {
        if (i >= this.mNumPages || i < 0) {
            return null;
        }
        PDSPDFPage pDSPDFPage = this.mPages.get(Integer.valueOf(i));
        if (pDSPDFPage != null) {
            return pDSPDFPage;
        }
        PDSPDFPage pDSPDFPage2 = new PDSPDFPage(i, this);
        this.mPages.put(Integer.valueOf(i), pDSPDFPage2);
        return pDSPDFPage2;
    }

    public static Object getLockObject() {
        return sLockObject;
    }

    public PdfRenderer getRenderer() {
        return this.mRenderer;
    }

    public int getNumPages() {
        return this.mNumPages;
    }

    private boolean isValidPDF(ParcelFileDescriptor parcelFileDescriptor) {
        try {
            byte[] bArr = new byte[4];
            if (new FileInputStream(parcelFileDescriptor.getFileDescriptor()).read(bArr) == 4 && bArr[0] == 37 && bArr[1] == 80 && bArr[2] == 68) {
                if (bArr[3] == 70) {
                    return true;
                }
            }
        } catch (IOException unused) {
        }
        return false;
    }

    public Uri getDocumentUri() {
        return this.pdfDocument;
    }
}
