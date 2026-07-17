package com.simplepdf.pdfeditor.Activity;

import com.simplepdf.pdfeditor.Fragment.HomeFragment;
import com.simplepdf.pdfeditor.model.PDFFileModel;

import java.util.Comparator;



public final  class MainActivityExternal1 implements Comparator {
    public static final MainActivityExternal1 INSTANCE = new MainActivityExternal1();

    private MainActivityExternal1() {
    }

    @Override 
    public final int compare(Object obj, Object obj2) {
        return HomeFragment.lambda$getAllPdfFileList$10((PDFFileModel) obj, (PDFFileModel) obj2);
    }
}
