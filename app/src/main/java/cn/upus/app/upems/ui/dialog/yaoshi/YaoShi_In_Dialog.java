package cn.upus.app.upems.ui.dialog.yaoshi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.EditTextUtil;

/**
 * 钥匙柜 存
 */
public class YaoShi_In_Dialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.tv_posno)
    TextView tv_posno;

    private String lockno;
    private String shelfno;
    private String boardno;
    private String posno;//用于语音提示 格子编号

    private String cardno;//扫码到的卡号

    public YaoShi_In_Dialog(@NonNull Context context, @NonNull Activity activity, String lockno, String shelfno, String boardno, String posno) {
        super(context, activity);
        this.lockno = lockno;
        this.shelfno = shelfno;
        this.boardno = boardno;
        this.posno = posno;
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_yaoshi_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        iv_close.setOnClickListener(v -> dismiss());
        initKeyboard();
        EditTextUtil.setEditText(activity, et_password);
        initEtBarCode();

        //EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
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
     * 接收开锁的信息
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getOpenLockCallBack(OpenLockCallBackBean bean) {
        if (null == bean) {
            return;
        }
        BaseActivity.ttsSpeak(posno + " " + bean.getMessage());
        if (bean.getType() == 1) {
            Resources res = getContext().getResources();
            String text = String.format(res.getString(R.string.Already_opened_number), posno);
            tv_posno.setText(text);
            showToast(posno + " " + bean.getMessage(), 0);

            storage(cardno);
        } else {
            showToast(posno + " " + bean.getMessage(), 2);
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
                    cardno = Objects.requireNonNull(et_password.getText()).toString().trim();
                    et_password.setText("");
                    et_password.requestFocus();
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY))) {

                        EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
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
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 指纹柜存件开锁成功后调用接口上传数据
     */
    private void storage(String barno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));
            jsonObject.put("barno", barno);
            jsonObject.put("shelfno", shelfno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/finger/storage", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
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
            JSONArray mJsonArray;
            if (TextUtils.isEmpty(jsonObject.toString())) {
                showToast(getContext().getString(R.string.net_error_1), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
                return;
            }
            switch (WEB_TYPT) {
                case TYPE_0:
                    //{"msg":"操作成功!","data":"数据更新成功!","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_error_1));
                        et_password.setText("");
                        et_password.requestFocus();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"));
                        et_password.setText("");
                        et_password.requestFocus();
                        return;
                    }
                    showToast(getContext().getResources().getString(R.string.net_tip_type_4_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_success));
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
