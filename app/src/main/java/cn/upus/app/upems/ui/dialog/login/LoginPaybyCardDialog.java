package cn.upus.app.upems.ui.dialog.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.LoginDataEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.dialog.storage.BoxListDialog;
import cn.upus.app.upems.ui.dialog.SystemSetDialog;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.EditTextUtil;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 刷卡登录
 */
public class LoginPaybyCardDialog extends BaseDialog {

    private static final String TAG = LoginPaybyCardDialog.class.getSimpleName();

    @BindView(R.id.bt_password_login)
    Button bt_password_login;
    @BindView(R.id.bt_tel_login)
    Button bt_tel_login;
    @BindView(R.id.bt_weixin_login)
    Button bt_weixin_login;
    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.tv_time)
    TextView tv_time;

    private String cardno = null;

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
            dismiss();
        }
    };

    public LoginPaybyCardDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_login_pay_by_card;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MApp.mSp.getString(UserData.DEVKIND).equals("20")) {
            //互吧 隐藏两个登录类型
            bt_tel_login.setVisibility(View.GONE);
            bt_weixin_login.setVisibility(View.GONE);
        }
        start.start();
        initKeyboard();

        EditTextUtil.setEditText(activity, et_password);
        initEtBarCode();
    }

    private void initKeyboard() {
        //软键盘不自动弹出
        Window window = getWindow();
        if (null != window) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

    @OnClick({R.id.iv_close, R.id.bt_password_login, R.id.bt_tel_login, R.id.bt_weixin_login})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_tel_login:
                LoginTelLogin login_tel_login = new LoginTelLogin(getContext(), activity);
                login_tel_login.setType(type);
                login_tel_login.show();
                break;
            case R.id.bt_weixin_login:
                LoginWeiXinDialog loginWeiXinDialog = new LoginWeiXinDialog(getContext(), activity);
                loginWeiXinDialog.setType(type);
                loginWeiXinDialog.show();
                dismiss();
                break;
            case R.id.bt_password_login:
                LoginPasswordDialog login_password_dialog = new LoginPasswordDialog(getContext(), activity);
                login_password_dialog.setType(type);
                login_password_dialog.show();
                dismiss();
                break;
            default:
                break;
        }
    }

    /**
     * 条码 输入框 输入监听
     */
    private void initEtBarCode() {
        @SuppressLint("HandlerLeak")
        Handler etBarCodeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    String str = Objects.requireNonNull(et_password.getText()).toString().trim();
                    et_password.setText("");
                    et_password.requestFocus();

                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY))) {
                        cardno = DESUtil.encrypt(str);
                        cardlogin(cardno);
                    } else {
                        showToast(getContext().getString(R.string.not_login), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.not_login));
                    }

                }
            }
        };

        Runnable etBarCodeRunnable = () -> etBarCodeHandler.sendEmptyMessage(0);

        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etBarCodeHandler.removeCallbacks(etBarCodeRunnable);
                if (!TextUtils.isEmpty(et_password.getText()) && Objects.requireNonNull(et_password.getText()).length() > 0) {
                    /*800毫秒没有输入认为输入完毕*/
                    etBarCodeHandler.postDelayed(etBarCodeRunnable, 800);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStop() {
        if (null != start) {
            start.cancel();
            start = null;
        }
        super.onStop();
    }

    /**
     * 刷卡登录
     *
     * @param cardno
     */
    private void cardlogin(String cardno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("cardno", cardno);
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/cardlogin", jsonObject.toString(), this);
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
                    MApp.mSp.put(UserData.COMPANY, loginDataEntity.getCocode());
                    MApp.mSp.put(UserData.USER_NAME, loginDataEntity.getLogno());
                    MApp.mSp.put(UserData.IS_LOGIN, true);
                    showToast(getContext().getString(R.string.net_tip_type_1_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_success));

                    if (!TextUtils.isEmpty(cardno)) {
                        MApp.mSp.put(UserData.CARDNO, cardno);
                    } else {
                        MApp.mSp.put(UserData.CARDNO, "");
                    }

                    if (type == 0) {
                        if (loginDataEntity.getUserlev() <= 2) {
                            SystemSetDialog systemSetDialog = new SystemSetDialog(getContext(), activity);
                            systemSetDialog.show();
                        } else {
                            showToast(getContext().getResources().getString(R.string.not_an_administrator), 2);
                        }
                    } else {
                        if (MApp.mSp.getString(UserData.DEVKIND).equals(BaseActivity.TYPE_20)) {

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
