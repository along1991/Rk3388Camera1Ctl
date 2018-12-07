package com.ctl.rk3399.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;

import java.util.List;

public class LogUtils {
    private static String TAG = "LogUtils";
    private static boolean DEBUG = true;
    private static boolean SHOW_MORE_INFO = false;
    private static CrashHandler crashHandler;

    private LogUtils() {
    }

    public static void init(Context context, String tag, boolean debug) {
        TAG = tag;
        DEBUG = debug;
        if (debug) {
            if (crashHandler == null) {
                crashHandler = new CrashHandler(context);
            }
        }
    }

    public static String getTAG() {
        return TAG;
    }

    public static void setTAG(String TAG) {
        LogUtils.TAG = TAG;
    }

    public static boolean isDEBUG() {
        return DEBUG;
    }

    public static void setDEBUG(boolean DEBUG) {
        LogUtils.DEBUG = DEBUG;
    }

    public static boolean isShowMoreInfo() {
        return SHOW_MORE_INFO;
    }

    public static void setShowMoreInfo(boolean showMoreInfo) {
        SHOW_MORE_INFO = showMoreInfo;
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void v(String msg) {
        if (DEBUG) {
            Log.v(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (DEBUG) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public static void dCamera(String s) {
        if (DEBUG) {
            Log.d("CameraProxy", s);
        }
    }

    public static void eCamera(String s) {
        if (DEBUG) {
            Log.e("CameraProxy", s);
        }
    }

    public static void logParams(String msg) {
        Log.w("CameraProxy", msg);
    }

    public static <T> void logParams(String TAG, String prew, List<T> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        int size = list.size();
        Log.w(TAG, "\n--" + prew + "--" + "---begin-------------------------------------------------");
        for (int i = 0; i < size; i++) {
            if (list.get(i) instanceof Camera.Size) {
                Log.w(TAG, "--" + prew + "--" + i + ":" + ((Camera.Size) list.get(i)).width + "x" + ((Camera.Size) list.get(i)).height);
            } else if (list.get(i) instanceof int[]) {
                int[] arr = (int[]) list.get(i);
                String arr_s = "";
                for (int j = 0; j < arr.length; j++) {
                    arr_s += arr[j] + "  ";
                }
                Log.w(TAG, "--" + prew + "--" + i + ":" + arr_s);
            } else {
                Log.w(TAG, "--" + prew + "--" + i + ":" + list.get(i).toString());
            }
        }
        Log.w(TAG, "--" + prew + "--" + "---end-------------------------------------------\n");
    }
}
