package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetDocumentOptionBinding;
import com.simplepdf.pdfeditor.util.Constant;


public class DocumentOptionBottomSheet extends PdfBottomSheetDialogFragment {
    public static RecyclerItemClick listener;
    BottomsheetDocumentOptionBinding binding;

    public static DocumentOptionBottomSheet newInstance(RecyclerItemClick recyclerItemClick) {
        DocumentOptionBottomSheet documentOptionBottomSheet = new DocumentOptionBottomSheet();
        listener = recyclerItemClick;
        return documentOptionBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetDocumentOptionBinding bottomsheetDocumentOptionBinding = (BottomsheetDocumentOptionBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_document_option, viewGroup, false);
        this.binding = bottomsheetDocumentOptionBinding;
        View root = bottomsheetDocumentOptionBinding.getRoot();
        setViewListener();
        return root;
    }

    public void setViewListener() {
        this.binding.mcvText.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m100x8b96be40(view);
            }
        });
        this.binding.mcvSign.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m101x2804ba9f(view);
            }
        });
        this.binding.mcvDate.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m102xc472b6fe(view);
            }
        });
        this.binding.mcvStamps.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m103x60e0b35d(view);
            }
        });
        this.binding.mcvIcon.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m104xfd4eafbc(view);
            }
        });
        this.binding.mcvPhoto.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DocumentOptionBottomSheet.this.m105x99bcac1b(view);
            }
        });
    }

    
    
    public  void m100x8b96be40(View view) {
        listener.onItemClick(100);
        dismiss();
    }

    
    
    public  void m101x2804ba9f(View view) {
        listener.onItemClick(111);
        dismiss();
    }

    
    
    public  void m102xc472b6fe(View view) {
        listener.onItemClick(Constant.AD_DATE);
        dismiss();
    }

    
    
    public  void m103x60e0b35d(View view) {
        listener.onItemClick(Constant.AD_STAMPS);
        dismiss();
    }

    
    
    public  void m104xfd4eafbc(View view) {
        listener.onItemClick(Constant.AD_ICON);
        dismiss();
    }

    
    
    public  void m105x99bcac1b(View view) {
        listener.onItemClick(555);
        dismiss();
    }
}
