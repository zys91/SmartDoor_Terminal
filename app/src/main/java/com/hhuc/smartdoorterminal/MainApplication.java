package com.hhuc.smartdoorterminal;

import android.app.Application;

public class MainApplication extends Application {

    public static com.hhuc.smartdoorterminal.MainApplication mainApplication;
    public static String app_uid = null;
    public static String app_hid = null;
    public static String app_tel = null;
    public static int size_flag = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        mainApplication = this;
    }

}
