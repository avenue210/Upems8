package cn.upus.app.upems.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.jiangdg.usbcamera.UVCCameraHelper;
import com.youth.banner.Banner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.bean.CameraSettingsBean;
import cn.upus.app.upems.bean.DevallBean;
import cn.upus.app.upems.bean.ExpnoImageBean;
import cn.upus.app.upems.bean.SystemInfoEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.OpenLockService;
import cn.upus.app.upems.service.TimingTskService;
import cn.upus.app.upems.ui.dialog.DepositDialog;
import cn.upus.app.upems.ui.dialog.UserRegisterDialog;
import cn.upus.app.upems.ui.dialog.login.LoginLetterBoxDialog;
import cn.upus.app.upems.ui.dialog.login.LoginPasswordDialog;
import cn.upus.app.upems.ui.dialog.login.LoginPaybyCardDialog;
import cn.upus.app.upems.ui.dialog.login.LoginSweepCodeDialog;
import cn.upus.app.upems.ui.dialog.storage.StorageLaundryCabinetDialog;
import cn.upus.app.upems.ui.dialog.take.PayByCardTakeDialog;
import cn.upus.app.upems.ui.dialog.take.VerificationCodeSweepTakeDialog;
import cn.upus.app.upems.ui.dialog.take.VerificationCodeTakeDialog;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.FileUtil;
import cn.upus.app.upems.util.NetWorkUtils;
import cn.upus.app.upems.util.banner.BannerLoad;
import cn.upus.app.upems.util.gson.GsonUtil;
import cn.upus.app.upems.util.serialport_usb485.Data;
import cn.upus.app.upems.util.serialport_usb485.StringUtil;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 信报箱 界面
 */
public class Main4 extends BaseActivity {

    private static final String TAG = Main4.class.getSimpleName();

    @BindView(R.id.tv_title_left_1)
    TextView tv_title_left_1;
    @BindView(R.id.tv_title_left_2)
    TextView tv_title_left_2;
    @BindView(R.id.tv_title_right_1)
    TextView tv_title_right_1;
    @BindView(R.id.tv_title_right_2)
    TextView tv_title_right_2;
    @BindView(R.id.tv_title_right_3)
    TextView tv_title_right_3;
    @BindView(R.id.iv_title_signal_cellular)
    ImageView iv_title_signal_cellular;
    @BindView(R.id.banner)
    Banner banner;
    @BindView(R.id.iv_left1)
    ImageView iv_left1;
    @BindView(R.id.iv_right1)
    ImageView iv_right1;
    @BindView(R.id.iv_right2)
    ImageView iv_right2;
    @BindView(R.id.ll_kuaidiyuan_login)
    LinearLayout ll_kuaidiyuan_login;

    @BindView(R.id.camera_view)
    View mTextureView;

    @BindView(R.id.iv_register)
    ImageView iv_register;
    @BindView(R.id.iv_set)
    ImageView iv_set;

    @BindView(R.id.bt_test)
    Button bt_test;

    private BannerLoad mBannerLoad;//轮播
    private Thread mADThread;//广告定时器
    private Disposable mTimeDisposable;//时间定时器

    private String devkind;

    private LoginPasswordDialog login_password_dialog;

    //{"kindno":"20","kindna":"有屏校园配餐柜"}
    //{"kindno":"22","kindna":"有屏刷卡存包柜"}
    //{"kindno":"23","kindna":"有屏扫码存包柜"}
    //{"kindno":"25","kindna":"有屏社区快递柜"}
    //{"kindno":"26","kindna":"有屏商务快递柜"}
    //{"kindno":"27","kindna":"有屏智能储物柜"}
    //{"kindno":"28","kindna":"有屏指纹寄存柜"}
    //{"kindno":"29","kindna":"有屏智能洗衣柜"}
    //{"kindno":"32","kindna":"横屏智能寄存柜"}

    //WIFI信息传递
    private Handler handler;

