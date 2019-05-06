package cn.upus.app.upems.ui.dialog.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.LoginDataEntity;
import cn.upus.app.upems.bean.event_bus.ScanCodeLoginCallBackBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.dialog.SystemSetDialog;
import cn.upus.app.upems.ui.dialog.storage.BoxListDialog;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 微信登录
 */
public class LoginWeiXinDialog extends BaseDialog {

    private static final String TAG = LoginWeiXinDialog.class.getSimpleName();


    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.iv)
    ImageView iv;

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

    public LoginWeiXinDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_login_sweep_code;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        start.start();
        iv_close.setOnClickListener(v -> dismiss());
        getdate();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        if (null != start) {
            start.cancel();
            start = null;
        }
        super.onStop();
    }

    /**
     * 接收开锁的信息
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getScanCodeLoginCallBack(ScanCodeLoginCallBackBean bean) {
        if (null == bean) {
            return;
        }
        if (TextUtils.isEmpty(bean.getJson())) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(bean.getJson());
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

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取服务器时间
     */
    private void getdate() {
        WEB_TYPT = TYPE_0;
        mHttpUtil.get(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/getdate", this);
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
                    String time = jsonObject.optString("time");
                    if (TextUtils.isEmpty(time)) {
                        return;
                    }
                    String mUrl = "http://www.upus.cn/upus_client/new_client/r.html?compno=" + MApp.mSp.getString(UserData.COMPNO) + "&devno=" + MApp.mSp.getString(UserData.DEVNO) + "&time=" + DESUtil.encrypt(time);
                    Bitmap qrCode = QRUtils.getInstance().createQRCode(mUrl);
                    iv.setImageBitmap(qrCode);
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
