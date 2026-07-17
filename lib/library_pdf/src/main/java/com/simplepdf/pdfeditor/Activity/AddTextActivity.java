package com.simplepdf.pdfeditor.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepdf.pdfeditor.CallbackListener.RecycleListener;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.TextAdapter;
import com.simplepdf.pdfeditor.databinding.ActivityAddTextBinding;
import com.simplepdf.pdfeditor.databinding.ColorPickerDialogBinding;
import com.simplepdf.pdfeditor.databinding.FileAlertDialogBinding;
import com.simplepdf.pdfeditor.databinding.SignatureDeletetDialogBinding;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.AppPref;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class AddTextActivity extends BaseActivity {
    TextAdapter adapter;
    ActivityAddTextBinding binding;
    int signatureColor;
    List<String> textList = new ArrayList();

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override 
    public void setBinding() {
        ActivityAddTextBinding activityAddTextBinding = (ActivityAddTextBinding) DataBindingUtil.setContentView(this, R.layout.activity_add_text);
        this.binding = activityAddTextBinding;
        this.textList = AppPref.getHistoryPrefData();
    }

    @Override 
    public void setToolbar() {
        setActionBarToolbar(true, "Text", this.binding.toolbar);
    }

    @Override 
    public void init() {
        this.binding.noData.txtNoData.setText("No Text Available");
        this.binding.noData.txtDescription.setText("Create a new Text");
        this.binding.rvTextRecyclerView.setLayoutManager(new LinearLayoutManager(this));


//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);
//        adAdmob.FullscreenAd(this);


        setRecyclerView();
    }

    public void setRecyclerView() {
        this.adapter = new TextAdapter(this, this.textList, new RecycleListener() {
            @Override 
            public void onItemClicked(int i, int i2, View view) {
                if (i == R.id.mcvTextOption) {
                    AddTextActivity.this.OpenOptionMenu(i2, view);
                } else if (i == R.id.cdText) {
                    AddTextActivity.this.showColorPicker(i2);
                }
            }
        });
        this.binding.rvTextRecyclerView.setAdapter(this.adapter);
        this.binding.rvTextRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() { 
            @Override 
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (i2 > 0) {
                    AddTextActivity.this.binding.fabAddText.hide();
                } else {
                    AddTextActivity.this.binding.fabAddText.show();
                }
                super.onScrolled(recyclerView, i, i2);
            }
        });
        noDataView();
    }

    public void noDataView() {
        this.binding.noData.nodata.setVisibility(this.textList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void showColorPicker(final int i) {
        final ColorPickerDialogBinding colorPickerDialogBinding = (ColorPickerDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.color_picker_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(colorPickerDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        this.signatureColor = getResources().getColor(R.color.black);
        colorPickerDialogBinding.colorPickerView.setInitialColor(this.signatureColor);
        colorPickerDialogBinding.colorPickerView.setColorListener(new ColorEnvelopeListener() { 
            @Override 
            public final void onColorSelected(ColorEnvelope colorEnvelope, boolean z) {
                AddTextActivity.this.m50x872ffa97(colorPickerDialogBinding, colorEnvelope, z);
            }
        });
        colorPickerDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AddTextActivity.this.m51xb50894f6(i, dialog, view);
            }
        });
        colorPickerDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    
    
    public  void m50x872ffa97(ColorPickerDialogBinding colorPickerDialogBinding, ColorEnvelope colorEnvelope, boolean z) {
        this.signatureColor = colorEnvelope.getColor();
        colorPickerDialogBinding.alphaTileView.setPaintColor(colorEnvelope.getColor());
    }

    
    
    public  void m51xb50894f6(int i, Dialog dialog, View view) {
        try {
            Bitmap textToImageBitmap = AppConstants.getTextToImageBitmap(this.textList.get(i), this.signatureColor, AppConstants.DEFAULT_WIDTH, AppConstants.DEFAULT_HEIGHT);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            textToImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            Intent intent = new Intent();
            intent.putExtra("image_byteArray", byteArray);
            setResult(-1, intent);
            finish();
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override 
    public void setViewListener() {
        this.binding.fabAddText.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AddTextActivity.this.m49x19cdaaad(view);
            }
        });
    }

    
    
    public  void m49x19cdaaad(View view) {

            addEditText();
    }

    public Bitmap drawTextToBitmap(Context context, int i, String str) {
        Resources resources = context.getResources();
        float f = resources.getDisplayMetrics().density;
        Bitmap decodeResource = BitmapFactory.decodeResource(resources, i);
        Bitmap.Config config = decodeResource.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        Bitmap copy = decodeResource.copy(config, true);
        Canvas canvas = new Canvas(copy);
        Paint paint = new Paint(1);
        paint.setColor(Color.rgb(61, 61, 61));
        paint.setTextSize((int) (f * 14.0f));
        paint.setShadowLayer(1.0f, 0.0f, 1.0f, -1);
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        canvas.drawText(str, (copy.getWidth() - rect.width()) / 2, (copy.getHeight() + rect.height()) / 2, paint);
        return copy;
    }

    public void addEditText() {
        final FileAlertDialogBinding fileAlertDialogBinding = (FileAlertDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.file_alert_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(fileAlertDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        fileAlertDialogBinding.txtHeading.setText("Enter Text");
        fileAlertDialogBinding.txtTitle.setText("Text");
        fileAlertDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AddTextActivity.this.m46x6474818f(fileAlertDialogBinding, dialog, view);
            }
        });
        fileAlertDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    
    
    public  void m46x6474818f(FileAlertDialogBinding fileAlertDialogBinding, Dialog dialog, View view) {
        try {
            String obj = fileAlertDialogBinding.etFileName.getText().toString();
            if (obj.length() == 0) {
                showToast("Text should not be empty");
            } else {
                this.textList.add(obj);
                this.adapter.notifyItemInserted(this.textList.size());
                AppPref.setHistoryPrefData(this.textList);
                noDataView();
            }
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OpenOptionMenu(final int i, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_text_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { 
            @Override 
            public final boolean onMenuItemClick(MenuItem menuItem) {
                return AddTextActivity.this.m45x9e561f0d(i, menuItem);
            }
        });
        popupMenu.show();
    }

    
    
    public  boolean m45x9e561f0d(int i, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.menu_delete) {
            deleteText(i);
            return true;
        } else if (menuItem.getItemId() == R.id.menu_edit) {
            editText(i);
            return true;
        } else {
            return false;
        }
    }

    public void deleteText(final int i) {
        SignatureDeletetDialogBinding signatureDeletetDialogBinding = (SignatureDeletetDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.signature_deletet_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        signatureDeletetDialogBinding.txtDescription.setText("Do you really want to delete\nthese Text?");
        dialog.setContentView(signatureDeletetDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        signatureDeletetDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AddTextActivity.this.m47x5ac06332(i, dialog, view);
            }
        });
        signatureDeletetDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    
    
    public  void m47x5ac06332(int i, Dialog dialog, View view) {
        try {
            this.textList.remove(i);
            this.adapter.notifyItemRemoved(i);
            AppPref.setHistoryPrefData(this.textList);
            noDataView();
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void editText(final int i) {
        final FileAlertDialogBinding fileAlertDialogBinding = (FileAlertDialogBinding) DataBindingUtil.inflate(LayoutInflater.from(this.mContext), R.layout.file_alert_dialog, null, false);
        final Dialog dialog = new Dialog(this.mContext);
        dialog.setContentView(fileAlertDialogBinding.getRoot());
        dialog.getWindow().setBackgroundDrawableResource(17170445);
        dialog.getWindow().setLayout(-1, -2);
        dialog.setCancelable(false);
        fileAlertDialogBinding.txtTitle.setText("Rename");
        fileAlertDialogBinding.txtHeading.setText("Rename Text");
        fileAlertDialogBinding.etFileName.setText(this.textList.get(i));
        fileAlertDialogBinding.txtSave.setText("Update");
        fileAlertDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                AddTextActivity.this.m48x5daeb131(fileAlertDialogBinding, i, dialog, view);
            }
        });
        fileAlertDialogBinding.cardCancel.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    
    
    public  void m48x5daeb131(FileAlertDialogBinding fileAlertDialogBinding, int i, Dialog dialog, View view) {
        try {
            String obj = fileAlertDialogBinding.etFileName.getText().toString();
            if (TextUtils.isEmpty(obj)) {
                showToast("Empty name not allow");
                return;
            }
            this.textList.set(i, obj);
            this.adapter.notifyItemChanged(i);
            AppPref.setHistoryPrefData(this.textList);
            showToast("Rename Text Successfully");
            dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
