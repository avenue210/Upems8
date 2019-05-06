package cn.upus.app.upems.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.noober.background.BackgroundLibrary;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.DevallBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.activity.ADActivity;
import cn.upus.app.upems.util.SpeakerUtil;
import cn.upus.app.upems.util.http.HttpCallBack;
import cn.upus.app.upems.util.http.HttpUtil;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends BaseLanguageActivity implements HttpCallBack, CameraDialog.CameraDialogParent, CameraViewInterface.Callback {

    private static final String TAG = BaseActivity.class.getSimpleName();

    /*设备格子信息列表*/
    public static List<DevallBean> devallBeans = new ArrayList<>();

    protected Unbinder unbinder = null;
    protected Context context;
    protected HttpUtil mHttpUtil;
    protected CompositeDisposable compositeDisposable;

    public int WEB_TYPT = -1;
    public final int TYPE_0 = 0;
    public final int TYPE_1 = 1;
    public final int TYPE_2 = 2;
    public final int TYPE_3 = 3;
    public final int TYPE_4 = 4;
    public final int TYPE_5 = 5;
    public final int TYPE_6 = 6;
    public final int TYPE_7 = 7;
    public final int TYPE_8 = 8;
    public final int TYPE_9 = 9;
    public final int TYPE_10 = 10;
    public final int TYPE_11 = 11;
    public final int TYPE_12 = 12;
    public final int TYPE_13 = 13;
    public final int TYPE_14 = 14;
    public final int TYPE_15 = 15;

    public static final String TYPE_20 = "20";
    public static final String TYPE_22 = "22";
    public static final String TYPE_23 = "23";
    public static final String TYPE_25 = "25";
    public static final String TYPE_26 = "26";
    public static final String TYPE_29 = "29";//有屏智能洗衣柜
    public static final String TYPE_32 = "32";//寄存界面

    /**
     * 定义一个tts对象
     */
    private static TextToSpeech tts;
    /**
     * 扩音器
     */
    private SpeakerUtil speakerUtil;

    /**
     * 文字转语音 说话
     *
     * @param text 内容
     */
    public static void ttsSpeak(String text) {
        if (!MApp.mSp.getBoolean(UserData.TTS_OPEN)) {
            return;
        }
        if (null != tts) {
            LogUtils.e("文字转语音", text);
            try {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*-----------------------------------------------------------------------------------*/
    public CountDownTimer adCountDownTimer;
    private long adTime = 15 * 1000;//定时跳转广告时间

    /**
     * 启动广告跳转监听
     */
    public void startAdTimer() {
        try {
            //LogUtils.e("启动广告跳转监听");
            if (null == adCountDownTimer) {
                adCountDownTimer = new CountDownTimer(adTime, 1000L) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        startActivity(new Intent(context, ADActivity.class));
                    }
                };
            }
            adCountDownTimer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止广告跳转监听
     */
    public void stopAdTimer() {
        //LogUtils.e("停止广告跳转监听");
        if (null != adCountDownTimer) {
            adCountDownTimer.cancel();
            adCountDownTimer = null;
        }
    }

    protected void hideBottomUIMenu() {
        Window window = getWindow();
        if (null != window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);//API 19
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                );
            }
        }
    }

    protected abstract int initLayoutView();

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // LogUtils.e("得到焦点");
            startAdTimer();
        } else {
            //  LogUtils.e("失去焦点");
            stopAdTimer();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // LogUtils.e("BASE ACTIVITY ", ev.getAction() + "");
        KeyboardUtils.hideSoftInput(this);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: //有按下动作时取消定时
                stopAdTimer();
                break;
            case MotionEvent.ACTION_UP: //抬起时启动定时
                startAdTimer();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /*屏幕常亮*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideBottomUIMenu();
        BackgroundLibrary.inject(this);
        super.onCreate(savedInstanceState);
        //setlanguage();

        setContentView(initLayoutView());

        BarUtils.setNavBarVisibility(this, false);
        BarUtils.setStatusBarVisibility(this, false);

        unbinder = ButterKnife.bind(this);
        context = this;
        mHttpUtil = new HttpUtil();
        setPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tts = new TextToSpeech(getApplicationContext(), onInitListener());
        speakerUtil = new SpeakerUtil(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAdTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != tts) {
            tts.stop(); // 不管是否正在朗读TTS都被打断
            tts.shutdown(); // 关闭，释放资源
        }
    }

    @Override
    protected void onDestroy() {
        resetToast();
        stopAdTimer();
        unbinder.unbind();
        dispose();
        if (MApp.mUsb485Util.driverOpen) {
            MApp.mUsb485Util.close();
        }
        if (MApp.mSerialportUtil.driverOpen) {
            MApp.mSerialportUtil.close();
        }
        if (MApp.ZHI_WEN_COM.driverOpen) {
            MApp.ZHI_WEN_COM.close();
        }
        super.onDestroy();
    }

    public void setlanguage() {
        //获取系统当前的语言
        String able = getResources().getConfiguration().locale.getLanguage();
        LogUtils.e("语言 " + able);
        Resources resources = getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();
        //根据系统语言进行设置
        /*if (able.equals("zh")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);
        } else if (able.equals("en")) {
            config.locale = Locale.US;
            resources.updateConfiguration(config, dm);
        }*/
        if (able.equals("en")) {
            config.locale = Locale.US;
            resources.updateConfiguration(config, dm);
        } else {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);
        }
    }

    /**
     * 文字转语音回调实现
     *
     * @return
     */
    protected TextToSpeech.OnInitListener onInitListener() {
        return status -> {
            // 判断是否转化成功
            if (status == TextToSpeech.SUCCESS) {
                speakerUtil.openSpeaker();
                //默认设定语言为中文，原生的android貌似不支持中文。
                int result = tts.setLanguage(Locale.CHINESE);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    //toastys.error("语音转换无法使用");
                } else {
                    //不支持中文就将语言设置为英文
                    tts.setLanguage(Locale.US);
                }
            }
        };
    }

    /**
     * 将网络请求队列销毁
     */
    public void dispose() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @SuppressLint({"CheckResult", "InlinedApi"})
    private void setPermissions() {
        RxPermissions rxPermission = new RxPermissions(this);
        rxPermission.requestEach(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        ).subscribe(permission -> {
            if (permission.granted) {
                // 用户已经同意该权限
                LogUtils.d(TAG, permission.name + " is granted.");
            } else if (permission.shouldShowRequestPermissionRationale) {
                // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                LogUtils.d(TAG, permission.name + " is denied. More info should be provided.");
            } else {
                // 用户拒绝了该权限，并且选中『不再询问』
                LogUtils.d(TAG, permission.name + " is denied.");
            }
        });
    }

    private void resetToast() {
        ToastUtils.setMsgColor(-0x1000001);
        ToastUtils.setBgColor(-0x1000001);
        ToastUtils.setBgResource(-1);
        ToastUtils.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 64);
    }

    /**
     * 吐司
     *
     * @param message 提示信息
     * @param type    0 成功 1 警告 2 失败
     */
    protected void showToast(String message, int type) {
        resetToast();
        int color;
        switch (type) {
            case 0:
                color = 0xFF1B5E20;
                break;
            case 1:
                color = 0xFFF57F17;
                break;
            case 2:
                color = Color.RED;
                break;
            default:
                color = 0xFF1B5E20;
                break;
        }
        ToastUtils.setBgColor(color);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.showShort(new SpanUtils()
                .appendImage(R.drawable.ic_tip, SpanUtils.ALIGN_CENTER)
                .append("  ")
                .append(message)
                .setForegroundColor(Color.WHITE)
                .setFontSize(16, true)
                .create()
        );
    }

    @Override
    public void showDialog() {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void onSuccess(String data) {

    }

    @Override
    public void onError(String data) {

    }

    /*-USB 摄像头初始化------------------------------------------------------------------------------------------------------------------------------------------------*/

    protected UVCCameraHelper mCameraHelper;
    protected CameraViewInterface mUVCCameraView;
    protected boolean isRequest;
    protected boolean isPreview;

    /**
     * 摄像头分辨率
     */
    private AlertDialog mDialog;

    public void showResolutionListDialog() {
        try {
            if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
                LogUtils.e("sorry,camera open failed");
                showToast("sorry,camera open failed", 2);
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.camera_resolution));
            View rootView = LayoutInflater.from(this).inflate(R.layout.layout_dialog_list, null);
            ListView listView = rootView.findViewById(R.id.listview_dialog);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Objects.requireNonNull(getResolutionList()));
            if (adapter != null) {
                listView.setAdapter(adapter);
            }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if (mCameraHelper == null || !mCameraHelper.isCameraOpened())
                        return;
                    final String resolution = (String) adapterView.getItemAtPosition(position);
                    String[] tmp = resolution.split("x");
                    if (tmp != null && tmp.length >= 2) {
                        int widht = Integer.valueOf(tmp[0]);
                        int height = Integer.valueOf(tmp[1]);
                        try {
                            mCameraHelper.updateResolution(widht, height);
                        } catch (Exception e) {
                            LogUtils.e(TAG, "修改相机分辨率失败 " + e.getMessage());
                        }
                    }
                    mDialog.dismiss();
                }
            });
            builder.setView(rootView);
            builder.setNegativeButton(getResources().getString(R.string.back), (dialog, which) -> dialog.dismiss());
            mDialog = builder.create();
            mDialog.setCanceledOnTouchOutside(false);
            Objects.requireNonNull(mDialog.getWindow()).setGravity(Gravity.CENTER);
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // example: {640x480,320x240,etc}
    private List<String> getResolutionList() {
        try {
            List<Size> list = mCameraHelper.getSupportedPreviewSizes();
            List<String> resolutions = null;
            if (list != null && list.size() != 0) {
                resolutions = new ArrayList<>();
                for (Size size : list) {
                    if (size != null) {
                        resolutions.add(size.width + "x" + size.height);
                    }
                }
            }
            return resolutions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void initUVCCamera1(Activity activity, View mTextureView) {
        try {
            // step.1 initialize UVCCameraHelper
            LogUtils.e(TAG, " step.1 initialize UVCCameraHelper");
            mUVCCameraView = (CameraViewInterface) mTextureView;
            mUVCCameraView.setCallback(this);
            mCameraHelper = UVCCameraHelper.getInstance();
            mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_YUYV);
            mCameraHelper.initUSBMonitor(activity, mUVCCameraView, listener);

            mCameraHelper.setOnPreviewFrameListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
                @Override
                public void onPreviewResult(byte[] nv21Yuv) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initUVCCamera2() {
        try {
            // step.2 register USB event broadcast
            if (mCameraHelper != null) {
                LogUtils.e(TAG, "step.2 register USB event broadcast");
                mCameraHelper.registerUSB();
            } else {
                LogUtils.e(TAG, "step.2 register USB event broadcast  mCameraHelper == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initUVCCamera3() {
        try {
            // step.3 unregister USB event broadcast
            if (mCameraHelper != null) {
                LogUtils.e(TAG, "step.3 unregister USB event broadcast");
                mCameraHelper.unregisterUSB();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initUVCCamera4() {
        try {
            // step.4 release uvc camera resources
            if (mCameraHelper != null) {
                mCameraHelper.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
            try {
                if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                    showToast("check no usb camera", 2);
                    return;
                }
                // request open permission
                if (!isRequest) {
                    isRequest = true;
                    if (mCameraHelper != null) {
                        mCameraHelper.requestPermission(0);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            try {
                // close camera
                if (isRequest) {
                    isRequest = false;
                    mCameraHelper.closeCamera();
                    showToast(device.getDeviceName() + " is out", 2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            try {
                if (!isConnected) {
                    showToast("fail to connect,please check resolution params", 2);
                    isPreview = false;
                } else {
                    isPreview = true;
                    showToast("connecting", 0);
                    // initialize seekbar
                    // need to wait UVCCamera initialize over
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Looper.prepare();
                            if (mCameraHelper != null && mCameraHelper.isCameraOpened()) {
                                //mSeekBrightness.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_BRIGHTNESS));
                                //mSeekContrast.setProgress(mCameraHelper.getModelValue(UVCCameraHelper.MODE_CONTRAST));
                            }
                            Looper.loop();
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            showToast("disconnecting", 2);
        }
    };

    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showToast(getResources().getString(R.string.ok_cancel), 2);
        }
    }

    @Override
    public void onSurfaceCreated(CameraViewInterface cameraViewInterface, Surface surface) {
        try {
            if (!isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.startPreview(mUVCCameraView);
                isPreview = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface cameraViewInterface, Surface surface, int i, int i1) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface cameraViewInterface, Surface surface) {
        try {
            if (isPreview && mCameraHelper.isCameraOpened()) {
                mCameraHelper.stopPreview();
                isPreview = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
