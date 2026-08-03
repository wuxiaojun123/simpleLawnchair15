package com.simplepdf.pdfeditor.splash;

import android.content.Context;
import android.content.Intent;

import com.nice.library_splash.SplashFragmentFactory;
import com.nice.library_splash.SplashRegistry;
import com.simplepdf.pdfeditor.Activity.MainActivity;
import com.simplepdf.pdfeditor.Activity.PdfLauncherChoiceActivity;
import com.simplepdf.pdfeditor.fragment.PDFSplashFragment;

public final class PdfSplashInstaller {

    private static final String TARGET_LAUNCHER_ACTIVITY = "app.lawnchair.LawnchairLauncher";

    private PdfSplashInstaller() {
    }

    public static void install() {
        SplashRegistry.setFragmentFactory(new SplashFragmentFactory() {
            @Override
            public PDFSplashFragment createFragment() {
                return new PDFSplashFragment();
            }
        });
        SplashRegistry.setTargetActivityFactory(PdfSplashInstaller::createTargetLauncherIntent);
        SplashRegistry.setFallbackActivityFactory(PdfSplashInstaller::createChoiceIntent);
        SplashRegistry.setDefaultHomeFallbackActivityFactory(PdfSplashInstaller::createPdfMainIntent);
    }

    private static Intent createTargetLauncherIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(context.getPackageName(), TARGET_LAUNCHER_ACTIVITY);
        intent.addCategory(Intent.CATEGORY_HOME);
        return intent;
    }

    private static Intent createChoiceIntent(Context context) {
        return new Intent(context, PdfLauncherChoiceActivity.class);
    }

    private static Intent createPdfMainIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
}
