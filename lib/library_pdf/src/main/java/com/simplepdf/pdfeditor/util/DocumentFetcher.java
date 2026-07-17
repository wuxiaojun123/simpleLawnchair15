package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.simplepdf.pdfeditor.model.FileListModel;

import org.bouncycastle.i18n.MessageBundle;

import java.util.ArrayList;
import java.util.List;


public class DocumentFetcher {
    public static final String FILE_TYPE_PDF = "pdf";
    public static final String FILE_TYPE_PDF_CAPS = "PDF";
    public static int ORDER_AZ = 1;
    public static int PDF = 2;


    public interface OnFileFetchListnear {
        void onFileFetched(List<FileListModel> list);
    }

    public DocumentFetcher(final Context context, int i, final int i2, final String str, int i3, final OnFileFetchListnear onFileFetchListnear) {
        LoaderManager.getInstance((AppCompatActivity) context).initLoader(i, null, new LoaderManager.LoaderCallbacks<Cursor>() { 
            private String sortOrder;

            @Override 
            public void onLoaderReset(Loader<Cursor> loader) {
            }

            @Override 
            public Loader<Cursor> onCreateLoader(int i4, Bundle bundle) {
                String str2;
                Uri contentUri;
                String[] strArr = {"_display_name", "date_added", "_data", "mime_type", "_size", MessageBundle.TITLE_ENTRY, "_id"};
                this.sortOrder = "date_added DESC";
                if (str.isEmpty()) {
                    str2 = "";
                } else {
                    str2 = String.valueOf(new String[]{"%" + (Environment.DIRECTORY_DOCUMENTS + "/Docx") + "%"});
                }
                String str3 = str2 + DocumentFetcher.this.getSelectionFromType(i2);
                if (Build.VERSION.SDK_INT >= 29) {
                    contentUri = MediaStore.Files.getContentUri("external");
                } else {
                    contentUri = MediaStore.Files.getContentUri("external");
                }
                return new CursorLoader(context, contentUri, strArr, str3, null, this.sortOrder);
            }

            @Override 
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                if (cursor != null) {
                    try {
                        if (cursor.isClosed()) {
                            return;
                        }
                        Log.d("TAG", "onLoadFinished: " + cursor.getCount());
                        ArrayList arrayList = new ArrayList();
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex("_data");
                            int columnIndex2 = cursor.getColumnIndex("date_added");
                            cursor.getColumnIndex(MessageBundle.TITLE_ENTRY);
                            int columnIndex3 = cursor.getColumnIndex("_size");
                            int columnIndex4 = cursor.getColumnIndex("mime_type");
                            cursor.getColumnIndex("_id");
                            do {
                                FileListModel fileListModel = new FileListModel();
                                fileListModel.setFilename(cursor.getString(columnIndex).substring(cursor.getString(columnIndex).lastIndexOf("/") + 1));
                                fileListModel.setFilePath(cursor.getString(columnIndex));
                                if (!TextUtils.isEmpty(cursor.getString(columnIndex3)) && !TextUtils.isEmpty(cursor.getString(columnIndex2))) {
                                    fileListModel.setFileDate(Long.parseLong(cursor.getString(columnIndex2)) * 1000);
                                    fileListModel.setFileSize(Long.parseLong(cursor.getString(columnIndex3)));
                                    fileListModel.setFileType(cursor.getString(columnIndex4));
                                    if (fileListModel.getFileSize() > 0) {
                                        arrayList.add(fileListModel);
                                    }
                                }
                            } while (cursor.moveToNext());
                            cursor.close();
                            onFileFetchListnear.onFileFetched(arrayList);
                        }
                        cursor.close();
                        onFileFetchListnear.onFileFetched(arrayList);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public String getSelectionFromType(int i) {
        String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FILE_TYPE_PDF);
        String mimeTypeFromExtension2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FILE_TYPE_PDF_CAPS);
        return "mime_type LIKE '" + mimeTypeFromExtension + "%' OR mime_type LIKE '" + mimeTypeFromExtension2 + "%' ";
    }
}
