package com.simplepdf.pdfeditor.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nice.library_splash.SplashActivity;
import com.simplepdf.pdfeditor.R;

public class PdfLauncherChoiceActivity extends AppCompatActivity {

    private static final String DEFAULT_TARGET_ACTIVITY = "app.lawnchair.LawnchairLauncher";

    private boolean waitingForDefaultLauncherResult = false;
    private String targetActivityClassName = DEFAULT_TARGET_ACTIVITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        targetActivityClassName = getIntent().getStringExtra(SplashActivity.EXTRA_TARGET_ACTIVITY);
        if (targetActivityClassName == null || targetActivityClassName.length() == 0) {
            targetActivityClassName = DEFAULT_TARGET_ACTIVITY;
        }
        setContentView(R.layout.activity_pdf_launcher_choice);
        findViewById(R.id.buttonSetDefaultLauncher).setOnClickListener(view -> requestSetDefaultLauncher());
        findViewById(R.id.buttonOpenPdf).setOnClickListener(view -> openPdfMainActivity());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (waitingForDefaultLauncherResult && isTargetDefaultLauncher()) {
            waitingForDefaultLauncherResult = false;
            openTargetLauncherActivity();
        }
    }

    private void requestSetDefaultLauncher() {
        if (isTargetDefaultLauncher()) {
            openTargetLauncherActivity();
            return;
        }

        waitingForDefaultLauncherResult = true;
        boolean openedSettings = startFirstAvailableActivity(
                new Intent(Settings.ACTION_HOME_SETTINGS),
                new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
                new Intent(Settings.ACTION_SETTINGS)
        );
        if (!openedSettings) {
            waitingForDefaultLauncherResult = false;
            Toast.makeText(this, R.string.home_settings_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openTargetLauncherActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(getPackageName(), targetActivityClassName);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isTargetDefaultLauncher() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        android.content.pm.ResolveInfo resolveInfo = getPackageManager().resolveActivity(
                homeIntent,
                PackageManager.MATCH_DEFAULT_ONLY
        );
        if (resolveInfo == null || resolveInfo.activityInfo == null) {
            return false;
        }
        return getPackageName().equals(resolveInfo.activityInfo.packageName)
                && targetActivityClassName.equals(resolveInfo.activityInfo.name);
    }

    private boolean startFirstAvailableActivity(Intent... intents) {
        for (Intent intent : intents) {
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                return true;
            }
        }
        return false;
    }
}