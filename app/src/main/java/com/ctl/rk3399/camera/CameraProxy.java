package com.ctl.rk3399.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.ctl.rk3399.utils.LogUtils;
import com.ctl.rk3399.view.CameraView;
import com.ctl.rk3399.view.PreviewView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraProxy implements ICameraCtl {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String TAG = CameraProxy.class.getName();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    private int displayOrientation = 0;
    private int cameraId = 0;
    private int flashMode;
    private AtomicBoolean takingPicture = new AtomicBoolean(false);

    private Context context;
    private Camera camera;
    private HandlerThread cameraHandlerThread = null;
    private Handler cameraHandler = null;
    private Handler uiHandler = null;

    private Camera.Parameters parameters;
    private Rect previewFrame = new Rect();

    private int preferredWidth = 1920;
    private int preferredHeight = 1080;

    @CameraFacing
    private int cameraFacing = CAMERA_FACING_FRONT;
    private int mRotation;
    private Camera.ErrorCallback mErrorCallback;

    public CameraProxy(Context context) {
        this.context = context;
    }

    public void setErrorCallback(Camera.ErrorCallback errorCallback) {
        mErrorCallback = errorCallback;
    }

    @Override
    public void setCameraFacing(@CameraFacing int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }

    @Override
    public void setDisplayOrientation(@CameraView.Orientation int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    @Override
    public void start() {
        postStartCamera();
    }

    private SurfaceTexture surfaceTexture;

    private void postStartCamera() {
        if (cameraHandlerThread == null || !cameraHandlerThread.isAlive()) {
            LogUtils.dCamera("-----------postStartCamera--------------");
            cameraHandlerThread = new HandlerThread("camera");
            cameraHandlerThread.start();
            cameraHandler = new Handler(cameraHandlerThread.getLooper());
            uiHandler = new Handler(Looper.getMainLooper());
        }
        if (cameraHandler == null) {
            return;
        }

        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.dCamera("---------StartCamera--------------");
                    startCamera();
                } catch (RuntimeException e) {
                    LogUtils.eCamera("---------ERROR_CONNECT_FAILED---------");
                    if (mErrorCallback != null)
                        mErrorCallback.onError(ERROR_CONNECT_FAILED, camera);
                } catch (Exception e) {
                    LogUtils.eCamera("---------ERROR_UNKNOW---------");
                    if (mErrorCallback != null)
                        mErrorCallback.onError(ERROR_UNKNOW, camera);
                }
            }
        });
    }

    public static final int ERROR_CONNECT_FAILED = 11599;
    public static final int ERROR_PERMISSION_GRANTED = 11597;
    public static final int ERROR_UNKNOW = 11598;
    public static final int ERROR_SET_PARAMS = 1100;

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (mErrorCallback != null)
                mErrorCallback.onError(ERROR_PERMISSION_GRANTED, camera);
            return;
        }
        if (camera == null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                LogUtils.dCamera("-----------------cameraId: " + cameraId
                        + "  cameraInfo.facing:" +cameraInfo.facing
                );
                if (cameraInfo.facing == cameraFacing) {
                    cameraId = i;
                }
            }
            LogUtils.dCamera("-----------------cameraId: " + cameraId + "  NumberOfCameras:" + Camera.getNumberOfCameras());
            camera = Camera.open(cameraId);
            camera.setErrorCallback(mErrorCallback);
        }

        if (parameters == null) {
            parameters = camera.getParameters();
            //todo:测试USB摄像头没有对焦功能
            parameters.setFocusMode(parameters.getFocusMode());
        }
        int detectRotation = 0;
        if (cameraFacing == ICameraCtl.CAMERA_FACING_FRONT) {
            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId);
            camera.setDisplayOrientation(rotation);
            detectRotation = rotation;
            if (displayOrientation == CameraView.ORIENTATION_PORTRAIT) {
                if (detectRotation == 90 || detectRotation == 270) {
                    detectRotation = (detectRotation + 180) % 360;
                }
            }
        } else if (cameraFacing == ICameraCtl.CAMERA_FACING_BACK) {
            mRotation = ORIENTATIONS.get(displayOrientation);
            mRotation = getCameraDisplayOrientation(mRotation, cameraId);
            camera.setDisplayOrientation(mRotation);
            detectRotation = mRotation;
        } else if (cameraFacing == ICameraCtl.CAMERA_USB) {
            camera.setDisplayOrientation(0);
            detectRotation = 0;
        }
        opPreviewSize(preferredWidth, preferredHeight);
        final Camera.Size size = camera.getParameters().getPreviewSize();
        if (detectRotation % 180 == 90) {
            previewView.setPreviewSize(size.height, size.width);
        } else {
            previewView.setPreviewSize(size.width, size.height);
        }
        final int temp = detectRotation;
        try {
            if (cameraFacing == ICameraCtl.CAMERA_USB) {
                camera.setPreviewTexture(textureView.getSurfaceTexture());
            } else {
                surfaceTexture = new SurfaceTexture(11);
                camera.setPreviewTexture(surfaceTexture);
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (textureView != null) {
                            surfaceTexture.detachFromGLContext();
                            textureView.setSurfaceTexture(surfaceTexture);
                        }
                    }
                });
            }
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    onFrameListener.onPreviewFrame(data, temp, size.width, size.height);
                }
            });

        } catch (IOException e) {
            if (mErrorCallback != null)
                mErrorCallback.onError(ERROR_UNKNOW, camera);
        } catch (RuntimeException e) {
            if (mErrorCallback != null)
                mErrorCallback.onError(ERROR_UNKNOW, camera);
        }
    }

    private TextureView textureView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        if (surfaceTexture != null) {
            surfaceTexture.detachFromGLContext();
            textureView.setSurfaceTexture(surfaceTexture);
        }
    }

    private int getCameraDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360;
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }
        return rotation;
    }

    @Override
    public void stop() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
            cameraHandlerThread = null;
        }
    }

    @Override
    public void pause() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    @Override
    public void resume() {
        takingPicture.set(false);
        postStartCamera();
    }

    private OnFrameListener onFrameListener;

    @Override
    public void setOnFrameListener(OnFrameListener listener) {
        this.onFrameListener = listener;
    }

    @Override
    public void setPreferredPreviewSize(int width, int height) {
        this.preferredWidth = Math.max(width, height);
        this.preferredHeight = Math.min(width, height);
    }

    private PreviewView previewView;

    @Override
    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
        setTextureView(previewView.getTextureView());
    }

    @Override
    public void takePicture(final OnTakePictureCallback onTakePictureCallback) {
        if (takingPicture.get()) {
            return;
        }
//        int ori= ORIENTATIONS.get(displayOrientation);
//        parameters.setRotation(ori);
        if (cameraFacing != ICameraCtl.CAMERA_USB) {
            switch (displayOrientation) {
                case CameraView.ORIENTATION_PORTRAIT:
                    parameters.setRotation(90);
                    break;
                case CameraView.ORIENTATION_HORIZONTAL:
                    parameters.setRotation(0);
                    break;
                case CameraView.ORIENTATION_INVERT:
                    parameters.setRotation(180);
                    break;
                default:
                    break;
            }
        }
        Camera.Size picSize =
                getOptimalSize(preferredWidth, preferredHeight, camera.getParameters().getSupportedPictureSizes());
        parameters.setPreviewSize(picSize.width, picSize.height);
        parameters.setPictureSize(picSize.width, picSize.height);
        camera.setParameters(parameters);
        takingPicture.set(true);
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                camera.cancelAutoFocus();
                try {
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            camera.startPreview();
                            takingPicture.set(false);
                            if (onTakePictureCallback != null) {
                                onTakePictureCallback.onPictureTaken(data, mRotation);
                            }
                        }
                    });
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    camera.startPreview();
                    takingPicture.set(false);
                }
            }
        });
    }


    // 开启预览
    private void startPreview(boolean checkPermission) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (mErrorCallback != null)
                mErrorCallback.onError(ERROR_PERMISSION_GRANTED, camera);
            return;
        }
        camera.startPreview();
    }

    private void opPreviewSize(int width, @SuppressWarnings("unused") int height) {
        if (camera != null && width > 0) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                printLog(parameters);
                Camera.Size optSize = getOptimalSize(width, height, camera.getParameters().getSupportedPreviewSizes());
                Log.w("wtf", "xx opPreviewSize-> " + optSize.width + " " + optSize.height);
                parameters.setPreviewSize(optSize.width, optSize.height);
                // parameters.setPreviewFpsRange(10, 15);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (RuntimeException e) {
                if (mErrorCallback != null) {
                    if (e.toString().contains("setParameters")) {
                        mErrorCallback.onError(ERROR_SET_PARAMS, camera);
                    } else {
                        mErrorCallback.onError(ERROR_CONNECT_FAILED, camera);
                    }
                }

            }
        }
    }

    private void printLog(Camera.Parameters parameters) {
        LogUtils.logParams(TAG, "picturesize", parameters.getSupportedPictureSizes());
        LogUtils.logParams(TAG, "WhiteBalance", parameters.getSupportedWhiteBalance());
        LogUtils.logParams(TAG, "coloreffects", parameters.getSupportedColorEffects());
        LogUtils.logParams(TAG, "picformats", parameters.getSupportedPictureFormats());
        LogUtils.logParams(TAG, "prewformats", parameters.getSupportedPreviewFormats());
        LogUtils.logParams(TAG, "fpsRange", parameters.getSupportedPreviewFpsRange());
        LogUtils.logParams(TAG, "VideoSizes", parameters.getSupportedVideoSizes());
        LogUtils.logParams(TAG, "JpegThumbnail", parameters.getSupportedJpegThumbnailSizes());
        LogUtils.logParams(TAG, "getSupportedPreviewSizes ", parameters.getSupportedPreviewSizes());

//        log("setupCamera prew: " + previewSize.width + "x" + previewSize.height);
        LogUtils.logParams("setupCamera awb: " + parameters.getWhiteBalance());
        LogUtils.logParams("setupCamera ExposureCompensationStep: " + parameters.getExposureCompensationStep());
        LogUtils.logParams("setupCamera focus: " + parameters.getFocusMode());
        LogUtils.logParams("setupCamera ExposureCompensation: " + parameters.getExposureCompensation());
        LogUtils.logParams("setupCamera ExposureCompensationStep: " + parameters.getExposureCompensationStep());
        LogUtils.logParams("setupCamera AutoExposureLock: " + parameters.getAutoExposureLock());
        LogUtils.logParams("setupCamera MaxExposureCompensatio: " + parameters.getMaxExposureCompensation());
        LogUtils.logParams("setupCamera MinExposureCompensatio: " + parameters.getMinExposureCompensation());
        LogUtils.logParams("setupCamera getAutoWhiteBalanceLock: " + parameters.getAutoWhiteBalanceLock());
        LogUtils.logParams("setupCamera getJpegQuality: " + parameters.getJpegQuality());
        LogUtils.logParams("setupCamera Antibanding: " + parameters.getAntibanding());
        LogUtils.logParams("setupCamera getMaxNumDetectedFaces: " + parameters.getMaxNumDetectedFaces());
    }


    private Camera.Size getOptimalSize(int width, int height, List<Camera.Size> sizes) {
        Camera.Size pictureSize = sizes.get(0);
        List<Camera.Size> candidates = new ArrayList<>();
        for (Camera.Size size: sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size);
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        for (Camera.Size size: sizes) {
            if (size.width >= width && size.height >= height) {
                return size;
            }
        }

        return pictureSize;
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };

}
