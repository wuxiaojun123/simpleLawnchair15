package com.simplepdf.pdfeditor.model;

import android.graphics.Bitmap;


public class IconBitmapModel {
    Bitmap IconBitmap;
    String IconName;

    public IconBitmapModel(String str, Bitmap bitmap) {
        this.IconName = str;
        this.IconBitmap = bitmap;
    }

    public String getIconName() {
        return this.IconName;
    }

    public void setIconName(String str) {
        this.IconName = str;
    }

    public Bitmap getIconBitmap() {
        return this.IconBitmap;
    }

    public void setIconBitmap(Bitmap bitmap) {
        this.IconBitmap = bitmap;
    }
}
