package com.simplepdf.pdfeditor.Document;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SizeF;

import com.simplepdf.pdfeditor.PDF.PDSPDFPage;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class PDSRenderPageAsyncTask {
    private static final int MAX_BITMAP_SIZE = 3072;
    public CompositeDisposable disposable;
    private SizeF mBitmapSize = null;
    private Context mContext;
    private boolean mForPrint;
    private SizeF mImageViewSize;
    private boolean mIncludePageElements;
    private OnPostExecuteListener mListener;
    private final PDSPDFPage mPage;
    private float mScale;

    
    public interface OnPostExecuteListener {
        void onPostExecute(PDSRenderPageAsyncTask pDSRenderPageAsyncTask, Bitmap bitmap);
    }

    
    public PDSRenderPageAsyncTask(Context context, PDSPDFPage pDSPDFPage, SizeF sizeF, float f, boolean z, boolean z2, OnPostExecuteListener onPostExecuteListener) {
        this.mContext = context;
        this.mPage = pDSPDFPage;
        this.mImageViewSize = sizeF;
        this.mScale = f;
        this.mIncludePageElements = z;
        this.mForPrint = z2;
        this.mListener = onPostExecuteListener;
    }

    public void getRenderPdfData() {
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        this.disposable = compositeDisposable;
        compositeDisposable.add((Disposable) Observable.fromCallable(new Callable() { 
            @Override 
            public final Object call() throws Exception {
                return PDSRenderPageAsyncTask.this.m118x2221e2d();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<Bitmap>() { 
            @Override 
            public void onComplete() {
            }

            @Override 
            public void onError(Throwable th) {
            }

            @Override 
            public void onNext(Bitmap bitmap) {
                PDSRenderPageAsyncTask.this.onPostExecute(bitmap);
            }
        }));
    }

    
    
    public  Bitmap m118x2221e2d() throws Exception {
        if (this.mImageViewSize.getWidth() <= 0.0f) {
            return null;
        }
        SizeF computePageBitmapSize = computePageBitmapSize();
        this.mBitmapSize = computePageBitmapSize;
        float width = computePageBitmapSize.getWidth() * this.mScale;
        float height = this.mBitmapSize.getHeight() * this.mScale;
        float f = width / height;
        if (width > 3072.0f && width > height) {
            height = 3072.0f / f;
            width = 3072.0f;
        } else if (height > 3072.0f && height > width) {
            width = f * 3072.0f;
            height = 3072.0f;
        }
        try {
            Bitmap createBitmap = Bitmap.createBitmap(Math.round(width), Math.round(height), Bitmap.Config.ARGB_8888);
            createBitmap.setHasAlpha(false);
            createBitmap.eraseColor(-1);
            this.mPage.renderPage(this.mContext, createBitmap, this.mIncludePageElements, this.mForPrint);
            return createBitmap;
        } catch (OutOfMemoryError unused) {
            return null;
        }
    }

    public void onPostExecute(Bitmap bitmap) {
        OnPostExecuteListener onPostExecuteListener = this.mListener;
        if (onPostExecuteListener != null) {
            onPostExecuteListener.onPostExecute(this, bitmap);
        }
    }

    public void onCancelled(Bitmap bitmap) {
        OnPostExecuteListener onPostExecuteListener = this.mListener;
        if (onPostExecuteListener != null) {
            onPostExecuteListener.onPostExecute(this, null);
        }
    }

    public SizeF getBitmapSize() {
        return this.mBitmapSize;
    }

    public float getScale() {
        return this.mScale;
    }

    public PDSPDFPage getPage() {
        return this.mPage;
    }

    private SizeF computePageBitmapSize() {
        float f;
        float f2;
        SizeF pageSize = this.mPage.getPageSize();
        float width = pageSize.getWidth() / pageSize.getHeight();
        if (width > this.mImageViewSize.getWidth() / this.mImageViewSize.getHeight()) {
            float width2 = this.mImageViewSize.getWidth();
            f = width2 <= 3072.0f ? width2 : 3072.0f;
            f2 = Math.round(f / width);
        } else {
            float height = this.mImageViewSize.getHeight();
            f = height <= 3072.0f ? height : 3072.0f;
            float f3 = f;
            f = width * f;
            f2 = f3;
        }
        return new SizeF(f, f2);
    }
}
