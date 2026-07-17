package com.simplepdf.pdfeditor.Signature;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;

import java.util.ArrayList;

public class SignatureView extends RelativeLayout {
    public static final float DEFAULT_STROKE_WIDTH = 3.0f;
    private static final double EPSILON_FOR_DOUBLE_COMPARISON = 0.001d;
    public static final float STROKE_WIDTH_MINIMUM = 2.0f;
    private static final float TOUCH_TOLERANCE = 0.1f;
    public int mActualColor;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;
    private Canvas mCanvas;
    private ArrayList<Float> mGestureInkList;
    public ArrayList<ArrayList<Float>> mInkList;
    private boolean mIsEditable;
    private boolean mIsFirstBoundingRect;
    private int mLayoutHeight;
    private int mLayoutWidth;
    private Path mPath;
    private float mQuadEndPointX;
    private float mQuadEndPointY;
    public float mRectBottom;
    public float mRectLeft;
    public float mRectRight;
    public float mRectTop;
    public ArrayList<ArrayList<Float>> mRedoInkList;
    private boolean mSignatureCreationMode;
    public int mStrokeColor;
    public float mStrokeWidthInDocSpace;
    private float mTouchDownPointX;
    private float mTouchDownPointY;
    private float mX;
    private float mY;

    public SignatureView(Context context) {
        super(context);
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mQuadEndPointX = 0.0f;
        this.mQuadEndPointY = 0.0f;
        this.mTouchDownPointX = 0.0f;
        this.mTouchDownPointY = 0.0f;
        this.mStrokeWidthInDocSpace = 0.0f;
        this.mStrokeColor = 0;
        this.mIsFirstBoundingRect = true;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mBitmap = null;
        this.mCanvas = null;
        this.mPath = null;
        this.mBitmapPaint = null;
        this.mGestureInkList = null;
        this.mInkList = null;
        this.mRedoInkList = null;
        this.mSignatureCreationMode = true;
        this.mIsEditable = false;
        this.mLayoutHeight = -1;
        this.mLayoutWidth = -1;
        initializeOverlayView();
    }

    public SignatureView(Context context, int i, int i2) {
        super(context);
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mQuadEndPointX = 0.0f;
        this.mQuadEndPointY = 0.0f;
        this.mTouchDownPointX = 0.0f;
        this.mTouchDownPointY = 0.0f;
        this.mStrokeWidthInDocSpace = 0.0f;
        this.mStrokeColor = 0;
        this.mIsFirstBoundingRect = true;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mBitmap = null;
        this.mCanvas = null;
        this.mPath = null;
        this.mBitmapPaint = null;
        this.mGestureInkList = null;
        this.mInkList = null;
        this.mRedoInkList = null;
        this.mIsEditable = false;
        this.mSignatureCreationMode = false;
        this.mLayoutHeight = i2;
        this.mLayoutWidth = i;
        initializeOverlayView();
    }

