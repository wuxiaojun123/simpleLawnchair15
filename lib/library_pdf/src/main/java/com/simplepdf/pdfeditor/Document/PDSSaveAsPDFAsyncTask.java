package com.simplepdf.pdfeditor.Document;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.simplepdf.pdfeditor.Activity.DigitalSignatureActivity;
import com.simplepdf.pdfeditor.PDF.PDSPDFDocument;
import com.simplepdf.pdfeditor.model.PDSElement;
import com.simplepdf.pdfeditor.util.AppPref;
import com.simplepdf.pdfeditor.util.Constant;
import com.simplepdf.pdfeditor.util.ScaleImageMatrix;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class PDSSaveAsPDFAsyncTask {
    public CompositeDisposable disposable = new CompositeDisposable();
    boolean isUpdate;
    DigitalSignatureActivity mCtx;
    private String mfileName;

    public PDSSaveAsPDFAsyncTask(DigitalSignatureActivity digitalSignatureActivity, String str, boolean z) {
        this.isUpdate = false;
        this.mCtx = digitalSignatureActivity;
        this.mfileName = str;
        this.isUpdate = z;
        digitalSignatureActivity.savingProgress.setVisibility(View.VISIBLE);
        this.disposable.add(Observable.fromCallable(new Callable() { 
            @Override 
            public final Object call() {
                return PDSSaveAsPDFAsyncTask.this.m119x7a2e4b57();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() { 
            @Override 
            public final void accept(Object obj) throws Exception {
                PDSSaveAsPDFAsyncTask.this.m120x71b6276((String) obj);
            }
        }));
    }

    
    
    public  void m120x71b6276(String str) throws Exception {
        try {
            this.mCtx.runPostExecution(str, this.disposable);
            if (TextUtils.isEmpty(str)) {
                Toast.makeText(this.mCtx, "Something went wrong while Signing PDF document, Please try again", Toast.LENGTH_LONG).show();
            } else if (AppPref.getProVersion()) {
                Toast.makeText(this.mCtx, "PDF document saved successfully", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public String m119x7a2e4b57() {
        float width;
        PDSPDFDocument document = this.mCtx.getDocument();
        File file = new File(Constant.DirectoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getAbsolutePath(), this.mfileName);
        if (!this.isUpdate && file2.exists()) {
            file2.delete();
        }
        try {
            InputStream inputStream = document.stream;
            PDDocument load = PDDocument.load(inputStream);
            for (int i = 0; i < document.getNumPages(); i++) {
                PDPage page = load.getPage(i);
                for (int i2 = 0; i2 < document.getPage(i).getNumElements(); i2++) {
                    PDSElement element = document.getPage(i).getElement(i2);
                    RectF rect = element.getRect();
                    Bitmap bitmap = element.getBitmap();
                    if (element.getType() == PDSElement.PDSElementType.PDSElementTypeImageSign) {
                        width = rect.width() == 0.0f ? element.getmDefaultWidth() * 2.0f : rect.width();
                    } else {
                        width = rect.width() == 0.0f ? element.getmDefaultWidth() : rect.width();
                    }
                    float f = width;
                    float height = rect.height() == 0.0f ? element.getmDefaultHeight() : rect.height();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    byteArrayOutputStream.toByteArray();
                    PDImageXObject createFromImage = LosslessFactory.createFromImage(load, bitmap);
                    ScaleImageMatrix scaleImageMatrix = new ScaleImageMatrix(createFromImage.getWidth(), createFromImage.getHeight());
                    scaleImageMatrix.scaleToFit(f, height);
                    PDPageContentStream pDPageContentStream = new PDPageContentStream(load, page, true, false, true);
                    pDPageContentStream.drawImage(createFromImage, rect.left - ((scaleImageMatrix.getScaledWidth() - f) / 2.0f), page.getMediaBox().getHeight() - (rect.top + height), f, height);
                    pDPageContentStream.close();
                    byteArrayOutputStream.close();
                }
            }
            load.save(file2.getPath());
            load.close();
            inputStream.close();
            return file2.getPath();
        } catch (Exception e) {
            e.printStackTrace();
            if (file2.exists()) {
                file2.delete();
                return "";
            }
            return "";
        }
    }
}
