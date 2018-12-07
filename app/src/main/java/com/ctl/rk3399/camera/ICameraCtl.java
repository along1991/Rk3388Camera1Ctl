package com.ctl.rk3399.camera;

import android.graphics.Rect;
import android.view.View;

import com.ctl.rk3399.view.CameraView;
import com.ctl.rk3399.view.PreviewView;

import androidx.annotation.IntDef;

public interface ICameraCtl <T> {

    interface OnFrameListener<T> {
        void onPreviewFrame(T data, int rotation, int width, int height);
    }

    int CAMERA_FACING_BACK = 0;

    int CAMERA_FACING_FRONT = 1;

    int CAMERA_USB = 2;

    @IntDef({CAMERA_FACING_FRONT, CAMERA_FACING_BACK, CAMERA_USB})
    @interface CameraFacing {

    }

    /**
     * 照相回调。
     */
    interface OnTakePictureCallback {
        void onPictureTaken(byte[] data,int ratation);
    }

    /**
     * 设置水平方向
     *
     * @param displayOrientation 参数值见 {@link CameraView.Orientation}
     */
    void setDisplayOrientation(@CameraView.Orientation int displayOrientation);

    /**
     * 打开相机。
     */
    void start();

    /**
     * 关闭相机
     */
    void stop();

    void pause();

    void resume();

    void setOnFrameListener(OnFrameListener listener);

    void setPreferredPreviewSize(int width, int height);

    void setPreviewView(PreviewView previewView);

    /**
     * 拍照
     * @param callback 拍照结果回调
     */
    void takePicture(OnTakePictureCallback callback);

    void setCameraFacing(@CameraFacing int cameraFacing);

}
