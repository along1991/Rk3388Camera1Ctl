package com.ctl.rk3399.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ctl.rk3399.MyApplication;
import com.ctl.rk3399.R;
import com.ctl.rk3399.utils.ActivityManager;


public abstract class DefaultBaseActivity extends BaseActivity {

    protected Context context;
    protected Activity activity;

    protected int screenWidth;
    protected int screenHeight;

    protected boolean addTask = true;

    protected void thisHome() {
        this.addTask = false;
    }

    @Override
    protected void onBefore() {
        super.onBefore();
        this.context = this;
        this.activity = this;
        screenWidth = MyApplication.getInstance().getScreenWidth();
        screenHeight = MyApplication.getInstance().getScreenHeight();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (addTask)
            ActivityManager.getInstance().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addTask)
            ActivityManager.getInstance().delActivity(this);
    }

    /**
     * 增加默认的界面切换动画
     */
    @Override
    public void startActivity(Intent intent) {
        startActivity(intent, true);
    }

    public void startActivity(Intent intent, boolean anim) {
        super.startActivity(intent);
        if (anim) overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode, true);
    }

    public void startActivityForResult(Intent intent, int requestCode, boolean anim) {
        super.startActivityForResult(intent, requestCode);
        if (anim) overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public void finish() {
        finish(true);
    }

    public void finish(boolean anim) {
        super.finish();
        if (anim) overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    /*protected void getImgTitleBar() {
        titleBack = (ImageButton) findViewById(R.id.title_back);
        titleText = (TextView) findViewById(R.id.title_text);
        titleActionImg = (ImageButton) findViewById(R.id.title_action);
    }*/

}
