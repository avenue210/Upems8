package cn.upus.app.upems.ui.dialog.take;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.ExpnoImageBean;
import cn.upus.app.upems.bean.StorageEntity;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.bean.event_bus.PaymentCallBackBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.SocketService;
import cn.upus.app.upems.ui.view.CustomerKeyboard;
import cn.upus.app.upems.ui.view.PasswordEditText;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.EditTextUtil;
import cn.upus.app.upems.util.MD5Util;

/**
 * 验证码 + 二维码 取件
 */
public class VerificationCodeSweepTakeDialog extends BaseDialog {

    private static final String TAG = VerificationCodeSweepTakeDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.tv_time)
    TextView tv_time;
    @BindView(R.id.custom_key_board)
    CustomerKeyboard mCustomerKeyboard;
    @BindView(R.id.password_edit_text)
    PasswordEditText mPasswordEt;
    @BindView(R.id.et_password)
    EditText et_password;

    @BindView(R.id.iv)
    ImageView iv;

    private String boardno;//板号
    private String lockno;//锁号
    private String shelfno;
    private String expno;
    private String price;

    /*微信/支付宝支付*/
    private int weixin_zhifubao = -1;

    private CountDownTimer start = new CountDownTimer(5 * 60 * 1000, 1000) {
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

    public VerificationCodeSweepTakeDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_verification_code_sweep_take;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        start.start();
        iv_close.setOnClickListener(v -> dismiss());
        initPasswordEditText();
        EditTextUtil.setEditText(activity, et_password);
        et_password.setInputType(InputType.TYPE_NULL);
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    String str = et_password.getText().toString().trim();
                    Log.e("回车", "(" + str + ") " + str.length());
                    pickupbill(str);
                }
            }
        });

        String str = String.valueOf(System.currentTimeMillis());
        String mUrl = "http://www.upus.cn/upus_client/new_client/sc.html?v=" + str + "&fixno=" + MApp.mSp.getString(UserData.DEVNO) + "&compno=" + MApp.mSp.getString(UserData.COMPNO) + "&devkind=" + MApp.mSp.getString(UserData.DEVKIND);
        Bitmap qrCode = QRUtils.getInstance().createQRCode(mUrl);
        iv.setImageBitmap(qrCode);
        initKeyboard();
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
        if (bean.getType() == 1) {
            BaseActivity.ttsSpeak(bean.getMessage() + getContext().getResources().getString(R.string.take_ok_tip));
            showToast(getContext().getResources().getString(R.string.take_ok_tip), 0);
            updboxstate(shelfno, expno);
        } else {
            BaseActivity.ttsSpeak(bean.getMessage());
            showToast(bean.getMessage(), 2);
        }
    }

    /**
     * 接收付款信息回调
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getPaymentCallBack(PaymentCallBackBean bean) {
        if (null == bean) {
            return;
        }
        if (bean.getType() == 1) {
            /*发送开锁信息*/
            EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
        }
    }

    private void initPasswordEditText() {
        mPasswordEt.setEnabled(false);
        mPasswordEt.setFocusable(false);
        mPasswordEt.setKeyListener(null);//重点
        mCustomerKeyboard.setOnCustomerKeyboardClickListener(new CustomerKeyboard.CustomerKeyboardClickListener() {
            @Override
            public void click(String number) {
                mPasswordEt.addPassword(number);
            }

            @Override
            public void delete() {
                mPasswordEt.deleteLastPassword();
            }
        });
        mPasswordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) {
                    String str = Objects.requireNonNull(mPasswordEt.getText()).toString().trim();
                    pickupbill(str);
                }
            }
        });
    }

    /**
     * 支付选择弹窗
     */
    private AlertDialog paymentDialog;

    /**
     * 支付选择弹窗
     *
     * @param money
     * @param expno
     */
    @SuppressLint("SetTextI18n")
    private void initPaymentDialog(double money, String expno) {

        if (null == MApp.systemInfoEntity) {
            showToast(getContext().getResources().getString(R.string.No_payment_address), 2);
            BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.No_payment_address));
            return;
        } else {
            if (TextUtils.isEmpty(MApp.systemInfoEntity.getTentoken()) && TextUtils.isEmpty(MApp.systemInfoEntity.getAlitoken()) && TextUtils.isEmpty(MApp.systemInfoEntity.getSwitchtoken())) {
                showToast(getContext().getResources().getString(R.string.No_payment_address), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.No_payment_address));
                return;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_edit);
        builder.setTitle(context.getResources().getString(R.string.Please_choose_payment_method));
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_payment, null);
        builder.setView(view);

        TextView tv_money = view.findViewById(R.id.tv_money);
        ImageView iv_weixin = view.findViewById(R.id.iv_weixin);
        ImageView iv_zhifubao = view.findViewById(R.id.iv_zhifubao);
        ImageView iv_niriliya = view.findViewById(R.id.iv_niriliya);
        ImageView iv_shuaka = view.findViewById(R.id.iv_shuaka);
        iv_shuaka.setVisibility(View.VISIBLE);

        tv_money.setText("￥ : " + money);
        if (TextUtils.isEmpty(MApp.systemInfoEntity.getTentoken())) {
            iv_weixin.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(MApp.systemInfoEntity.getAlitoken())) {
            iv_zhifubao.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(MApp.systemInfoEntity.getSwitchtoken())) {
            iv_niriliya.setVisibility(View.GONE);
        }

        iv_weixin.setOnClickListener(v -> {
            weixin_zhifubao = 0;
            payment(money, expno, weixin_zhifubao);
        });
        iv_zhifubao.setOnClickListener(v -> {
            weixin_zhifubao = 1;
            payment(money, expno, weixin_zhifubao);
        });
        iv_niriliya.setOnClickListener(v -> {
            weixin_zhifubao = 2;
            payment(money, expno, weixin_zhifubao);
        });
        /*刷卡支付*/
        iv_shuaka.setOnClickListener(v -> initCardnoDialog(money, expno));

        builder.setNegativeButton(context.getResources().getString(R.string.ok_cancel), (dialog, which) -> dialog.dismiss());

        paymentDialog = builder.create();
        paymentDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(paymentDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
    }

    /**
     * 刷卡支付弹窗
     */
    private AlertDialog cardDialog;

    private void initCardnoDialog(double money, String expno) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_edit);
        builder.setTitle(context.getResources().getString(R.string.Payment_by_credit_card));
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_payment_card, null);
        builder.setView(view);

        EditText et_card = view.findViewById(R.id.et_card);
        EditTextUtil.setEditText(activity, et_card);

        cardDialog = builder.create();
        cardDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(cardDialog.getWindow()).setGravity(Gravity.CENTER);
        cardDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);

        //软键盘不自动弹出
        Window window = cardDialog.getWindow();
        if (null != window) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        @SuppressLint("HandlerLeak") Handler etBarCodeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.COMPNO))) {
                        String str = et_card.getText().toString().trim();
                        et_card.setText("");
                        et_card.requestFocus();
                        pay(str, money, expno);
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
     * 支付地址弹窗
     */
    private AlertDialog paymentUrlDialog;

    /**
     * 二维码扫描栏
     *
     * @param paymentUrl
     */
    private void initQrCodeDialog(String paymentUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_edit);
        builder.setTitle(context.getResources().getString(R.string.Please_sweep_code_payment));
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_payment_qr_code, null);
        builder.setView(view);

        ImageView iv_qr_code = view.findViewById(R.id.iv_qr_code);
        Bitmap qrCode;
        if (weixin_zhifubao == 0) {
            qrCode = QRUtils.getInstance().createQRCodeAddLogo(paymentUrl, 250, 250, BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(), R.mipmap.ic_weixinzhifu_200));
            iv_qr_code.setImageBitmap(qrCode);
        } else if (weixin_zhifubao == 1) {
            qrCode = QRUtils.getInstance().createQRCodeAddLogo(paymentUrl, 250, 250, BitmapFactory.decodeResource(Objects.requireNonNull(getContext()).getResources(), R.mipmap.ic_zhifubaozhifu_200));
            iv_qr_code.setImageBitmap(qrCode);
        }

        paymentUrlDialog = builder.create();
        paymentUrlDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(paymentUrlDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentUrlDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
    }

    /**
     * 支付地址弹窗
     */
    private AlertDialog paymentCodeDialog;

    /**
     * 尼日利亚 付款码
     *
     * @param b
     * @param paymentCode
     */
    @SuppressLint("SetTextI18n")
    private void initPaymentCodeDialog(boolean b, String paymentCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_edit);
        builder.setTitle("Payment code");
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_payment_code, null);
        builder.setView(view);

        TextView tv = view.findViewById(R.id.tv);
        if (b) {
            tv.setText("Payment code : " + paymentCode);
        } else {
            tv.setText("Fail : " + paymentCode);
        }

        paymentCodeDialog = builder.create();
        paymentCodeDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(paymentCodeDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentCodeDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
    }

    /**
     * 取件
     */
    private void pickupbill(String takecode) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("takecode", takecode);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/pickupbill", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 验证码开锁后回传
     *
     * @param shelfno
     * @param expno
     */
    private void updboxstate(String shelfno, String expno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("shelfno", shelfno);
            jsonObject.put("expno", expno);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/updboxstate", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付
     *
     * @param m,
     * @param expno
     * @param weixin_zhifubao
     */
    private void payment(double m, String expno, int weixin_zhifubao) {
        try {
            JSONObject jsonObject = new JSONObject();
            String date = DateTimeUtil.getCurDateStr("yyyyMMddHHmmss");
            Random rand = new Random();
            int numbel = rand.nextInt(10000);
            String merordno = "T" + date + numbel;

            double paymny = m * 100;

            String mMoney = String.valueOf(paymny);
            // mMoney = "1";
            mMoney = mMoney.substring(0, mMoney.indexOf("."));
            LogUtils.e(TAG, "金额:" + paymny + "  money:" + mMoney);
            jsonObject.put("merordno", merordno);
            jsonObject.put("paymny", mMoney);
            jsonObject.put("orderinf", getContext().getResources().getString(R.string.app_name));
            String token = "";
            if (weixin_zhifubao == 0) {//"UPUS-99F47CBDB732C30510291CF4AA350AC83FC89915369"
                token = MApp.systemInfoEntity.getTentoken();
            } else if (weixin_zhifubao == 1) {
                token = MApp.systemInfoEntity.getAlitoken();
            } else if (weixin_zhifubao == 2) {
                token = MApp.systemInfoEntity.getSwitchtoken();
            }
            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotno())) {
                jsonObject.put("sign", MD5Util.MD5Encode(merordno + mMoney + token, "UTF-8"));
            } else {
                jsonObject.put("sign", MD5Util.MD5Encode(merordno + mMoney + MApp.systemInfoEntity.getDepotno() + token, "UTF-8"));
            }
            jsonObject.put("ip", "");
            jsonObject.put("payfor", "0");//存 0 取 1
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotno())) {
                jsonObject.put("depotno", "");
            } else {
                jsonObject.put("depotno", MApp.systemInfoEntity.getDepotno());
            }
            JSONArray mJsonArray = new JSONArray();

            JSONObject object = new JSONObject();
            object.put("expno", expno);
            object.put("price", m);
            mJsonArray.put(object);

            jsonObject.put("dtl", mJsonArray);
            if (weixin_zhifubao == 0) {
                //微信
                WEB_TYPT = TYPE_2;
                mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/tenpay/expresspay", jsonObject.toString(), this);
            } else if (weixin_zhifubao == 1) {
                //支付宝
                WEB_TYPT = TYPE_2;
                mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/alipay/expresspay", jsonObject.toString(), this);
            } else if (weixin_zhifubao == 2) {
                //尼日利亚支付
                WEB_TYPT = TYPE_3;
                mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/switchpay/paycode", jsonObject.toString(), this);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷卡支付
     *
     * @param cardno
     * @param money
     * @param expno
     */
    private void pay(String cardno, double money, String expno) {
        try {
            JSONObject jsonObject = new JSONObject();
            String date = DateTimeUtil.getCurDateStr("yyyyMMddHHmmss");
            Random rand = new Random();
            int numbel = rand.nextInt(10000);
            String merordno = "T" + date + numbel;

            double paymny = money * 100;

            String mMoney = String.valueOf(paymny);
            // mMoney = "1";
            mMoney = mMoney.substring(0, mMoney.indexOf("."));
            LogUtils.e(TAG, "金额:" + paymny + "  money:" + money);
            jsonObject.put("merordno", merordno);
            jsonObject.put("paymny", mMoney);
            jsonObject.put("orderinf", getContext().getResources().getString(R.string.app_name));

            String token = "sdfertgbncfer.7811";

            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotno())) {
                jsonObject.put("sign", MD5Util.MD5Encode(merordno + mMoney + token, "UTF-8"));
            } else {
                jsonObject.put("sign", MD5Util.MD5Encode(merordno + mMoney + MApp.systemInfoEntity.getDepotno() + token, "UTF-8"));
            }
            jsonObject.put("ip", "");
            jsonObject.put("cardno", cardno);//余额支付 ：logno 刷卡支付 : cardno
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotno())) {
                jsonObject.put("depotno", "");
            } else {
                jsonObject.put("depotno", MApp.systemInfoEntity.getDepotno());
            }
            JSONArray mJsonArray = new JSONArray();

            JSONObject object = new JSONObject();
            object.put("expno", expno);
            object.put("price", money);
            object.put("shelfno", shelfno);
            mJsonArray.put(object);

            jsonObject.put("dtl", mJsonArray);
            //余额支付
            WEB_TYPT = TYPE_4;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/card/balance/pay", jsonObject.toString(), this);
            //mHttpUtil.postJson("http://192.168.1.201:8091/" + "upus_APP/app/card/balance/pay", jsonObject.toString(), this);
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
                case TYPE_0://取件码取件
                    //{"msg":"验证码不正确!","data":null,"success":"0"}
                    //{"msg":"操作成功!","data":{"expno":"66045642564737","shelfno":"A06","boardno":"1","lockno":"6","posno":"A06","price":"1"},"success":"1"}
                    //toastys.success("网络请求TYPE:（ " + webType + " ）取件码取件返回信息信息: " + body.toString());
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        dismiss();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_2) + jsonObject.optString("msg"));
                        dismiss();
                        return;
                    }
                    //{"expno":"02224763774535","shelfno":"00107B206","boardno":"2","lockno":"16","posno":"B206","price":"0.0"}
                    JSONObject mJsonObject = jsonObject.optJSONObject("data");
                    if (TextUtils.isEmpty(mJsonObject.optString("boardno")) ||
                            TextUtils.isEmpty(mJsonObject.optString("lockno")) ||
                            TextUtils.isEmpty(mJsonObject.optString("shelfno")) ||
                            TextUtils.isEmpty(mJsonObject.optString("expno"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_2) + "  服务器返回参数有空值："
                                + "boardno: " + (TextUtils.isEmpty(mJsonObject.optString("boardno")))
                                + "lockno: " + (TextUtils.isEmpty(mJsonObject.optString("lockno")))
                                + "shelfno: " + (TextUtils.isEmpty(mJsonObject.optString("shelfno")))
                                + "expno: " + (TextUtils.isEmpty(mJsonObject.optString("expno"))), 2);
                        dismiss();
                        return;
                    }
                    boardno = mJsonObject.optString("boardno");
                    lockno = mJsonObject.optString("lockno");
                    shelfno = mJsonObject.optString("shelfno");
                    expno = mJsonObject.optString("expno");

                    if (TextUtils.isEmpty(boardno) || TextUtils.isEmpty(lockno) || TextUtils.isEmpty(shelfno) || TextUtils.isEmpty(expno)) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_2), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_2));
                        dismiss();
                        return;
                    }

                    showToast(getContext().getResources().getString(R.string.net_tip_type_5_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_success));

                    LogUtils.e(TAG, "是否拍照上传附件：" + MApp.mSp.getString(UserData.TAKESHOT));
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.TAKESHOT)) && MApp.mSp.getString(UserData.TAKESHOT).equals("1") && !TextUtils.isEmpty(expno)) {
                        List<String> expnos = new ArrayList<>();
                        expnos.add(expno);
                        EventBus.getDefault().post(new ExpnoImageBean("1", expnos));
                    }

                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && MApp.mSp.getString(UserData.DEVKIND).equals("20")) {
                        LogUtils.e(TAG, "判断到是取餐柜 跳过付款开锁取件");
                    } else {
                        price = mJsonObject.optString("price");
                        if (!TextUtils.isEmpty(price) && !TextUtils.isEmpty(expno)) {
                            try {
                                Double money = Double.valueOf(price);
                                if (money > 0) {
                                    initPaymentDialog(money, expno);
                                    LogUtils.e(TAG, "必须付款后再开锁取件");
                                    return;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    /*发送开锁信息*/
                    EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));

                    break;
                case TYPE_1://验证码取件后 回传是否成功
                    if (null != paymentUrlDialog) {
                        paymentUrlDialog.dismiss();
                    }
                    if (null != paymentCodeDialog) {
                        paymentCodeDialog.dismiss();
                    }
                    if (null != paymentDialog) {
                        paymentDialog.dismiss();
                    }
                    dismiss();
                    break;
                case TYPE_2://微信/支付宝 付款地址请求
                    //{"msg" : "weixin://wxpay/bizpayurl?pr=uHurLHa","success" : 1}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    String msg = jsonObject.optString("msg");
                    if (TextUtils.isEmpty(msg)) {
                        //没有支付地址
                        showToast(getContext().getResources().getString(R.string.generate_order_failure), 2);
                        return;
                    }
                    if (!SocketService.socketConnect) {
                        //SOCKET 未连接
                        showToast(getContext().getResources().getString(R.string.network_service_exceptions), 2);
                        return;
                    }
                    initQrCodeDialog(msg);
                    if (null != paymentDialog) {
                        paymentDialog.dismiss();
                    }
                    break;
                case TYPE_3://尼日利亚
                    //{"msg":"Opt Success!","data":"91772544","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        initPaymentCodeDialog(false, jsonObject.optString("Data error"));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        initPaymentCodeDialog(false, jsonObject.optString("msg"));
                        return;
                    }
                    if (TextUtils.isEmpty(jsonObject.optString("data"))) {
                        initPaymentCodeDialog(false, jsonObject.optString("The payment code is empty."));
                        return;
                    }
                    initPaymentCodeDialog(true, jsonObject.optString("data"));
                    if (null != paymentDialog) {
                        paymentDialog.dismiss();
                    }
                    break;
                case TYPE_4://刷卡支付
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.Payment_failed) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.Payment_failed) + jsonObject.optString("msg"));
                        return;
                    }
                    showToast(getContext().getResources().getString(R.string.payment_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.payment_success));

                    if (null != cardDialog) {
                        cardDialog.dismiss();
                    }
                    /*发送开锁信息*/
                    EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
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
