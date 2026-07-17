package com.simplepdf.pdfeditor.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.adapter.AssertStampAdapter;
import com.simplepdf.pdfeditor.databinding.ActivityStampsBinding;
import com.simplepdf.pdfeditor.model.IconBitmapModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class StampsActivity extends BaseActivity {
    AssertStampAdapter adapter;
    ActivityStampsBinding binding;

    @Override 
    public void setViewListener() {
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override 
    public void setBinding() {
        ActivityStampsBinding activityStampsBinding = (ActivityStampsBinding) DataBindingUtil.setContentView(this, R.layout.activity_stamps);
        this.binding = activityStampsBinding;
    }

    @Override 
    public void setToolbar() {
        setActionBarToolbar(true, "Stamps", this.binding.toolbar);
    }

    @Override 
    public void init() {


//        AdAdmob adAdmob = new AdAdmob( this);
//        adAdmob.BannerAd(binding.banner, this);
//        adAdmob.FullscreenAd(this);



        String[] list;
        this.binding.rvStampsList.setLayoutManager(new GridLayoutManager(this, 2));
        this.binding.rvStampsList.setHasFixedSize(true);
        final ArrayList arrayList = new ArrayList();
        try {
            for (String str : this.assetManager.list("stamps")) {
                arrayList.add(new IconBitmapModel(str, BitmapFactory.decodeStream(this.assetManager.open("stamps/" + str))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        AssertStampAdapter assertStampAdapter = new AssertStampAdapter(this, arrayList);
        this.adapter = assertStampAdapter;
        assertStampAdapter.setOnItemClickListener(new AssertStampAdapter.OnItemClickListener() { 
            @Override 
            public final void onItemClick(View view, int i) {
                StampsActivity.this.m92lambda$init$0$comappworldpdfsignatureActivityStampsActivity(arrayList, view, i);
            }
        });
        this.binding.rvStampsList.setAdapter(this.adapter);
    }

    
    
    public  void m92lambda$init$0$comappworldpdfsignatureActivityStampsActivity(List list, View view, int i) {
        Bitmap iconBitmap = ((IconBitmapModel) list.get(i)).getIconBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        Intent intent = new Intent();
        intent.putExtra("image_byteArray", byteArray);
        setResult(-1, intent);
        finish();
    }
}
