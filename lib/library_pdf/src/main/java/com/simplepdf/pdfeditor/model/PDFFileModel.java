package com.simplepdf.pdfeditor.model;

import com.simplepdf.pdfeditor.util.AppConstants;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;


public class PDFFileModel {
    File file;
    boolean isCheck;

    String detial = "";

    public PDFFileModel(File file, boolean z) {
        this.file = file;
        this.isCheck = z;
        detial =  hi(file);
    }


    public String hi(File file) {

        File file1 = file;
        long time1 = file1.lastModified();

        long length = file1.length();
        length = length / 1024;
        DateFormat sdf
                = new SimpleDateFormat("dd/mm/yyyy hh:mm a");
        return sdf.format(time1) + "  " + AppConstants.FileSizeWithUnits(length);
    }

    public File getFile() {
        return this.file;
    }


    public void setFile(File file) {
        this.file = file;
    }

    
    public String getDetial() {
        return this.detial;
    }

    public boolean isCheck() {
        return this.isCheck;
    }

    public void setCheck(boolean z) {
        this.isCheck = z;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PDFFileModel pDFFileModel = (PDFFileModel) obj;
        return this.isCheck == pDFFileModel.isCheck && Objects.equals(this.file, pDFFileModel.file);
    }

    public int hashCode() {
        return Objects.hash(this.file, Boolean.valueOf(this.isCheck));
    }
}
