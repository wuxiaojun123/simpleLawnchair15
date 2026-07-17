package com.simplepdf.pdfeditor.Signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.Constant;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class SignatureUtils {
    private static int EXTRA_WIDTH_PADDING;

    
    public static class ViewHolder {
        public RectF boundingBox;
        public int inkColor;
        public ArrayList<ArrayList<Float>> inkList;
        public float strokeWidth;
    }

    public static void saveSignature(Context context, SignatureView signatureView) {
        if (signatureView.mInkList.size() != 0) {
            saveAsImage(context, signatureView);
        }
    }

    public static void saveAsImage(Context context, SignatureView signatureView) {
        File file = new File(Constant.SignaturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String absolutePath = file.getAbsolutePath();
        File file2 = new File(absolutePath, System.currentTimeMillis() + ".png");
        Bitmap image = signatureView.getImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            fileOutputStream.write(byteArray);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String saveAsImageBitmap(Context context, SignatureView signatureView) {
        File file = new File(Constant.SignaturePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String absolutePath = file.getAbsolutePath();
        File file2 = new File(absolutePath, System.currentTimeMillis() + ".png");
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(signatureView.getImage(), AppConstants.DEFAULT_WIDTH, AppConstants.DEFAULT_HEIGHT, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        createScaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            fileOutputStream.write(byteArray);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file2.getPath();
    }

    public static Bitmap compressSignImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        int i = 100;
        while (byteArrayOutputStream.toByteArray().length / 1024 > 100) {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.PNG, i, byteArrayOutputStream);
            i -= 10;
        }
        return BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, null);
    }

    public static SignatureView createFreeHandView(int i, File file, Context context) {
        int i2 = i - 30;
        try {
            ViewHolder readSignatureHolder = readSignatureHolder(context, file);
            if (readSignatureHolder != null) {
                if (i > readSignatureHolder.boundingBox.height()) {
                    EXTRA_WIDTH_PADDING = 30;
                    return createFreeHandView(i, i, file, context);
                }
                RectF rectF = readSignatureHolder.boundingBox;
                float height = i2 / readSignatureHolder.boundingBox.height();
                return createFreeHandView(((int) (readSignatureHolder.boundingBox.width() * height)) + 30 + 30, i, readSignatureHolder.inkList, rectF, height, height, (rectF.left * height) - 15.0f, (rectF.top * height) - 15.0f, readSignatureHolder.strokeWidth, readSignatureHolder.inkColor, context);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ImageView createImageView(int i, Bitmap bitmap, Context context) {
        int width = 0;
        ImageView imageView = null;
        ImageView imageView2 = null;
        try {
            width = ((int) (bitmap.getWidth() * ((double)(i - 30) / bitmap.getHeight()))) + 30;
            imageView = new ImageView(context);
        } catch (Exception e) {

        }
        try {
            imageView.setLayoutParams(new RelativeLayout.LayoutParams(width, i));
            return imageView;
        } catch (Exception e2) {

            imageView2 = imageView;

            return imageView2;
        }

    }

    private static SignatureView createFreeHandView(int i, int i2, ArrayList<ArrayList<Float>> arrayList, RectF rectF, float f, float f2, float f3, float f4, float f5, int i3, Context context) {
        SignatureView signatureView = new SignatureView(context, i, i2);
        signatureView.setStrokeWidth(f5);
        signatureView.setStrokeColor(i3);
        signatureView.setmActualColor(i3);
        signatureView.setEditable(false);
        signatureView.initializeInkList(arrayList);
        signatureView.fillColor();
        signatureView.scaleAndTranslatePath(arrayList, rectF, f, f2, f3, f4);
        signatureView.invalidate();
        return signatureView;
    }

    
    
    
    
    public static SignatureView createFreeHandView(int i, int i2, File file, Context context) {
        int i3;
        float fitXYScale;
        float f = 0;
        SignatureView signatureView = null;
        try {
            ViewHolder readSignatureHolder = readSignatureHolder(context, file);
            if (readSignatureHolder != null) {
                RectF rectF = readSignatureHolder.boundingBox;
                try {
                    if (rectF.height() <= 1.0f && rectF.width() <= 1.0f) {
                        i3 = i2;
                        fitXYScale = 1.0f;
                        float f2 = i3;
                        signatureView = createFreeHandView(i + EXTRA_WIDTH_PADDING, i2, readSignatureHolder.inkList, rectF, fitXYScale, fitXYScale, (rectF.left * fitXYScale) - (i >= readSignatureHolder.boundingBox.width() * fitXYScale ? (int) ((f - (readSignatureHolder.boundingBox.width() * fitXYScale)) / 2.0f) : 15), (rectF.top * fitXYScale) - (f2 < readSignatureHolder.boundingBox.height() * fitXYScale ? (int) ((f2 - (readSignatureHolder.boundingBox.height() * fitXYScale)) / 2.0f) : 15), readSignatureHolder.strokeWidth, readSignatureHolder.inkColor, context);
                        EXTRA_WIDTH_PADDING = 0;
                    }
                    EXTRA_WIDTH_PADDING = 0;
                } catch (Exception unused) {
                }
                i3 = i2;
                fitXYScale = getFitXYScale(i, i2, file, context);
                float f22 = i3;
                if (f22 < readSignatureHolder.boundingBox.height() * fitXYScale) {
                }
                if (i >= readSignatureHolder.boundingBox.width() * fitXYScale) {
                }
                signatureView = createFreeHandView(i + EXTRA_WIDTH_PADDING, i2, readSignatureHolder.inkList, rectF, fitXYScale, fitXYScale, (rectF.left * fitXYScale) - (i >= readSignatureHolder.boundingBox.width() * fitXYScale ? (int) ((f - (readSignatureHolder.boundingBox.width() * fitXYScale)) / 2.0f) : 15), (rectF.top * fitXYScale) - (f22 < readSignatureHolder.boundingBox.height() * fitXYScale ? (int) ((f22 - (readSignatureHolder.boundingBox.height() * fitXYScale)) / 2.0f) : 15), readSignatureHolder.strokeWidth, readSignatureHolder.inkColor, context);
            }
            return signatureView;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeToStream(OutputStream outputStream, String str) throws IOException {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        outputStreamWriter.write(str);
        outputStreamWriter.close();
    }

    public static ViewHolder readSignatureHolder(Context context, File file) {
        FileInputStream fileInputStream;
        if (file.exists()) {
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fileInputStream = null;
            }
            try {
                return (ViewHolder) new Gson().fromJson(getStringFromStream(fileInputStream), new TypeToken<ViewHolder>() { 
                }.getType());
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    public static String getStringFromStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine != null) {
                sb.append(readLine);
            } else {
                bufferedReader.close();
                return sb.toString();
            }
        }
    }

    private static float getFitXYScale(int i, int i2, File file, Context context) {
        float f;
        float height;
        ViewHolder readSignatureHolder = readSignatureHolder(context, file);
        if (readSignatureHolder != null) {
            float f2 = 0.0f;
            if (readSignatureHolder.boundingBox.height() != 0.0f) {
                float width = readSignatureHolder.boundingBox.width() / readSignatureHolder.boundingBox.height();
                Integer num = 1;
                int i3 = i - 15;
                int i4 = i2 - 15;
                while (num != null) {
                    if (width > i3 / i4) {
                        f = i3;
                        height = readSignatureHolder.boundingBox.width();
                    } else {
                        f = i4;
                        height = readSignatureHolder.boundingBox.height();
                    }
                    f2 = f / height;
                    if (i2 <= readSignatureHolder.boundingBox.height() * f2) {
                        i4 -= 7;
                    } else if (i > readSignatureHolder.boundingBox.width() * f2) {
                        num = null;
                    } else {
                        i3 -= 7;
                    }
                }
                return f2;
            }
            return 1.0f;
        }
        return 1.0f;
    }

    public static int getSignatureWidth(int i, File file, Context context) {
        int i2 = i - 30;
        try {
            ViewHolder readSignatureHolder = readSignatureHolder(context, file);
            if (readSignatureHolder != null && i <= readSignatureHolder.boundingBox.height()) {
                return ((int) (readSignatureHolder.boundingBox.width() * (i2 / readSignatureHolder.boundingBox.height()))) + 30 + 30;
            }
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return i;
        }
    }
}
