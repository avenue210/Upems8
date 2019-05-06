package cn.upus.app.upems.ui.dialog.yaoshi;

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
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.LoginDataEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.EditTextUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 钥匙柜 员工刷卡
 */
public class YaoShi_LoginDialog extends BaseDialog {

    private static final String TAG = YaoShi_LoginDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_password)
    EditText et_password;

    private LoginDataEntity loginDataEntity;
    private String cardno;

    private int type;// 0 存  1 取
    private String lockno;
    private String shelfno;
    private String boardno;
    private String posno;//用于语音提示

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

    public YaoShi_LoginDialog(@NonNull Context context, @NonNull Activity activity, int type) {
        super(context, activity);
        this.type = type;
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_yaoshi_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start.start();
        iv_close.setOnClickListener(v -> dismiss());
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
                        cardno = DESUtil.encrypt(str);//3286608621
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

    /**
     * 获取一个随机货格
     */
    private void shelfno() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/finger/shelfno", jsonObject.toString(), this);
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
                        /*存 查询可用货格*/
                        shelfno();
                    } else if (type == 1) {
                        /*取 登录跳转*/
                        new YaoShi_Out_Dialog(getContext(), activity).show();
                        dismiss();
                    }

                    break;
                case TYPE_1:
                    //{"msg":"查询成功!","data":{"lockno":"7","shelfno":"TEST01A202","boardno":"1"},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast("获取货格失败", 2);
                        BaseActivity.ttsSpeak("获取货格失败");
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast("获取货格失败", 2);
                        BaseActivity.ttsSpeak("获取货格失败");
                        return;
                    }
                    jsonObject = jsonObject.optJSONObject("data");
                    if (TextUtils.isEmpty(jsonObject.toString())) {
                        showToast("获取货格失败", 2);
                        BaseActivity.ttsSpeak("获取货格失败");
                        return;
                    }
                    lockno = jsonObject.optString("lockno");
                    shelfno = jsonObject.optString("shelfno");
                    boardno = jsonObject.optString("boardno");
                    posno = jsonObject.optString("posno");//用于语音提示
                    if (TextUtils.isEmpty(lockno) || TextUtils.isEmpty(shelfno) || TextUtils.isEmpty(boardno) || TextUtils.isEmpty(posno)) {
                        showToast("获取货格失败", 2);
                        BaseActivity.ttsSpeak("获取货格失败");
                        return;
                    }
                    new YaoShi_In_Dialog(getContext(), activity, lockno, shelfno, boardno, posno).show();
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
