package com.simplepdf.pdfeditor.Document;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.SizeF;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.RelativeLayout;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import com.simplepdf.pdfeditor.Activity.DigitalSignatureActivity;
import com.simplepdf.pdfeditor.PDF.PDSPDFPage;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.Signature.SignatureView;
import com.simplepdf.pdfeditor.model.PDSElement;
import com.simplepdf.pdfeditor.util.PDSSignatureUtils;
import com.simplepdf.pdfeditor.util.ViewUtils;

import java.io.File;
import java.util.Observable;
import java.util.Observer;


public class PDSPageViewer extends FrameLayout implements Observer {
    private static final int DRAG_SHADOW_OPACITY = 180;
    DigitalSignatureActivity activity;
    private float mBitmapScale;
    private final Context mContext;
    private ImageView mDragShadowView;
    private boolean mElementAlreadyPresentOnTap;
    private View mElementCreationMenu;
    private View mElementPropMenu;
    private PointF mFocus;
    private GestureDetector mGestureDetector;
    private Bitmap mImage;
    private RectF mImageContentRect;
    private final ImageView mImageView;
    private final LayoutInflater mInflater;
    SizeF mInitialImageSize;
    PDSRenderPageAsyncTask mInitialRenderingTask;
    private float mInterceptedDownX;
    private float mInterceptedDownY;
    private boolean mIsFirstScrollAfterIntercept;
    private boolean mIsInterceptedScrolling;
    private int mKeyboardHeight;
    private boolean mKeyboardShown;
    private float mLastDragPointX;
    private float mLastDragPointY;
    private PDSElementViewer mLastFocusedElementViewer;
    private long mLastZoomTime;
    private int mMaxScrollX;
    private int mMaxScrollY;
    private PDSPDFPage mPage;
    private RelativeLayout mPageView;
    private final LinearLayout mProgressView;
    private PDSRenderPageAsyncTask mRenderPageTask;
    private boolean mRenderPageTaskPending;
    private boolean mRenderingComplete;
    private boolean mResizeInOperation;
    private float mScaleFactor;
    private ScaleGestureDetector mScaleGestureDetector;
    private final ScaleGestureListener mScaleGestureListener;
    private PointF mScroll;
    private RelativeLayout mScrollView;
    private OverScroller mScroller;
    private float mStartScaleFactor;
    private Matrix mToPDFCoordinatesMatrix;
    private Matrix mToViewCoordinatesMatrix;
    private float mTouchSlop;
    private float mTouchX;
    private float mTouchY;

    @Override 
    public void update(Observable observable, Object obj) {
    }

