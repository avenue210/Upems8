package cn.upus.app.upems.ui.dialog.take;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.PartParameterEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.EditTextUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 刷卡取件
 */
public class PayByCardTakeDialog extends BaseDialog {

    private static final String TAG = PayByCardTakeDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_password)
    EditText et_password;

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

    public PayByCardTakeDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_pay_by_card_take;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start.start();

        EditTextUtil.setEditText(activity, et_password);
        initEtBarCode();
        initKeyboard();
    }

    private void initKeyboard(){
        //软键盘不自动弹出
        Window window = getWindow();
        if (null != window){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }

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
                        showToast(getContext().getResources().getString(R.string.tips_a), 0);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.tips_a));
                        pickupgoods(DESUtil.encrypt(str));
                    } else {
                        showToast(getContext().getResources().getString(R.string.not_login), 2);
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

    /**
     * 刷卡取件
     *
     * @param cardno
     */
    private void pickupgoods(String cardno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("cardno", cardno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/pickupgoods", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    //{"msg": "操作成功!","data": [{ "expno": "34871513697447", "lockno": "1","shelfno": "A01", "boardno": "1"}],"success": "1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        et_password.requestFocus();
                        et_password.setText("");
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getString(R.string.net_tip_type_5_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1) + jsonObject.optString("msg"));
                        et_password.requestFocus();
                        et_password.setText("");
                        return;
                    }
                    JSONArray mJsonArray = jsonObject.optJSONArray("data");
                    if (mJsonArray.length() == 0) {
                        showToast(getContext().getString(R.string.net_tip_type_5_error_3), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_3));
                        et_password.requestFocus();
                        et_password.setText("");
                        return;
                    }
                    List<PartParameterEntity> partParameterEntities = new ArrayList<>();
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject object = (JSONObject) mJsonArray.opt(i);
                        PartParameterEntity partParameterEntity = (PartParameterEntity) GsonUtil.stringToObject(object.toString(), PartParameterEntity.class);
                        partParameterEntities.add(partParameterEntity);
                    }
                    PayByCardTakeListDialog payByCardTakeListDialog = new PayByCardTakeListDialog(getContext(), activity);
                    payByCardTakeListDialog.setPartParameterEntities(partParameterEntities);
                    payByCardTakeListDialog.show();
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
