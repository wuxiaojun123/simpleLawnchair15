package com.simplepdf.pdfeditor.Activity;

import androidx.activity.result.ActivityResult;

import com.simplepdf.pdfeditor.Fragment.HomeFragment;
import com.simplepdf.pdfeditor.util.BetterActivityResult;


public final  class MainActivityExternal implements BetterActivityResult.OnActivityResult {
    public static final MainActivityExternal INSTANCE = new MainActivityExternal();

    private MainActivityExternal() {
    }

    @Override 
    public final void onActivityResult(Object obj) {
        HomeFragment.lambda$editPdf$11((ActivityResult) obj);
    }
}
