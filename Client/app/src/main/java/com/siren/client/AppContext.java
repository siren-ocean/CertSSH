package com.siren.client;

import android.app.Application;

/**
 * 全局Application
 * Created by Siren on 2022/9/15.
 */
public class AppContext extends Application {

    public static AppContext get() {
        return mContext;
    }

    private static AppContext mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
