package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetPdfOprtionBinding;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.Constant;

import java.io.File;


public class PDFOptionBottomSheet extends PdfBottomSheetDialogFragment {
    public static RecyclerItemClick listener;
    BottomsheetPdfOprtionBinding binding;
    File file;

    public static PDFOptionBottomSheet newInstance(String str, RecyclerItemClick recyclerItemClick) {
        PDFOptionBottomSheet pDFOptionBottomSheet = new PDFOptionBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString("path", str);
        pDFOptionBottomSheet.setArguments(bundle);
        listener = recyclerItemClick;
        return pDFOptionBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetPdfOprtionBinding bottomsheetPdfOprtionBinding = (BottomsheetPdfOprtionBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_pdf_oprtion, viewGroup, false);
        this.binding = bottomsheetPdfOprtionBinding;
        View root = bottomsheetPdfOprtionBinding.getRoot();
        File file = new File(getArguments().getString("path"));
        this.binding.fileItemTextview.setText(file.getName());
        this.binding.dateItemTimeTextView.setText(String.format("%s  %s", AppConstants.getFormattedDate(file.lastModified(), Constant.FILE_DATE_FORMAT), AppConstants.FileSizeWithUnits(file.length())));
        setViewListener();
        return root;
    }

    public void setViewListener() {
        this.binding.mcvopen.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                PDFOptionBottomSheet.this.m111x3b7ada11(view);
            }
        });
        this.binding.mcvEdit.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                PDFOptionBottomSheet.this.m112x2f0a5e52(view);
            }
        });
        this.binding.mcvRename.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                PDFOptionBottomSheet.this.m113x2299e293(view);
            }
        });
        this.binding.mcvShare.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                PDFOptionBottomSheet.this.m114x162966d4(view);
            }
        });
        this.binding.mcvDelete.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                PDFOptionBottomSheet.this.m115x9b8eb15(view);
            }
        });
    }

    
    
    public  void m111x3b7ada11(View view) {
        listener.onItemClick(11);
        dismiss();
    }

    
    
    public  void m112x2f0a5e52(View view) {
        listener.onItemClick(22);
        dismiss();
    }

    
    
    public  void m113x2299e293(View view) {
        listener.onItemClick(33);
        dismiss();
    }

    
    
    public  void m114x162966d4(View view) {
        listener.onItemClick(44);
        dismiss();
    }

    
    
    public  void m115x9b8eb15(View view) {
        listener.onItemClick(55);
        dismiss();
    }
}
