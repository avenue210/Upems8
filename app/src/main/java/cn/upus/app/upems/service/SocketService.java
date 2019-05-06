package cn.upus.app.upems.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.bean.PartParameterEntity;
import cn.upus.app.upems.bean.event_bus.PaymentCallBackBean;
import cn.upus.app.upems.bean.event_bus.ScanCodeLoginCallBackBean;
import cn.upus.app.upems.bean.event_bus.ScanCodeRegistrationCallBackBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.gson.GsonUtil;
import cn.upus.app.upems.util.http.HttpCallBack;
import cn.upus.app.upems.util.http.HttpUtil;
import cn.upus.app.upems.util.serialport_usb485.GuoHeOpenLockUtil;
import cn.upus.app.upems.util.serialport_usb485.OpenLockUtil;
import cn.upus.app.upems.util.serialport_usb485.YinLongOpenLockUtil;
import io.reactivex.disposables.Disposable;

/**
 * 服务端 SOCKET 接收服务
 */
@SuppressLint("Registered")
public class SocketService extends Service {

    private static final String TAG = SocketService.class.getSimpleName();

    private String wsUrl;
    private WebSocketClient mWebSocketClient;
    public static boolean socketConnect = false;

    private String notify_url;//接收到的动态 回调地址
    private String orderno;//接收到的 单号信息
    private String calltype;//0 websocket 1 get 2 post
    private List<PartParameterEntity> partParameterEntities = new ArrayList<>();

    private String lockind = "0";

