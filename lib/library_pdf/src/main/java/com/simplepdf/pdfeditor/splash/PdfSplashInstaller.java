package com.simplepdf.pdfeditor.splash;

import com.nice.library_splash.SplashFragmentFactory;
import com.nice.library_splash.SplashRegistry;

public final class PdfSplashInstaller {

    private PdfSplashInstaller() {
    }

    public static void install() {
        SplashRegistry.setFragmentFactory(new SplashFragmentFactory() {
            @Override
            public PdfSplashFragment createFragment() {
                return new PdfSplashFragment();
            }
        });
    }
}
