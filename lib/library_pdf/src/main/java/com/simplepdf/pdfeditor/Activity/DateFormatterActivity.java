package com.simplepdf.pdfeditor.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simplepdf.pdfeditor.CallbackListener.DatePickerDialogListener;
import com.simplepdf.pdfeditor.CallbackListener.RecycleViewCallBackListener;
import com.simplepdf.pdfeditor.DateTimePIckerDialog.DateTimePickerDialogFragment;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.DateFormatAdapter;
import com.simplepdf.pdfeditor.databinding.ActivityDateFormatterBinding;
import com.simplepdf.pdfeditor.databinding.ColorPickerDialogBinding;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class DateFormatterActivity extends BaseActivity {
    DateFormatAdapter adapter;
    ActivityDateFormatterBinding binding;
    List<String> listDateFormatter = new ArrayList();
    int signatureColor;

    @Override
    public void setViewListener() {
    }

    @Override
    public void setBinding() {
        ActivityDateFormatterBinding activityDateFormatterBinding = (ActivityDateFormatterBinding) DataBindingUtil.setContentView(this, R.layout.activity_date_formatter);
        this.binding = activityDateFormatterBinding;
    }

    @Override
    public void setToolbar() {
        setActionBarToolbar(true, "Dates", this.binding.toolbar);
    }

    @Override
    public void init() {
//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);
//        adAdmob.FullscreenAd(this);


        this.listDateFormatter = AppConstants.getDateFormatterList();
        this.binding.rvDateFormatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.binding.rvDateFormatRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this.binding.rvDateFormatRecyclerView.getContext(), 1);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.date_divider));
        this.binding.rvDateFormatRecyclerView.addItemDecoration(dividerItemDecoration);
        this.adapter = new DateFormatAdapter(this, this.listDateFormatter, new RecycleViewCallBackListener() {
            @Override
            public final void onItemClicked(int i, int i2) {
                DateFormatterActivity.this.m54x389fce8f(i, i2);
            }
        });
        this.binding.rvDateFormatRecyclerView.setAdapter(this.adapter);
    }


    public void m54x389fce8f(int i, int i2) {
        showColorPicker(i2);
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
                DateFormatterActivity.this.m55x96182b84(colorPickerDialogBinding, colorEnvelope, z);
            }
        });
        colorPickerDialogBinding.cardSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                DateFormatterActivity.this.m56x230542a3(i, dialog, view);
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


    public void m55x96182b84(ColorPickerDialogBinding colorPickerDialogBinding, ColorEnvelope colorEnvelope, boolean z) {
        this.signatureColor = colorEnvelope.getColor();
        colorPickerDialogBinding.alphaTileView.setPaintColor(colorEnvelope.getColor());
    }


    public void m56x230542a3(int i, Dialog dialog, View view) {
        try {
            Bitmap textToImageBitmap = AppConstants.getTextToImageBitmap(AppConstants.getFormattedDate(this.adapter.getCurrentMillis(), new SimpleDateFormat(this.listDateFormatter.get(i))), this.signatureColor, AppConstants.DEFAULT_WIDTH, AppConstants.DEFAULT_HEIGHT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem add = menu.add(0, 1, 1, "Date");
        add.setIcon(R.drawable.menu_date);
        add.setShowAsActionFlags(2);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == 1) {
            DateTimePickerDialogFragment.newInstance(new DatePickerDialogListener() {
                @Override
                public void onDatePicked(Calendar calendar) {
                    DateFormatterActivity.this.adapter.setCurrentMillis(calendar.getTimeInMillis());
                }
            }).show(getSupportFragmentManager(), "DatePicker");
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