    private HttpUtil mHttpUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        //EventBus.getDefault().register(this);
        mHttpUtil = new HttpUtil();
        mThread.start();
    }

    private Thread mThread = new Thread(() -> {
        while (true) {
            if (NetworkUtils.isConnected() && !socketConnect && !TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL))) {
                try {
                    wsUrl = MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/expresssoc";
                    initClient(wsUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void onDestroy() {
        //EventBus.getDefault().unregister(this);
        closeClient();
        if (null != mThread) {
            mThread.interrupt();
            mThread = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化
     *
     * @param url
     * @throws URISyntaxException
     */
    private void initClient(String url) throws URISyntaxException {
        if (mWebSocketClient == null) {
            mWebSocketClient = new WebSocketClient(new URI(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    LogUtils.e(TAG, "socke onOpen");
                    socketConnect = true;
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.LOCKIND))) {
                        lockind = MApp.mSp.getString(UserData.LOCKIND);
                    } else {
                        lockind = "0";
                    }
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
                        jsonObject.put("fixno", MApp.mSp.getString(UserData.DEVNO));
                        sendMessage(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onMessage(String message) {
                    LogUtils.e(TAG, "socke onMessage : " + message);
                    mHandleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LogUtils.e(TAG, "socke onClose");
                    closeClient();
                }

                @Override
                public void onError(Exception ex) {
                    LogUtils.e(TAG, "socke onError : " + ex.getMessage());
                    closeClient();
                }
            };
        }
        if (!socketConnect && NetworkUtils.isConnected()) {
            try {
                mWebSocketClient.connect();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接
     */
    private void connectClient() {
        if (null != mWebSocketClient && !socketConnect) {
            mWebSocketClient.connect();
        }
    }

    /**
     * 关闭
     */
    private void closeClient() {
        try {
            mWebSocketClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mWebSocketClient = null;
        }
        socketConnect = false;
    }

    /**
     * 发送信息
     *
     * @param data
     * @return
     */
    private boolean sendMessage(String data) {
        try {
            if (null != mWebSocketClient && socketConnect) {
                mWebSocketClient.send(data);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 处理接收到的信息
     *
     * @param message
     */
    private void mHandleMessage(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(message);
            String success = jsonObject.optString("success");
            if (TextUtils.isEmpty(success)) {
                return;
            }
            /* success : 1  //参数说明：1开指定板号锁号 2开全部板号锁号 3微信二维码登陆 4获取设备全部板号锁号状态 5支付成功标志  6注册成功  11 通知刷新页面*/
            switch (success) {
                case "1":
                    orderno = null;
                    notify_url = null;
                    calltype = null;
                    partParameterEntities.clear();
                    JSONArray jsonArray = jsonObject.optJSONArray("data");
                    if (jsonArray.length() == 0) {
                        return;
                    }
                    orderno = jsonObject.optString("orderno");
                    notify_url = jsonObject.optString("notify_url");
                    calltype = jsonObject.optString("calltype");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.opt(i);
                        PartParameterEntity parameterEntity = (PartParameterEntity) GsonUtil.stringToObject(object.toString(), PartParameterEntity.class);
                        LogUtils.e(TAG, "添加板号：" + parameterEntity.getBoardno() + " 添加锁号：" + parameterEntity.getLockno());
                        partParameterEntities.add(parameterEntity);
                    }
                    openLocks(partParameterEntities, orderno);
                    break;
                case "2"://全部开锁

                    break;
                case "3"://微信扫码登录信息
                    EventBus.getDefault().post(new ScanCodeLoginCallBackBean(1, jsonObject.optJSONObject("data").toString()));
                    break;
                case "4"://获取设备全部状态
                    queryAllBox();
                    break;
                case "5"://快递柜支付成功返回成功
                    EventBus.getDefault().post(new PaymentCallBackBean(1));
                    break;
                case "6"://快递员扫码注册 返回信息
                    EventBus.getDefault().post(new ScanCodeRegistrationCallBackBean(1));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 查询全部格子状态
     */
    private void queryAllBox() {
        if (BaseActivity.devallBeans.size() == 0) {
            return;
        }
        try {
            int size = Integer.parseInt(BaseActivity.devallBeans.get(0).getBordercnt());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开锁
     *
     * @param partParameterEntities
     * @param orderno
     */
    private void openLocks(List<PartParameterEntity> partParameterEntities, String orderno) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean openSuccess = false;
        for (int i = 0; i < partParameterEntities.size(); i++) {
            LogUtils.e(TAG, partParameterEntities.get(i).getBoardno() + "   -  " + partParameterEntities.get(i).getLockno());
            String action = partParameterEntities.get(i).getAction();
            int boardno = Integer.valueOf(partParameterEntities.get(i).getBoardno());
            int lockno = Integer.valueOf(partParameterEntities.get(i).getLockno());

            MApp.mUsb485Util.readData = null;
            MApp.mSerialportUtil.readData = null;

            switch (OpenLockService.usb485_serialport) {
                case 0://USB 485
                    switch (action) {
                        case "0":
                            switch (OpenLockService.protocol_type) {
                                case 0://默认协议
                                    if (MApp.mUsb485Util.sendData(OpenLockUtil.openLock(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        String msg;
                                        if (lockind.equals("0")) {
                                            msg = "11";
                                        } else if (lockind.equals("1")) {
                                            msg = "00";
                                        } else {
                                            msg = "11";
                                        }
                                        if (!TextUtils.isEmpty(MApp.mUsb485Util.readData) && MApp.mUsb485Util.readData.substring(0, 2).equals("8a") && MApp.mUsb485Util.readData.substring(6, 8).equals(msg)) {
                                            LogUtils.e(TAG, "开锁成功");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                            openSuccess = true;
                                        } else {
                                            LogUtils.e(TAG, "开锁失败");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                            openSuccess = false;
                                        }
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                case 1://银龙协议
                                    if (MApp.mUsb485Util.sendData(YinLongOpenLockUtil.openLock(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开锁成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                case 2://果核协议
                                    if (MApp.mUsb485Util.sendData(GuoHeOpenLockUtil.openLock(lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开锁成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                            }
                            break;
                        case "1"://开电
                            switch (OpenLockService.protocol_type) {
                                case 0:
                                    if (MApp.mUsb485Util.sendData(OpenLockUtil.openD(boardno, lockno, 1))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开电超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.open_electricity_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开电成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_dev_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开电发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "协议有误 开电发送失败");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    break;
                            }
                            break;
                        case "2"://关电
                            switch (OpenLockService.protocol_type) {
                                case 0:
                                    if (MApp.mUsb485Util.sendData(OpenLockUtil.openD(boardno, lockno, 1))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "关电超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.close_electricity_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "关电成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "关电发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "协议有误 关电发送失败");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_error));
                                    break;
                            }
                            break;
                        case "3"://查询格子状态
                            switch (OpenLockService.protocol_type) {
                                case 0://默认协议
                                    if (MApp.mUsb485Util.sendData(OpenLockUtil.queryBox(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mUsb485Util.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "查询超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.query_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        String msg;
                                        if (lockind.equals("0")) {
                                            msg = "11";
                                        } else if (lockind.equals("1")) {
                                            msg = "00";
                                        } else {
                                            msg = "11";
                                        }
                                        if (!TextUtils.isEmpty(MApp.mUsb485Util.readData) && MApp.mUsb485Util.readData.substring(0, 2).equals("80")) {
                                            if (MApp.mUsb485Util.readData.substring(6, 8).equals(msg)) {
                                                LogUtils.e(TAG, "查询成功 开");
                                                stringBuilder.append(backMessage(boardno, lockno, R.string.query_success_open));
                                                openSuccess = true;
                                            } else {
                                                LogUtils.e(TAG, "查询成功 关");
                                                stringBuilder.append(backMessage(boardno, lockno, R.string.query_success_close));
                                                openSuccess = true;
                                            }
                                        } else {
                                            LogUtils.e(TAG, "查询失败");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.query_failure));
                                            openSuccess = false;
                                        }
                                    } else {
                                        LogUtils.e(TAG, "获取板子状态发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_query_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "获取板子状态发送失败 协议有误");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_query_error));
                                    break;
                            }
                            break;
                    }
                    break;
                case 1://串口
                    switch (action) {
                        case "0":
                            switch (OpenLockService.protocol_type) {
                                case 0://默认协议
                                    if (MApp.mSerialportUtil.sendData(OpenLockUtil.openLock(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        String msg;
                                        if (lockind.equals("0")) {
                                            msg = "11";
                                        } else if (lockind.equals("1")) {
                                            msg = "00";
                                        } else {
                                            msg = "11";
                                        }
                                        if (!TextUtils.isEmpty(MApp.mSerialportUtil.readData) && MApp.mSerialportUtil.readData.substring(0, 2).equals("8a") && MApp.mSerialportUtil.readData.substring(6, 8).equals(msg)) {
                                            LogUtils.e(TAG, "开锁成功");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                            openSuccess = true;
                                        } else {
                                            LogUtils.e(TAG, "开锁失败");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                            openSuccess = false;
                                        }
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                case 1://银龙协议
                                    if (MApp.mSerialportUtil.sendData(YinLongOpenLockUtil.openLock(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开锁成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                case 2://果核协议
                                    if (MApp.mSerialportUtil.sendData(GuoHeOpenLockUtil.openLock(lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开锁超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_overtime));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开锁成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开锁发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                            }
                            break;
                        case "1"://开电
                            switch (OpenLockService.protocol_type) {
                                case 0:
                                    if (MApp.mSerialportUtil.sendData(OpenLockUtil.openD(boardno, lockno, 1))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "开电超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.open_electricity_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "开电成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_dev_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "开电发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "协议有误 开电发送失败");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_open_lock_error));
                                    break;
                            }
                            break;
                        case "2"://关电
                            switch (OpenLockService.protocol_type) {
                                case 0:
                                    if (MApp.mSerialportUtil.sendData(OpenLockUtil.openD(boardno, lockno, 1))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "关电超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.close_electricity_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        LogUtils.e(TAG, "关电成功");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_success));
                                        openSuccess = true;
                                    } else {
                                        LogUtils.e(TAG, "关电发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "协议有误 关电发送失败");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_error));
                                    break;
                            }
                            break;
                        case "3"://查询格子状态
                            switch (OpenLockService.protocol_type) {
                                case 0://默认协议
                                    if (MApp.mSerialportUtil.sendData(OpenLockUtil.queryBox(boardno, lockno))) {
                                        int index = 0;
                                        while (TextUtils.isEmpty(MApp.mSerialportUtil.readData) && index < 20) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            index++;
                                        }
                                        if (index >= 20) {
                                            LogUtils.e(TAG, "查询超时");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.query_timeout));
                                            openSuccess = false;
                                            break;
                                        }
                                        String msg;
                                        if (lockind.equals("0")) {
                                            msg = "11";
                                        } else if (lockind.equals("1")) {
                                            msg = "00";
                                        } else {
                                            msg = "11";
                                        }
                                        if (!TextUtils.isEmpty(MApp.mSerialportUtil.readData) && MApp.mSerialportUtil.readData.substring(0, 2).equals("80")) {
                                            if (MApp.mSerialportUtil.readData.substring(6, 8).equals(msg)) {
                                                LogUtils.e(TAG, "查询成功 开");
                                                stringBuilder.append(backMessage(boardno, lockno, R.string.query_success_open));
                                                openSuccess = true;
                                            } else {
                                                LogUtils.e(TAG, "查询成功 关");
                                                stringBuilder.append(backMessage(boardno, lockno, R.string.query_success_close));
                                                openSuccess = true;
                                            }
                                        } else {
                                            LogUtils.e(TAG, "查询失败");
                                            stringBuilder.append(backMessage(boardno, lockno, R.string.query_failure));
                                            openSuccess = false;
                                        }
                                    } else {
                                        LogUtils.e(TAG, "获取板子状态发送失败");
                                        stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_query_error));
                                    }
                                    break;
                                default:
                                    LogUtils.e(TAG, "获取板子状态发送失败 协议有误");
                                    stringBuilder.append(backMessage(boardno, lockno, R.string.usb_close_dev_query_error));
                                    break;
                            }
                            break;
                    }
                    break;
                default:
                    break;
            }

            LogUtils.e(TAG, "完成：" + boardno + "   " + lockno);
            try {
                Thread.sleep(500);
                LogUtils.e(TAG, "休眠 1S");
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogUtils.e(TAG, "休眠 1S异常");
            }
        }

        partParameterEntities.clear();
        if (TextUtils.isEmpty(calltype) || calltype.equals("0")) {
            //{"compno":"87550088","fixno":"HB01-03","data":"1号板1号锁成功,1号板2号锁成功,1号板3号锁成功","success":"1"}
            try {
                JSONObject object = new JSONObject();
                object.put("compno", MApp.mSp.getString(UserData.COMPNO));
                object.put("fixno", MApp.mSp.getString(UserData.DEVNO));
                if (TextUtils.isEmpty(orderno)) {
                    object.put("orderno", "");
                } else {
                    object.put("orderno", orderno);
                }
                if (!TextUtils.isEmpty(stringBuilder.toString())) {
                    object.put("data", stringBuilder.toString().substring(0, stringBuilder.toString().length() - 1));
                }
                //2018-11-15 新增参数
                if (openSuccess) {
                    object.put("success", "1");
                } else {
                    object.put("success", "0");
                }
                sendMessage(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (!TextUtils.isEmpty(orderno) && !TextUtils.isEmpty(notify_url) && !TextUtils.isEmpty(calltype) && calltype.equals("2")) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
                jsonObject.put("orderno", orderno);
                mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + notify_url, jsonObject.toString(), mHttpCallBack);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 信息拼接
     *
     * @param boardno
     * @param lockno
     * @param string
     * @return
     */
    private String backMessage(int boardno, int lockno, int string) {
        return getResources().getString(R.string.boardno_id) + ":" +
                boardno +
                getResources().getString(R.string.lockno_id) + ":" +
                lockno +
                getResources().getString(string) + ",";
    }

    private HttpCallBack mHttpCallBack = new HttpCallBack() {
        @Override
        public void showDialog() {

        }

        @Override
        public void hideDialog() {

        }

        @Override
        public void addDisposable(@NonNull Disposable disposable) {

        }

        @Override
        public void onSuccess(String data) {

        }

        @Override
        public void onError(String data) {

        }
    };

}
