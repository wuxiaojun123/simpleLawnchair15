package com.simplepdf.pdfeditor.util;

import android.os.Environment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class Constant {
    public static final int AD_DATE = 222;
    public static final int AD_ICON = 444;
    public static final int AD_PHOTO = 555;
    public static final int AD_SIGN = 111;
    public static final int AD_STAMPS = 333;
    public static final int AD_TEXT = 100;
    public static final String APP_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=";
    public static final int CAMERA_PERMISSION = 1002;
    public static final int CHOOSE_PDF = 66;
    public static String DISCLOSURE_DIALOG_DESC = "We would like to inform you regarding the 'Consent to Collection and Use Of PostFeed'\n\nApp needs manage external storage permission to get files.\n\nWe store your data on your device only, we don’t store them on our server.";
    public static final int IMAGE_LIBRARY = 666;
    public static final String RATTING_BAR_TITLE = "Support us by giving rate and your precious review !!\nIt will take few seconds only.";

    public static String DirectoryPath = Environment.getExternalStorageDirectory().getPath() + "/PDF File/PDFSignature";
    public static String SignaturePath = Environment.getExternalStorageDirectory().getPath() + "/PDF File/DigitalSignature";
    public static int REQUEST_CODE_CAMERA = 1111;
    public static final DateFormat FILE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault());
    public static final DateFormat SYNC_DATE_FORMAT = new SimpleDateFormat("EEE, MMM dd hh:mm aa", Locale.getDefault());
    public static final DateFormat TODAY_TIME_FORMAT = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
    public static final DateFormat HISTORY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    public static final DateFormat FILE_SUFFIX_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
}
