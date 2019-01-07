package com.example.mycustomview.activity;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext=this.getApplicationContext();
    }

    public static Context getContext(){
        return mContext;
    }
}
