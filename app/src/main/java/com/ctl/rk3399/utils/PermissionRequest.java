package com.ctl.rk3399.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionRequest {
    private String[] permissionArray = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final int REQUEST_CODE_PERMISSION_ALL = 1;
    public static final int REQUEST_CODE_PERMISSION = 2;
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    private static final PermissionRequest ourInstance = new PermissionRequest();

    public static PermissionRequest getInstance() {
        return ourInstance;
    }

    private PermissionRequest() {
    }

    public void requestAllPermission(Activity activity) {
        mPermissionList.clear();
        LogUtils.dCamera("requestAllPermission build version : " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissionArray.length; i++) {
                if (ContextCompat.checkSelfPermission(activity, permissionArray[i]) != PackageManager.PERMISSION_GRANTED) {
                    LogUtils.dCamera("requestAllPermission add request : " + permissionArray[i]);
                    mPermissionList.add(permissionArray[i]);
                }
            }
            if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
                Toast.makeText(activity, "已经授权", Toast.LENGTH_LONG).show();
            } else {//请求权限方法
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
                ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_PERMISSION);
            }
        }
    }

    public boolean checkIsAskPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkIsAskPermissionState(Map<String, Integer> maps, String[] list) {
        for (int i = 0; i < list.length; i++) {
            if (maps.get(list[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean checkInternetPermission(Context context) {
        return checkIsAskPermission(context, Manifest.permission.INTERNET);
    }

    public boolean checkStoragePermission(Context context) {
        return checkIsAskPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) && checkIsAskPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

}
