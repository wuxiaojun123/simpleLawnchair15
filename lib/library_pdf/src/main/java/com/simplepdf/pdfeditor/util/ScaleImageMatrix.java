package com.simplepdf.pdfeditor.util;


public class ScaleImageMatrix {
    public static final int AX = 0;
    public static final int AY = 1;
    public static final int BX = 2;
    public static final int BY = 3;
    public static final int CX = 4;
    public static final int CY = 5;
    public static final int DX = 6;
    public static final int DY = 7;
    protected float Height;
    protected float Width;
    protected float plainHeight;
    protected float plainWidth;
    protected float scaledHeight;
    protected float scaledWidth;
    protected float rotationRadians = 0.0f;
    private float widthPercentage = 100.0f;

    public ScaleImageMatrix(float f, float f2) {
        this.Width = f;
        this.Height = f2;
    }

    public float getWidth() {
        return this.Width;
    }

    public float getHeight() {
        return this.Height;
    }

    public float getScaledWidth() {
        return this.scaledWidth;
    }

    public float getScaledHeight() {
        return this.scaledHeight;
    }

    public ScaleImageMatrix() {
    }

    private void scalePercent(float f) {
        scalePercent(f, f);
    }

    private void scalePercent(float f, float f2) {
        this.plainWidth = (getWidth() * f) / 100.0f;
        this.plainHeight = (getHeight() * f2) / 100.0f;
        float[] matrix = matrix();
        this.scaledWidth = matrix[6] - matrix[4];
        this.scaledHeight = matrix[7] - matrix[5];
        setWidthPercentage(0.0f);
    }

    public void scaleToFit(float f, float f2) {
        scalePercent(100.0f);
        scalePercent(Math.min((f * 100.0f) / getScaledWidth(), (f2 * 100.0f) / getScaledHeight()));
        setWidthPercentage(0.0f);
    }

    public void setWidthPercentage(float f) {
        this.widthPercentage = f;
    }

    private float[] matrix() {
        return matrix(1.0f);
    }

    private float[] matrix(float f) {
        float[] fArr = new float[8];
        float cos = (float) Math.cos(this.rotationRadians);
        float sin = (float) Math.sin(this.rotationRadians);
        float f2 = this.plainWidth;
        fArr[0] = f2 * cos * f;
        fArr[1] = f2 * sin * f;
        float f3 = this.plainHeight;
        fArr[2] = (-f3) * sin * f;
        fArr[3] = f3 * cos * f;
        float f4 = this.rotationRadians;
        if (f4 < 1.5707963267948966d) {
            fArr[4] = fArr[2];
            fArr[5] = 0.0f;
            fArr[6] = fArr[0];
            fArr[7] = fArr[1] + fArr[3];
        } else if (f4 < 3.141592653589793d) {
            fArr[4] = fArr[0] + fArr[2];
            fArr[5] = fArr[3];
            fArr[6] = 0.0f;
            fArr[7] = fArr[1];
        } else if (f4 < 4.71238898038469d) {
            fArr[4] = fArr[0];
            fArr[5] = fArr[1] + fArr[3];
            fArr[6] = fArr[2];
            fArr[7] = 0.0f;
        } else {
            fArr[4] = 0.0f;
            fArr[5] = fArr[1];
            fArr[6] = fArr[0] + fArr[2];
            fArr[7] = fArr[3];
        }
        return fArr;
    }
}
