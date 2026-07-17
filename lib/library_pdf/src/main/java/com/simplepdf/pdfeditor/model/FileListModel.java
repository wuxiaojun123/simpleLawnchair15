package com.simplepdf.pdfeditor.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;


public class FileListModel implements Parcelable {
    public static final Creator<FileListModel> CREATOR = new Creator<FileListModel>() { 
        
        @Override 
        public FileListModel createFromParcel(Parcel parcel) {
            return new FileListModel(parcel);
        }

        
        @Override 
        public FileListModel[] newArray(int i) {
            return new FileListModel[i];
        }
    };
    long fileDate;
    String fileImage;
    String filePath;
    long fileSize;
    String fileType;
    String filename;
    boolean isChecked = false;

    @Override 
    public int describeContents() {
        return 0;
    }

    public FileListModel() {
    }

    public FileListModel(String str, String str2, long j, long j2, String str3, String str4) {
        this.filePath = str;
        this.filename = str2;
        this.fileSize = j;
        this.fileDate = j2;
        this.fileType = str3;
        this.fileImage = str4;
    }

    protected FileListModel(Parcel parcel) {
        this.filePath = parcel.readString();
        this.filename = parcel.readString();
        this.fileSize = parcel.readLong();
        this.fileDate = parcel.readLong();
        this.fileType = parcel.readString();
        this.fileImage = parcel.readString();
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean z) {
        this.isChecked = z;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String str) {
        this.filePath = str;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String str) {
        this.filename = str;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long j) {
        this.fileSize = j;
    }

    public long getFileDate() {
        return this.fileDate;
    }

    public void setFileDate(long j) {
        this.fileDate = j;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String str) {
        this.fileType = str;
    }

    public String getFileImage() {
        return this.fileImage;
    }

    public void setFileImage(String str) {
        this.fileImage = str;
    }

    @Override 
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.filePath);
        parcel.writeString(this.filename);
        parcel.writeLong(this.fileSize);
        parcel.writeLong(this.fileDate);
        parcel.writeString(this.fileType);
        parcel.writeString(this.fileImage);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(this.filePath, ((FileListModel) obj).filePath);
    }

    public int hashCode() {
        return Objects.hash(this.filePath);
    }
}
