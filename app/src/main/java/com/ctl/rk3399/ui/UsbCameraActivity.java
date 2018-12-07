package com.ctl.rk3399.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ctl.rk3399.R;
import com.ctl.rk3399.camera.CameraProxy;
import com.ctl.rk3399.camera.ICameraCtl;
import com.ctl.rk3399.view.CameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.core.content.FileProvider;

/**
 * create by Felix 20181207
 * connect to Felix-qq : 995676373
 * connect to Felix-email : linpeng@eyemore.cn
 */
public class UsbCameraActivity extends Activity {
    private static final String TAG = UsbCameraActivity.class.getName();
    private Context context;
    //todo: 拍照缩略图比例尺，手定,demo测试
    private int screenWidth = 160;
    private int screenHeight = 240;
    //底部高度 主要是计算切换正方形时的动画高度
    private int menuPopviewHeight;
    private CameraProxy mCameraProxy;
    private CameraView previewView;
    private ImageView mIv_takephoto;
    private ImageView mIv_photo;
    private Handler mAsyncHandler;
    private HandlerThread mHandlerThread;
    private Bitmap mBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_camera);
        context = this.getApplicationContext();
        initThread();
        initView();
        setCameraType();

    }

    private void initThread() {
        mHandlerThread = new HandlerThread("proc photo");
        mHandlerThread.start();
        mAsyncHandler = new Handler(mHandlerThread.getLooper());
    }

    private void releaseThread() {
        if (mAsyncHandler != null) {
            mAsyncHandler.removeCallbacksAndMessages(null);
            mAsyncHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    private void initView() {
        previewView = (CameraView) findViewById(R.id.preview_view);
        mIv_takephoto = findViewById(R.id.iv_takephoto);
        mIv_photo = findViewById(R.id.iv_photo);
        mIv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!takingPicture.get()) {
                    Toast.makeText(getApplicationContext(), "图片保存ing", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intentImage = new Intent(Intent.ACTION_VIEW);
                intentImage.addCategory(Intent.CATEGORY_DEFAULT);
                intentImage.setDataAndType(mUri, "image/*");
                intentImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intentImage.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivity(intentImage);
            }
        });
        mIv_takephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraProxy != null)
                    mCameraProxy.takePicture(new ICameraCtl.OnTakePictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, int rotation) {
                            procBigBitmap(data, rotation);
                        }
                    });
            }
        });
    }

    private Uri mUri;

    private void setCameraType() {
        mCameraProxy = new CameraProxy(getContext());
        mCameraProxy.setCameraFacing(ICameraCtl.CAMERA_USB);
        mCameraProxy.setDisplayOrientation(CameraView.ORIENTATION_PORTRAIT);
        mCameraProxy.setPreferredPreviewSize(1920, 1080);
        mCameraProxy.setPreviewView(previewView);
        mCameraProxy.setOnFrameListener(new ICameraCtl.OnFrameListener<byte[]>() {
            @Override
            public void onPreviewFrame(byte[] data, int rotation, int width, int height) {
//                Log.w("onFrame", "-------------onFrame: " + width + "  " + height + "  " + data.length);
            }
        });
        mCameraProxy.setCameraFacing(ICameraCtl.CAMERA_USB);
        mCameraProxy.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                Log.w(TAG, "-------------onError: " + error);
                switch (error) {
                    case 100:
                        toastS("相机已断开");
                        Log.w(TAG, "-------------onError:相机已断开");
                        finish();
                        break;
                    case CameraProxy.ERROR_CONNECT_FAILED:
                        toastS("打开相机失败，请检查接口连接");
                        Log.w(TAG, "-------------onError:打开相机失败，请检查接口连接");
                        finish();
                        break;
                    case CameraProxy.ERROR_UNKNOW:
                        toastS("ERROR_UNKNOW");
                        Log.w(TAG, "-------------onError:ERROR_UNKNOW");
                        finish();
                        break;
                    case CameraProxy.ERROR_PERMISSION_GRANTED:
                        toastS("木有给Camera权限呀！！");
                        Log.w(TAG, "-------------onError:木有给Camera权限呀！！");
                        finish();
                        break;
                    case CameraProxy.ERROR_SET_PARAMS:
                        toastS("设置某参数失败！");
                        Log.w(TAG, "-------------onError:设置某参数失败");
                        finish();
                        break;
                }
            }
        });
        previewView.getTextureView().setScaleX(-1);
//        mCameraControl.start();放到resume启动相机了
        takingPicture.set(true);
    }

    private void toastS(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraProxy.stop();
        mCameraProxy = null;
        releaseThread();
        Log.w(TAG, "--------------onDestroy--------------");
    }

    private Context getContext() {
        return context;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraProxy.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //todo:rk3399系统camera默认打开的地址是 /dev/video0，为了避免在pause生命周期时，发生热拔插，导致持有video0-hold,发生fd地址重复问题
        mCameraProxy.stop();
        destoryBitmap();
    }

    /*
     * 保存文件，文件名为当前日期
     */
    public Uri saveBitmap(byte[] data) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String bitName = format.format(new Date(System.currentTimeMillis()));
        String fileName;
        File file;
        if (Build.BRAND.equals("Xiaomi")) { // 小米手机
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/" + bitName + ".jpg";
        } else {  // Meizu 、Oppo
            fileName = Environment.getExternalStorageDirectory().getPath() + "/DCIM/" + bitName + ".jpg";
        }
        file = new File(fileName);
        Log.w(TAG, "save jpg file-path: " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
//            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
            out.write(data);
            out.flush();
            // 插入图库
//            MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), bitName, null);
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception rr) {
            }
        }
        // 发送广播，通知刷新图库的显示
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
        //todo: 待修改,短暂存储耗时,demo测试
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getUriForFile(this, file);
    }

    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    public static Uri getUriForFile24(Context context, File file) {
        Uri fileUri = FileProvider.getUriForFile(context,
                "com.ctl.rk3399.fileprovider",
                file);
        return fileUri;
    }

    private AtomicBoolean takingPicture = new AtomicBoolean(false);

    private void procBigBitmap(final byte[] data, int rotation) {
        takingPicture.set(false);
        //创建bitmap工厂的配置参数
        BitmapFactory.Options options = new BitmapFactory.Options();
        //返回null，不去真正解析位图，只是得到宽高等信息
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        int imgWidth;
        int imgHeight;
        imgWidth = options.outWidth;
        imgHeight = options.outHeight;
        Log.w(TAG, " 图片宽 " + imgWidth + "  图片高" + imgHeight + "  rotation: " + rotation);
        // 计算缩放比
        int scale = 1;
        int scalex = imgWidth / screenWidth;
        int scaley = imgHeight / screenHeight;
        scale = scalex > scaley ? scalex : scaley;
        //按照缩放比显示图片
        options.inSampleSize = scale;
        //开始真正解析位图
        options.inJustDecodeBounds = false;
        mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIv_photo.setImageBitmap(mBitmap);
            }
        });
        mAsyncHandler.post(new Runnable() {

            @Override
            public void run() {
                mUri = saveBitmap(data);
                takingPicture.set(true);
            }
        });
    }

    private void destoryBitmap() {
        mIv_photo.setImageBitmap(null);
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }
}
