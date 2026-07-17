package com.simplepdf.pdfeditor.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CompressImage {
    Context context;
    Uri uri;

    public CompressImage(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    public String compressImageData(String str) throws FileNotFoundException {
        Bitmap bitmap;
        Bitmap bitmap2;
        Bitmap bitmap3;
        Matrix matrix = null;
        String realPathFromURI = getRealPathFromURI(str);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap decodeFile = BitmapFactory.decodeFile(realPathFromURI, options);
        int i = options.outHeight;
        int i2 = options.outWidth;
        float f = i2 / i;
        float f2 = i;
        if (f2 > 816.0f || i2 > 612.0f) {
            if (f < 0.75f) {
                i2 = (int) ((816.0f / f2) * i2);
                i = (int) 816.0f;
            } else {
                i = f > 0.75f ? (int) ((612.0f / i2) * f2) : (int) 816.0f;
                i2 = (int) 612.0f;
            }
        }
        options.inSampleSize = calculateInSampleSize(options, i2, i);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16384];
        try {
            decodeFile = BitmapFactory.decodeFile(realPathFromURI, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        try {
            bitmap = Bitmap.createBitmap(i2, i, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            bitmap = null;
        }
        Bitmap bitmap4 = bitmap;
        float f3 = i2;
        float f4 = f3 / options.outWidth;
        float f5 = i;
        float f6 = f5 / options.outHeight;
        float f7 = f3 / 2.0f;
        float f8 = f5 / 2.0f;
        Matrix matrix2 = new Matrix();
        matrix2.setScale(f4, f6, f7, f8);
        Canvas canvas = new Canvas(bitmap4);
        canvas.setMatrix(matrix2);
        canvas.drawBitmap(decodeFile, f7 - (decodeFile.getWidth() / 2), f8 - (decodeFile.getHeight() / 2), new Paint(2));
        try {
            int attributeInt = new ExifInterface(realPathFromURI).getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + attributeInt);
            matrix = new Matrix();
            if (attributeInt == 6) {
                matrix.postRotate(90.0f);
                Log.d("EXIF", "Exif: " + attributeInt);
            } else if (attributeInt == 3) {
                matrix.postRotate(180.0f);
                Log.d("EXIF", "Exif: " + attributeInt);
            } else if (attributeInt == 8) {
                matrix.postRotate(270.0f);
                Log.d("EXIF", "Exif: " + attributeInt);
            }
            bitmap2 = bitmap4;
        } catch (IOException e3) {

            bitmap2 = bitmap4;
        }
        try {
            bitmap3 = Bitmap.createBitmap(bitmap4, 0, 0, bitmap4.getWidth(), bitmap4.getHeight(), matrix, true);
        } catch (Exception e4) {

            bitmap3 = bitmap2;
            String filename = getFilename();
            bitmap3.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(filename));
            return filename;
        }
        String filename2 = getFilename();
        try {
            bitmap3.compress(Bitmap.CompressFormat.JPEG, 80, new FileOutputStream(filename2));
        } catch (FileNotFoundException e5) {
            e5.printStackTrace();
        }
        return filename2;
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
    }

    @SuppressLint("Range")
    private String getRealPathFromURI(String str) {
        Uri parse = Uri.parse(str);
        Cursor query = this.context.getContentResolver().query(parse, null, null, null, null);
        if (query == null) {
            return parse.getPath();
        }
        query.moveToFirst();
        return query.getString(query.getColumnIndex("_data"));
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int i, int i2) {
        int round;
        int i3 = options.outHeight;
        int i4 = options.outWidth;
        if (i3 > i2 || i4 > i) {
            round = Math.round(i3 / i2);
            int round2 = Math.round(i4 / i);
            if (round >= round2) {
                round = round2;
            }
        } else {
            round = 1;
        }
        while ((i4 * i3) / (round * round) > i * i2 * 2) {
            round++;
        }
        return round;
    }
}
