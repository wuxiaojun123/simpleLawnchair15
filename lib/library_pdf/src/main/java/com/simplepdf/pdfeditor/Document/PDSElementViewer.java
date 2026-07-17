package com.simplepdf.pdfeditor.Document;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.Signature.SignatureView;
import com.simplepdf.pdfeditor.model.PDSElement;
import com.simplepdf.pdfeditor.util.ViewUtils;


public class PDSElementViewer {
    private static int MOTION_THRESHOLD = 3;
    private static int MOTION_THRESHOLD_LONG_PRESS = 12;
    private Context mContext;
    private PDSElement mElement;
    public PDSPageViewer mPageViewer;
    private boolean mBorderShown = false;
    private RelativeLayout mContainerView = null;
    private View mElementView = null;
    private boolean mHasDragStarted = false;
    private ImageButton mImageButton = null;
    private float mLastMotionX = 0.0f;
    private float mLastMotionY = 0.0f;
    private boolean mLongPress = false;
    private float mResizeInitialPos = 0.0f;

    public PDSElementViewer(Context context, PDSPageViewer pDSPageViewer, PDSElement pDSElement) {
        this.mContext = context;
        this.mPageViewer = pDSPageViewer;
        this.mElement = pDSElement;
        pDSElement.mElementViewer = this;
        createElement(pDSElement);
    }

    public PDSPageViewer getPageViewer() {
        return this.mPageViewer;
    }

    public PDSElement getElement() {
        return this.mElement;
    }

    public View getElementView() {
        return this.mElementView;
    }

    public RelativeLayout getContainerView() {
        return this.mContainerView;
    }

    public ImageButton getImageButton() {
        return this.mImageButton;
    }

    private void createElement(PDSElement pDSElement) {
        this.mElementView = createElementView(pDSElement);
        this.mPageViewer.getPageView().addView(this.mElementView);
        this.mElementView.setTag(pDSElement);
        if (!isElementInModel()) {
            addElementInModel(pDSElement);
        }
        setListeners();
    }

    public void removeElement() {
        if (this.mElementView.getParent() != null) {
            this.mPageViewer.getPage().removeElement((PDSElement) this.mElementView.getTag());
            this.mPageViewer.hideElementPropMenu();
            this.mPageViewer.getPageView().removeView(this.mElementView);
        }
    }

    private View createElementView(PDSElement pDSElement) {
        int i = pDSElement.getType().ordinal();

        Log.e("createElementView", ""+i);

        if( i == 0){
            i = 2;
        }

        if (i == 1) {
            SignatureView createSignatureView = ViewUtils.createSignatureView(this.mContext, pDSElement, this.mPageViewer.getToViewCoordinatesMatrix());
            pDSElement.setRect(new RectF(pDSElement.getRect().left, pDSElement.getRect().top, pDSElement.getRect().left + this.mPageViewer.mapLengthToPDFCoordinates(createSignatureView.getSignatureViewWidth()), pDSElement.getRect().bottom));
            pDSElement.setStrokeWidth(this.mPageViewer.mapLengthToPDFCoordinates(createSignatureView.getStrokeWidth()));
            createSignatureView.setFocusable(true);
            createSignatureView.setFocusableInTouchMode(true);
            createSignatureView.setClickable(true);
            createSignatureView.setLongClickable(true);
            createResizeButton(createSignatureView);
            return createSignatureView;
        } else if (i == 2 || i == 3 || i == 4) {
            ImageView createImageView = ViewUtils.createImageView(this.mContext, pDSElement, this.mPageViewer.getToViewCoordinatesMatrix());
            createImageView.setImageBitmap(pDSElement.getBitmap());
//            pDSElement.setmDefaultWidth(pDSElement.getRect().width());
//            pDSElement.setmDefaultHeight(pDSElement.getRect().height());

            pDSElement.setmDefaultWidth(pDSElement.getBitmap().getWidth());
            pDSElement.setmDefaultHeight(pDSElement.getBitmap().getHeight());

            pDSElement.setRect(new RectF((int) pDSElement.getRect().left, (int) pDSElement.getRect().top, ((int) pDSElement.getRect().left) + this.mPageViewer.mapLengthToPDFCoordinates(createImageView.getWidth()), pDSElement.getRect().bottom));

            createImageView.setFocusable(true);
            createImageView.setFocusableInTouchMode(true);
            createImageView.setClickable(true);
            createImageView.setLongClickable(true);
            createImageView.invalidate();
            createResizeButton(createImageView);
            return createImageView;
        } else if (i != 5) {
            return null;
        } else {
            ImageView createImageView2 = ViewUtils.createImageView(this.mContext, pDSElement, this.mPageViewer.getToViewCoordinatesMatrix());
            createImageView2.setDrawingCacheEnabled(true);
            createImageView2.setImageBitmap(pDSElement.getBitmap());
            pDSElement.setmDefaultWidth(pDSElement.getRect().width());
            pDSElement.setmDefaultHeight(pDSElement.getRect().height());
            pDSElement.setRect(new RectF((int) pDSElement.getRect().left, (int) pDSElement.getRect().top, ((int) pDSElement.getRect().left) + this.mPageViewer.mapLengthToPDFCoordinates(createImageView2.getWidth()), pDSElement.getRect().bottom));
            createImageView2.setFocusable(true);
            createImageView2.setFocusableInTouchMode(true);
            createImageView2.setClickable(true);
            createImageView2.setLongClickable(true);
            createImageView2.invalidate();
            createResizeButton(createImageView2);
            return createImageView2;
        }
    }

