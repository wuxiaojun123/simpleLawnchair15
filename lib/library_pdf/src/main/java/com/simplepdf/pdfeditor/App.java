package com.simplepdf.pdfeditor;

import android.app.Application;
import android.content.Context;


public class App extends Application {
    private static Context context;
    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        init(this);
    }

    public static void init(Context appContext) {
        context = appContext.getApplicationContext();
        if (appContext instanceof App) {
            mInstance = (App) appContext;
        }
    }

    public static Context getContext() {
        return context;
    }

    public static synchronized App getInstance() {
        App app;
        synchronized (App.class) {
            app = mInstance;
        }
        return app;
    }

}
