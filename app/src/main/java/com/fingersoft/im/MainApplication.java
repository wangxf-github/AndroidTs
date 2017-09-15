package com.fingersoft.im;

import android.app.Application;

import com.mobile.device.manage.core.MDMService;

/**
 * Created by Administrator on 2017/9/1.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            MDMService.getInstance().init(getApplicationContext(),"6c611dfbdd74a938");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
