package com.simplepdf.pdfeditor.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

import androidx.databinding.DataBindingUtil;

import com.simplepdf.pdfeditor.R;
import com.simplepdf.pdfeditor.databinding.ActivitySplashBinding;
import com.simplepdf.pdfeditor.util.AppPref;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class SplashActivity extends BaseActivity {
    public static boolean Ad_Show = false;
    public static boolean isRate = false;
    public static boolean isRateFlag = false;
    ActivitySplashBinding binding;
    Context context;
    SplashActivity splash_activity;
    private WeakReference<SplashActivity> splash_activityWeakReference;

    @Override
    public void setToolbar() {
    }

    @Override
    public void setViewListener() {
    }

    @Override
    public void setBinding() {
        this.binding = (ActivitySplashBinding) DataBindingUtil.setContentView(this, R.layout.activity_splash);
        this.context = this;
        this.splash_activity = this;
        this.splash_activityWeakReference = new WeakReference<>(this.splash_activity);
        this.binding.textView.setText(R.string.app_name);

        AppPref.setProVersion(true);
    }

    @Override
    public void init() {
        new Handler(getMainLooper()).postDelayed(new C08841(), 1200L);
    }


    class C08841 implements Runnable {
        C08841() {
        }

        @Override
        public void run() {
            SplashActivity.this.GoToMainScreen();

        }
    }


    public void GoToMainScreen() {
        Ad_Show = false;
        try {
            if (!AppPref.getStartApp()) {
                startActivity(new Intent(this, IntroActivity.class));
            } else {
                HandleExternalData();
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void HandleExternalData() {
        Uri data;
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (("android.intent.action.SEND".equals(action) || "android.intent.action.VIEW".equals(action)) && type != null) {
            if ("application/pdf".equals(type)) {
                if ("android.intent.action.SEND".equals(action)) {
                    data = (Uri) intent.getParcelableExtra("android.intent.extra.STREAM");
                } else {
                    data = "android.intent.action.VIEW".equals(action) ? intent.getData() : null;
                }
                if (data != null) {
                    ArrayList<Uri> arrayList = new ArrayList<>();
                    arrayList.add(data);
                    StartSignatureActivity("PDFOpen", arrayList);
                    return;
                }
                return;
            }
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
    }

    public void StartSignatureActivity(String str, ArrayList<Uri> arrayList) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("ActivityAction", str);
        intent.putExtra("PDFOpen", arrayList);
        startActivity(intent);
    }
}
