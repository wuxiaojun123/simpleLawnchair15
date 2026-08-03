package com.simplepdf.pdfeditor.splash;

import com.nice.library_splash.SplashFragmentFactory;
import com.nice.library_splash.SplashRegistry;
import com.simplepdf.pdfeditor.fragment.PDFSplashFragment;

public final class PdfSplashInstaller {

    private PdfSplashInstaller() {
    }

    public static void install() {
        SplashRegistry.setFragmentFactory(new SplashFragmentFactory() {
            @Override
            public PDFSplashFragment createFragment() {
                return new PDFSplashFragment();
            }
        });
    }
}
