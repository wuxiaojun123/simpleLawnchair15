package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetSignatureBinding;
import com.simplepdf.pdfeditor.util.Constant;


public class SignatureBottomSheet extends PdfBottomSheetDialogFragment {
    public static RecyclerItemClick listener;
    BottomsheetSignatureBinding binding;

    public static SignatureBottomSheet newInstance(RecyclerItemClick recyclerItemClick) {
        SignatureBottomSheet signatureBottomSheet = new SignatureBottomSheet();
        listener = recyclerItemClick;
        return signatureBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetSignatureBinding bottomsheetSignatureBinding = (BottomsheetSignatureBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_signature, viewGroup, false);
        this.binding = bottomsheetSignatureBinding;
        View root = bottomsheetSignatureBinding.getRoot();
        setViewListener();
        return root;
    }

    public void setViewListener() {
        this.binding.mcvSignatureCollection.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                SignatureBottomSheet.this.m116xac843cc0(view);
            }
        });
        this.binding.mcvImageLibrary.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                SignatureBottomSheet.this.m117xa013c101(view);
            }
        });
    }

    
    
    public  void m116xac843cc0(View view) {
        listener.onItemClick(555);
        dismiss();
    }

    
    
    public  void m117xa013c101(View view) {
        listener.onItemClick(Constant.IMAGE_LIBRARY);
        dismiss();
    }
}
