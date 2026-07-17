package com.simplepdf.pdfeditor.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simplepdf.pdfeditor.CallbackListener.BottomSheetItemClick;
import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.DialogFragment.DocumentOptionBottomSheet;
import com.simplepdf.pdfeditor.DialogFragment.IconBottomSheet;
import com.simplepdf.pdfeditor.DialogFragment.SignatureBottomSheet;
import com.simplepdf.pdfeditor.Document.PDSPageViewer;
import com.simplepdf.pdfeditor.Document.PDSSaveAsPDFAsyncTask;
import com.simplepdf.pdfeditor.Document.PDSViewPager;
import com.simplepdf.pdfeditor.PDF.PDSPDFDocument;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.Signature.SignatureActivity;
import com.simplepdf.pdfeditor.adapter.PDSPageAdapter;
import com.simplepdf.pdfeditor.databinding.FileAlertDialogBinding;
import com.simplepdf.pdfeditor.model.PDSElement;
import com.simplepdf.pdfeditor.util.BetterActivityResult;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.reactivex.disposables.Disposable;


public class DigitalSignatureActivity extends BaseActivity {
    public MaterialButton btnSave;
    public FloatingActionButton fabEdit;
    PDSPageAdapter imageAdapter;
    private PDSViewPager mViewPager;
    public ProgressBar savingProgress;
    public Toolbar toolbar;
    Uri pdfData = null;
    private boolean mFirstTap = true;
    private int mVisibleWindowHt = 0;
    private PDSPDFDocument mDocument = null;
    private final UIElementsHandler mUIElemsHandler = new UIElementsHandler(this);
    private static final String READER_PREFS = "pdf_reader_position";
    private String currentPdfKey = null;
    public boolean isSigned = false;
    boolean is_Update = false;
    String updateFileName = "";
    int elementActive = 0;

    @Override 
    public void setBinding() {
        setContentView(R.layout.activity_digital_signature);
    }