    public SignatureView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mQuadEndPointX = 0.0f;
        this.mQuadEndPointY = 0.0f;
        this.mTouchDownPointX = 0.0f;
        this.mTouchDownPointY = 0.0f;
        this.mStrokeWidthInDocSpace = 0.0f;
        this.mStrokeColor = 0;
        this.mIsFirstBoundingRect = true;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mBitmap = null;
        this.mCanvas = null;
        this.mPath = null;
        this.mBitmapPaint = null;
        this.mGestureInkList = null;
        this.mInkList = null;
        this.mRedoInkList = null;
        this.mSignatureCreationMode = true;
        this.mIsEditable = false;
        this.mLayoutHeight = -1;
        this.mLayoutWidth = -1;
        initializeOverlayView();
    }

    public SignatureView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mQuadEndPointX = 0.0f;
        this.mQuadEndPointY = 0.0f;
        this.mTouchDownPointX = 0.0f;
        this.mTouchDownPointY = 0.0f;
        this.mStrokeWidthInDocSpace = 0.0f;
        this.mStrokeColor = 0;
        this.mIsFirstBoundingRect = true;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mBitmap = null;
        this.mCanvas = null;
        this.mPath = null;
        this.mBitmapPaint = null;
        this.mGestureInkList = null;
        this.mInkList = null;
        this.mRedoInkList = null;
        this.mSignatureCreationMode = true;
        this.mIsEditable = false;
        this.mLayoutHeight = -1;
        this.mLayoutWidth = -1;
        initializeOverlayView();
    }

    public int getSignatureViewWidth() {
        return this.mLayoutWidth;
    }

    public int getSignatureViewHeight() {
        return this.mLayoutHeight;
    }

    public void initializeOverlayView() {
        setWillNotDraw(false);
        this.mStrokeWidthInDocSpace = 6.0f;
        this.mStrokeColor = ViewCompat.MEASURED_STATE_MASK;
        this.mPath = new Path();
        Paint paint = new Paint();
        this.mBitmapPaint = paint;
        paint.setAntiAlias(true);
        this.mBitmapPaint.setDither(true);
        this.mBitmapPaint.setColor(this.mStrokeColor);
        this.mBitmapPaint.setStyle(Paint.Style.STROKE);
        this.mBitmapPaint.setStrokeJoin(Paint.Join.ROUND);
        this.mBitmapPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mBitmapPaint.setStrokeWidth(this.mStrokeWidthInDocSpace);
        this.mInkList = new ArrayList<>();
        this.mRedoInkList = new ArrayList<>();
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mTouchDownPointX = 0.0f;
        this.mTouchDownPointY = 0.0f;
        this.mIsFirstBoundingRect = true;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mIsEditable = false;
    }

    public void initializeInkList(ArrayList<ArrayList<Float>> arrayList) {
        this.mInkList = arrayList;
    }

    public void scaleAndTranslatePath(ArrayList<ArrayList<Float>> arrayList, RectF rectF, float f, float f2, float f3, float f4) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ArrayList<Float> arrayList2 = arrayList.get(i);
            for (int i2 = 0; i2 < arrayList2.size(); i2 += 2) {
                arrayList2.set(i2, Float.valueOf((arrayList2.get(i2).floatValue() * f) - f3));
                int i3 = i2 + 1;
                arrayList2.set(i3, Float.valueOf((arrayList2.get(i3).floatValue() * f2) - f4));
            }
        }
        this.mRectLeft = rectF.left * f;
        this.mRectTop = rectF.top * f2;
        this.mRectRight = rectF.right * f;
        this.mRectBottom = rectF.bottom * f2;
    }

    public void redrawPath() {
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            redrawPath(canvas);
        }
    }

    public void drawTransparent() {
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
    }

    public void fillColor() {
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            canvas.drawColor(-16776961, PorterDuff.Mode.DARKEN);
        }
    }

    public void setStrokeColor(int i) {
        this.mStrokeColor = i;
        this.mBitmapPaint.setColor(i);
        redrawPath();
    }

    public void setmActualColor(int i) {
        this.mActualColor = i;
    }

    public int getActualColor() {
        return this.mActualColor;
    }

    public void setStrokeWidth(float f) {
        if (f <= 0.0f) {
            f = 0.5f;
        }
        this.mStrokeWidthInDocSpace = f;
        this.mBitmapPaint.setStrokeWidth(f);
        invalidate();
        drawTransparent();
        redrawPath();
        invalidate();
    }

    public float getStrokeWidth() {
        return this.mStrokeWidthInDocSpace;
    }

    public void setLayoutParams(int i, int i2) {
        this.mBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        this.mCanvas = new Canvas(this.mBitmap);
        setStrokeWidth(this.mStrokeWidthInDocSpace);
        setLayoutParams(new LayoutParams(i, i2));
        this.mLayoutHeight = i2;
        this.mLayoutWidth = i;
        ArrayList<ArrayList<Float>> arrayList = this.mInkList;
        RectF boundingBox = getBoundingBox();
        clear();
        initializeInkList(arrayList);
        scaleAndTranslatePath(arrayList, boundingBox, 1.0f, 1.0f, 0.0f, 0.0f);
    }



    public RectF getBoundingBox() {
        return new RectF(this.mRectLeft, this.mRectTop, this.mRectRight, this.mRectBottom);
    }

    public ArrayList<ArrayList<Float>> getInkList() {
        return this.mInkList;
    }

    public void setEditable(boolean z) {
        this.mIsEditable = !z;
    }

    private void redrawPath(Canvas canvas) {
        int size = this.mInkList.size();
        for (int i = 0; i < size; i++) {
            ArrayList<Float> arrayList = this.mInkList.get(i);
            touch_start(arrayList.get(0).floatValue(), arrayList.get(1).floatValue());
            for (int i2 = 2; i2 < arrayList.size(); i2 += 2) {
                touch_move(arrayList.get(i2).floatValue(), arrayList.get(i2 + 1).floatValue());
            }
            this.mPath.lineTo(this.mX, this.mY);
            canvas.drawPath(this.mPath, this.mBitmapPaint);
            this.mPath.reset();
        }
    }

    @Override 
    public void onDraw(Canvas canvas) {
        if (!this.mSignatureCreationMode) {
            drawTransparent();
            redrawPath(canvas);
            return;
        }
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
        }
    }

    private void setBoundingRect(float f, float f2) {
        if (f < this.mRectLeft) {
            this.mRectLeft = f;
        } else if (f > this.mRectRight) {
            this.mRectRight = f;
        }
        if (f2 < this.mRectTop) {
            this.mRectTop = f2;
        } else if (f2 > this.mRectBottom) {
            this.mRectBottom = f2;
        }
    }

    private void touch_start(float f, float f2) {
        this.mPath.reset();
        this.mPath.moveTo(f, f2);
        this.mQuadEndPointX = f;
        this.mQuadEndPointY = f2;
        this.mX = f;
        this.mY = f2;
        this.mTouchDownPointX = f;
        this.mTouchDownPointY = f2;
        ArrayList<Float> arrayList = new ArrayList<>();
        this.mGestureInkList = arrayList;
        arrayList.add(Float.valueOf(this.mX));
        this.mGestureInkList.add(Float.valueOf(this.mY));
        if (this.mIsFirstBoundingRect) {
            float f3 = this.mX;
            this.mRectRight = f3;
            this.mRectLeft = f3;
            float f4 = this.mY;
            this.mRectBottom = f4;
            this.mRectTop = f4;
            this.mIsFirstBoundingRect = false;
            return;
        }
        setBoundingRect(this.mX, this.mY);
    }

    private void touch_move(float f, float f2) {
        float abs = Math.abs(f - this.mX);
        float abs2 = Math.abs(f2 - this.mY);
        if (abs >= 0.1f || abs2 >= 0.1f) {
            float f3 = this.mX;
            float f4 = (f3 + f) / 2.0f;
            this.mQuadEndPointX = f4;
            float f5 = this.mY;
            float f6 = (f5 + f2) / 2.0f;
            this.mQuadEndPointY = f6;
            this.mPath.quadTo(f3, f5, f4, f6);
            this.mX = f;
            this.mY = f2;
        }
        this.mGestureInkList.add(Float.valueOf(this.mX));
        this.mGestureInkList.add(Float.valueOf(this.mY));
        setBoundingRect(this.mX, this.mY);
    }

    private void touch_up(float f, float f2) {
        drawPointIfRequired(f, f2);
        this.mPath.lineTo(this.mX, this.mY);
        Canvas canvas = this.mCanvas;
        if (canvas != null) {
            canvas.drawPath(this.mPath, this.mBitmapPaint);
        }
        this.mPath.reset();
        this.mInkList.add(this.mGestureInkList);
        if (this.mRedoInkList.size() != 0) {
            this.mRedoInkList.clear();
        }
    }

    private void drawPointIfRequired(float f, float f2) {
        float abs = Math.abs(f - this.mTouchDownPointX);
        float abs2 = Math.abs(f2 - this.mTouchDownPointY);
        if (abs >= 0.1f || abs2 >= 0.1f) {
            return;
        }
        this.mX = f;
        this.mY = f2;
        if (compareDoubleValues(abs, 0.0d) && compareDoubleValues(abs2, 0.0d)) {
            this.mY = f2 - 1.0f;
        }
        this.mGestureInkList.add(Float.valueOf(this.mX));
        this.mGestureInkList.add(Float.valueOf(this.mY));
        setBoundingRect(this.mX, this.mY);
    }

    private boolean compareDoubleValues(double d, double d2) {
        return Math.abs(d - d2) < EPSILON_FOR_DOUBLE_COMPARISON;
    }

    public int getStatusBarHeight() {
        try {
            int identifier = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (identifier > 0) {
                return getResources().getDimensionPixelSize(identifier);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override 
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        try {
            this.mBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            this.mCanvas = new Canvas(this.mBitmap);
            scaleAndTranslatePath(this.mInkList, new RectF(this.mRectLeft, this.mRectTop, this.mRectRight, this.mRectBottom), i3 != 0 ? i / i3 : 1.0f, i4 != 0 ? i2 / i4 : 1.0f, 0.0f, 0.0f);
            redrawPath();
        } catch (IllegalArgumentException | OutOfMemoryError unused) {
        }
    }

    @Override 
    public void onMeasure(int i, int i2) {
        int i3;
        if (!this.mSignatureCreationMode && (i3 = this.mLayoutWidth) != -1 && this.mLayoutHeight != -1) {
            int makeMeasureSpec = MeasureSpec.makeMeasureSpec(i3, MeasureSpec.EXACTLY);
            i2 = MeasureSpec.makeMeasureSpec(this.mLayoutHeight, MeasureSpec.EXACTLY);
            i = makeMeasureSpec;
        }
        super.onMeasure(i, i2);
    }

    @Override 
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            redrawPath();
            invalidate();
        }
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mIsEditable) {
            super.onTouchEvent(motionEvent);
            return true;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        enableToolBarButton();
        int action = motionEvent.getAction();


        Log.e("onTouchEvent","= "+action);
        if (action == MotionEvent.ACTION_DOWN) {
            touch_start(x, y);
            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            touch_up(x, y);
            drawTransparent();
            redrawPath();
            invalidate();
        } else if (action == MotionEvent.ACTION_MOVE) {
            int historySize = motionEvent.getHistorySize();
            for (int i = 0; i < historySize; i++) {
                touch_move(motionEvent.getHistoricalX(i), motionEvent.getHistoricalY(i));
            }
            touch_move(x, y);
            Canvas canvas = this.mCanvas;
            if (canvas != null) {
                canvas.drawPath(this.mPath, this.mBitmapPaint);
                this.mPath.reset();
                this.mPath.moveTo(this.mQuadEndPointX, this.mQuadEndPointY);
            }
            invalidate();
        }
        return true;
    }

    public void enableToolBarButton() {
        if (getContext() != null) {
            ((FreeHandActivity) getContext()).enableClear(true);
            ((FreeHandActivity) getContext()).enableSave(true);
        }
    }

    public Bitmap getImage() {
        return this.mBitmap;
    }

    public void clear() {
        this.mX = 0.0f;
        this.mY = 0.0f;
        this.mRectLeft = 0.0f;
        this.mRectTop = 0.0f;
        this.mRectRight = 0.0f;
        this.mRectBottom = 0.0f;
        this.mIsFirstBoundingRect = true;
        drawTransparent();
        this.mPath.reset();
        this.mInkList = new ArrayList<>();
        invalidate();
    }
}
