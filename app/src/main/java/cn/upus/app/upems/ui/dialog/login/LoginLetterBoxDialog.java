package cn.upus.app.upems.ui.dialog.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.EditTextUtil;

/**
 * 信报箱登录
 */
public class LoginLetterBoxDialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_user)
    TextInputEditText et_user;
    @BindView(R.id.et_password)
    TextInputEditText et_password;
    @BindView(R.id.bt_login)
    Button bt_login;
    @BindView(R.id.bt_card_login)
    Button bt_card_login;
    @BindView(R.id.bt_register)
    Button bt_register;
    @BindView(R.id.et_card)
    EditText et_card;
    @BindView(R.id.iv_crad)
    ImageView iv_crad;

    @BindView(R.id.ll1)
    LinearLayout ll1;
    @BindView(R.id.ll2)
    LinearLayout ll2;
    @BindView(R.id.ll3)
    LinearLayout ll3;

    private String userName;
    private String passWord;
    private String shelfno;

    private CountDownTimer start = new CountDownTimer(5 * 60 * 1000, 1000) {
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

    public LoginLetterBoxDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_letter_box;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        start.start();
        EditTextUtil.setEditText(activity, et_card);
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

    @Override
    protected void onStop() {
        if (null != start) {
            start.cancel();
            start = null;
        }
        EventBus.getDefault().unregister(this);
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

    @OnClick({R.id.iv_close, R.id.bt_login, R.id.bt_card_login, R.id.bt_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_login:
                ll1.setVisibility(View.VISIBLE);
                ll2.setVisibility(View.GONE);
                ll3.setVisibility(View.GONE);
                if (!NetworkUtils.isConnected()) {
                    showToast(getContext().getString(R.string.net_error), 2);
                    return;
                }
                userName = et_user.getText().toString().trim();
                passWord = et_password.getText().toString().trim();
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
                devlogin(userName, passWord);
                break;
            case R.id.bt_card_login:
                ll1.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
                ll3.setVisibility(View.GONE);
                et_card.requestFocus();
                break;
            case R.id.bt_register:
                ll1.setVisibility(View.GONE);
                ll2.setVisibility(View.GONE);
                ll3.setVisibility(View.VISIBLE);
                if (null == MApp.systemInfoEntity || TextUtils.isEmpty(MApp.systemInfoEntity.getCustno())) {
                    return;
                }
                String mUrl = "http://www.upus.cn/upus_client/new_client/scanMail.html?cocode="
                        + MApp.mSp.getString(UserData.COMPANY)
                        + "&custno=" + MApp.systemInfoEntity.getCustno()
                        + "&devno=" + MApp.mSp.getString(UserData.DEVNO) + "&action=2";
                Bitmap qrCode = QRUtils.getInstance().createQRCode(mUrl);
                iv_crad.setImageBitmap(qrCode);
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
                    String str = Objects.requireNonNull(et_card.getText()).toString().trim();
                    et_card.setText("");
                    et_card.requestFocus();
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY))) {
                        String cardno = DESUtil.encrypt(str);
                        cardlogin(cardno);
                    } else {
                        showToast(getContext().getString(R.string.not_login), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.not_login));
                    }

                }
            }
        };

        Runnable etBarCodeRunnable = () -> etBarCodeHandler.sendEmptyMessage(0);

        et_card.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etBarCodeHandler.removeCallbacks(etBarCodeRunnable);
                if (!TextUtils.isEmpty(et_card.getText()) && Objects.requireNonNull(et_card.getText()).length() > 0) {
                    /*800毫秒没有输入认为输入完毕*/
                    etBarCodeHandler.postDelayed(etBarCodeRunnable, 800);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 接收开锁的信息
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getOpenLockCallBack(OpenLockCallBackBean bean) {
        if (null == bean) {
            return;
        }
        LogUtils.e(bean.toString());
        BaseActivity.ttsSpeak(bean.getMessage());
        if (bean.getType() == 1) {
            showToast(bean.getMessage(), 0);
            take();
        } else {
            showToast(bean.getMessage(), 2);
            //storage(shelfno);
            dismiss();
        }
    }

    /**
     * 用户登录
     *
     * @param userName
     * @param passWord
     */
    private void devlogin(String userName, String passWord) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compcode", MApp.mSp.getString(UserData.COMPANY));
            jsonObject.put("devno", "");
            jsonObject.put("devid", MApp.mSp.getString(UserData.DEVID));
            jsonObject.put("logno", userName);
            jsonObject.put("pwd", passWord);
            jsonObject.put("type", "1");
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/devlogin", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    /**
     * 获取箱格锁号
     */
    private void login() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("logno", userName);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/mailbox/login", jsonObject.toString(), this);
            //mHttpUtil.postJson("http://192.168.1.201:8091/" + "upus_APP/app/expressextra/mailbox/login", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开锁成功后回写
     */
    private void take() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("logno", userName);
            jsonObject.put("shelfno", shelfno);
            WEB_TYPT = TYPE_2;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/mailbox/take", jsonObject.toString(), this);
            //mHttpUtil.postJson("http://192.168.1.201:8091/" + "upus_APP/app/expressextra/mailbox/take", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 光栅感应存件
     *
     * @param shelfno
     */
    private void storage(String shelfno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("shelfno", shelfno);
            WEB_TYPT = TYPE_3;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/mailbox/storage", jsonObject.toString(), this);
            //mHttpUtil.postJson("http://192.168.1.201:8091/" + "upus_APP/app/expressextra/mailbox/storage", jsonObject.toString(), this);
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
                    showToast(getContext().getString(R.string.net_tip_type_1_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_success));
                    login();
                    break;
                case TYPE_1:
                    //{"msg":"登陆成功!","data":{"reskind":"0","resstate":"1","lockno":"1","shelfno":"A01","boardno":"1","statena":"已审核"},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getString(R.string.net_tip_type_1_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_1_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(jsonObject.optString("msg"));
                        return;
                    }
                    if (TextUtils.isEmpty(jsonObject.optJSONObject("data").toString())) {
                        showToast(jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(jsonObject.optString("msg"));
                        return;
                    }
                    jsonObject = jsonObject.optJSONObject("data");
                    shelfno = jsonObject.optString("shelfno");
                    String boardno = jsonObject.optString("boardno");
                    String lockno = jsonObject.optString("lockno");
                    /*发送开锁信息*/
                    EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
                    break;
                case TYPE_2://开锁后回写
                    //{"msg":"操作成功!","data":"数据更新成功!","success":"1"}
                    dismiss();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String data) {
        super.onError(data);
        showToast(getContext().getString(R.string.net_error_1), 2);
        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
    }
}