    @Override 
    public void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarDigital);
        this.toolbar = toolbar;
        try {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            this.toolbar.setNavigationIcon(R.drawable.ic_back);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override 
    public void init() {


//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);
//        adAdmob.FullscreenAd(this);



        ArrayList parcelableArrayListExtra;
        PDFBoxResourceLoader.init(getApplicationContext());
        this.mViewPager = (PDSViewPager) findViewById(R.id.viewpager);
        this.savingProgress = (ProgressBar) findViewById(R.id.savingProgress);
        this.btnSave = (MaterialButton) findViewById(R.id.mPdfSave);
        this.fabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("ActivityAction");
        if (stringExtra.equals("FileSearch")) {
            performFileSearch();
        } else if (stringExtra.equals("PDFEdit")) {
            this.btnSave.setText("Update");
            this.is_Update = true;
            Uri parse = Uri.parse(intent.getStringExtra("PDFUri"));
            this.updateFileName = intent.getStringExtra("PDFName");
            OpenPDFViewer(parse);
        } else if (stringExtra.equals("ImageToPdf") || stringExtra.equals("selectPdf")) {
            OpenPDFViewer(Uri.fromFile(new File(intent.getStringExtra("filePath"))));
        } else if (stringExtra.equals("PDFOpen") && (parcelableArrayListExtra = intent.getParcelableArrayListExtra("PDFOpen")) != null) {
            for (int i = 0; i < parcelableArrayListExtra.size(); i++) {
                OpenPDFViewer((Uri) parcelableArrayListExtra.get(i));
            }
        }
    }

    @Override 
    public void setViewListener() {
        this.btnSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DigitalSignatureActivity.this.m59xd862851c(view);
            }
        });
        this.fabEdit.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DigitalSignatureActivity.this.m60x92d8259d(view);
            }
        });
    }

    
    
    public  void m59xd862851c(View view) {
        if (this.is_Update) {
            new PDSSaveAsPDFAsyncTask(this, this.updateFileName, true);
        } else {
            savePDFDocument();
        }
    }

    
    
    public  void m60x92d8259d(View view) {
        showPDFEditOption();
    }

    private void showPDFEditOption() {
        DocumentOptionBottomSheet newInstance = DocumentOptionBottomSheet.newInstance(new RecyclerItemClick() {
            @Override 
            public final void onItemClick(int i) {
                DigitalSignatureActivity.this.m66x72255fdc(i);
            }
        });
        newInstance.setCancelable(true);
        newInstance.show(getSupportFragmentManager(), "Document Option");
    }

    
    
    public  void m61xcdd93d57(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        byte[] byteArrayExtra = data.getByteArrayExtra("image_byteArray");
        addElementText(PDSElement.PDSElementType.PDSElementTypeImageSign, BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length), getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
    }

    
    
    public  void m62x884eddd8(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        byte[] byteArrayExtra = data.getByteArrayExtra("image_byteArray");
        addElementText(PDSElement.PDSElementType.PDSElementTypeImageSign, BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length), getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
    }

    
    
    public  void m63x42c47e59(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        byte[] byteArrayExtra = data.getByteArrayExtra("image_byteArray");
        addElementText(PDSElement.PDSElementType.PDSElementTypeImageStamps, BitmapFactory.decodeByteArray(byteArrayExtra, 0, byteArrayExtra.length), getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
    }

    
    
    public  void m64xfd3a1eda(Bitmap bitmap) {
        addElementText(PDSElement.PDSElementType.PDSElementTypeImageIcon, bitmap, getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
    }

    
    
    public  void m66x72255fdc(int i) {
        if (i == 100) {
            this.activityLauncher.launch(new Intent(getApplicationContext(), AddTextActivity.class), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m61xcdd93d57((ActivityResult) obj);
                }
            });
        } else if (i == 111) {
            showSignatureCollection();
        } else if (i == 222) {
            this.activityLauncher.launch(new Intent(getApplicationContext(), DateFormatterActivity.class), new BetterActivityResult.OnActivityResult() { 
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m62x884eddd8((ActivityResult) obj);
                }
            });
        } else if (i == 333) {
            this.activityLauncher.launch(new Intent(getApplicationContext(), StampsActivity.class), new BetterActivityResult.OnActivityResult() { 
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m63x42c47e59((ActivityResult) obj);
                }
            });
        } else if (i == 444) {
            IconBottomSheet newInstance = IconBottomSheet.newInstance(new BottomSheetItemClick() {
                @Override 
                public final void onItemClick(Bitmap bitmap) {
                    DigitalSignatureActivity.this.m64xfd3a1eda(bitmap);
                }
            });
            newInstance.setCancelable(true);
            newInstance.show(getSupportFragmentManager(), "Icon");
        } else if (i != 555) {
        } else {
            this.activityLauncher.launch(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), new BetterActivityResult.OnActivityResult() { 
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m65xb7afbf5b((ActivityResult) obj);
                }
            });
        }
    }

    
    
    public  void m65xb7afbf5b(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            m57x11fe8230(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("IOException", "" + e.getMessage());
        }
    }

    public void showSignatureCollection() {
        SignatureBottomSheet newInstance = SignatureBottomSheet.newInstance(new RecyclerItemClick() {
            @Override 
            public final void onItemClick(int i) {
                DigitalSignatureActivity.this.m67xf1d7cbd9(i);
            }
        });
        newInstance.setCancelable(true);
        newInstance.show(getSupportFragmentManager(), "Signature Collection");
    }

    
    
    public  void m68x987268e2(ActivityResult activityResult) {
        Intent data;
        Bitmap decodeFile;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null || (decodeFile = BitmapFactory.decodeFile(new File(data.getStringExtra("FileName")).getPath())) == null) {
            return;
        }
        addElement(PDSElement.PDSElementType.PDSElementTypeImageSign, decodeFile, getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
    }

    
    
    public  void m67xf1d7cbd9(int i) {
        if (i == 555) {
            this.activityLauncher.launch(new Intent(getApplicationContext(), SignatureActivity.class), new BetterActivityResult.OnActivityResult() {
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m68x987268e2((ActivityResult) obj);
                }
            });
        } else if (i != 666) {
        } else {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.setType("image/jpeg");
            intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"image/jpeg", "image/png"});
            this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult() { 
                @Override 
                public final void onActivityResult(Object obj) {
                    DigitalSignatureActivity.this.m69x52e80963((ActivityResult) obj);
                }
            });
        }
    }

    
    
    public  void m69x52e80963(ActivityResult activityResult) {
        Intent data;
        if (activityResult.getResultCode() != -1 || (data = activityResult.getData()) == null) {
            return;
        }
        try {
            InputStream openInputStream = getContentResolver().openInputStream(data.getData());
            Bitmap decodeStream = BitmapFactory.decodeStream(openInputStream);
            openInputStream.close();
            if (decodeStream != null) {
                addElement(PDSElement.PDSElementType.PDSElementTypeImageSign, decodeStream, getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void OpenPDFViewer(Uri uri) {
        try {
            this.currentPdfKey = buildPdfPositionKey(uri);
            PDSPDFDocument pDSPDFDocument = new PDSPDFDocument(this, uri);
            pDSPDFDocument.open();
            this.mDocument = pDSPDFDocument;
            this.imageAdapter = new PDSPageAdapter(getSupportFragmentManager(), pDSPDFDocument);
            this.mViewPager.setAdapter(this.imageAdapter);
            int restoredPageIndex = getSavedPageIndex(pDSPDFDocument.getNumPages());
            this.mViewPager.setCurrentItem(restoredPageIndex, false);
            updatePageNumber(restoredPageIndex + 1);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Cannot open PDF, either PDF is corrupted or password protected");
            finish();
        }
    }

    private String buildPdfPositionKey(Uri uri) {
        return "pdf_page_" + uri.toString();
    }

    private int getSavedPageIndex(int pageCount) {
        if (this.currentPdfKey == null || pageCount <= 0) {
            return 0;
        }
        int savedPage = getReaderPreferences().getInt(this.currentPdfKey, 1);
        if (savedPage < 1) {
            savedPage = 1;
        } else if (savedPage > pageCount) {
            savedPage = pageCount;
        }
        return savedPage - 1;
    }

    private void saveCurrentPage(int pageNumber) {
        if (this.currentPdfKey == null || pageNumber < 1) {
            return;
        }
        getReaderPreferences().edit().putInt(this.currentPdfKey, pageNumber).apply();
    }

    private SharedPreferences getReaderPreferences() {
        return getSharedPreferences(READER_PREFS, MODE_PRIVATE);
    }

    public void performFileSearch() {
        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
        intent.setType("image/jpeg");
        intent.putExtra("android.intent.extra.MIME_TYPES", new String[]{"application/pdf"});
        this.activityLauncher.launch(intent, new BetterActivityResult.OnActivityResult<ActivityResult>() { 
            @Override 
            public void onActivityResult(ActivityResult activityResult) {
                if (activityResult.getResultCode() == -1) {
                    Intent data = activityResult.getData();
                    if (data != null) {
                        DigitalSignatureActivity.this.pdfData = data.getData();
                        DigitalSignatureActivity digitalSignatureActivity = DigitalSignatureActivity.this;
                        digitalSignatureActivity.OpenPDFViewer(digitalSignatureActivity.pdfData);
                        return;
                    }
                    return;
                }
                DigitalSignatureActivity.this.finish();
            }
        });
    }

    private int computeVisibleWindowHtForNonFullScreenMode() {
        return findViewById(R.id.docviewer).getHeight();
    }

    public boolean isFirstTap() {
        return this.mFirstTap;
    }

    public void setFirstTap(boolean z) {
        this.mFirstTap = z;
    }

    public int getVisibleWindowHeight() {
        if (this.mVisibleWindowHt == 0) {
            this.mVisibleWindowHt = computeVisibleWindowHtForNonFullScreenMode();
        }
        return this.mVisibleWindowHt;
    }

    public PDSPDFDocument getDocument() {
        return this.mDocument;
    }

    public void invokeMenuButton(boolean z) {
        if (z) {
            this.elementActive++;
        } else {
            this.elementActive--;
        }
    }

    public void addElement(PDSElement.PDSElementType pDSElementType, File file, float f, float f2) {
        View focusedChild = this.mViewPager.getFocusedChild();
        if (focusedChild != null) {
            PDSPageViewer pDSPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
            if (pDSPageViewer != null) {
                RectF visibleRect = pDSPageViewer.getVisibleRect();
                pDSPageViewer.createElement(pDSElementType, file, (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f), (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f), f, f2);
            }
            this.isSigned = true;
            invokeMenuButton(true);
            return;
        }
        Toast.makeText(this.mContext, "Focused not set", Toast.LENGTH_SHORT).show();
    }

    public void addElement(PDSElement.PDSElementType pDSElementType, Bitmap bitmap, float f, float f2) {
        View focusedChild = this.mViewPager.getFocusedChild();
        if (focusedChild == null || bitmap == null) {
            return;
        }

        Log.e("bitmapbitmap",""+bitmap.getWidth());
        Log.e("bitmapbitmap",""+bitmap.getHeight());
        PDSPageViewer pDSPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
        if (pDSPageViewer != null) {
            RectF visibleRect = pDSPageViewer.getVisibleRect();
            Log.e("yessss","ssssssss");
            pDSPageViewer.createElement(pDSElementType, bitmap, (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f), (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f), f, f2);
        }
        this.isSigned = true;
        invokeMenuButton(true);
    }

    private void addElementText(PDSElement.PDSElementType pDSElementType, Bitmap bitmap, float f, float f2) {
        View focusedChild = this.mViewPager.getFocusedChild();
        if (focusedChild == null || bitmap == null) {
            return;
        }
        PDSPageViewer pDSPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0);
        if (pDSPageViewer != null) {
            RectF visibleRect = pDSPageViewer.getVisibleRect();
            pDSPageViewer.createElement(pDSElementType, bitmap, (visibleRect.left + (visibleRect.width() / 2.0f)) - (f / 2.0f), (visibleRect.top + (visibleRect.height() / 2.0f)) - (f2 / 2.0f), f, f2);
        }
        this.isSigned = true;
        invokeMenuButton(true);
    }

    public void updatePageNumber(int i) {
        saveCurrentPage(i);
        findViewById(R.id.pageNumberOverlay).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.pageNumberTxt)).setText(i + "/" + this.mDocument.getNumPages());
        resetTimerHandlerForPageNumber(1000);
    }

    private void resetTimerHandlerForPageNumber(int i) {
        this.mUIElemsHandler.removeMessages(1);
        Message message = new Message();
        message.what = 1;
        this.mUIElemsHandler.sendMessageDelayed(message, i);
    }

    
    public void fadePageNumberOverlay() {
        Animation loadAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        View findViewById = findViewById(R.id.pageNumberOverlay);
        if (findViewById.getVisibility() == View.VISIBLE) {
            findViewById.startAnimation(loadAnimation);
            findViewById.setVisibility(View.INVISIBLE);
        }
    }

    
    
    public static class UIElementsHandler extends Handler {
        private final WeakReference<DigitalSignatureActivity> mActivity;

        public UIElementsHandler(DigitalSignatureActivity digitalSignatureActivity) {
            this.mActivity = new WeakReference<>(digitalSignatureActivity);
        }

        @Override 
        public void handleMessage(Message message) {
            DigitalSignatureActivity digitalSignatureActivity = this.mActivity.get();
            if (digitalSignatureActivity != null && message.what == 1) {
                digitalSignatureActivity.fadePageNumberOverlay();
            }
            super.handleMessage(message);
        }
    }

    public void runPostExecution(String str, Disposable disposable) {
        this.savingProgress.setVisibility(View.INVISIBLE);
        if (disposable != null) {
            disposable.dispose();
        }
        makeResult(str);
    }

    public void makeResult(final String str) {

                Intent intent = new Intent();
                intent.putExtra("NewPDFPath", str);
                DigitalSignatureActivity.this.setResult(-1, intent);
                DigitalSignatureActivity.this.finish();

    }

    public void savePDFDocument() {
        final FileAlertDialogBinding fileAlertDialogBinding = (FileAlertDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.file_alert_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(fileAlertDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        fileAlertDialogBinding.txtHeading.setText("Enter PDF File Name");
        fileAlertDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DigitalSignatureActivity.this.m58x4b67f43(fileAlertDialogBinding, dialog, view);
            }
        });
        fileAlertDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    
    
    public  void m58x4b67f43(final FileAlertDialogBinding fileAlertDialogBinding, final Dialog dialog, View view) {

                try {
                    String obj = fileAlertDialogBinding.etFileName.getText().toString();
                    if (obj.length() == 0) {
                        DigitalSignatureActivity.this.showToast("File name should not be empty");
                        return;
                    }
                    SplashActivity.isRate = true;
                    DigitalSignatureActivity digitalSignatureActivity = DigitalSignatureActivity.this;
                    new PDSSaveAsPDFAsyncTask(digitalSignatureActivity, obj + ".pdf", false);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

    }

    
    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
    }

    
    
    public  void m57x11fe8230(Bitmap bitmap) {
        if (bitmap != null) {
            try {
                addElement(PDSElement.PDSElementType.PDSElementTypeImage, bitmap, getResources().getDimension(R.dimen.sign_field_default_height), getResources().getDimension(R.dimen.sign_field_default_height));
            } catch (Exception e) {
                e.printStackTrace();

                Log.e("ExceptionException",""+e.getMessage());
            }
        }
    }

    @Override 
    public void onBackPressed() {
        if (this.elementActive > 0) {
            new AlertDialog.Builder(this).setTitle("Save Document").setMessage("Want to save your changes to PDF document?").setPositiveButton("Save", new DialogInterface.OnClickListener() { 
                @Override 
                public void onClick(DialogInterface dialogInterface, int i) {
                    DigitalSignatureActivity.this.savePDFDocument();
                }
            }).setNegativeButton("Exit", new DialogInterface.OnClickListener() { 
                @Override 
                public void onClick(DialogInterface dialogInterface, int i) {
                    DigitalSignatureActivity.this.finish();
                }
            }).show();
        } else {
            finish();
        }
    }
}
