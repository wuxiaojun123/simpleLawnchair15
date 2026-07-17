package com.simplepdf.pdfeditor.Document;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.simplepdf.pdfeditor.Activity.DigitalSignatureActivity;
import com.simplepdf.pdfeditor.R;


public class PDSFragment extends Fragment {
    PDSPageViewer mPageViewer = null;

    public static PDSFragment newInstance(int i) {
        PDSFragment pDSFragment = new PDSFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pageNum", i);
        pDSFragment.setArguments(bundle);
        return pDSFragment;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_layout, viewGroup, false);
        LinearLayout linearLayout = (LinearLayout) inflate.findViewById(R.id.fragment);
        try {
            PDSPageViewer pDSPageViewer = new PDSPageViewer(viewGroup.getContext(), (DigitalSignatureActivity) getActivity(), ((DigitalSignatureActivity) getActivity()).getDocument().getPage(getArguments().getInt("pageNum")));
            this.mPageViewer = pDSPageViewer;
            linearLayout.addView(pDSPageViewer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inflate;
    }

    @Override 
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    @Override 
    public void onDestroyView() {
        PDSPageViewer pDSPageViewer = this.mPageViewer;
        if (pDSPageViewer != null) {
            pDSPageViewer.cancelRendering();
            this.mPageViewer = null;
        }
        super.onDestroyView();
    }
}
