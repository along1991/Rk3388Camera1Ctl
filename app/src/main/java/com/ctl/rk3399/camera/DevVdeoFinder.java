package com.ctl.rk3399.camera;

import android.util.Log;

import com.ctl.rk3399.utils.LogUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

public class DevVdeoFinder {
    private static final String TAG = DevVdeoFinder.class.getName();
    private static final String DeviceRoot = "dev/video";

    public class Driver {
        public Driver(String name) {
            mDriverName = name;
        }

        private String mDriverName;

        public String getName() {
            return mDriverName;
        }
    }

    Vector<File> mDevices = null;

    public Vector<File> getDevices() {
        if (mDevices == null) {
            mDevices = new Vector<File>();
            File dev = new File("/dev");
            if (!dev.exists() || !dev.canRead()) {
                LogUtils.e(TAG, "/dev file is not exists! or can not read dev-fd");
                return null;
            }


//            try {
//                /* Missing read/write permission, trying to chmod the file */
//                Process su;
//                su = Runtime.getRuntime().exec("su");
//                String cmd = "chmod 666 " + dev.getAbsolutePath() + "\n"
//                        + "exit\n";
//                su.getOutputStream().write(cmd.getBytes());
//                Log.e(TAG, "/dev  waitFor: " + (su.waitFor() != 0) + " canRead:" + dev.canRead() + "  canWrite: " + dev.canWrite());
//            } catch (Exception e) {
//                LogUtils.e(TAG, "Can not to get su-exec permission !");
//                throw new RuntimeException("Can not to get su-exec permission !");
//            }
            File[] files = dev.listFiles();
            if (files == null) {
                Log.e(TAG, "Found folder list : null");
                return mDevices;
            }
            int i;
            for (i = 0; i < files.length; i++) {
                Log.d(TAG, "foreach  files: " + files[i].getAbsolutePath());
                if (files[i].getAbsolutePath().contains(DeviceRoot)) {
                    Log.d(TAG, "Found new device: " + files[i]);
                    mDevices.add(files[i]);
                }
            }
        }
        return mDevices;
    }

}
