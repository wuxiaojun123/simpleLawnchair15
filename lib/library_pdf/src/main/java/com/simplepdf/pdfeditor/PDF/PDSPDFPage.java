package com.simplepdf.pdfeditor.PDF;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.pdf.PdfRenderer;
import android.util.SizeF;

import com.simplepdf.pdfeditor.Document.PDSPageViewer;
import com.simplepdf.pdfeditor.model.PDSElement;

import java.util.ArrayList;


public class PDSPDFPage {
    private static final transient SizeF DEF_PAGE_SIZE = new SizeF(595.0f, 842.0f);
    private static final transient Object sSynchronizedObject = new Object();
    private PDSPDFDocument mPDFDocument;
    private int mpageNumber;
    private PDSPageViewer mviewer;
    private SizeF mPageSize = null;
    private ArrayList<PDSElement> mElements = new ArrayList<>();

    public PDSPDFPage(int i, PDSPDFDocument pDSPDFDocument) {
        this.mpageNumber = i;
        this.mPDFDocument = pDSPDFDocument;
    }

    public SizeF getPageSize() {
        if (this.mPageSize == null) {
            synchronized (PDSPDFDocument.getLockObject()) {
                synchronized (getDocument()) {
                    PdfRenderer.Page openPage = getDocument().getRenderer().openPage(getNumber());
                    this.mPageSize = new SizeF(openPage.getWidth(), openPage.getHeight());
                    openPage.close();
                }
            }
        }
        return this.mPageSize;
    }

    public void renderPage(Context context, Bitmap bitmap, boolean z, boolean z2) {
        synchronized (PDSPDFDocument.getLockObject()) {
            synchronized (getDocument()) {
                PdfRenderer.Page openPage = getDocument().getRenderer().openPage(getNumber());
                this.mPageSize = new SizeF(openPage.getWidth(), openPage.getHeight());
                openPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                openPage.close();
            }
        }
    }

    public void setPageViewer(PDSPageViewer pDSPageViewer) {
        this.mviewer = pDSPageViewer;
    }

    public PDSPageViewer getPageViewer() {
        return this.mviewer;
    }

    public PDSPDFDocument getDocument() {
        return this.mPDFDocument;
    }

    public int getNumber() {
        return this.mpageNumber;
    }

    public void removeElement(PDSElement pDSElement) {
        this.mElements.remove(pDSElement);
    }

    public void addElement(PDSElement pDSElement) {
        this.mElements.add(pDSElement);
    }

    public int getNumElements() {
        return this.mElements.size();
    }

    public PDSElement getElement(int i) {
        return this.mElements.get(i);
    }

    public ArrayList<PDSElement> getElements() {
        return this.mElements;
    }

    public void updateElement(PDSElement pDSElement, RectF rectF, float f, float f2, float f3, float f4) {
        if (rectF != null && !rectF.equals(pDSElement.getRect())) {
            pDSElement.setRect(rectF);
            Integer.valueOf(1);
        }
        if (f != 0.0f && f != pDSElement.getSize()) {
            pDSElement.setSize(f);
            Integer.valueOf(1);
        }
        if (f2 != 0.0f && f2 != pDSElement.getMaxWidth()) {
            pDSElement.setMaxWidth(f2);
            Integer.valueOf(1);
        }
        if (f3 != 0.0f && f3 != pDSElement.getStrokeWidth()) {
            pDSElement.setStrokeWidth(f3);
            Integer.valueOf(1);
        }
        if (f4 == 0.0f || f4 == pDSElement.getLetterSpace()) {
            return;
        }
        pDSElement.setLetterSpace(f4);
        Integer.valueOf(1);
    }
}
