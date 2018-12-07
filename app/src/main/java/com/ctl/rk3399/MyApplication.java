package com.ctl.rk3399;

import android.app.Application;
import android.content.Context;

import com.ctl.rk3399.bean.AppConfig;
import com.ctl.rk3399.utils.DeviceInfoUtils;
import com.ctl.rk3399.utils.LogUtils;

import java.io.File;
import java.io.IOException;


public class MyApplication extends Application {

    //Application单例
    private static MyApplication instance;
    //Context
    private Context context;

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    //屏幕尺寸
    private int screenWidth;
    private int screenHeight;
    //应用的最高申请内存
    private long maxMemory;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        context = this.getApplicationContext();
//        CrashReport.initCrashReport(this, "900011702", AppConfig.DEBUG);//bugly
        LogUtils.init(this, AppConfig.TAG, AppConfig.DEBUG);//初始化LOG
        screenWidth = DeviceInfoUtils.getScreenWidth(this);//获取屏幕宽度
        screenHeight = DeviceInfoUtils.getScreenHeight(this);//获取屏幕高度
        initMemorySize();//打印APP最大可以申请的内存

    }

    //用于打印APP最多可申请的内存
    private void initMemorySize() {
        maxMemory = Runtime.getRuntime().maxMemory();
        LogUtils.i("最大申请内存:" + Long.toString(maxMemory / (1024 * 1024)) + "MB");
    }


    public Context getContext() {
        return context;
    }
}
