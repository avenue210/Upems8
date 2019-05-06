package cn.upus.app.upems.ui.dialog.storage;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
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
 * 洗衣柜
 */
public class StorageLaundryCabinetDialog extends BaseDialog {

    private static final String TAG = StorageLaundryCabinetDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.et_number)
    EditText et_number;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private String origno;
    private String tel;
    private String cvtype;

    private String shelfno;
    private String boardno;
    private String lockno;

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

    public StorageLaundryCabinetDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_storage_laundry_cabinet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start.start();
        initKeyboard();
        EditTextUtil.setEditText(activity, et_number);
        initEtBarCode();
        iv_close.setOnClickListener(v -> dismiss());
    }

    private void initKeyboard() {
        //软键盘不自动弹出
        Window window = getWindow();
        if (null != window) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
        LogUtils.e(TAG, bean.toString());
        BaseActivity.ttsSpeak(bean.getMessage());
        et_number.setText("");
        et_number.requestFocus();
        if (bean.getType() == 1) {
            showToast(bean.getMessage(), 0);
            storagebill(shelfno, origno, tel, cvtype);
        } else {
            showToast(bean.getMessage(), 2);
        }
    }

    private void initEtBarCode() {
        @SuppressLint("HandlerLeak")
        Handler etBarCodeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPNO))) {
                        String[] str = et_number.getText().toString().trim().split("_");
                        LogUtils.e(TAG, "扫描拆分后：" + str.length + "  " + Arrays.toString(str).trim());
                        origno = null;
                        tel = null;
                        cvtype = null;
                        if (str.length == 3) {
                            origno = str[0].trim();
                            tel = str[1].trim();
                            cvtype = str[2].trim();
                            scan(origno, cvtype);
                        }

                        et_number.setText("");
                        et_number.requestFocus();
                    } else {
                        showToast(getContext().getString(R.string.not_login), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.not_login));
                    }

                }
            }
        };

        Runnable etBarCodeRunnable = () -> etBarCodeHandler.sendEmptyMessage(0);

        et_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etBarCodeHandler.removeCallbacks(etBarCodeRunnable);
                if (!TextUtils.isEmpty(et_number.getText()) && Objects.requireNonNull(et_number.getText()).length() > 0) {
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
     * 洗衣柜扫描二维码提交接口
     *
     * @param origno 单号
     * @param cvtype 状态 未洗 已洗
     */
    private void scan(String origno, String cvtype) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("origno", origno);
            jsonObject.put("cvtype", cvtype);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/washer/scan", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交已经完成的数据
     *
     * @param shelfno 仓位编号
     * @param origno  原单号
     * @param tel     电话
     */
    private void storagebill(String shelfno, String origno, String tel, String cvtype) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));

            JSONArray jsonArray = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("shelfno", shelfno);
            object.put("billno", origno);
            object.put("tel", tel);
            jsonObject.put("cvtype", cvtype);
            object.put("expno", "");
            jsonArray.put(object);
            jsonObject.put("data", jsonArray);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/storagebill", jsonObject.toString(), this);
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
                    //{"msg":"获取信息成功!","data":{"shelfno":"006025A11","boardno":"1","lockno":"1"},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        et_number.setText("");
                        et_number.requestFocus();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(jsonObject.optString("msg"), 2);
                        et_number.setText("");
                        et_number.requestFocus();
                        return;
                    }
                    JSONObject object = jsonObject.optJSONObject("data");
                    shelfno = object.optString("shelfno");
                    boardno = object.optString("boardno");
                    lockno = object.optString("lockno");
                    if (TextUtils.isEmpty(shelfno) || TextUtils.isEmpty(boardno) || TextUtils.isEmpty(lockno)) {
                        showToast(getContext().getResources().getString(R.string.net_not_data), 2);
                        et_number.setText("");
                        et_number.requestFocus();
                        return;
                    }
                    /*发送开锁信息*/
                    EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
                    break;
                case TYPE_1:
                    //{"msg":"操作成功!","success":1}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1) + jsonObject.optString("msg"));
                        return;
                    }
                    showToast(getContext().getResources().getString(R.string.net_tip_type_4_success), 0);
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
