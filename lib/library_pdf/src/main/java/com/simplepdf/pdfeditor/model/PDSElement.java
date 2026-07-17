package com.simplepdf.pdfeditor.model;

import android.graphics.Bitmap;
import android.graphics.RectF;

import com.simplepdf.pdfeditor.Document.PDSElementViewer;

import java.io.File;


public class PDSElement {
    private Bitmap bitmap;
    private float mDefaultHeight;
    private float mDefaultWidth;
    public PDSElementViewer mElementViewer;
    private float mHorizontalPadding = 0.0f;
    private float mLetterSpace = 0.0f;
    private float mMaxWidth = 0.0f;
    private float mMinWidth = 0.0f;
    private RectF mRect = null;
    private float mSize = 0.0f;
    private float mStrokeWidth = 0.0f;
    private PDSElementType mType;
    private float mVerticalPadding;
    private String malises;
    private File mfile;

    
    public enum PDSElementType {
        PDSElementTypeImage,
        PDSElementTypeSignature,
        PDSElementTypeImageSign,
        PDSElementTypeImageStamps,
        PDSElementTypeImageIcon
    }

    public PDSElement(PDSElementType pDSElementType, File file) {
        PDSElementType pDSElementType2 = PDSElementType.PDSElementTypeSignature;
        this.bitmap = null;
        this.mVerticalPadding = 0.0f;
        this.mDefaultWidth = 0.0f;
        this.mDefaultHeight = 0.0f;
        this.mType = pDSElementType;
        this.mfile = file;
    }

    public PDSElement(PDSElementType pDSElementType, Bitmap bitmap) {
        PDSElementType pDSElementType2 = PDSElementType.PDSElementTypeSignature;
        this.mfile = null;
        this.mVerticalPadding = 0.0f;
        this.mDefaultWidth = 0.0f;
        this.mDefaultHeight = 0.0f;
        this.mType = pDSElementType;
        this.bitmap = bitmap;
    }

    public PDSElementType getType() {
        return this.mType;
    }

    public void setRect(RectF rectF) {
        this.mRect = rectF;
    }

    public RectF getRect() {
        return this.mRect;
    }

    public void setSize(float f) {
        this.mSize = f;
    }

    public float getSize() {
        return this.mSize;
    }

    public void setMaxWidth(float f) {
        this.mMaxWidth = f;
    }

    public float getMaxWidth() {
        return this.mMaxWidth;
    }

    public void setMinWidth(float f) {
        this.mMinWidth = f;
    }

    public float getMinWidth() {
        return this.mMinWidth;
    }

    public float getmDefaultWidth() {
        return this.mDefaultWidth;
    }

    public void setmDefaultWidth(float f) {
        this.mDefaultWidth = f;
    }

    public float getmDefaultHeight() {
        return this.mDefaultHeight;
    }

    public void setmDefaultHeight(float f) {
        this.mDefaultHeight = f;
    }

    public void setHorizontalPadding(float f) {
        this.mHorizontalPadding = f;
    }

    public float getHorizontalPadding() {
        return this.mHorizontalPadding;
    }

    public void setVerticalPadding(float f) {
        this.mVerticalPadding = f;
    }

    public float getVerticalPadding() {
        return this.mVerticalPadding;
    }

    public void setStrokeWidth(float f) {
        this.mStrokeWidth = f;
    }

    public float getStrokeWidth() {
        return this.mStrokeWidth;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setLetterSpace(float f) {
        this.mLetterSpace = f;
    }

    public float getLetterSpace() {
        return this.mLetterSpace;
    }

    public File getFile() {
        return this.mfile;
    }

    public String getAlises() {
        return this.malises;
    }

    public void setAlises(String str) {
        this.malises = str;
    }
}
