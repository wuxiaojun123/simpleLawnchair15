package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.Signature.SignatureUtils;
import com.simplepdf.pdfeditor.Signature.SignatureView;

import java.io.File;


public class PDSSignatureUtils {
    private static PopupWindow sSignaturePopUpMenu;

    public static SignatureView showFreeHandView(Context context, File file) {
        SignatureView createFreeHandView = SignatureUtils.createFreeHandView((((int) context.getResources().getDimension(R.dimen.sign_menu_width)) - ((int) context.getResources().getDimension(R.dimen.sign_left_offset))) - (((int) context.getResources().getDimension(R.dimen.sign_right_offset)) * 3), ((int) context.getResources().getDimension(R.dimen.sign_button_height)) - ((int) context.getResources().getDimension(R.dimen.sign_top_offset)), file, context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams.addRule(9);
        layoutParams.setMargins((int) context.getResources().getDimension(R.dimen.sign_left_offset), (int) context.getResources().getDimension(R.dimen.sign_top_offset), 0, 0);
        createFreeHandView.setLayoutParams(layoutParams);
        return createFreeHandView;
    }

    public static boolean isSignatureMenuOpen() {
        PopupWindow popupWindow = sSignaturePopUpMenu;
        return popupWindow != null && popupWindow.isShowing();
    }

    public static void dismissSignatureMenu() {
        PopupWindow popupWindow = sSignaturePopUpMenu;
        if (popupWindow == null || !popupWindow.isShowing()) {
            return;
        }
        sSignaturePopUpMenu.dismiss();
    }
}
