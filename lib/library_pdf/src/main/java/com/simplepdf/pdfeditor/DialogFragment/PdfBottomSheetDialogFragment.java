package com.simplepdf.pdfeditor.DialogFragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.simplepdf.pdfeditor.R;

public abstract class PdfBottomSheetDialogFragment extends BottomSheetDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.Theme_PdfSignature);
        // 关键1：允许点击外部关闭
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80F5F5F5")));
        }
        if (dialog instanceof BottomSheetDialog) {
            FrameLayout bottomSheet = ((BottomSheetDialog) dialog).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.parseColor("#80F5F5F5"));
            }
        }
    }
    protected LayoutInflater pdfThemeInflater(LayoutInflater inflater) {
        return inflater.cloneInContext(new ContextThemeWrapper(requireContext(), R.style.Theme_PdfSignature));
    }
}
