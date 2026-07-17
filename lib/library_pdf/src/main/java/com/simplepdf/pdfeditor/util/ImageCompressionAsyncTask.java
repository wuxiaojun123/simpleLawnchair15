package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.simplepdf.pdfeditor.CallbackListener.ImageListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ImageCompressionAsyncTask {
    Context context;
    public CompositeDisposable disposable;
    long id;
    ImageListener imageCopy;
    Uri initialFilePath;
    String path;

    public ImageCompressionAsyncTask(final Context context, Uri uri, final ImageListener imageListener) {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        this.disposable = compositeDisposable;
        this.path = "";
        this.context = context;
        this.id = this.id;
        this.initialFilePath = uri;
        this.imageCopy = imageListener;
        compositeDisposable.add(Observable.fromCallable(new Callable() { 
            @Override 
            public final Object call() throws Exception {
                return ImageCompressionAsyncTask.this.m133xb493f15(context);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() { 
            @Override 
            public final void accept(Object obj) throws Exception {
                ImageCompressionAsyncTask.lambda$new$3(imageListener, obj);
            }
        }));
    }

    
    
    public  Object m133xb493f15(Context context) throws Exception {
        try {
            return getCompressBitmapFormUri(context, this.initialFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    
    public static  void lambda$new$3(ImageListener imageListener, Object obj) throws Exception {
        try {
            imageListener.onImageCopy((Bitmap) obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    public Bitmap getCompressBitmapFormUri(Context context, Uri uri) throws FileNotFoundException, IOException {
        int i;
        InputStream openInputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeStream(openInputStream, null, options);
        openInputStream.close();
        int i2 = options.outWidth;
        int i3 = options.outHeight;
        if (i2 == -1 || i3 == -1) {
            return null;
        }
        if (i2 > i3) {
            float f = i2;
            if (f > 612.0f) {
                i = (int) (f / 612.0f);
                if (i <= 0) {
                    i = 1;
                }
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inSampleSize = i;
                options2.inDither = true;
                options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
                InputStream openInputStream2 = context.getContentResolver().openInputStream(uri);
                Bitmap decodeStream = BitmapFactory.decodeStream(openInputStream2, null, options2);
                openInputStream2.close();
                return compressImage(decodeStream);
            }
        }
        if (i2 < i3) {
            float f2 = i3;
            if (f2 > 816.0f) {
                i = (int) (f2 / 816.0f);
                if (i <= 0) {
                }
                BitmapFactory.Options options22 = new BitmapFactory.Options();
                options22.inSampleSize = i;
                options22.inDither = true;
                options22.inPreferredConfig = Bitmap.Config.ARGB_8888;
                InputStream openInputStream22 = context.getContentResolver().openInputStream(uri);
                Bitmap decodeStream2 = BitmapFactory.decodeStream(openInputStream22, null, options22);
                openInputStream22.close();
                return compressImage(decodeStream2);
            }
        }
        i = 1;
        if (i <= 0) {
        }
        BitmapFactory.Options options222 = new BitmapFactory.Options();
        options222.inSampleSize = i;
        options222.inDither = true;
        options222.inPreferredConfig = Bitmap.Config.ARGB_8888;
        InputStream openInputStream222 = context.getContentResolver().openInputStream(uri);
        Bitmap decodeStream22 = BitmapFactory.decodeStream(openInputStream222, null, options222);
        openInputStream222.close();
        return compressImage(decodeStream22);
    }

    public static Bitmap compressImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        int i = 100;
        while (byteArrayOutputStream.toByteArray().length / 1024 > 100) {
            byteArrayOutputStream.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG, i, byteArrayOutputStream);
            i -= 10;
        }
        return BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), null, null);
    }


}
