package com.simplepdf.pdfeditor.adapter;

import com.simplepdf.pdfeditor.model.FileListModel;

import java.util.Comparator;



public final  class ChoosePDFListViewAdapter1 implements Comparator {
    public static final ChoosePDFListViewAdapter1 INSTANCE = new ChoosePDFListViewAdapter1();

    private ChoosePDFListViewAdapter1() {
    }

    @Override 
    public final int compare(Object obj, Object obj2) {
        int compareToIgnoreCase;
        compareToIgnoreCase = ((FileListModel) obj).getFilename().compareToIgnoreCase(((FileListModel) obj2).getFilename());
        return compareToIgnoreCase;
    }
}
