package com.simplepdf.pdfeditor.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.simplepdf.pdfeditor.Document.PDSFragment;
import com.simplepdf.pdfeditor.PDF.PDSPDFDocument;


public class PDSPageAdapter extends FragmentPagerAdapter {
    private PDSPDFDocument mDocument;

    public PDSPageAdapter(FragmentManager fragmentManager, PDSPDFDocument pDSPDFDocument) {
        super(fragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mDocument = pDSPDFDocument;
    }

    @Override 
    public int getCount() {
        return this.mDocument.getNumPages();
    }

    @Override 
    public Fragment getItem(int i) {
        return PDSFragment.newInstance(i);
    }
}
