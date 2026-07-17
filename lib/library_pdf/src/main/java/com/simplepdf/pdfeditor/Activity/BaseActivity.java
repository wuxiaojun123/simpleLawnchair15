package com.simplepdf.pdfeditor.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ToolbarBinding;
import com.simplepdf.pdfeditor.util.AppConstants;
import com.simplepdf.pdfeditor.util.BetterActivityResult;

import io.reactivex.disposables.CompositeDisposable;


public abstract class BaseActivity extends AppCompatActivity {
    public AssetManager assetManager;
    public Activity mContext;
    protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
    public CompositeDisposable disposable = new CompositeDisposable();
    public final int READ_WRITE_PERMISSION = 1001;
    public boolean permissionNotify = false;

    public abstract void init();

    public abstract void setBinding();

    public abstract void setToolbar();

    public abstract void setViewListener();

    @Override 
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = this;
        this.permissionNotify = false;
        this.assetManager = getAssets();
        setBinding();
        setToolbar();
        init();
        setViewListener();
    }

    public void setActionBarToolbar(boolean z, String str, ToolbarBinding toolbarBinding) {
        try {
            setSupportActionBar(toolbarBinding.toolbar);
            if (z) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                toolbarBinding.toolbar.setNavigationIcon(R.drawable.ic_back);
            }
            toolbarBinding.tvToolbarTitle.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setToolbarData(boolean z, String str) {
        if (z) {
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        getSupportActionBar().setElevation(1.0f);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        spannableStringBuilder.setSpan(new StyleSpan(1), 0, str.length(), 33);
        getSupportActionBar().setTitle(spannableStringBuilder);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.textColor1)));
    }

    public void setToolbarData(String str, ToolbarBinding toolbarBinding) {
        try {
            setSupportActionBar(toolbarBinding.toolbar);
            getSupportActionBar().setElevation(1.0f);
            toolbarBinding.tvToolbarTitle.setText(AppConstants.fromHtml(str));
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToast(String str) {
        Toast.makeText(this.mContext, str, Toast.LENGTH_LONG).show();
    }

    public void showLog(String str) {
        Log.e("AppLog: ", str);
    }
}
