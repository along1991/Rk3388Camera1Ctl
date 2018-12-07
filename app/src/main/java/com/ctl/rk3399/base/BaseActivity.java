package com.ctl.rk3399.base;

import android.os.Bundle;
import android.view.KeyEvent;


import com.ctl.rk3399.utils.PermissionRequest;

import androidx.fragment.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onBefore();
        super.onCreate(savedInstanceState);
        initialize();
        initView();
        initData();
        onAfter();
    }

    /**
     * onCreate 执行之前的操作
     */
    protected void onBefore() {
    }

    /**
     * 用于初始化对象,获取Intent数据等操作
     */
    protected abstract void initialize();

    /**
     * 用于初始化视图,获取控件实例
     */
    protected abstract void initView();

    /**
     * 用于初始化数据,填充视图
     */
    protected void initData() {
    }

    /**
     * 用于执行数据填充完后的操作
     */
    protected void onAfter() {
    }


    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onPressBack()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 复写返回键操作,返回true则不继续下发
     *
     * @return
     */
    protected boolean onPressBack() {
        return false;
    }
}