    /**
     * 获取WIFI信号的强弱
     */
    @SuppressLint("HandlerLeak")
    public void getWifiStrength(final ImageView ivSign) {

        // 获得WifiManager
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        // 使用定时器,每隔5秒获得一次信号强度值
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (NetworkUtils.isWifiAvailable()) {
                    if (null == wifiManager) {
                        return;
                    }
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获得信号强度值
                    int level = wifiInfo.getRssi();
                    //根据获得的信号强度发送信息
                    if (level <= 0 && level >= -50) {
                        Message msg = new Message();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    } else if (level < -50 && level >= -70) {
                        Message msg = new Message();
                        msg.what = 2;
                        handler.sendMessage(msg);
                    } else if (level < -70 && level >= -80) {
                        Message msg = new Message();
                        msg.what = 3;
                        handler.sendMessage(msg);
                    } else if (level < -80 && level >= -100) {
                        Message msg = new Message();
                        msg.what = 4;
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = 5;
                        handler.sendMessage(msg);
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }

        }, 1000, 5000);
        // 使用Handler实现UI线程与Timer线程之间的信息传递,每5秒告诉UI线程获得wifiInto
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // 如果收到正确的消息就获取WifiInfo，改变图片并显示信号强度
                    case 1:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_4);
                        break;
                    case 2:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_3);
                        break;
                    case 3:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_2);
                        break;
                    case 4:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_1);
                        break;
                    case 5:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_off);
                        break;
                    default:
                        ivSign.setImageResource(R.drawable.ic_signal_wifi_off);
                        break;
                }
            }
        };
    }

    @Override
    protected int initLayoutView() {
        return R.layout.activity_main4;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initData();
        initBanner();
        startTime();
        //WIFI监听
        getWifiStrength(iv_title_signal_cellular);
        getFixinfo();

        bt_test.setOnClickListener(v -> {
            try {
                String picPath = UVCCameraHelper.ROOT_PATH + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;
                mCameraHelper.capturePicture(picPath, path -> LogUtils.e(TAG, "save path：" + path));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "拍照异常 " + e.getMessage());
            }
        });

        if (null != mThread) {
            mThread.interrupt();
            mThread = null;
        }
        mThread = new Thread(mRunnable, "xbx");
        mThread.start();
    }

    private String msg;
    private Thread mThread;

    private Runnable mRunnable = () -> {
        while (true) {
            if (!TextUtils.isEmpty(MApp.mSerialportUtil.xbxMsg)) {
                msg = MApp.mSerialportUtil.xbxMsg;
                MApp.mSerialportUtil.xbxMsg = null;
                /*信报箱 55aa010a7101007d*/
                if (msg.length() == 16 && msg.substring(0, 4).equals("55aa") && msg.substring(8, 10).equals("71")) {
                    try {
                        String boardno = String.valueOf(StringUtil.HexToInt(msg.substring(4, 6)));
                        String lockno = String.valueOf(StringUtil.HexToInt(msg.substring(6, 8)));
                        LogUtils.e("接收到信报箱开门存件信息 " + msg + "  " + boardno + "  " + lockno);

                        if (null != devallBeans && devallBeans.size() > 0) {
                            for (DevallBean bean : devallBeans) {
                                if (boardno.equals(bean.getBoardno()) && lockno.equals(bean.getLockno())) {
                                    LogUtils.a("提交信报箱信息");
                                    storage(bean.getShelfno());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void getFixinfo() {
        if (!TextUtils.isEmpty(UserData.DEVNO) && !TextUtils.isEmpty(UserData.WEB_URL)) {
            try {
                new Thread(() -> {
                    while (true) {
                        try {
                            if (NetWorkUtils.isNetworkConnected(getApplicationContext())) {
                                LogUtils.e(TAG, "网络已连接 调用 fixinfo");
                                devnoinfo(MApp.mSp.getString(UserData.DEVID));
                                break;
                            }
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, "getFixinfo").start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick({R.id.iv_right1, R.id.iv_right2, R.id.iv_left1, R.id.ll_kuaidiyuan_login, R.id.iv_register, R.id.iv_set})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        //MApp.mSp.put(UserData.DEVID, "BCDD3C3EFA2115824DEF38AC93BDB4653557924EEC188DFE");
        //MApp.mSp.put(UserData.DEVNO, "JD13");
        //MApp.mSp.put(UserData.DEVKIND, "25");
        //OpenLockService.protocol_type = 0;
        //OpenLockService.usb485_serialport = 1;
        //devkind = MApp.mSp.getString(UserData.DEVKIND);

        switch (view.getId()) {
            case R.id.iv_set:
                login_password_dialog = new LoginPasswordDialog(this, this);
                login_password_dialog.setType(0);
                login_password_dialog.show();
                break;
            case R.id.ll_kuaidiyuan_login://存件
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                if (TextUtils.isEmpty(devkind)) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                if ((OpenLockService.usb485_serialport == 0 && !MApp.mUsb485Util.driverOpen) || (OpenLockService.usb485_serialport == 1 && !MApp.mSerialportUtil.driverOpen)) {
                    showToast(getResources().getString(R.string.Failure_of_locking_board_connection), 2);
                    ttsSpeak(getResources().getString(R.string.Failure_of_locking_board_connection));
                    return;
                }
                switch (devkind) {
                    case TYPE_23://有屏扫码存包柜
                        LoginSweepCodeDialog login_sweepCode_dialog = new LoginSweepCodeDialog(this, this);
                        login_sweepCode_dialog.setType(1);
                        login_sweepCode_dialog.show();
                        break;
                    case TYPE_20://有屏校园配餐柜
                    case TYPE_22://有屏刷卡存包柜
                    case TYPE_25://有屏社区快递柜
                    case TYPE_26://有屏商务快递柜
                        LoginPaybyCardDialog mLogin_paybyCard_dialog = new LoginPaybyCardDialog(this, this);
                        mLogin_paybyCard_dialog.setType(1);
                        mLogin_paybyCard_dialog.show();
                        break;
                    case TYPE_29://有屏智能洗衣柜
                        StorageLaundryCabinetDialog storageLaundryCabinetDialog = new StorageLaundryCabinetDialog(this, this);
                        storageLaundryCabinetDialog.show();
                        break;
                    default:
                        mLogin_paybyCard_dialog = new LoginPaybyCardDialog(this, this);
                        mLogin_paybyCard_dialog.setType(1);
                        mLogin_paybyCard_dialog.show();
                        break;
                }
                break;
            case R.id.iv_left1:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                if (TextUtils.isEmpty(devkind)) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                if ((OpenLockService.usb485_serialport == 0 && !MApp.mUsb485Util.driverOpen) || (OpenLockService.usb485_serialport == 1 && !MApp.mSerialportUtil.driverOpen)) {
                    showToast(getResources().getString(R.string.Failure_of_locking_board_connection), 2);
                    ttsSpeak(getResources().getString(R.string.Failure_of_locking_board_connection));
                    return;
                }
                switch (devkind) {
                    case TYPE_22://有屏刷卡存包柜
                        PayByCardTakeDialog payByCardTakeDialog = new PayByCardTakeDialog(this, this);
                        payByCardTakeDialog.show();
                        break;
                    case TYPE_23://有屏扫码存包柜
                        VerificationCodeSweepTakeDialog verificationCodeSweepTakeDialog = new VerificationCodeSweepTakeDialog(this, this);
                        verificationCodeSweepTakeDialog.show();
                        break;
                    case TYPE_25://有屏社区快递柜
                    case TYPE_26://有屏商务快递柜
                    case TYPE_29://有屏智能洗衣柜
                        VerificationCodeTakeDialog verificationCodeTakeDialog = new VerificationCodeTakeDialog(this, this);
                        verificationCodeTakeDialog.show();
                        break;
                    default:
                        verificationCodeTakeDialog = new VerificationCodeTakeDialog(this, this);
                        verificationCodeTakeDialog.show();
                        break;
                }
                break;
            case R.id.iv_register:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                if (TextUtils.isEmpty(devkind)) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                UserRegisterDialog userRegisterDialog = new UserRegisterDialog(this, this);
                userRegisterDialog.show();
                break;
            case R.id.iv_right1:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getResources().getString(R.string.login_tip_e), 2);
                    ttsSpeak(getResources().getString(R.string.login_tip_e));
                    return;
                }
                if (TextUtils.isEmpty(devkind)) {
                    showToast(getResources().getString(R.string.not_register), 2);
                    ttsSpeak(getResources().getString(R.string.not_register));
                    return;
                }
                DepositDialog depositDialog = new DepositDialog(this, this);
                depositDialog.show();
                /*switch (devkind) {
                    case TYPE_26://有屏商务快递柜
                        DepositDialog depositDialog = new DepositDialog(this, this);
                        depositDialog.show();
                        break;
                    default:
                        break;
                }*/
                break;
            case R.id.iv_right2:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getResources().getString(R.string.login_tip_e), 2);
                    ttsSpeak(getResources().getString(R.string.login_tip_e));
                    return;
                }
                LoginLetterBoxDialog loginLetterBoxDialog = new LoginLetterBoxDialog(this, this);
                loginLetterBoxDialog.show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mBannerLoad.start(banner);
        initUVCCamera2();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBannerLoad.stop(banner);
        initUVCCamera3();
    }

    @Override
    protected void onDestroy() {
        if (null != mThread) {
            mThread.interrupt();
            mThread = null;
        }
        if (null != mADThread) {
            mADThread.interrupt();
            mADThread = null;
        }
        initUVCCamera4();
        EventBus.getDefault().unregister(this);
        MApp.mSerialportUtil.close();
        MApp.mUsb485Util.close();
        closeTimer();
        super.onDestroy();
    }

    /**
     * 设置摄像头分辨率
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setCamera(CameraSettingsBean bean) {
        if (bean.getStart() == 1) {
            showResolutionListDialog();
        }
    }

    /**
     * 接收到的上传图片信息
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void updataExpnoImage(ExpnoImageBean bean) {
        if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
            showToast("sorry,camera open failed", 2);
            return;
        }
        if (TextUtils.isEmpty(bean.getType()) && null == bean.getExpnos()) {
            return;
        }
        for (int i = 0; i < bean.getExpnos().size(); i++) {
            String picPath = FileUtil.imageFileDir + System.currentTimeMillis() + UVCCameraHelper.SUFFIX_JPEG;
            int finalI = i;
            mCameraHelper.capturePicture(picPath, path -> {
                LogUtils.e(TAG, "save path：" + path);
                if (TextUtils.isEmpty(path)) {
                    return;
                }
                List<File> files = new ArrayList<>();
                File file = new File(path);
                files.add(file);
                uploadfile(bean.getExpnos().get(finalI), bean.getType(), MApp.mSp.getString(UserData.COMPNO), files);
            });
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 启动定时器 显示时间刷新
     */
    public void startTime() {
        int count_time = 60; //总时间
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count_time + 1)
                .map(aLong -> {
                    return count_time - aLong;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mTimeDisposable = d;
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onNext(Long value) {
                        if (null != MApp.systemInfoEntity) {
                            tv_title_right_1.setText(DateTimeUtil.getCurDateStr("HH:mm:ss"));
                            tv_title_right_2.setText(DateTimeUtil.getWeek(context));
                            tv_title_right_3.setText(DateTimeUtil.getCurDateStr("yyyy-MM-dd"));
                            tv_title_left_1.setText(MApp.systemInfoEntity.getDepotna() + MApp.systemInfoEntity.getLibna());
                            tv_title_left_2.setText(MApp.systemInfoEntity.getFixna() + "(" + MApp.mSp.getString(UserData.DEVNO) + ")");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        closeTimer();
                        startTime();
                    }
                });
    }

    /**
     * 关闭定时器
     */
    public void closeTimer() {
        if (mTimeDisposable != null) {
            mTimeDisposable.dispose();
        }
    }

    /**
     * 初始化一些默认数据
     */
    private void initData() {
        /*启动摄像头*/
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEPSHOT))
                && !TextUtils.isEmpty(MApp.mSp.getString(UserData.TAKESHOT))
                && MApp.mSp.getString(UserData.DEPSHOT).equals("1")
                && MApp.mSp.getString(UserData.TAKESHOT).equals("1")) {
            initUVCCamera1(this, mTextureView);
        } else {
            initUVCCamera1(this, mTextureView);
        }
        /*启动默认 USB / 串口 驱动*/
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.OPENKIND))) {
            if (MApp.mSp.getString(UserData.OPENKIND).equals("1")) {
                OpenLockService.usb485_serialport = 1;
            } else {
                OpenLockService.usb485_serialport = 0;
            }
            if (TextUtils.isEmpty(MApp.mSp.getString(UserData.PROTKIND))) {//默认
                OpenLockService.protocol_type = 0;
            } else if (MApp.mSp.getString(UserData.PROTKIND).equals("20")) {//银龙
                OpenLockService.protocol_type = 1;
            } else if (MApp.mSp.getString(UserData.PROTKIND).equals("30")) {//果核
                OpenLockService.protocol_type = 2;
            } else {//默认
                OpenLockService.protocol_type = 0;
            }
            initDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        /*启动默认 USB / 串口 驱动*/
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.OPENKIND))) {
            if (MApp.mSp.getString(UserData.OPENKIND).equals("1")) {
                OpenLockService.usb485_serialport = 1;
            } else {
                OpenLockService.usb485_serialport = 0;
            }
            if (TextUtils.isEmpty(MApp.mSp.getString(UserData.PROTKIND))) {//默认
                OpenLockService.protocol_type = 0;
            } else if (MApp.mSp.getString(UserData.PROTKIND).equals("20")) {//银龙
                OpenLockService.protocol_type = 1;
            } else if (MApp.mSp.getString(UserData.PROTKIND).equals("30")) {//果核
                OpenLockService.protocol_type = 2;
            } else {//默认
                OpenLockService.protocol_type = 0;
            }
            initDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
        }
    }

    /**
     * 初始化驱动
     *
     * @param usb485_serialport 0 USB / 1 串口
     * @param protocol_type     0 默认 / 1 银龙 / 2 果核
     */
    private void initDriver(int usb485_serialport, int protocol_type) {
        switch (usb485_serialport) {
            case 0://USB
                switch (protocol_type) {
                    case 0://默认
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(9600);
                            LogUtils.e(TAG, "usb 默认");
                        }
                        break;
                    case 1://银龙
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(115200);
                            LogUtils.e(TAG, "usb 银龙");
                        }
                        break;
                    case 2://果核
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(9600);
                            LogUtils.e(TAG, "usb 果核");
                        }
                        break;
                    default:
                        break;
                }
                break;
            case 1://串口
                MApp.mSerialportUtil.close();
                new Thread(() -> {
                    int size = 5;
                    while (!MApp.mSerialportUtil.driverOpen && size > 0) {
                        size -= 1;
                        if (!TextUtils.isEmpty(MApp.mSp.getString(Data.DEVICE)) && !TextUtils.isEmpty(MApp.mSp.getString(Data.BAUDRATE))) {
                            if (protocol_type == 1) {
                                MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE), "115200");
                            } else {
                                MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE), MApp.mSp.getString(Data.BAUDRATE));
                            }
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    /**
     * 轮播广告
     */
    private void initBanner() {
        mBannerLoad = new BannerLoad();
        banner.setFocusable(true);
        banner.setFocusableInTouchMode(true);
        banner.requestFocus();
        List<Object> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.fixplusEntitiesAD1))) {
            try {
                JSONArray jsonArray = new JSONArray(MApp.mSp.getString(UserData.fixplusEntitiesAD1));
                if (jsonArray.length() == 0) {
                    urls.add(R.drawable.ic_ad1);
                    urls.add(R.drawable.ic_ad2);
                    urls.add(R.drawable.ic_ad3);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    LogUtils.e(TAG, "解析 本地保存的半屏广告地址 " + jsonArray.get(i));
                    urls.add(String.valueOf(jsonArray.get(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            urls.add(R.drawable.ic_ad1);
            urls.add(R.drawable.ic_ad2);
            urls.add(R.drawable.ic_ad3);
        }
        mBannerLoad.init(banner, urls);
        //启动广告定时器
        mADThread = new Thread(mADRunnable, "ADThread");
        mADThread.start();
    }

    /**
     * 定时广告
     */
    private Runnable mADRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                mADHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 定时广告
     */
    @SuppressLint("HandlerLeak")
    private Handler mADHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (null == TimingTskService.fixplusEntitiesAD1 || TimingTskService.fixplusEntitiesAD1.size() == 0) {
                    return;
                }
                List<Object> urls = new ArrayList<>();
                try {
                    JSONArray urlsArray = new JSONArray();
                    for (int i = 0; i < TimingTskService.fixplusEntitiesAD1.size(); i++) {
                        if (!TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD1.get(i).getStatetime())//开始时间
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD1.get(i).getEndtime())//结束时间
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD1.get(i).getStateno())//开关 0 关 1 开
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD1.get(i).getFileurl())//图片地址
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD1.get(i).getPluskind())//子设备号
                        ) {
                            Date nowTime = DateTimeUtil.strToDateLong(DateTimeUtil.getCurDateStr("HH:mm"));
                            Date startTime = DateTimeUtil.strToDateLong(TimingTskService.fixplusEntitiesAD1.get(i).getStatetime());
                            Date endTime = DateTimeUtil.strToDateLong(TimingTskService.fixplusEntitiesAD1.get(i).getEndtime());
                            if (DateTimeUtil.isEffectiveDate(nowTime, startTime, endTime)
                                    && TimingTskService.fixplusEntitiesAD1.get(i).getStateno().equals("1")
                                    && TimingTskService.fixplusEntitiesAD1.get(i).getPluskind().equals("2")) {
                                urls.add(TimingTskService.fixplusEntitiesAD1.get(i).getFileurl());
                                //半屏广告保存到本地
                                urlsArray.put(TimingTskService.fixplusEntitiesAD1.get(i).getFileurl());
                            }
                        }
                    }
                    MApp.mSp.put(UserData.fixplusEntitiesAD1, urlsArray.toString());
                    if (urls.size() > 0) {
                        mBannerLoad.stop(banner);
                        mBannerLoad.update(banner, urls);
                        LogUtils.e(TAG, "图片更新了 数量:" + urls.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 验证设备是否注册
     *
     * @param devid
     */
    private void devnoinfo(String devid) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devid", devid);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson("http://www.upus.cn:8091/" + "upus_APP/app/expressbox/devnoinfo", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备类型
     */
    private void fixinfo() {
        if (TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO))) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/fixinfo", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备信息 位置号/设备号。。。。。
     */
    private void devinfo() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_2;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/devinfo", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有货格 对应表
     */
    private void devall() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_3;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/devall", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照上传
     *
     * @param expno
     * @param type
     * @param compno
     * @param files
     */
    private void uploadfile(String expno, String type, String compno, List<File> files) {
        if (TextUtils.isEmpty(expno) || TextUtils.isEmpty(compno) || files.size() == 0) {
            return;
        }
        String url = MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/uploadfile/" + expno + "," + type + "," + compno;
        WEB_TYPT = TYPE_4;
        mHttpUtil.updata(url, files, this);
    }

    /**
     * 获取所有货格 对应表
     */
    private void storage(String shelfno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("shelfno", shelfno);
            WEB_TYPT = TYPE_5;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/mailbox/storage", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(String data) {
        super.onSuccess(data);
        if (TextUtils.isEmpty(data)) {
            showToast(getString(R.string.net_error_1), 2);
            ttsSpeak(getResources().getString(R.string.net_error_1));
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray mJsonArray;
            if (TextUtils.isEmpty(jsonObject.toString())) {
                showToast(getString(R.string.net_error_1), 2);
                ttsSpeak(getResources().getString(R.string.net_error_1));
                return;
            }
            switch (WEB_TYPT) {
                case TYPE_0:
                    //{"msg":"未注册，请先注册！","data":null,"success":"0"}
                    //{"msg":"参数不正确!","data":null,"success":"0"}
                    //{"msg":"操作成功!","data":"JD13,0004002","success":"1"}
                    //{"msg":"操作成功!","data":"0004002","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getString(R.string.not_register), 2);
                        ttsSpeak(getResources().getString(R.string.not_register));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getString(R.string.not_register), 2);
                        ttsSpeak(getResources().getString(R.string.not_register));
                        return;
                    }
                    if (!TextUtils.isEmpty(jsonObject.optString("data"))) {
                        MApp.mSp.put(UserData.DEVNO, jsonObject.optString("data"));
                        fixinfo();
                    }
                    break;
                case TYPE_1://获取设备类型
                    //{"msg":"查询成功!","data":[{"devkind":"3","protkind":null,"lockind":"0"}],"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        devinfo();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        devinfo();
                        return;
                    }
                    mJsonArray = jsonObject.optJSONArray("data");
                    if (mJsonArray.length() == 0) {
                        devinfo();
                        return;
                    }
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject object = (JSONObject) mJsonArray.opt(i);
                        devkind = object.optString("devkind");
                        String protkind = object.optString("protkind");//协议类型
                        String lockind = object.optString("lockind");
                        //depshot 存件是否拍照,takeshot 取件是否拍照
                        String depshot = object.optString("depshot");
                        String takeshot = object.optString("takeshot");
                        LogUtils.e(TAG, "存件是否拍照: " + depshot + "  取件是否拍照: " + takeshot);
                        MApp.mSp.put(UserData.DEPSHOT, depshot);
                        MApp.mSp.put(UserData.TAKESHOT, takeshot);
                        LogUtils.e(TAG, "柜子类型 devkind:" + devkind + " protkind:" + protkind + " lockind:" + lockind);
                        MApp.mSp.put(UserData.DEVKIND, devkind);
                        MApp.mSp.put(UserData.PROTKIND, protkind);//20 银龙
                        MApp.mSp.put(UserData.LOCKIND, lockind);//开门通/闭门通

                        if (TextUtils.isEmpty(protkind)) {//默认
                            OpenLockService.protocol_type = 0;
                        } else if (protkind.equals("20")) {//银龙
                            OpenLockService.protocol_type = 1;
                        } else if (protkind.equals("30")) {//果核
                            OpenLockService.protocol_type = 2;
                        } else {//默认
                            OpenLockService.protocol_type = 0;
                        }

                        if (!TextUtils.isEmpty(object.optString("openkind"))) {
                            //协议类型 0 USB 1 串口
                            MApp.mSp.put(UserData.OPENKIND, object.optString("openkind"));
                            if (object.optString("openkind").equals("1")) {
                                OpenLockService.usb485_serialport = 1;
                            } else {
                                OpenLockService.usb485_serialport = 0;
                            }
                        } else {
                            MApp.mSp.put(UserData.OPENKIND, "0");
                            OpenLockService.usb485_serialport = 0;
                        }

                        initDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                        LogUtils.e(TAG, "usb485_serialport:" + OpenLockService.usb485_serialport + " protocol_type: " + OpenLockService.protocol_type);
                    }
                    devinfo();
                    break;
                case TYPE_2://获取设备信息 位置号/设备号。。。。。
                    //{"msg":"获取设备信息成功!","data":{"depotna":"上海互巴","depotno":"001","libno":"00103","fixna":"大配餐柜3号","libna":"1","mainpic":""," custno":""},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        devall();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        devall();
                        return;
                    }
                    JSONObject object = jsonObject.optJSONObject("data");
                    if (TextUtils.isEmpty(object.toString())) {
                        return;
                    }
                    MApp.systemInfoEntity = (SystemInfoEntity) GsonUtil.stringToObject(object.toString(), SystemInfoEntity.class);
                    if (null != MApp.systemInfoEntity && TextUtils.isEmpty(MApp.systemInfoEntity.getDepotna())) {
                        MApp.systemInfoEntity.setDepotna("");
                    }
                    devall();
                    break;
                case TYPE_3://获取所有货格 对应表
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    mJsonArray = jsonObject.optJSONArray("data");
                    if (mJsonArray.length() == 0) {
                        return;
                    }
                    devallBeans.clear();
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject obj = (JSONObject) mJsonArray.opt(i);
                        DevallBean devallBean = (DevallBean) GsonUtil.stringToObject(obj.toString(), DevallBean.class);
                        devallBean.setBordercnt(jsonObject.optString("bordercnt"));
                        devallBeans.add(devallBean);
                    }
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(getString(R.string.net_error_2), 2);
            ttsSpeak(getResources().getString(R.string.net_error_2));
        }
    }

    @Override
    public void onError(String data) {
        super.onError(data);
        showToast(getString(R.string.net_error_1), 2);
        ttsSpeak(getResources().getString(R.string.net_error_1));
    }

}