    public PDSPageViewer(Context context, DigitalSignatureActivity digitalSignatureActivity, PDSPDFPage pDSPDFPage) {
        super(context);
        this.mScaleGestureDetector = null;
        this.mGestureDetector = null;
        this.mInitialRenderingTask = null;
        this.mRenderPageTask = null;
        this.mScaleFactor = 1.0f;
        this.mScroll = new PointF(0.0f, 0.0f);
        this.mFocus = new PointF(0.0f, 0.0f);
        this.mStartScaleFactor = 1.0f;
        this.mMaxScrollX = 0;
        this.mMaxScrollY = 0;
        this.mPageView = null;
        this.mScrollView = null;
        this.mScroller = null;
        this.mLastZoomTime = 0L;
        this.mIsFirstScrollAfterIntercept = false;
        this.mIsInterceptedScrolling = false;
        this.mKeyboardHeight = 0;
        this.mKeyboardShown = false;
        this.mResizeInOperation = false;
        this.mRenderPageTaskPending = false;
        this.mBitmapScale = 1.0f;
        this.mInitialImageSize = null;
        this.mImage = null;
        this.mImageContentRect = null;
        this.mToPDFCoordinatesMatrix = null;
        this.mRenderingComplete = false;
        this.mToViewCoordinatesMatrix = null;
        this.mInterceptedDownX = 0.0f;
        this.mInterceptedDownY = 0.0f;
        this.mTouchSlop = 0.0f;
        this.mLastDragPointX = -1.0f;
        this.mLastDragPointY = -1.0f;
        this.mElementPropMenu = null;
        this.mLastFocusedElementViewer = null;
        this.mElementCreationMenu = null;
        this.mTouchX = 0.0f;
        this.mTouchY = 0.0f;
        this.mDragShadowView = null;
        this.activity = null;
        Log.d("TAG", "PDSPageViewer: Constructor");
        this.mContext = context;
        this.activity = digitalSignatureActivity;
        this.mPage = pDSPDFPage;
        pDSPDFPage.setPageViewer(this);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mInflater = layoutInflater;
        View inflate = layoutInflater.inflate(R.layout.pdfviewer, (ViewGroup) null, false);
        addView(inflate);
        this.mScrollView = (RelativeLayout) inflate.findViewById(R.id.scrollview);
        this.mPageView = (RelativeLayout) inflate.findViewById(R.id.pageview);
        this.mImageView = (ImageView) inflate.findViewById(R.id.imageview);
        setHorizontalScrollBarEnabled(true);
        setVerticalScrollBarEnabled(true);
        setScrollbarFadingEnabled(true);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linlaProgress);
        this.mProgressView = linearLayout;
        linearLayout.setVisibility(View.VISIBLE);
        this.mScroller = new OverScroller(getContext());
        ScaleGestureListener scaleGestureListener = new ScaleGestureListener(this, this);
        this.mScaleGestureListener = scaleGestureListener;
        this.mScaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        GestureDetector gestureDetector = new GestureDetector(context, new GestureListener(this, this));
        this.mGestureDetector = gestureDetector;
        gestureDetector.setIsLongpressEnabled(true);
        requestFocus();
    }

    
    public void attachListeners() {
        setOnTouchListener(new OnTouchListener() { 
            @Override 
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean z = PDSPageViewer.this.mGestureDetector.onTouchEvent(motionEvent) || PDSPageViewer.this.mScaleGestureDetector.onTouchEvent(motionEvent);
                if (!z && motionEvent.getAction() != 1) {
                    motionEvent.getAction();
                }
                return z;
            }
        });
        this.mPageView.setOnDragListener(new OnDragListener() { 
            @Override 
            public boolean onDrag(View view, DragEvent dragEvent) {
                int action = dragEvent.getAction();
                if (action == 1) {
                    PDSPageViewer.this.mLastDragPointX = -1.0f;
                    PDSPageViewer.this.mLastDragPointY = -1.0f;
                } else if (action == 2) {
                    PDSPageViewer.this.handleDragMove(dragEvent);
                } else if (action == 3) {
                    PDSPageViewer.this.mLastDragPointX = dragEvent.getX();
                    PDSPageViewer.this.mLastDragPointY = dragEvent.getY();
                } else if (action == 4) {
                    PDSPageViewer.this.handleDragEnd(dragEvent);
                } else if (action == 5) {
                    PDSPageViewer.this.mLastDragPointX = -1.0f;
                    PDSPageViewer.this.mLastDragPointY = -1.0f;
                }
                return true;
            }
        });
    }

    
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private GestureListener() {
        }

        GestureListener(PDSPageViewer pDSPageViewer, PDSPageViewer pDSPageViewer2) {
            this();
        }

        @Override 
        public boolean onDown(MotionEvent motionEvent) {
            PDSPageViewer.this.mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(PDSPageViewer.this);
            return true;
        }

        @Override 
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (motionEvent2.getPointerCount() > 1 || SystemClock.elapsedRealtime() - PDSPageViewer.this.mLastZoomTime < 200) {
                return false;
            }
            PDSPageViewer.this.mScroller.abortAnimation();
            PDSPageViewer.this.mScroller.fling(PDSPageViewer.this.mScrollView.getScrollX(), PDSPageViewer.this.mScrollView.getScrollY(), ((int) (-f)) * 2, ((int) (-f2)) * 2, 0, PDSPageViewer.this.getMaxScrollX(), 0, PDSPageViewer.this.getMaxScrollY());
            ViewCompat.postInvalidateOnAnimation(PDSPageViewer.this);
            return true;
        }

        @Override 
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (PDSPageViewer.this.mIsInterceptedScrolling && PDSPageViewer.this.mIsFirstScrollAfterIntercept) {
                PDSPageViewer.this.mIsFirstScrollAfterIntercept = false;
                return false;
            } else if (motionEvent2.getPointerCount() > 1 || SystemClock.elapsedRealtime() - PDSPageViewer.this.mLastZoomTime < 200) {
                return false;
            } else {
                PDSPageViewer pDSPageViewer = PDSPageViewer.this;
                pDSPageViewer.applyScroll(pDSPageViewer.mScrollView.getScrollX() + Math.round(f), PDSPageViewer.this.mScrollView.getScrollY() + Math.round(f2));
                return true;
            }
        }

        @Override 
        public void onLongPress(MotionEvent motionEvent) {
            super.onLongPress(motionEvent);
            PDSPageViewer.this.onTap(motionEvent, true);
        }

        @Override 
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            super.onSingleTapUp(motionEvent);
            PDSPageViewer.this.mElementAlreadyPresentOnTap = false;
            PDSPageViewer.this.onTap(motionEvent, false);
            return true;
        }
    }

    
    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ScaleGestureListener() {
        }

        ScaleGestureListener(PDSPageViewer pDSPageViewer, PDSPageViewer pDSPageViewer2) {
            this();
        }

        @Override 
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return PDSPageViewer.this.scaleBegin(scaleGestureDetector.getFocusX(), scaleGestureDetector.getFocusY());
        }

        @Override 
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return PDSPageViewer.this.scale(scaleGestureDetector.getScaleFactor());
        }

        @Override 
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            PDSPageViewer.this.scaleEnd();
        }

        public void resetScale() {
            PDSPageViewer.this.mFocus = new PointF(0.0f, 0.0f);
            PDSPageViewer.this.mScroll = new PointF(0.0f, 0.0f);
            PDSPageViewer.this.mStartScaleFactor = 1.0f;
            PDSPageViewer.this.mScaleFactor = 1.0f;
            PDSPageViewer.this.mMaxScrollX = 0;
            PDSPageViewer.this.mMaxScrollY = 0;
            PDSPageViewer.this.mPageView.setScaleX(PDSPageViewer.this.mScaleFactor);
            PDSPageViewer.this.mPageView.setScaleY(PDSPageViewer.this.mScaleFactor);
            PDSPageViewer.this.mScrollView.scrollTo(0, 0);
            PDSPageViewer.this.updateImageFoScale();
        }
    }

    public void resetScale() {
        this.mScaleGestureListener.resetScale();
    }

    
    public boolean scaleBegin(float f, float f2) {
        this.mPageView.setPivotX(0.0f);
        this.mPageView.setPivotY(0.0f);
        this.mFocus.set(f + this.mScrollView.getScrollX(), f2 + this.mScrollView.getScrollY());
        this.mScroll.set(this.mScrollView.getScrollX(), this.mScrollView.getScrollY());
        this.mStartScaleFactor = this.mScaleFactor;
        if (this.mLastFocusedElementViewer != null) {
            View view = this.mElementPropMenu;
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            } else {
                View view2 = this.mElementCreationMenu;
                if (view2 != null) {
                    view2.setVisibility(View.INVISIBLE);
                }
            }
            this.mLastFocusedElementViewer.hideBorder();
            return true;
        }
        View view3 = this.mElementCreationMenu;
        if (view3 != null) {
            view3.setVisibility(View.INVISIBLE);
            return true;
        }
        return true;
    }

    
    public boolean scale(float f) {
        float f2 = ((this.mScaleFactor * ((f / 10000.0f) * 10000.0f)) / 10000.0f) * 10000.0f;
        this.mScaleFactor = f2;
        this.mScaleFactor = Math.max(1.0f, Math.min(f2, 3.0f));
        this.mMaxScrollX = Math.round(this.mScrollView.getWidth() * (this.mScaleFactor - 1.0f));
        this.mMaxScrollY = Math.round(this.mScrollView.getHeight() * (this.mScaleFactor - 1.0f));
        int round = Math.round((this.mFocus.x * ((this.mScaleFactor / this.mStartScaleFactor) - 1.0f)) + this.mScroll.x);
        int round2 = Math.round((this.mFocus.y * ((this.mScaleFactor / this.mStartScaleFactor) - 1.0f)) + this.mScroll.y);
        int max = Math.max(0, Math.min(round, getMaxScrollX()));
        int max2 = Math.max(0, Math.min(round2, getMaxScrollY()));
        this.mPageView.setScaleX(this.mScaleFactor);
        this.mPageView.setScaleY(this.mScaleFactor);
        this.mScrollView.scrollTo(max, max2);
        invalidate();
        return true;
    }

    
    public void scaleEnd() {
        this.mLastZoomTime = SystemClock.elapsedRealtime();
        updateImageFoScale();
        PDSElementViewer pDSElementViewer = this.mLastFocusedElementViewer;
        if (pDSElementViewer == null || this.mElementPropMenu == null) {
            return;
        }
        showElementPropMenu(pDSElementViewer);
    }

    
    public void applyScroll(int i, int i2) {
        this.mScrollView.scrollTo(Math.max(0, Math.min(i, getMaxScrollX())), Math.max(0, Math.min(i2, getMaxScrollY())));
    }

    
    public int getMaxScrollY() {
        int i = this.mMaxScrollY;
        return this.mKeyboardShown ? i + this.mKeyboardHeight : i;
    }

    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        if (this.mResizeInOperation && this.mScaleFactor != 1.0f) {
            if (this.mLastFocusedElementViewer != null) {
                Rect rect = new Rect();
                this.mLastFocusedElementViewer.getContainerView().getHitRect(rect);
                if (rect.contains((int) ((motionEvent.getX() + this.mScrollView.getScrollX()) / this.mScaleFactor), (int) ((motionEvent.getY() + this.mScrollView.getScrollY()) / this.mScaleFactor))) {
                    return false;
                }
            }
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        if (!this.mIsInterceptedScrolling) {
                            float abs = Math.abs(motionEvent.getX() - this.mInterceptedDownX);
                            float abs2 = Math.abs(motionEvent.getY() - this.mInterceptedDownY);
                            float f = this.mTouchSlop;
                            if (abs > f || abs2 > f) {
                                this.mIsInterceptedScrolling = true;
                                this.mIsFirstScrollAfterIntercept = true;
                            }
                        }
                    }
                }
                this.mIsInterceptedScrolling = false;
            } else {
                this.mIsInterceptedScrolling = false;
                this.mInterceptedDownX = motionEvent.getX();
                this.mInterceptedDownY = motionEvent.getY();
                this.mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            }
        }
        return false;
    }

    
    public void onTap(MotionEvent motionEvent, boolean z) {
        manualScale(motionEvent.getX(), motionEvent.getY());
        float x = (motionEvent.getX() + this.mScrollView.getScrollX()) / this.mScaleFactor;
        float y = (motionEvent.getY() + this.mScrollView.getScrollY()) / this.mScaleFactor;
        if (!getImageContentRect().contains(new RectF(x, y, getResources().getDimension(R.dimen.element_min_width) + x + (getResources().getDimension(R.dimen.element_horizontal_padding) * 2.0f), getResources().getDimension(R.dimen.element_min_width) + y + (getResources().getDimension(R.dimen.element_vertical_padding) * 2.0f)))) {
            if (PDSSignatureUtils.isSignatureMenuOpen()) {
                PDSSignatureUtils.dismissSignatureMenu();
            }
            removeFocus();
            return;
        }
        this.mTouchX = x;
        this.mTouchY = y;
        if (PDSSignatureUtils.isSignatureMenuOpen()) {
            PDSSignatureUtils.dismissSignatureMenu();
        } else if (z) {
            onLongTap(x, y);
        } else {
            onSingleTap(x, y);
        }
    }

    private void onLongTap(float f, float f2) {
        if (this.mLastFocusedElementViewer != null) {
            removeFocus();
        }
    }

    private void onSingleTap(float f, float f2) {
        if (this.mLastFocusedElementViewer != null) {
            removeFocus();
            hideElementCreationMenu();
        }
    }

    public void manualScale(float f, float f2) {
        if (this.mScaleFactor == 1.0f && getDocumentViewer().isFirstTap()) {
            getDocumentViewer().setFirstTap(false);
            scaleBegin(f, f2);
            scale(1.15f);
            scale(1.3f);
            scale(1.45f);
            scaleEnd();
        }
    }

    public DigitalSignatureActivity getDocumentViewer() {
        return (DigitalSignatureActivity) this.mContext;
    }

    
    public int getMaxScrollX() {
        return this.mMaxScrollX;
    }

    
    public void setImageBitmap(Bitmap bitmap) {
        Bitmap bitmap2 = this.mImage;
        if (bitmap2 != null) {
            bitmap2.recycle();
        }
        this.mImage = bitmap;
        this.mImageView.setImageBitmap(bitmap);
    }

    public RectF getImageContentRect() {
        return this.mImageContentRect;
    }

    @Override 
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!z || this.mImageView.getWidth() <= 0) {
            return;
        }
        initRendering();
    }

    @Override 
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        if (this.mImageView.getWidth() > 0) {
            initRendering();
        }
    }

    private void initRendering() {
        if (this.mImage != null || this.mImageView.getWidth() <= 0) {
            return;
        }
        PDSRenderPageAsyncTask pDSRenderPageAsyncTask = new PDSRenderPageAsyncTask(this.mContext, this.mPage, new SizeF(this.mImageView.getWidth(), this.mImageView.getHeight()), 1.0f, false, false, new PDSRenderPageAsyncTask.OnPostExecuteListener() { 
            @Override 
            public void onPostExecute(PDSRenderPageAsyncTask pDSRenderPageAsyncTask2, Bitmap bitmap) {
                if (bitmap != null && PDSPageViewer.this.mScaleFactor == 1.0f && pDSRenderPageAsyncTask2.getPage() == PDSPageViewer.this.mPage) {
                    int visibleWindowHeight = PDSPageViewer.this.getDocumentViewer().getVisibleWindowHeight();
                    if (visibleWindowHeight > 0) {
                        PDSPageViewer.this.mScrollView.setLayoutParams(new LayoutParams(-1, visibleWindowHeight));
                    }
                    PDSPageViewer.this.mInitialImageSize = pDSRenderPageAsyncTask2.getBitmapSize();
                    PDSPageViewer.this.computeImageContentRect();
                    PDSPageViewer.this.computeCoordinateConversionMatrices();
                    PDSPageViewer.this.setImageBitmap(bitmap);
                    PDSPageViewer.this.renderElements();
                    PDSPageViewer.this.mProgressView.setVisibility(View.INVISIBLE);
                    PDSPageViewer.this.attachListeners();
                } else if (bitmap != null) {
                    bitmap.recycle();
                }
                PDSPageViewer.this.mRenderingComplete = true;
            }
        });
        this.mInitialRenderingTask = pDSRenderPageAsyncTask;
        pDSRenderPageAsyncTask.getRenderPdfData();
    }

    
    public void renderElements() {
        for (int i = 0; i < this.mPage.getNumElements(); i++) {
            addElement(this.mPage.getElement(i));
        }
    }

    
    public void computeImageContentRect() {
        float f;
        float f2;
        float f3;
        float width = this.mInitialImageSize.getWidth() / this.mInitialImageSize.getHeight();
        float f4 = 0.0f;
        if (width >= this.mImageView.getWidth() / this.mImageView.getHeight()) {
            f2 = this.mImageView.getWidth();
            f3 = f2 / width;
            f = (this.mImageView.getHeight() - f3) / 2.0f;
        } else {
            float height = this.mImageView.getHeight();
            float f5 = width * height;
            f4 = (this.mImageView.getWidth() - f5) / 2.0f;
            f = 0.0f;
            f2 = f5;
            f3 = height;
        }
        this.mImageContentRect = new RectF(f4, f, f2 + f4, f3 + f);
    }

    
    public void computeCoordinateConversionMatrices() {
        SizeF pageSize = this.mPage.getPageSize();
        RectF imageContentRect = getImageContentRect();
        Matrix matrix = new Matrix();
        this.mToPDFCoordinatesMatrix = matrix;
        matrix.postTranslate(0.0f - imageContentRect.left, 0.0f - imageContentRect.top);
        this.mToPDFCoordinatesMatrix.postScale(pageSize.getWidth() / imageContentRect.width(), pageSize.getHeight() / imageContentRect.height());
        Matrix matrix2 = new Matrix();
        this.mToViewCoordinatesMatrix = matrix2;
        matrix2.postScale(imageContentRect.width() / pageSize.getWidth(), imageContentRect.height() / pageSize.getHeight());
        this.mToViewCoordinatesMatrix.postTranslate(imageContentRect.left, imageContentRect.top);
    }

    
    public synchronized void updateImageFoScale() {
        if (this.mScaleFactor != this.mBitmapScale) {
            PDSRenderPageAsyncTask pDSRenderPageAsyncTask = new PDSRenderPageAsyncTask(this.mContext, this.mPage, new SizeF(this.mImageView.getWidth(), this.mImageView.getHeight()), this.mScaleFactor, false, false, new PDSRenderPageAsyncTask.OnPostExecuteListener() { 
                @Override 
                public void onPostExecute(PDSRenderPageAsyncTask pDSRenderPageAsyncTask2, Bitmap bitmap) {
                    if (bitmap != null && PDSPageViewer.this.mScaleFactor == pDSRenderPageAsyncTask2.getScale()) {
                        PDSPageViewer.this.setImageBitmap(bitmap);
                        PDSPageViewer pDSPageViewer = PDSPageViewer.this;
                        pDSPageViewer.mBitmapScale = pDSPageViewer.mScaleFactor;
                    } else if (bitmap != null) {
                        bitmap.recycle();
                    }
                    if (PDSPageViewer.this.mRenderPageTaskPending) {
                        PDSPageViewer.this.mRenderPageTask = null;
                        PDSPageViewer.this.mRenderPageTaskPending = false;
                        PDSPageViewer.this.updateImageFoScale();
                    }
                }
            });
            this.mRenderPageTask = pDSRenderPageAsyncTask;
            pDSRenderPageAsyncTask.getRenderPdfData();
        }
    }

    public RectF getVisibleRect() {
        return new RectF(this.mScrollView.getScrollX() / this.mScaleFactor, this.mScrollView.getScrollY() / this.mScaleFactor, (this.mScrollView.getScrollX() + this.mPageView.getWidth()) / this.mScaleFactor, (this.mScrollView.getScrollY() + this.mPageView.getHeight()) / this.mScaleFactor);
    }

    public void cancelRendering() {
        PDSRenderPageAsyncTask pDSRenderPageAsyncTask = this.mInitialRenderingTask;
        if (pDSRenderPageAsyncTask != null) {
            pDSRenderPageAsyncTask.disposable.dispose();
        }
    }

    @Override 
    public void computeScroll() {
        if (this.mScroller.isFinished()) {
            return;
        }
        this.mScroller.computeScrollOffset();
        applyScroll(this.mScroller.getCurrX(), this.mScroller.getCurrY());
    }

    @Override 
    public int computeHorizontalScrollRange() {
        return Math.round(this.mScrollView.getWidth() * this.mScaleFactor) - 1;
    }

    @Override 
    public int computeVerticalScrollRange() {
        return Math.round(this.mScrollView.getHeight() * this.mScaleFactor) - 1;
    }

    @Override 
    public int computeHorizontalScrollOffset() {
        return this.mScrollView.getScrollX();
    }

    @Override 
    public int computeVerticalScrollOffset() {
        return this.mScrollView.getScrollY();
    }

    public float getScaleFactor() {
        return this.mScaleFactor;
    }

    public RelativeLayout getPageView() {
        return this.mPageView;
    }

    public PDSPDFPage getPage() {
        return this.mPage;
    }

    public void hideElementPropMenu() {
        View view = this.mElementPropMenu;
        if (view != null) {
            this.mScrollView.removeView(view);
            this.mElementPropMenu = null;
        }
        PDSElementViewer pDSElementViewer = this.mLastFocusedElementViewer;
        if (pDSElementViewer != null) {
            pDSElementViewer.hideBorder();
            this.mLastFocusedElementViewer = null;
        }
    }

    public PDSElementViewer getLastFocusedElementViewer() {
        return this.mLastFocusedElementViewer;
    }

    public PDSElement createElement(PDSElement.PDSElementType pDSElementType, File file, float f, float f2, float f3, float f4) {
        PDSElement pDSElement = new PDSElement(pDSElementType, file);
        float f5 = (int) f;
        float f6 = (int) f2;
        pDSElement.setRect(mapRectToPDFCoordinates(new RectF(f5, f6, f3 + f5, f4 + f6)));
        PDSElementViewer addElement = addElement(pDSElement);
        if (pDSElementType == PDSElement.PDSElementType.PDSElementTypeSignature) {
            addElement.getElementView().requestFocus();
        }
        return pDSElement;
    }

    public PDSElement createElement(PDSElement.PDSElementType pDSElementType, Bitmap bitmap, float f, float f2, float f3, float f4) {
        PDSElement pDSElement = new PDSElement(pDSElementType, bitmap);
        float f5 = (int) f;
        float f6 = (int) f2;
        pDSElement.setRect(mapRectToPDFCoordinates(new RectF(f5, f6, f3 + f5, f4 + f6)));
        addElement(pDSElement).getElementView().requestFocus();
        return pDSElement;
    }

    private PDSElementViewer addElement(PDSElement pDSElement) {
        return new PDSElementViewer(this.mContext, this, pDSElement);
    }

    public RectF mapRectToPDFCoordinates(RectF rectF) {
        this.mToPDFCoordinatesMatrix.mapRect(rectF);
        return rectF;
    }

    public float mapLengthToPDFCoordinates(float f) {
        return this.mToPDFCoordinatesMatrix.mapRadius(f);
    }

    public void setElementAlreadyPresentOnTap(boolean z) {
        this.mElementAlreadyPresentOnTap = z;
    }

    public void showElementPropMenu(final PDSElementViewer pDSElementViewer) {
        hideElementPropMenu();
        hideElementCreationMenu();
        pDSElementViewer.showBorder();
        this.mLastFocusedElementViewer = pDSElementViewer;
        View inflate = this.mInflater.inflate(R.layout.element_prop_menu_layout, (ViewGroup) null, false);
        inflate.setTag(pDSElementViewer);
        this.mScrollView.addView(inflate);
        this.mElementPropMenu = inflate;
        ((ImageButton) inflate.findViewById(R.id.delButton)).setOnClickListener(new OnClickListener() { 
            @Override 
            public void onClick(View view) {
                pDSElementViewer.removeElement();
                PDSPageViewer.this.activity.invokeMenuButton(false);
            }
        });
        setMenuPosition(pDSElementViewer.getContainerView(), inflate);
    }

    public void hideElementCreationMenu() {
        View view = this.mElementCreationMenu;
        if (view != null) {
            this.mScrollView.removeView(view);
            this.mElementCreationMenu = null;
        }
    }

    private void setMenuPosition(float f, float f2, View view, boolean z) {
        view.measure(0, 0);
        float f3 = this.mScaleFactor * f;
        if (z) {
            f3 -= view.getMeasuredWidth() / 2;
        }
        float dimension = (this.mScaleFactor * f2) - ((int) getResources().getDimension(R.dimen.menu_offset_y));
        RectF rectF = new RectF(f3, dimension, view.getMeasuredWidth() + f3, view.getMeasuredHeight() + dimension);
        RectF visibleRect = getVisibleRect();
        visibleRect.intersect(getImageContentRect());
        if (!visibleRect.contains(rectF)) {
            if (f3 > (visibleRect.right * this.mScaleFactor) - view.getMeasuredWidth()) {
                f3 = (visibleRect.right * this.mScaleFactor) - view.getMeasuredWidth();
            } else if (f3 < visibleRect.left * this.mScaleFactor && z) {
                f3 = visibleRect.left * this.mScaleFactor;
            }
            float f4 = visibleRect.top;
            float f5 = this.mScaleFactor;
            if (dimension < f4 * f5) {
                if (z) {
                    dimension = (f2 * f5) + ((int) getResources().getDimension(R.dimen.menu_offset_x));
                } else if (f3 > (visibleRect.left * this.mScaleFactor) + view.getMeasuredWidth() + ((int) getResources().getDimension(R.dimen.menu_offset_x))) {
                    f3 = ((f * this.mScaleFactor) - view.getMeasuredWidth()) - ((int) getResources().getDimension(R.dimen.menu_offset_x));
                    dimension = f2 * this.mScaleFactor;
                }
            }
        }
        view.setX(f3);
        view.setY(dimension);
    }

    private void setMenuPosition(View view, View view2) {
        setMenuPosition(view.getX(), view.getY(), view2, false);
    }

    public float mapLengthToViewCoordinates(float f) {
        return this.mToViewCoordinatesMatrix.mapRadius(f);
    }

    public Matrix getToViewCoordinatesMatrix() {
        return this.mToViewCoordinatesMatrix;
    }

    public void modifyElementSignatureSize(PDSElement pDSElement, View view, RelativeLayout relativeLayout, int i, int i2) {
        float f = i2;
        if (getImageContentRect().contains(new RectF(relativeLayout.getX(), relativeLayout.getY() - f, relativeLayout.getX() + relativeLayout.getWidth() + i, relativeLayout.getY() + relativeLayout.getHeight()))) {
            if (view instanceof SignatureView) {
                SignatureView signatureView = (SignatureView) view;
                signatureView.setLayoutParams(view.getWidth() + i, view.getHeight() + i2);
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() + i, relativeLayout.getHeight() + i2));
                relativeLayout.setY(relativeLayout.getY() - f);
                this.mPage.updateElement(pDSElement, mapRectToPDFCoordinates(new RectF((int) relativeLayout.getX(), (int) relativeLayout.getY(), (int) (relativeLayout.getX() + view.getWidth()), (int) (relativeLayout.getY() + view.getHeight()))), 0.0f, 0.0f, signatureView.getStrokeWidth(), 0.0f);
                setMenuPosition(relativeLayout, this.mElementPropMenu);
            } else if (view instanceof ImageView) {
                ((ImageView) view).setLayoutParams(new RelativeLayout.LayoutParams(view.getWidth() + i, view.getHeight() + i2));
                relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(relativeLayout.getWidth() + i, relativeLayout.getHeight() + i2));
                relativeLayout.setY(relativeLayout.getY() - f);
                this.mPage.updateElement(pDSElement, mapRectToPDFCoordinates(new RectF((int) relativeLayout.getX(), (int) relativeLayout.getY(), (int) (relativeLayout.getX() + view.getWidth()), (int) (relativeLayout.getY() + view.getHeight()))), 0.0f, 0.0f, 0.0f, 0.0f);
                setMenuPosition(relativeLayout, this.mElementPropMenu);
            }
        }
    }

    public void setResizeInOperation(boolean z) {
        this.mResizeInOperation = z;
    }

    public boolean getResizeInOperation() {
        return this.mResizeInOperation;
    }

    public void removeFocus() {
        if (Build.VERSION.SDK_INT < 28) {
            clearFocus();
        }
        hideElementPropMenu();
        hideElementCreationMenu();
        if (Build.VERSION.SDK_INT >= 28) {
            this.mImageView.requestFocus();
        }
    }

    
    public void handleDragMove(DragEvent dragEvent) {
        PDSElementViewer.DragEventData dragEventData = (PDSElementViewer.DragEventData) dragEvent.getLocalState();
        PDSElementViewer pDSElementViewer = dragEventData.viewer;
        View elementView = pDSElementViewer.getElementView();
        this.mLastDragPointX = dragEvent.getX();
        this.mLastDragPointY = dragEvent.getY();
        if (this.mDragShadowView == null) {
            hideDragElement(pDSElementViewer);
            pDSElementViewer.getElement();
            initDragShadow(elementView);
        }
        RectF rectF = new RectF(this.mLastDragPointX - dragEventData.x, this.mLastDragPointY - dragEventData.y, (elementView.getWidth() + this.mLastDragPointX) - dragEventData.x, (elementView.getHeight() + this.mLastDragPointY) - dragEventData.y);
        ViewUtils.constrainRectXY(rectF, getImageContentRect());
        this.mLastDragPointX = rectF.left + dragEventData.x;
        float f = rectF.top + dragEventData.y;
        this.mLastDragPointY = f;
        updateDragShadow(this.mLastDragPointX, f, dragEventData.x, dragEventData.y);
    }

    private void updateDragShadow(float f, float f2, float f3, float f4) {
        ImageView imageView = this.mDragShadowView;
        if (imageView != null) {
            imageView.setX(f - f3);
            this.mDragShadowView.setY(f2 - f4);
        }
    }

    
    public void handleDragEnd(DragEvent dragEvent) {
        if (this.mLastDragPointX == -1.0f && this.mLastDragPointY == -1.0f) {
            return;
        }
        PDSElementViewer.DragEventData dragEventData = (PDSElementViewer.DragEventData) dragEvent.getLocalState();
        PDSElementViewer pDSElementViewer = dragEventData.viewer;
        View elementView = pDSElementViewer.getElementView();
        float f = this.mLastDragPointX - dragEventData.x;
        float f2 = this.mLastDragPointY - dragEventData.y;
        elementView.getWidth();
        elementView.getHeight();
        float width = pDSElementViewer.getContainerView().getWidth();
        float height = pDSElementViewer.getContainerView().getHeight();
        RectF rectF = new RectF(f, f2, f + width, f2 + height);
        RectF imageContentRect = getImageContentRect();
        if (!imageContentRect.contains(rectF)) {
            if (f < imageContentRect.left) {
                f = imageContentRect.left;
            } else if (f > imageContentRect.right - width) {
                f = imageContentRect.right - width;
            }
            if (f2 < imageContentRect.top) {
                f2 = imageContentRect.top;
            } else if (f2 > imageContentRect.bottom - height) {
                f2 = imageContentRect.bottom - height;
            }
        }
        RelativeLayout containerView = pDSElementViewer.getContainerView();
        containerView.setX(f);
        containerView.setY(f2);
        containerView.setVisibility(View.VISIBLE);
        elementView.setVisibility(View.VISIBLE);
        RectF rectF2 = new RectF((int) f, (int) f2, (int) (f + elementView.getWidth()), (int) (f2 + elementView.getHeight()));
        mapRectToPDFCoordinates(rectF2);
        this.mPage.updateElement((PDSElement) elementView.getTag(), rectF2, 0.0f, 0.0f, 0.0f, 0.0f);
        showElementPropMenu(pDSElementViewer);
        releaseDragShadow();
    }

    private void hideDragElement(PDSElementViewer pDSElementViewer) {
        removeFocus();
        pDSElementViewer.showBorder();
        pDSElementViewer.getContainerView().setVisibility(View.INVISIBLE);
        pDSElementViewer.getElementView().setVisibility(View.INVISIBLE);
    }

    private void initDragShadow(View view) {
        if (this.mDragShadowView == null) {
            Bitmap createBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            view.draw(new Canvas(createBitmap));
            ImageView imageView = new ImageView(this.mContext);
            this.mDragShadowView = imageView;
            imageView.setImageBitmap(createBitmap);
            this.mDragShadowView.setImageAlpha(DRAG_SHADOW_OPACITY);
            this.mDragShadowView.setLayoutParams(new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight()));
            this.mPageView.addView(this.mDragShadowView);
        }
    }

    private void releaseDragShadow() {
        ImageView imageView = this.mDragShadowView;
        if (imageView != null) {
            imageView.setVisibility(View.INVISIBLE);
            this.mPageView.removeView(this.mDragShadowView);
            this.mDragShadowView = null;
        }
    }
}
