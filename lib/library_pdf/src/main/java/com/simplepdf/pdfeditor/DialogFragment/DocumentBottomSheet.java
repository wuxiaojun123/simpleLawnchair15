package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetDocumentBinding;


public class DocumentBottomSheet extends PdfBottomSheetDialogFragment {
    public static RecyclerItemClick listener;
    BottomsheetDocumentBinding binding;

    public static DocumentBottomSheet newInstance(RecyclerItemClick recyclerItemClick) {
        DocumentBottomSheet documentBottomSheet = new DocumentBottomSheet();
        listener = recyclerItemClick;
        return documentBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetDocumentBinding bottomsheetDocumentBinding = (BottomsheetDocumentBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_document, viewGroup, false);
        this.binding = bottomsheetDocumentBinding;
        View root = bottomsheetDocumentBinding.getRoot();
        setViewListener();
        return root;
    }

    public void setViewListener() {
        this.binding.mcvChoosePdf.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                DocumentBottomSheet.listener.onItemClick(66);
                DocumentBottomSheet.this.dismiss();
            }
        });
        this.binding.mcvLoadFromDevice.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentBottomSheet.this.m97x9fa75eb5(view);
            }
        });
        this.binding.mcvGallery.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentBottomSheet.this.m98x33e5ce54(view);
            }
        });
        this.binding.mcvTakePicture.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentBottomSheet.this.m99xc8243df3(view);
            }
        });
    }

    
    
    public  void m97x9fa75eb5(View view) {
        listener.onItemClick(77);
        dismiss();
    }

    
    
    public  void m98x33e5ce54(View view) {
        listener.onItemClick(88);
        dismiss();
    }

    
    
    public  void m99xc8243df3(View view) {
        listener.onItemClick(99);
        dismiss();
    }
}
