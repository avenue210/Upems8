package cn.upus.app.upems.ui.dialog.wangban;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
 * 网板柜 取
 */
public class WangBan_Out_Dialog extends BaseDialog {


    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.bt_post)
    Button bt_post;
    @BindView(R.id.tv_posno)
    TextView tv_posno;

    private String lockno;
    private String shelfno;
    private String boardno;
    private String posno;//用于语音提示 格子编号

    private String expno;//返回的单号

    public WangBan_Out_Dialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_wangban_out;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initKeyboard();
        EditTextUtil.setEditText(activity, et_password);
        initEtBarCode();

        iv_close.setOnClickListener(v -> dismiss());
        bt_post.setOnClickListener(v -> {
            if (TextUtils.isEmpty(expno)) {
                return;
            }
            EventBus.getDefault().post(new OpenLockDataBean(0, 3, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
            update();
        });
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
        BaseActivity.ttsSpeak(posno + "  " + bean.getMessage());
        if (bean.getType() == 1) {
            Resources res = getContext().getResources();
            String text = String.format(res.getString(R.string.have_access_to_number), posno);
            tv_posno.setText(text);
            showToast(posno + "  " + bean.getMessage(), 0);
        } else {
            showToast(posno + "  " + bean.getMessage(), 2);
            et_password.setText("");
            et_password.requestFocus();
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
                    et_password.requestFocus();
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPANY))) {
                        take(str);
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
     * 指纹柜取件获取货格号
     */
    private void take(String barno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("barno", barno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/finger/take", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指纹柜取件开锁成功后回调上传数据
     */
    private void update() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));
            jsonObject.put("expno", expno);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/finger/update", jsonObject.toString(), this);
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
            switch (WEB_TYPT) {
                case TYPE_0:
                    //{"msg":"查询成功!","data":{"expno":"23138740237138","lockno":"1","shelfno":"TEST01A203","boardno":"1"},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_not_data), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_not_data));
                        et_password.setText("");
                        et_password.requestFocus();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_not_data), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_not_data));
                        et_password.setText("");
                        et_password.requestFocus();
                        return;
                    }
                    jsonObject = jsonObject.optJSONObject("data");
                    expno = jsonObject.optString("expno");
                    lockno = jsonObject.optString("lockno");
                    boardno = jsonObject.optString("boardno");
                    shelfno = jsonObject.optString("shelfno");
                    posno = jsonObject.optString("posno");
                    if (TextUtils.isEmpty(lockno) || TextUtils.isEmpty(boardno) || TextUtils.isEmpty(expno) || TextUtils.isEmpty(posno)) {
                        showToast(getContext().getResources().getString(R.string.net_not_data), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_not_data));
                        return;
                    }
                    showToast(getContext().getResources().getString(R.string.unlocked), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.unlocked));
                    EventBus.getDefault().post(new OpenLockDataBean(0, 2, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
                    break;
                case TYPE_1:
                    //{"msg":"操作成功!","data":"数据更新成功!","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    dismiss();
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("获取信息异常", 2);
            BaseActivity.ttsSpeak("获取信息异常");
        }
    }

    @Override
    public void onError(String data) {
        super.onError(data);
        showToast(getContext().getString(R.string.net_error_1), 2);
        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
    }
}
