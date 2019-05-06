package cn.upus.app.upems.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.event_bus.ScanCodeRegistrationCallBackBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.http.HttpUtil;

/**
 * 快递员注册
 */
public class UserRegisterDialog extends BaseDialog {


    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.iv)
    ImageView iv;

    @BindView(R.id.bt_SMS_verification)
    Button bt_SMS_verification;
    @BindView(R.id.bt_register)
    Button bt_register;
    @BindView(R.id.et_cellphone)
    EditText et_cellphone;
    @BindView(R.id.et_vercode)
    EditText et_vercode;
    @BindView(R.id.et_logno)
    EditText et_logno;
    @BindView(R.id.et_userna)
    EditText et_userna;
    @BindView(R.id.et_password1)
    EditText et_password1;
    @BindView(R.id.et_password2)
    EditText et_password2;
    @BindView(R.id.et_mailbox)
    EditText et_mailbox;
    @BindView(R.id.et_cardno)
    EditText et_cardno;

    public UserRegisterDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_user_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        iv_close.setOnClickListener(v -> dismiss());
        if (null == MApp.systemInfoEntity) {
            return;
        }
        initQrCode();
        initRegister();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 扫码注册 成功回调
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getScanCodeRegistrationCallBackBean(ScanCodeRegistrationCallBackBean bean) {
        if (null == bean) {
            return;
        }
        if (bean.getType() == 1) {
            showToast(getContext().getResources().getString(R.string.net_tip_type_2_success), 0);
            dismiss();
        }
    }

    private void initRegister() {
        bt_SMS_verification.setOnClickListener(view13 -> {
            if (TextUtils.isEmpty(et_cellphone.getText())) {
                showToast(getContext().getResources().getString(R.string.tips_input_tel), 2);
                return;
            }
            getRegisterCode(et_cellphone.getText().toString().trim());
        });
        bt_register.setOnClickListener(view12 -> {
            if (TextUtils.isEmpty(et_cellphone.getText())) {
                showToast(getContext().getResources().getString(R.string.tips_input_tel), 2);
                et_cellphone.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(et_vercode.getText())) {
                showToast(getContext().getResources().getString(R.string.input_verification), 2);
                et_vercode.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(et_logno.getText())) {
                showToast(getContext().getResources().getString(R.string.tips_input_user), 2);
                et_logno.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(et_password1.getText())) {
                showToast(getContext().getResources().getString(R.string.tips_input_password), 2);
                et_password1.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(et_password2.getText())) {
                showToast(getContext().getResources().getString(R.string.Confirm_password), 2);
                et_password2.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(et_mailbox.getText())) {
                showToast(getContext().getResources().getString(R.string.Please_input_the_mailbox), 2);
                et_mailbox.requestFocus();
                return;
            }
            if (!et_password1.getText().toString().equals(et_password2.getText().toString())) {
                showToast(getContext().getResources().getString(R.string.Two_password_inconsistencies), 2);
                return;
            }
            expressRegisterPhAPP(et_cellphone.getText().toString(),
                    et_password1.getText().toString(),
                    et_password2.getText().toString(),
                    et_vercode.getText().toString(),
                    et_logno.getText().toString(),
                    et_userna.getText().toString(),
                    et_cardno.getText().toString(),
                    et_mailbox.getText().toString());

        });
    }

    private void initQrCode() {
        if (null == MApp.systemInfoEntity) {
            return;
        }
        List<HttpUtil.KeyValueBean> keyValueEntities = new ArrayList<>();
        keyValueEntities.add(new HttpUtil.KeyValueBean("cocode", MApp.mSp.getString(UserData.COMPANY)));
        keyValueEntities.add(new HttpUtil.KeyValueBean("custno", MApp.systemInfoEntity.getCustno()));
        keyValueEntities.add(new HttpUtil.KeyValueBean("devno", MApp.mSp.getString(UserData.DEVNO)));
        String url = "http://www.upus.cn/upus_client/new_client/sm.html?" + HttpUtil.getKeyValueString(keyValueEntities);
        Bitmap qrCode = QRUtils.getInstance().createQRCode(url, 300, 300);
        iv.setImageBitmap(qrCode);
    }

    /**
     * 手机号码
     *
     * @param cellphone
     */
    private void getRegisterCode(String cellphone) {
        List<HttpUtil.KeyValueBean> keyValueEntities = new ArrayList<>();
        keyValueEntities.add(new HttpUtil.KeyValueBean("cellphone", cellphone));
        keyValueEntities.add(new HttpUtil.KeyValueBean("cocode", MApp.mSp.getString(UserData.COMPANY)));
        WEB_TYPT = TYPE_0;
        mHttpUtil.get(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/Register/getRegisterCode?" + HttpUtil.getKeyValueString(keyValueEntities), this);
    }

    /**
     * 快递员注册
     *
     * @param cellphone
     * @param password
     * @param repass
     * @param vercode
     * @param logno
     * @param userna
     * @param cardno
     * @param mailaddr
     */
    private void expressRegisterPhAPP(String cellphone, String password, String repass, String vercode, String logno, String userna, String cardno, String mailaddr) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cocode", MApp.mSp.getString(UserData.COMPANY));
            jsonObject.put("cellphone", cellphone);
            jsonObject.put("password", password);
            jsonObject.put("repass", repass);
            jsonObject.put("vercode", vercode);
            jsonObject.put("logno", logno);
            jsonObject.put("userna", userna);
            if (null != MApp.systemInfoEntity) {
                jsonObject.put("custno", MApp.systemInfoEntity.getCustno());
            } else {
                jsonObject.put("custno", MApp.mSp.getString(UserData.CUSTNO));
            }
            jsonObject.put("cardno", cardno);
            jsonObject.put("mailaddr", mailaddr);
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            WEB_TYPT = TYPE_1;
            mHttpUtil.get(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/Register/expressRegisterPhAPP?data=" + jsonObject.toString(), this);
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
                    break;
                case TYPE_1:
                    //{"msg":"注册成功!","data":null,"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_2_error_1), 2);
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_2_error_1) + " : " + jsonObject.optString("msg"), 2);
                        return;
                    }
                    showToast(getContext().getResources().getString(R.string.net_tip_type_2_success), 0);
                    dismiss();
                    break;
                default:
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