    private void addElementInModel(PDSElement pDSElement) {
        this.mPageViewer.getPage().addElement(pDSElement);
    }

    private boolean isElementInModel() {
        for (int i = 0; i < this.mPageViewer.getPage().getNumElements(); i++) {
            if (this.mPageViewer.getPage().getElement(i) == this.mElementView.getTag()) {
                return true;
            }
        }
        return false;
    }

    public void setListeners() {
        setTouchListener();
        setFocusListener();
    }

    private void setTouchListener() {
        this.mElementView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.requestFocus();
                PDSElementViewer.this.mLongPress = true;
                return true;
            }
        });
        this.mElementView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction() & 255;

                Log.e("action",""+action);

                if (action == MotionEvent.ACTION_DOWN) {
                    PDSElementViewer.this.mHasDragStarted = false;
                    PDSElementViewer.this.mLongPress = false;
                    PDSElementViewer.this.mLastMotionX = motionEvent.getX();
                    PDSElementViewer.this.mLastMotionY = motionEvent.getY();
                } else if (action !=  MotionEvent.ACTION_UP) {
                    if (action == 2 && !PDSElementViewer.this.mHasDragStarted) {
                        int abs = Math.abs((int) (motionEvent.getX() - PDSElementViewer.this.mLastMotionX));
                        int abs2 = Math.abs((int) (motionEvent.getY() - PDSElementViewer.this.mLastMotionY));
                        int i = PDSElementViewer.this.mLongPress ? PDSElementViewer.MOTION_THRESHOLD_LONG_PRESS : PDSElementViewer.MOTION_THRESHOLD;
                        if (motionEvent.getX() >= 0.0f && motionEvent.getY() >= 0.0f && PDSElementViewer.this.mBorderShown && (abs > i || abs2 > i)) {
                            float x = motionEvent.getX();
                            float y = motionEvent.getY();
                            ClipData newPlainText = ClipData.newPlainText("pos", String.format("%d %d", Integer.valueOf(Math.round(x)), Integer.valueOf(Math.round(y))));
                            CustomDragShadowBuilder customDragShadowBuilder = new CustomDragShadowBuilder(view, Math.round(x), Math.round(y));
                            PDSElementViewer pDSElementViewer = PDSElementViewer.this;
                            view.startDrag(newPlainText, customDragShadowBuilder, new DragEventData(pDSElementViewer, x, y), 0);
                            PDSElementViewer.this.mHasDragStarted = true;
                        }
                        return true;
                    }
                } else {
                    PDSElementViewer.this.mHasDragStarted = false;
                    PDSElementViewer.this.mPageViewer.setElementAlreadyPresentOnTap(true);
                    if (view instanceof SignatureView) {
                        PDSElementViewer.this.mContainerView.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                }
                return false;
            }
        });
    }

    private void setFocusListener() {
        this.mElementView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    PDSElementViewer.this.assignFocus();
                }
            }
        });
    }

    public void assignFocus() {
        this.mPageViewer.showElementPropMenu(this);
    }

    public void relayoutContainer() {
        this.mElementView.measure(Math.round(-2.0f), Math.round(-2.0f));
        View view = this.mElementView;
        view.layout(0, 0, view.getMeasuredWidth(), this.mElementView.getMeasuredHeight());
        this.mImageButton.measure(Math.round(-2.0f), Math.round(-2.0f));
        ImageButton imageButton = this.mImageButton;
        imageButton.layout(0, 0, imageButton.getMeasuredWidth(), this.mImageButton.getMeasuredHeight());
        int measuredWidth = this.mElementView.getMeasuredWidth() + (this.mImageButton.getMeasuredHeight() / 2);
        int measuredHeight = this.mElementView.getMeasuredHeight();
        this.mImageButton.setVisibility(View.VISIBLE);
        this.mContainerView.setLayoutParams(new ViewGroup.LayoutParams(measuredWidth, measuredHeight));
    }

    private void createResizeButton(View view) {
        ImageButton imageButton = new ImageButton(this.mContext);
        this.mImageButton = imageButton;
        imageButton.setImageResource(R.drawable.ic_resize);
        this.mImageButton.setBackgroundColor(0);
        this.mImageButton.setPadding(0, 0, 0, 0);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams.addRule(11);
        layoutParams.addRule(15);
        this.mImageButton.setLayoutParams(layoutParams);
        this.mImageButton.measure(Math.round(-2.0f), Math.round(-2.0f));
        ImageButton imageButton2 = this.mImageButton;
        imageButton2.layout(0, 0, imageButton2.getMeasuredWidth(), this.mImageButton.getMeasuredHeight());
        RelativeLayout relativeLayout = new RelativeLayout(this.mContext);
        this.mContainerView = relativeLayout;
        relativeLayout.setFocusable(false);
        this.mContainerView.setFocusableInTouchMode(false);
        this.mImageButton.setOnTouchListener(new View.OnTouchListener() {

            @Override

            public boolean onTouch(View view2, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == 0) {
                    PDSElementViewer.this.mResizeInitialPos = motionEvent.getRawX();
                    PDSElementViewer.this.mPageViewer.setResizeInOperation(true);
                    Log.e("PDF111", "0 motionEvent.getRawX() 0= " + motionEvent.getRawX());
                } else {
                    if (action != 1) {
                        if (action == 2) {
                            if (PDSElementViewer.this.mPageViewer.getResizeInOperation()) {
                                float rawX = (motionEvent.getRawX() - PDSElementViewer.this.mResizeInitialPos) / 2.0f;
                                if ((rawX <= (-PDSElementViewer.this.mContext.getResources().getDimension(R.dimen.sign_field_step_size)) || rawX >= PDSElementViewer.this.mContext.getResources().getDimension(R.dimen.sign_field_step_size)) && PDSElementViewer.this.mElementView.getHeight() + rawX >= PDSElementViewer.this.mContext.getResources().getDimension(R.dimen.sign_field_min_height) && PDSElementViewer.this.mElementView.getHeight() + rawX <= PDSElementViewer.this.mContext.getResources().getDimension(R.dimen.sign_field_max_height)) {
                                    PDSElementViewer.this.mResizeInitialPos = motionEvent.getRawX();
                                    PDSElementViewer.this.mPageViewer.modifyElementSignatureSize((PDSElement) PDSElementViewer.this.mElementView.getTag(), PDSElementViewer.this.mElementView, PDSElementViewer.this.mContainerView, (int) ((PDSElementViewer.this.mElementView.getWidth() * rawX) / PDSElementViewer.this.mElementView.getHeight()), (int) rawX);
                                }
                            }
                        }
                    }
                    PDSElementViewer.this.mPageViewer.setResizeInOperation(true);
                }
                return true;
            }
        });
    }

    public void showBorder() {
        int measuredWidth;
        int i;
        changeColor(true);
        if (this.mContainerView.getParent() == null) {
            if (this.mElementView.getParent() == this.mPageViewer.getPageView()) {
                this.mElementView.setOnFocusChangeListener(null);
                this.mPageViewer.getPageView().removeView(this.mElementView);
                this.mContainerView.addView(this.mElementView);
            }
            this.mContainerView.addView(this.mImageButton);
            this.mContainerView.setX(this.mElementView.getX());
            this.mContainerView.setY(this.mElementView.getY());
            this.mElementView.setX(0.0f);
            this.mElementView.setY(0.0f);
            View view = this.mElementView;
            if (view instanceof SignatureView) {
                measuredWidth = ((SignatureView) view).getSignatureViewWidth() + (this.mImageButton.getMeasuredWidth() / 2);
                i = ((SignatureView) this.mElementView).getSignatureViewHeight();
            } else {

                measuredWidth = view.getLayoutParams().width + (this.mImageButton.getMeasuredWidth() / 2);
                i = this.mElementView.getLayoutParams().height;
            }


            this.mContainerView.setLayoutParams(new RelativeLayout.LayoutParams(measuredWidth, i));
            this.mPageViewer.getPageView().addView(this.mContainerView);
        }
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setStroke(4, this.mContext.getResources().getColor(R.color.color_btn2));
        this.mElementView.setBackground(gradientDrawable);
        this.mBorderShown = true;
    }

    public void hideBorder() {
        changeColor(false);
        if (this.mContainerView.getParent() == this.mPageViewer.getPageView()) {
            this.mElementView.setX(this.mContainerView.getX());
            this.mElementView.setY(this.mContainerView.getY());
            this.mPageViewer.getPageView().removeView(this.mContainerView);
            this.mContainerView.removeView(this.mImageButton);
            ViewParent parent = this.mElementView.getParent();
            RelativeLayout relativeLayout = this.mContainerView;
            if (parent == relativeLayout) {
                relativeLayout.removeView(this.mElementView);
                this.mPageViewer.getPageView().addView(this.mElementView);
                setFocusListener();
            }
        }
        this.mElementView.setBackground(null);
        this.mBorderShown = false;
    }

    public void changeColor(boolean z) {
        if (z) {
            this.mContext.getResources().getColor(R.color.color_btn2);
        }
        View view = this.mElementView;
        if (view instanceof SignatureView) {
            ((SignatureView) this.mElementView).setStrokeColor(((SignatureView) view).getActualColor());
            return;
        }
        boolean z2 = view instanceof ImageView;
    }

    public boolean isBorderShown() {
        return this.mBorderShown;
    }


    class CustomDragShadowBuilder extends View.DragShadowBuilder {
        int mX;
        int mY;

        @Override
        public void onDrawShadow(Canvas canvas) {
        }

        public CustomDragShadowBuilder(View view, int i, int i2) {
            super(view);
            this.mX = i;
            this.mY = i2;
        }

        @Override
        public void onProvideShadowMetrics(Point point, Point point2) {
            super.onProvideShadowMetrics(point, point2);
            point2.set((int) (this.mX * PDSElementViewer.this.mPageViewer.getScaleFactor()), (int) (this.mY * PDSElementViewer.this.mPageViewer.getScaleFactor()));
            point.set((int) (getView().getWidth() * PDSElementViewer.this.mPageViewer.getScaleFactor()), (int) (getView().getHeight() * PDSElementViewer.this.mPageViewer.getScaleFactor()));
        }
    }


    public class DragEventData {
        public PDSElementViewer viewer;
        public float x;
        public float y;

        public DragEventData(PDSElementViewer pDSElementViewer, float f, float f2) {
            this.viewer = pDSElementViewer;
            this.x = f;
            this.y = f2;
        }
    }
}
