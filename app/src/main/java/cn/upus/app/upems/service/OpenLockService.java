package cn.upus.app.upems.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import cn.upus.app.upems.ui.activity.*;
import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.serialport_usb485.GuoHeOpenLockUtil;
import cn.upus.app.upems.util.serialport_usb485.OpenLockUtil;
import cn.upus.app.upems.util.serialport_usb485.YinLongOpenLockUtil;
import cn.upus.app.upems.util.serialport_usb485.bean.Received;

/**
 * 开锁服务
 */
@SuppressLint("Registered")
public class OpenLockService extends Service {

    /**
     * 0 USB 485 / 1 串口
     */
    public static int usb485_serialport = -1;

    /**
     * 协议类型 0 默认 / 1 银龙 / 2 果核
     */
    public static int protocol_type = -1;

    private Thread startThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e("启动 发送指令 服务");
        EventBus.getDefault().register(this);
        startThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    if (MApp.mSp.getBoolean(UserData.START)) {
                        mHandler.sendEmptyMessage(0);
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        startThread.start();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                try {
                    String className;
                    Intent t;
                    String ad = "ADActivity";
                    if (MApp.mSp.getBoolean(UserData.isHorizontal)) {
                        className = "Main3";
                        if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                            t = new Intent(OpenLockService.this, Main3.class);
                            t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(t);
                        }
                    } else {
                        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && MApp.mSp.getString(UserData.DEVKIND).equals("26")) {
                            className = "Main2";
                            if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                                t = new Intent(OpenLockService.this, Main2.class);
                                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(t);
                            }
                        } else if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && MApp.mSp.getString(UserData.DEVKIND).equals("33")) {
                            className = "Main6";
                            if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                                t = new Intent(OpenLockService.this, Main6.class);
                                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(t);
                            }
                        } else if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && MApp.mSp.getString(UserData.DEVKIND).equals("34")) {
                            className = "Main7";
                            if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                                t = new Intent(OpenLockService.this, Main7.class);
                                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(t);
                            }
                        } else if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && MApp.mSp.getString(UserData.DEVKIND).equals("35")) {
                            className = "Main4";
                            if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                                t = new Intent(OpenLockService.this, Main4.class);
                                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(t);
                            }
                        } else {
                            className = "Main1";
                            if (!MApp.isForeground(OpenLockService.this, className) && !MApp.isForeground(OpenLockService.this, ad)) {
                                t = new Intent(OpenLockService.this, Main1.class);
                                t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(t);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (null != startThread) {
            startThread.interrupt();
            startThread = null;
        }
        super.onDestroy();
    }

    /**
     * 发送指令 后回传发送结果
     *
     * @param bean
     */
    private void callBackMessage(OpenLockCallBackBean bean) {
        EventBus.getDefault().post(bean);
    }

    /**
     * 接收到指令信息
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void getNewData(OpenLockDataBean bean) {
        LogUtils.e("接收到指令：" + bean.toString() + " usb485_serialport: " + usb485_serialport + " protocol_type :" + protocol_type);
        if (null == bean) {
            return;
        }
        switch (bean.getPosition()) {
            case 0://单个位置 指令
                switch (protocol_type) {
                    case 0://默认协议
                        sendInstructions_default(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    case 1://银龙协议
                        sendInstructions_YingLong(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    case 2://果核协议
                        sendInstructions_GuoHe(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    default:
                        break;
                }
                break;
            case 1://多个位置 指令
                if (bean.getLocks().size() == 0) {
                    return;
                }
                for (int i = 0; i < bean.getLocks().size(); i++) {
                    switch (protocol_type) {
                        case 0://默认协议
                            sendInstructions_default(bean.getType(), bean.getLocks().get(i).getBoardIndex(), bean.getLocks().get(i).getLockIndex());
                            break;
                        case 1://银龙协议
                            sendInstructions_YingLong(bean.getType(), bean.getLocks().get(i).getBoardIndex(), bean.getLocks().get(i).getLockIndex());
                            break;
                        case 2://果核协议
                            sendInstructions_GuoHe(bean.getType(), bean.getLocks().get(i).getBoardIndex(), bean.getLocks().get(i).getLockIndex());
                            break;
                        default:
                            break;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2://全部位置 指令
                switch (protocol_type) {
                    case 0://默认协议
                        sendInstructions_default(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    case 1://银龙协议
                        sendInstructions_YingLong(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    case 2://果核协议
                        sendInstructions_GuoHe(bean.getType(), bean.getBoardIndex(), bean.getLockIndex());
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private Received mReceived;

    /**
     * 发送默认指令
     *
     * @param type       指令 类型 0 开锁 / 1 查询单个 / 2 开灯 / 3 关灯 / 4 查询全部 / 5 全部开锁
     * @param boardIndex
     * @param lockIndex
     */
    private synchronized void sendInstructions_default(int type, int boardIndex, int lockIndex) {
        switch (usb485_serialport) {
            case 0://USB 485
                switch (type) {
                    case 0://开锁
                        mReceived = MApp.mUsb485Util.callBack(OpenLockUtil.openLock(boardIndex, lockIndex));
                        try {
                            Toast.makeText(this, "TYPE : " + mReceived.getType() + " " + mReceived.toString(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 1://查询单个
                        mReceived = MApp.mUsb485Util.callBack(OpenLockUtil.queryBox(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 2://开灯
                        mReceived = MApp.mUsb485Util.callBack(OpenLockUtil.openD(boardIndex, lockIndex, 1));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_dev_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.usb_open_dev_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_dev_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 3://关灯
                        mReceived = MApp.mUsb485Util.callBack(OpenLockUtil.openD(boardIndex, lockIndex, 0));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_close_dev_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.usb_close_dev_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_close_dev_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 4://查询全部
                        mReceived = MApp.mUsb485Util.callBack(OpenLockUtil.queryBoxAll(boardIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 5://全部开锁
                        if (MApp.mUsb485Util.sendData(OpenLockUtil.openLockAll(boardIndex))) {
                            MApp.mUsb485Util.readData = null;
                            callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_lock_success)));
                        } else {
                            //发送失败
                            callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                        }
                        break;
                    default:
                        break;
                }
                break;
            case 1://串口
                switch (type) {
                    case 0://开锁
                        mReceived = MApp.mSerialportUtil.callBack(OpenLockUtil.openLock(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 1://查询单个
                        mReceived = MApp.mSerialportUtil.callBack(OpenLockUtil.queryBox(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 2://开灯
                        mReceived = MApp.mSerialportUtil.callBack(OpenLockUtil.openD(boardIndex, lockIndex, 1));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_dev_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_dev_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_dev_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 3://关灯
                        mReceived = MApp.mSerialportUtil.callBack(OpenLockUtil.openD(boardIndex, lockIndex, 0));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_close_dev_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_close_dev_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_close_dev_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 4://查询全部
                        mReceived = MApp.mSerialportUtil.callBack(OpenLockUtil.queryBoxAll(boardIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 5://全部开锁
                        if (MApp.mSerialportUtil.sendData(OpenLockUtil.openLockAll(boardIndex))) {
                            MApp.mSerialportUtil.readData = null;
                            callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_lock_success)));
                        } else {
                            //发送失败
                            callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 发送 银龙指令
     *
     * @param type       指令 类型 0 开锁 / 1 查询单个 / 2 开灯 / 3 关灯 / 4 查询全部 / 5 全部开锁
     * @param boardIndex
     * @param lockIndex
     */
    private synchronized void sendInstructions_YingLong(int type, int boardIndex, int lockIndex) {
        switch (usb485_serialport) {
            case 0://USB 485
                switch (type) {
                    case 0://开锁指令
                        mReceived = MApp.mUsb485Util.callBack(YinLongOpenLockUtil.openLock(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 4://查询全部指令
                        mReceived = MApp.mUsb485Util.callBack(YinLongOpenLockUtil.queryBox(boardIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                }
                break;
            case 1://串口
                switch (type) {
                    case 0://开锁指令
                        mReceived = MApp.mSerialportUtil.callBack(YinLongOpenLockUtil.openLock(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 4://查询全部指令
                        mReceived = MApp.mSerialportUtil.callBack(YinLongOpenLockUtil.queryBox(boardIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 发送 果核指令
     *
     * @param type      指令 类型 0 开锁 / 1 查询单个 / 2 开灯 / 3 关灯 / 4 查询全部 / 5 全部开锁
     * @param lockIndex
     */
    private synchronized void sendInstructions_GuoHe(int type, int boardIndex, int lockIndex) {
        switch (usb485_serialport) {
            case 0://USB 485
                switch (type) {
                    case 0://开锁指令
                        mReceived = MApp.mUsb485Util.callBack(GuoHeOpenLockUtil.openLock(lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 1://查询指令
                        mReceived = MApp.mUsb485Util.callBack(GuoHeOpenLockUtil.queryBox(lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 2://烧入锁号
                        mReceived = MApp.mUsb485Util.callBack(GuoHeOpenLockUtil.writeLockID(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, "写入指定锁号 失败"));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mUsb485Util.readData, "写入指定锁号 成功"));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, "写入指定锁号 超时"));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, "写入指定锁号 写失败" + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                }
                break;
            case 1://串口
                switch (type) {
                    case 0://开锁指令
                        mReceived = MApp.mSerialportUtil.callBack(GuoHeOpenLockUtil.openLock(lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.usb_open_lock_error)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.usb_open_lock_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.usb_open_lock_overtime)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.usb_open_lock_error) + " serial port abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 1://查询指令
                        mReceived = MApp.mSerialportUtil.callBack(GuoHeOpenLockUtil.queryBox(lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, getResources().getString(R.string.query_failure)));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, getResources().getString(R.string.query_success)));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, getResources().getString(R.string.query_timeout)));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, getResources().getString(R.string.query_write_write_failure) + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                    case 2://烧入锁号
                        mReceived = MApp.mSerialportUtil.callBack(GuoHeOpenLockUtil.writeLockID(boardIndex, lockIndex));
                        if (null != mReceived) {
                            switch (mReceived.getType()) {
                                case 0://失败
                                    callBackMessage(new OpenLockCallBackBean(0, null, "写入指定锁号 失败"));
                                    break;
                                case 1://成功
                                    callBackMessage(new OpenLockCallBackBean(1, MApp.mSerialportUtil.readData, "写入指定锁号 成功"));
                                    break;
                                case 2://超时
                                    callBackMessage(new OpenLockCallBackBean(2, null, "写入指定锁号 超时"));
                                    break;
                                case 3://发送失败
                                    callBackMessage(new OpenLockCallBackBean(4, null, "写入指定锁号 写失败" + " USB abnormal"));
                                    break;
                            }
                        }
                        break;
                }
                break;
            default:
                break;
        }
    }
}
