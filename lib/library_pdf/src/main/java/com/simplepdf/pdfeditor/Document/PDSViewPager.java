package com.simplepdf.pdfeditor.Document;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.ViewPager;

import com.simplepdf.pdfeditor.Activity.DigitalSignatureActivity;


public class PDSViewPager extends ViewPager {
    private Context mActivityContext;
    private boolean mDownReceieved;

    public PDSViewPager(Context context) {
        super(context);
        this.mDownReceieved = true;
        this.mActivityContext = context;
        init();
    }

    public PDSViewPager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mDownReceieved = true;
        this.mActivityContext = context;
        init();
    }

    private void init() {
        setPageTransformer(false, new DefaultTransformer());
        addOnPageChangeListener(new OnPageChangeListener() { 
            @Override 
            public void onPageScrollStateChanged(int i) {
            }

            @Override 
            public void onPageScrolled(int i, float f, int i2) {
            }

            @Override 
            public void onPageSelected(int i) {
                PDSPageViewer pDSPageViewer;
                View focusedChild = PDSViewPager.this.getFocusedChild();
                if (focusedChild != null && (pDSPageViewer = (PDSPageViewer) ((ViewGroup) focusedChild).getChildAt(0)) != null) {
                    pDSPageViewer.resetScale();
                }
                if (PDSViewPager.this.mActivityContext != null) {
                    ((DigitalSignatureActivity) PDSViewPager.this.mActivityContext).updatePageNumber(i + 1);
                }
            }
        });
    }

    private MotionEvent swapTouchEvent(MotionEvent motionEvent) {
        float width = getWidth();
        float height = getHeight();
        motionEvent.setLocation((motionEvent.getY() / height) * width, (motionEvent.getX() / width) * height);
        return motionEvent;
    }

    @Override 
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean onInterceptTouchEvent = super.onInterceptTouchEvent(swapTouchEvent(motionEvent));
        swapTouchEvent(motionEvent);
        return onInterceptTouchEvent;
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(swapTouchEvent(motionEvent));
    }

    
    
    public class DefaultTransformer implements PageTransformer {
        private DefaultTransformer() {
        }

        @Override 
        public void transformPage(View view, float f) {
            float f2 = 0.0f;
            if (0.0f <= f && f <= 1.0f) {
                f2 = 1.0f - f;
            } else if (-1.0f < f && f < 0.0f) {
                f2 = f + 1.0f;
            }
            view.setAlpha(f2);
            view.setTranslationX(view.getWidth() * (-f));
            view.setTranslationY(f * view.getHeight());
        }
    }
}
