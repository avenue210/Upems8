package cn.upus.app.upems.ui.dialog.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.LoginDataEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.dialog.storage.BoxGroupDialog;
import cn.upus.app.upems.ui.dialog.storage.BoxListDialog;
import cn.upus.app.upems.ui.dialog.SystemSetDialog;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 用户登录
 */
public class LoginPasswordDialog extends BaseDialog {

    private static final String TAG = LoginPasswordDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.et_company)
    TextInputEditText et_company;
    @BindView(R.id.et_user)
    TextInputEditText et_user;
    @BindView(R.id.et_password)
    TextInputEditText et_password;
    @BindView(R.id.bt_login)
    Button bt_login;
    @BindView(R.id.bt_card_login)
    Button bt_card_login;
    @BindView(R.id.tv_devid)
    TextView tv_devid;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private String company;
    private String userName;
    private String passWord;

    private LoginDataEntity loginDataEntity;

    private int type = -1;

    public void setType(int type) {
        this.type = type;
    }

    private CountDownTimer start = new CountDownTimer(60 * 1000, 1000) {
        @SuppressLint("SetTextI18n")
        @Override
        public void onTick(long millisUntilFinished) {
            tv_time.setText(millisUntilFinished / 1000 + " s");
        }

        @Override
        public void onFinish() {
            start.cancel();
            try {
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public LoginPasswordDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_login;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY))) {
            et_company.setText(MApp.mSp.getString(UserData.COMPANY));
            if (type == TYPE_1) {
                et_company.setEnabled(false);
            }
        }
        tv_devid.setText("DEVID: " + MApp.mSp.getString(UserData.DEVID));
        start.start();
    }

    @Override
    protected void onStop() {
        if (null != start) {
            start.cancel();
            start = null;
        }
        super.onStop();
    }

    @Override
    protected void initWindow() {
        Window window = getWindow();
        if (null != window) {
            BarUtils.setNavBarVisibility(activity, false);
            BarUtils.setStatusBarVisibility(activity, false);
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams windowParams = window.getAttributes();
            //设置宽度顶满屏幕,无左右留白
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout(dm.widthPixels, window.getAttributes().height);
            windowParams.width = dm.widthPixels / 3 * 2;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            windowParams.dimAmount = 0.0f;
            window.setAttributes(windowParams);
            setCanceledOnTouchOutside(false);
        }
    }

    @OnClick({R.id.iv_close, R.id.bt_login, R.id.bt_card_login})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_login:
                if (!NetworkUtils.isConnected()) {
                    showToast(getContext().getString(R.string.net_error), 2);
                    return;
                }
                company = et_company.getText().toString().trim();
                userName = et_user.getText().toString().trim();
                passWord = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(company)) {
                    et_company.requestFocus();
                    showToast(getContext().getResources().getString(R.string.login_tip_a), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_a));
                    return;
                }
                if (TextUtils.isEmpty(userName)) {
                    et_user.requestFocus();
                    showToast(getContext().getResources().getString(R.string.login_tip_b), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_b));
                    return;
                }
                if (TextUtils.isEmpty(passWord)) {
                    et_password.requestFocus();
                    showToast(getContext().getResources().getString(R.string.login_tip_c), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_b));
                    return;
                }
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || (!TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY)) && !MApp.mSp.getString(UserData.COMPANY).equals(et_company.getText().toString().trim()))) {
                    getUrl();
                } else {
                    devlogin();
                }
                break;
            case R.id.bt_card_login:
                LoginPaybyCardDialog login_paybyCard_dialog = new LoginPaybyCardDialog(getContext(), activity);
                login_paybyCard_dialog.setType(type);
                login_paybyCard_dialog.show();
                dismiss();
                break;
            default:
                break;
        }
    }

    private void getUrl() {
        String url = "http://www.upus.cn:8091/zrtd_SPM/share.do";
        Map<String, String> params = new HashMap<>();
        params.put("act", "DataToJsons");
        params.put("ASQL", "select jeeurl from compinf where cocode='" + company + "'");
        WEB_TYPT = TYPE_0;
        mHttpUtil.postParams(url, params, this);
    }

    private void login() {
        String url;
        if (MApp.mSp.getBoolean(UserData.LOCAL_SERVICE)) {
            String serviceIP = MApp.mSp.getString(UserData.LOCAL_IP);
            url = "http://" + serviceIP + ":8091/zrtd_SPM/order.do";
        } else {
            String serviceIP = MApp.mSp.getString(UserData.WEB_URL);
            if (!serviceIP.substring(serviceIP.length() - 1).equals("/")) {
                serviceIP = serviceIP + "/";
            }
            url = serviceIP + "zrtd_SPM/order.do";
        }
        Map<String, String> params = new HashMap<>();
        params.put("act", "saveUserInfo");
        params.put("companyNo", company);
        params.put("userName", userName);
        params.put("userPwd", passWord);
        params.put("userType", "1");
        WEB_TYPT = TYPE_1;
        mHttpUtil.postParams(url, params, this);
    }

    private void devlogin() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compcode", company);
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("devid", MApp.mSp.getString(UserData.DEVID));
            jsonObject.put("logno", userName);
            jsonObject.put("pwd", passWord);
            jsonObject.put("type", "1");
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/devlogin", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(String data) {
        super.onSuccess(data);
        if (TextUtils.isEmpty(data)) {
            showToast(getContext().getString(R.string.net_error_1), 2);
            BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (TextUtils.isEmpty(jsonObject.toString())) {
                showToast(getContext().getString(R.string.net_error_1), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
                return;
            }
            switch (WEB_TYPT) {
                case TYPE_0:
                    if (TextUtils.isEmpty(jsonObject.optString("retcode")) || !jsonObject.optString("retcode").equals("1")) {
                        showToast(getContext().getString(R.string.net_tip_type_0_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_0_error_1));
                        return;
                    }
                    String listTask = jsonObject.optString("listTask");
                    if (TextUtils.isEmpty(listTask)) {
                        showToast(getContext().getString(R.string.net_tip_type_0_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_0_error_1));
                        return;
                    }
                    MApp.mSp.put(UserData.WEB_URL, listTask);
                    devlogin();
                    break;
                case TYPE_1:
                    if (TextUtils.isEmpty(jsonObject.optString("code")) || !jsonObject.optString("code").equals("0")) {
                        showToast(getContext().getString(R.string.net_tip_type_1_error_1) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_error_1) + jsonObject.optString("msg"));
                        return;
                    }
                    if (TextUtils.isEmpty(jsonObject.optJSONObject("data").toString())) {
                        showToast(getContext().getString(R.string.net_tip_type_1_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_error_1));
                        return;
                    }

                    if (!TextUtils.isEmpty(jsonObject.optString("devno"))) {
                        LogUtils.e(TAG, "devno:" + jsonObject.optString("devno"));
                        MApp.mSp.put(UserData.DEVNO, jsonObject.optString("devno"));
                    }

                    loginDataEntity = (LoginDataEntity) GsonUtil.stringToObject(jsonObject.optJSONObject("data").toString(), LoginDataEntity.class);
                    MApp.mSp.put(UserData.WSDL, loginDataEntity.getServUrl());
                    MApp.mSp.put(UserData.PASS_WORD_EYPT, loginDataEntity.getPwd());
                    MApp.mSp.put(UserData.NAME, loginDataEntity.getUserna());
                    MApp.mSp.put(UserData.COMPNO, loginDataEntity.getCompno());
                    MApp.mSp.put(UserData.WEB_URL, loginDataEntity.getJeeurl());
                    MApp.mSp.put(UserData.USER_LEVEL, loginDataEntity.getUserlev());
                    MApp.mSp.put(UserData.ROLCODE, loginDataEntity.getRolcode());
                    MApp.mSp.put(UserData.CODE_NAME, loginDataEntity.getCodeName());
                    MApp.mSp.put(UserData.ROLENO, loginDataEntity.getRoleno());
                    MApp.mSp.put(UserData.BRANNO, loginDataEntity.getBranno());
                    MApp.mSp.put(UserData.MANNO, loginDataEntity.getManno());
                    MApp.mSp.put(UserData.CUSTNO, loginDataEntity.getCustno());
                    try {
                        /*附件地址*/
                        String enclosureUrl = loginDataEntity.getServUrl().substring(0, loginDataEntity.getServUrl().indexOf("UpService.asmx"));
                        if (!TextUtils.isEmpty(enclosureUrl)) {
                            MApp.mSp.put(UserData.ENCLOSURE_URL, enclosureUrl);
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, e.getMessage());
                    }
                    MApp.mSp.put(UserData.COMPANY, company);
                    MApp.mSp.put(UserData.USER_NAME, userName);
                    MApp.mSp.put(UserData.PASS_WORD, passWord);
                    MApp.mSp.put(UserData.IS_LOGIN, true);
                    showToast(getContext().getString(R.string.net_tip_type_1_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_success));

                    if (type == 0) {
                        if (loginDataEntity.getUserlev() <= 2) {
                            SystemSetDialog systemSetDialog = new SystemSetDialog(getContext(), activity);
                            systemSetDialog.show();
                        } else {
                            showToast(getContext().getResources().getString(R.string.not_an_administrator), 2);
                        }
                    } else {
                        if (MApp.mSp.getString(UserData.DEVKIND).equals(BaseActivity.TYPE_20)) {
                            BoxGroupDialog boxGroupDialog = new BoxGroupDialog(getContext(), activity);
                            boxGroupDialog.show();
                        } else {
                            BoxListDialog boxListDialog = new BoxListDialog(getContext(), activity);
                            boxListDialog.show();
                        }
                    }
                    dismiss();
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast(getContext().getString(R.string.net_error_2), 2);
            BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_2));
        }
    }

    @Override
    public void onError(String data) {
        super.onError(data);
        showToast(getContext().getString(R.string.net_error_1), 2);
        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
    }
}
