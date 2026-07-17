package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.widget.ImageView;

import com.simplepdf.pdfeditor.Signature.SignatureUtils;
import com.simplepdf.pdfeditor.Signature.SignatureView;
import com.simplepdf.pdfeditor.model.PDSElement;


public class ViewUtils {
    public static void constrainRectXY(RectF rectF, RectF rectF2) {
        if (rectF.left < rectF2.left) {
            rectF.left = rectF2.left;
        } else if (rectF.right > rectF2.right) {
            rectF.left = rectF2.right - rectF.width();
        }
        if (rectF.top < rectF2.top) {
            rectF.top = rectF2.top;
        } else if (rectF.bottom > rectF2.bottom) {
            rectF.top = rectF2.bottom - rectF.height();
        }
    }

    public static SignatureView createSignatureView(Context context, PDSElement pDSElement, Matrix matrix) {
        RectF rectF = new RectF(pDSElement.getRect());
        float strokeWidth = pDSElement.getStrokeWidth();
        if (matrix != null) {
            matrix.mapRect(rectF);
            matrix.mapRadius(strokeWidth);
        }
        SignatureView createFreeHandView = SignatureUtils.createFreeHandView((int) rectF.height(), pDSElement.getFile(), context);
        if (createFreeHandView != null) {
            createFreeHandView.setX(rectF.left);
            createFreeHandView.setY(rectF.top);
        }
        return createFreeHandView;
    }

    public static ImageView createImageView(Context context, PDSElement pDSElement, Matrix matrix) {
        RectF rectF = new RectF(pDSElement.getRect());
        if (matrix != null) {
            matrix.mapRect(rectF);
        }
        ImageView createImageView = SignatureUtils.createImageView((int) rectF.height(), pDSElement.getBitmap(), context);
        if (createImageView != null) {
            createImageView.setX(rectF.left);
            createImageView.setY(rectF.top);
        }



        return createImageView;
    }
}
