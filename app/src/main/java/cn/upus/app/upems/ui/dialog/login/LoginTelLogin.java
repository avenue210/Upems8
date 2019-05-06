package cn.upus.app.upems.ui.dialog.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.LoginDataEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.dialog.storage.BoxListDialog;
import cn.upus.app.upems.ui.dialog.SystemSetDialog;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 验证码登录
 */
public class LoginTelLogin extends BaseDialog {

    private static final String TAG = LoginTelLogin.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.et1)
    EditText et1;
    @BindView(R.id.et2)
    EditText et2;
    @BindView(R.id.bt1)
    Button bt1;
    @BindView(R.id.bt2)
    Button bt2;

    private CountDownTimer countDownTimer;

    private int type = -1;

    public void setType(int type) {
        this.type = type;
    }

    public LoginTelLogin(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_login_tel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iv_close.setOnClickListener(v -> dismiss());
        bt1.setOnClickListener(v -> {
            if (TextUtils.isEmpty(et1.getText())) {
                showToast(getContext().getResources().getString(R.string.input_tel), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.input_tel));
                return;
            }
            bt1.setClickable(false);
            startTimer();
            getvalcode(et1.getText().toString().trim());
        });
        bt2.setOnClickListener(v -> {
            if (TextUtils.isEmpty(et1.getText())) {
                showToast(getContext().getResources().getString(R.string.input_tel), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.input_tel));
                return;
            }
            if (TextUtils.isEmpty(et2.getText())) {
                showToast(getContext().getResources().getString(R.string.input_verification), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.input_verification));
                return;
            }
            valcodelogin(et1.getText().toString().trim(), et2.getText().toString());
        });
    }

    @Override
    protected void onStop() {
        stopTimer();
        super.onStop();
    }

    /**
     * 开启倒计时
     */
    private void startTimer() {
        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(60 * 1000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    bt1.setText(millisUntilFinished / 1000 + " s");
                }

                public void onFinish() {
                    bt1.setText(getContext().getResources().getString(R.string.ok_sure));
                    bt1.setClickable(true);
                    stopTimer();
                }
            };
        }
        countDownTimer.start();
    }

    /**
     * 结束倒计时
     */
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * 获取短信验证码
     *
     * @param telno
     */
    private void getvalcode(String telno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("telno", telno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/getvalcode", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证码登陆
     *
     * @param telno
     * @param valcode
     */
    private void valcodelogin(String telno, String valcode) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("telno", telno);
            jsonObject.put("valcode", valcode);
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/valcodelogin", jsonObject.toString(), this);
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
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(jsonObject.optString("msg"), 2);
                        return;
                    }
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

                    LoginDataEntity loginDataEntity = (LoginDataEntity) GsonUtil.stringToObject(jsonObject.optJSONObject("data").toString(), LoginDataEntity.class);
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
