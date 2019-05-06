package cn.upus.app.upems.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.bean.SystemInfoEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.OpenLockService;
import cn.upus.app.upems.ui.dialog.login.LoginPasswordDialog;
import cn.upus.app.upems.ui.dialog.yaoshi.YaoShi_LoginDialog;
import cn.upus.app.upems.ui.dialog.yaoshi.YaoShi_Out_Dialog;
import cn.upus.app.upems.util.NetWorkUtils;
import cn.upus.app.upems.util.gson.GsonUtil;
import cn.upus.app.upems.util.serialport_usb485.Data;

/**
 * 钥匙柜
 */
@SuppressLint("Registered")
public class Main6 extends BaseActivity {

    private static final String TAG = Main6.class.getSimpleName();

    @BindView(R.id.iv1)
    ImageView iv1;
    @BindView(R.id.iv2)
    ImageView iv2;
    @BindView(R.id.bt_set)
    Button bt_set;
    @BindView(R.id.tv_depotna)
    TextView tv_depotna;
    @BindView(R.id.tv_devno)
    TextView tv_devno;

    private String devkind;

    @Override
    protected int initLayoutView() {
        return R.layout.activity_main6;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        getFixinfo();
    }

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

    private void initData() {
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
                        }
                        break;
                    case 1://银龙
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(115200);
                        }
                        break;
                    case 2://果核
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(9600);
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

    @OnClick({R.id.bt_set, R.id.iv1, R.id.iv2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_set:
                LoginPasswordDialog login_password_dialog = new LoginPasswordDialog(this, this);
                login_password_dialog.setType(0);
                login_password_dialog.show();
                break;
            case R.id.iv1:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL))) {
                    showToast(getString(R.string.not_register), 2);
                    ttsSpeak(getString(R.string.not_register));
                    return;
                }
                if ((OpenLockService.usb485_serialport == 0 && !MApp.mUsb485Util.driverOpen) || (OpenLockService.usb485_serialport == 1 && !MApp.mSerialportUtil.driverOpen)) {
                    showToast(getResources().getString(R.string.Failure_of_locking_board_connection), 2);
                    ttsSpeak(getResources().getString(R.string.Failure_of_locking_board_connection));
                    return;
                }
                new YaoShi_LoginDialog(this, this, 0).show();
                break;
            case R.id.iv2:
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL))) {
                    showToast(getString(R.string.not_register), 2);
                    ttsSpeak(getString(R.string.not_register));
                    return;
                }
                if ((OpenLockService.usb485_serialport == 0 && !MApp.mUsb485Util.driverOpen) || (OpenLockService.usb485_serialport == 1 && !MApp.mSerialportUtil.driverOpen)) {
                    showToast(getResources().getString(R.string.Failure_of_locking_board_connection), 2);
                    ttsSpeak(getResources().getString(R.string.Failure_of_locking_board_connection));
                    return;
                }
                new YaoShi_LoginDialog(this, this, 1).show();
                //new YaoShi_Out_Dialog(this, this).show();
                break;
        }
    }

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


    @SuppressLint("SetTextI18n")
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
                        MApp.mSp.put(UserData.PROTKIND, protkind);//20 银龙 30 果核
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
                    }
                    devinfo();
                    break;
                case TYPE_2://获取设备信息 位置号/设备号。。。。。
                    //{"msg":"获取设备信息成功!","data":{"depotna":"上海互巴","depotno":"001","libno":"00103","fixna":"大配餐柜3号","libna":"1","mainpic":""," custno":""},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
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
                    if (null != MApp.systemInfoEntity) {
                        try {
                            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotna())) {
                                MApp.systemInfoEntity.setDepotna("");
                            }
                            if (TextUtils.isEmpty(MApp.systemInfoEntity.getLibna())) {
                                MApp.systemInfoEntity.setLibna("");
                            }
                            tv_depotna.setText("[ " + MApp.systemInfoEntity.getDepotna() + MApp.systemInfoEntity.getLibna() + " ]");
                            tv_devno.setText("[ " + MApp.mSp.getString(UserData.DEVNO) + " ]");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
