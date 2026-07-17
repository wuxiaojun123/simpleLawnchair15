package com.simplepdf.pdfeditor.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.simplepdf.pdfeditor.App;

import java.util.ArrayList;
import java.util.List;


public class AppPref {
    static final String AD_FREE = "AD_FREE";
    static final String IS_ProVersion = "IS_ProVersion";
    static final String IS_RATE_US = "IS_RATE_US";
    static final String IS_RATE_US_ACTION = "IS_RATE_US_ACTION";
    static final String IS_START = "IS_START";
    static final String IS_TERM_OF_SERVICE = "IS_TERM_OF_SERVICE";
    static final String MyPref = "MySignaturePref";
    static final String RECENT_ADD_TEXT = "RECENT_ADD_TEXT";
    static final String SHOW_NEVER = "SHOW_NEVER";

    public static void setHistoryPrefData(List<String> list) {
        String json = new Gson().toJson(list);
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putString(RECENT_ADD_TEXT, json);
        edit.apply();
    }

    public static List<String> getHistoryPrefData() {
        Gson gson = new Gson();
        String string = App.getContext().getSharedPreferences(MyPref, 0).getString(RECENT_ADD_TEXT, null);
        return string == null ? new ArrayList() : (List) gson.fromJson(string, new TypeToken<List<String>>() { 
        }.getType());
    }

    public static boolean getProVersion() {
        return App.getContext().getSharedPreferences(MyPref, 0).getBoolean(IS_ProVersion, false);
    }

    public static void setProVersion(boolean z) {
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(IS_ProVersion, z);
        edit.commit();
    }

    public static void setShowRateUs(boolean z) {
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(IS_RATE_US, z);
        edit.apply();
    }

    public static boolean getShowRateUs() {
        return App.getContext().getSharedPreferences(MyPref, 0).getBoolean(IS_RATE_US, false);
    }

    public static boolean isShowNeverRate() {
        return App.getContext().getSharedPreferences(MyPref, 0).getBoolean(SHOW_NEVER, false);
    }

    public static void setShowNeverRate(boolean z) {
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(SHOW_NEVER, z);
        edit.apply();
    }

    public static void setIsTermOfService(boolean z) {
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(IS_TERM_OF_SERVICE, z);
        edit.apply();
    }

    public static boolean isIsTermOfService() {
        return App.getContext().getSharedPreferences(MyPref, 0).getBoolean(IS_TERM_OF_SERVICE, false);
    }

    public static void setStartApp(boolean z) {
        SharedPreferences.Editor edit = App.getContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(IS_START, z);
        edit.apply();
    }

    public static boolean getStartApp() {
        return App.getContext().getSharedPreferences(MyPref, 0).getBoolean(IS_START, false);
    }

    public static boolean IsRateUsAction(Context context) {
        return context.getApplicationContext().getSharedPreferences(MyPref, 0).getBoolean(IS_RATE_US_ACTION, false);
    }

    public static void setRateUsAction(Context context, boolean z) {
        SharedPreferences.Editor edit = context.getApplicationContext().getSharedPreferences(MyPref, 0).edit();
        edit.putBoolean(IS_RATE_US_ACTION, z);
        edit.commit();
    }
}
