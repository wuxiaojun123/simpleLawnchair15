package com.simplepdf.pdfeditor.DialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.CallbackListener.RecyclerItemClick;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.BottomsheetMenuBinding;


public class MenuBottomSheet extends PdfBottomSheetDialogFragment {
    public static final int ACTION_SIGNATURES = 1;
    public static final int ACTION_SHARE_APP = 2;

    public static RecyclerItemClick listener;
    BottomsheetMenuBinding binding;

    public static MenuBottomSheet newInstance(RecyclerItemClick recyclerItemClick) {
        MenuBottomSheet menuBottomSheet = new MenuBottomSheet();
        menuBottomSheet.setArguments(new Bundle());
        listener = recyclerItemClick;
        return menuBottomSheet;
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        BottomsheetMenuBinding bottomsheetMenuBinding = (BottomsheetMenuBinding) DataBindingUtil.inflate(pdfThemeInflater(layoutInflater), R.layout.bottomsheet_menu, viewGroup, false);
        this.binding = bottomsheetMenuBinding;
        View root = bottomsheetMenuBinding.getRoot();
        getArguments();

        setViewListener();
        return root;
    }

    public void setViewListener() {

        this.binding.llSignatures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuBottomSheet.this.notifyMenuItemClick(ACTION_SIGNATURES);
            }
        });
        this.binding.llDrawerShare.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                MenuBottomSheet.this.m107x7fb66d10(view);
            }
        });
//        this.binding.llDrawerRate.setOnClickListener(new View.OnClickListener() { 
//            @Override 
//            public final void onClick(View view) {
//                MenuBottomSheet.this.m108xca3842f(view);
//            }
//        });

    }


    
    
    public void m107x7fb66d10(View view) {
        notifyMenuItemClick(ACTION_SHARE_APP);
    }

    private void notifyMenuItemClick(int action) {
        if (listener != null) {
            listener.onItemClick(action);
        }
        dismiss();
    }

    
    
    /*public  void m108xca3842f(View view) {
        AppConstants.showRattingDialog(getActivity());
        dismiss();
    }*/

}
