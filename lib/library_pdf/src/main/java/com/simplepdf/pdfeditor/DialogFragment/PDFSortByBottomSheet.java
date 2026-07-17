package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetPdfSortBinding;


public class PDFSortByBottomSheet extends PdfBottomSheetDialogFragment {
    public static RecyclerItemClick listener;
    BottomsheetPdfSortBinding binding;

    public static PDFSortByBottomSheet newInstance(int i, RecyclerItemClick recyclerItemClick) {
        PDFSortByBottomSheet pDFSortByBottomSheet = new PDFSortByBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putInt("sortBy", i);
        pDFSortByBottomSheet.setArguments(bundle);
        listener = recyclerItemClick;
        return pDFSortByBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetPdfSortBinding bottomsheetPdfSortBinding = (BottomsheetPdfSortBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_pdf_sort, viewGroup, false);
        this.binding = bottomsheetPdfSortBinding;
        View root = bottomsheetPdfSortBinding.getRoot();
        switch (getArguments().getInt("sortBy")) {
            case 1:
                this.binding.rbAsc.setChecked(true);
                break;
            case 2:
                this.binding.rbDesc.setChecked(true);
                break;
            case 3:
                this.binding.rbLatest.setChecked(true);
                break;
            case 4:
                this.binding.rbOld.setChecked(true);
                break;
            case 5:
                this.binding.rbLow.setChecked(true);
                break;
            case 6:
                this.binding.rbHigh.setChecked(true);
                break;
        }
        setViewListener();
        return root;
    }

    public void setViewListener() {
        this.binding.rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { 
            @Override 
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(i);
                if (radioButton.isChecked()) {
                    if (radioButton.getId() == R.id.rbAsc) {
                        PDFSortByBottomSheet.listener.onItemClick(1);
                    } else if (radioButton.getId() == R.id.rbDesc) {
                        PDFSortByBottomSheet.listener.onItemClick(2);
                    } else if (radioButton.getId() == R.id.rbLatest) {
                        PDFSortByBottomSheet.listener.onItemClick(3);
                    } else if (radioButton.getId() == R.id.rbOld) {
                        PDFSortByBottomSheet.listener.onItemClick(4);
                    } else if (radioButton.getId() == R.id.rbLow) {
                        PDFSortByBottomSheet.listener.onItemClick(5);
                    } else if (radioButton.getId() == R.id.rbHigh) {
                        PDFSortByBottomSheet.listener.onItemClick(6);
                    }
                    PDFSortByBottomSheet.this.dismiss();
                }
            }
        });
    }
}
