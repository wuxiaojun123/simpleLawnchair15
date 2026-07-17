package com.simplepdf.pdfeditor.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.codemybrainsout.ratingdialog.RatingDialog;
import com.simplepdf.pdfeditor.App;
import com.simplepdf.pdfeditor.R;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class AppConstants {
    public static final int DEFAULT_HEIGHT = 430;
    public static final int DEFAULT_WIDTH = 1280;

    public static int exifToDegrees(int i) {
        if (i == 6) {
            return 90;
        }
        if (i == 3) {
            return 180;
        }
        return i == 8 ? 270 : 0;
    }

    public static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static Spanned fromHtml(String str) {
        if (str == null) {
            return new SpannableString("");
        }
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(str, 0);
        }
        return Html.fromHtml(str);
    }

    public static String getFormattedDate(long j, DateFormat dateFormat) {
        return j == 0 ? "" : dateFormat.format(new Date(j));
    }

    public static void showToast(String str) {
        Toast.makeText(App.getContext(), str, Toast.LENGTH_LONG).show();
    }

    /*public static void showRattingDialog(final Context context) {
        RatingDialog.Builder ratingBarBackgroundColor = new RatingDialog.Builder(context).session(1).title(Constant.RATTING_BAR_TITLE).threshold(4.0f).icon(context.getResources().getDrawable(R.drawable.icon200)).titleTextColor(R.color.textColor1).negativeButtonText("Never").feedbackTextColor(R.color.textColor1).positiveButtonTextColor(R.color.white).negativeButtonTextColor(R.color.textColor1).formTitle("Submit Feedback").formHint("Tell us where we can improve").formSubmitText("Submit").formCancelText("Cancel").ratingBarColor(R.color.rate_color).ratingBarBackgroundColor(R.color.color_btn1);
        RatingDialog build = ratingBarBackgroundColor.playstoreUrl(Constant.APP_PLAY_STORE_URL + context.getPackageName()).onRatingChanged(new RatingDialog.Builder.RatingDialogListener() { 
            @Override 
            public void onRatingSelected(float f, boolean z) {
                AppPref.setShowRateUs(true);
            }
        }).onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() { 
            @Override 
            public void onFormSubmitted(String str) {
                AppPref.setShowRateUs(true);
                AppPref.setShowNeverRate(true);
            }
        }).build();
        if (AppPref.isShowNeverRate()) {
            Toast.makeText(context, "Already Submitted", Toast.LENGTH_SHORT).show();
        } else {
            build.show();
        }
    }*/

    /*public static void showRatingDialogAction(final Context context) {
        RatingDialog.Builder ratingBarBackgroundColor = new RatingDialog.Builder(context).session(1).title(Constant.RATTING_BAR_TITLE).threshold(4.0f).icon(context.getResources().getDrawable(R.drawable.icon200)).titleTextColor(R.color.black).negativeButtonText("Never").positiveButtonTextColor(R.color.white).negativeButtonTextColor(R.color.black).formTitle("Submit Feedback").formHint("Tell us where we can improve").formSubmitText("Submit").formCancelText("Cancel").ratingBarColor(R.color.rate_color).ratingBarBackgroundColor(R.color.color_btn1);
        ratingBarBackgroundColor.playstoreUrl(Constant.APP_PLAY_STORE_URL + context.getPackageName()).onRatingBarFormSumbit(new RatingDialog.Builder.RatingDialogFormListener() { 
            @Override 
            public void onFormSubmitted(String str) {

                AppPref.setShowNeverRate(true);
                AppPref.setRateUsAction(context, true);
            }
        }).build().show();
    }*/



    public static String getTempDirectory(Context context) {
        File file = new File(context.getFilesDir(), "tempImage");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String FileSizeWithUnits(long j) {
        String[] strArr = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        double d = j;
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        int i = 0;
        while (i < 9 && d >= 1024.0d) {
            d /= 1024.0d;
            i++;
        }
        return decimalFormat.format(d).concat(" " + strArr[i]);
    }
    public static int getFitTextSize(Paint paint, int i, int i2, String str) {
        return Math.min((int) ((i / paint.measureText(str)) * paint.getTextSize()), i2);
    }


    public static boolean checkStoragePermissionApi30(Context context) {
        int unsafeCheckOpNoThrow = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            unsafeCheckOpNoThrow = ((AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE)).unsafeCheckOpNoThrow(AppOpsManager.permissionToOp("android.permission.MANAGE_EXTERNAL_STORAGE"), context.getApplicationInfo().uid, context.getApplicationInfo().packageName);
        }
        if (unsafeCheckOpNoThrow == 3) {
            if (context.checkCallingOrSelfPermission("android.permission.MANAGE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else if (unsafeCheckOpNoThrow == 0) {
            return true;
        }
        return false;
    }

    public static boolean checkStoragePermissionApi19(Context context) {
        return ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0 && ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") == 0;
    }

    public static void shareApp(Context context) {
        try {
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("text/plain");
            intent.putExtra("android.intent.extra.TEXT", "Fill & Sign PDF Document\n- Sign your PDF Documents, insert images, add logo, add text and add a stamp.\n- No need to use complex editor you can make changes through Fill & Sign PDF Document.\n- List of all your created PDF file, rename, share, delete it.\n- you can stamp out your document with just one click, easily place in pdf documents.\n\nhttps://play.google.com/store/apps/details?id=" + context.getPackageName());
            context.startActivity(Intent.createChooser(intent, "Share via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    public static void openUrl(Context context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(str));
        intent.addFlags(1208483840);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            showToast("No appropriate app found that handle this action. Please install a browser app");
        }
    }

    public static List<String> getDateFormatterList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add("MM/dd/yy");
        arrayList.add("dd/MM/yyyy");
        arrayList.add("MM-dd-yy");
        arrayList.add("MMM-yyyy");
        arrayList.add("MM/dd/yyyy");
        arrayList.add("MM-dd-yyyy");
        arrayList.add("dd/MM/yy");
        arrayList.add("MM.dd.yy");
        arrayList.add("MM.dd.yyyy");
        arrayList.add("dd.MM.yy");
        arrayList.add("MMM dd, yyyy");
        arrayList.add("EEE MMM dd, 2022");
        arrayList.add("EEE MMM dd, 2022 aa");
        arrayList.add("EEE, dd MMMM yyyy hh:mm:ss z");
        arrayList.add("yyyy-MM-dd hh:mm:ss z");
        return arrayList;
    }

    public static Bitmap getTextToImageBitmap(String str, int i, int i2, int i3) {
        Paint paint = new Paint(1);
        paint.setTextSize(getFitTextSize(paint, i2, i3, str));
        paint.setColor(i);
        paint.setTextAlign(Paint.Align.LEFT);
        Bitmap createBitmap = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        new Canvas(createBitmap).drawText(str, (createBitmap.getWidth() - rect.width()) / 2, (createBitmap.getHeight() + rect.height()) / 2, paint);
        return createBitmap;
    }

    public static String getTempDirectoryPath() {
        File file = new File(App.getContext().getFilesDir(), "tempWorks");
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static String getDirectoryPath() {
        File file = new File(Constant.DirectoryPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public static void DeleteTempData() {
        try {
            File file = new File(App.getContext().getFilesDir(), "tempWorks/tempPDF.pdf");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
