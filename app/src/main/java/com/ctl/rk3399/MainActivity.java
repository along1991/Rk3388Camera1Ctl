package com.ctl.rk3399;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ctl.rk3399.R;
import com.ctl.rk3399.base.DefaultBaseActivity;
import com.ctl.rk3399.camera.DevVdeoFinder;
import com.ctl.rk3399.ui.UsbCameraActivity;
import com.ctl.rk3399.utils.LogUtils;
import com.ctl.rk3399.utils.PermissionRequest;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends DefaultBaseActivity {

    private Button btn_camera;
    int mHasDevices = -1;
    private static final int NO_DEV_PERMISSION = 987;
    private static final int GET_DEV_VIDEO_0 = 988;
    private static final int GET_DEV_ONLY_VIDEO_1 = 989;
    private static final int GET_DEV_NONE = 990;
//    private AtomicBoolean GettingDevFd = new AtomicBoolean(false);

    @Override
    protected void initialize() {
        setContentView(R.layout.activity_main);
//        GettingDevFd.set(false);
        // getDevFdCheck();
//        GettingDevFd.set(true);
    }

    private void getDevFdCheck() {
        mHasDevices = GET_DEV_NONE;
        DevVdeoFinder devVdeoFinder = new DevVdeoFinder();
        Vector<File> devices = devVdeoFinder.getDevices();
        if (devices == null) {
            //返回null，权限不支持获取dev目录
            LogUtils.eCamera("权限不支持获取dev目录");
            mHasDevices = NO_DEV_PERMISSION;
        } else {
            for (int i = 0; i < devices.size(); i++) {
                LogUtils.dCamera("获取设备： " + devices.get(i).getAbsolutePath());
                if (devices.get(i).getAbsolutePath().contains("video0")) {
                    mHasDevices = GET_DEV_VIDEO_0;
                } else if (devices.get(i).getAbsolutePath().contains("video1")) {
                    if (mHasDevices != GET_DEV_VIDEO_0) {
                        mHasDevices = GET_DEV_ONLY_VIDEO_1;
                    }
                }
            }
        }
    }


    @Override
    protected void initView() {
        PermissionRequest.getInstance().requestAllPermission(this);
        btn_camera = (Button) findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDevFdCheck();
                if (mHasDevices == GET_DEV_ONLY_VIDEO_1) {
                    Toast.makeText(getApplicationContext(), "当前Camera挂载地址：video1 请重新连接USB Camera", Toast.LENGTH_SHORT).show();
                    return;
                } else if (mHasDevices == GET_DEV_NONE) {
                    Toast.makeText(getApplicationContext(), "为检测到相机地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(activity, UsbCameraActivity.class);
                activity.startActivity(intent);
            }
        });
    }
}
