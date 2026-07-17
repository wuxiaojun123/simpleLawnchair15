package com.simplepdf.pdfeditor.Signature;

import android.content.Intent;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.Activity.BaseActivity;
import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ActivityColorPickerBinding;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;


public class colorPickerActivity extends BaseActivity {
    ActivityColorPickerBinding binding;
    int signatureColor = 0;

    @Override 
    public void setToolbar() {
    }

    @Override 
    public void setBinding() {
        this.binding = (ActivityColorPickerBinding) DataBindingUtil.setContentView(this, R.layout.activity_color_picker);
    }

    @Override 
    public void init() {


//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);


        this.signatureColor = getResources().getColor(R.color.inkblue);
        this.binding.colorPickerView.setInitialColor(this.signatureColor);
        this.binding.colorPickerView.setColorListener(new ColorEnvelopeListener() { 
            @Override 
            public final void onColorSelected(ColorEnvelope colorEnvelope, boolean z) {
                colorPickerActivity.this.m126xaff29429(colorEnvelope, z);
            }
        });
    }

    
    
    public  void m126xaff29429(ColorEnvelope colorEnvelope, boolean z) {
        this.signatureColor = colorEnvelope.getColor();
        this.binding.alphaTileView.setPaintColor(colorEnvelope.getColor());
    }

    @Override 
    public void setViewListener() {
        this.binding.mcvBack.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                colorPickerActivity.this.onBackPressed();
            }
        });
        this.binding.mBtnSave.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public void onClick(View view) {
                Intent intent = colorPickerActivity.this.getIntent();
                intent.putExtra("pickColor", colorPickerActivity.this.signatureColor);
                colorPickerActivity.this.setResult(-1, intent);
                colorPickerActivity.this.finish();
            }
        });
    }
}
