package com.mpaas.demo.sync;

import android.app.Application;

import com.mpaas.mas.adapter.api.MPLogger;
import com.mpaas.mss.adapter.api.MPSync;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MPSync.setup(this);
        MPLogger.setUserId("mPaaSTest");
    }
}
