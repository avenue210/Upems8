package cn.upus.app.upems.ui.dialog.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import java.util.List;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.adapter.Storage_Input_Adapter;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.BoxEntity;
import cn.upus.app.upems.bean.ExpnoImageBean;
import cn.upus.app.upems.bean.StorageEntity;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.bean.event_bus.PaymentCallBackBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.SocketService;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.MD5Util;
import cn.upus.app.upems.util.serialport_usb485.StringUtil;

/**
 * 存件 手动输入
 */
public class StorageInputDialog extends BaseDialog {

    private static final String TAG = StorageInputDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.bt_open_lock_all)
    Button bt_open_lock_all;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.bt_post)
    Button bt_post;

    private List<BoxEntity> boxEntities;
    private List<StorageEntity> storageEntities = new ArrayList<>();
    private Storage_Input_Adapter mStorageInputAdapter;
    /*需要提交的数据*/
    private List<StorageEntity> storageEntityPostDatas = new ArrayList<>();
    private StorageEntity storageEntityPostData;
    private int postDataType = -1;

    /*生成的单据名称列表*/
    private ArrayList<String> expnos = new ArrayList<>();
    /*发送 指令的 位置*/
    private int openLockType = -1;
    /*微信/支付宝支付*/
    private int weixin_zhifubao = -1;

    public void setBoxEntities(List<BoxEntity> boxEntities) {
        this.boxEntities = boxEntities;
    }

    private BoxListDialog boxListDialog;

    public void setBoxListDialog(BoxListDialog boxListDialog) {
        this.boxListDialog = boxListDialog;
    }

    public StorageInputDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_storage_input;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initAdapter();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @OnClick({R.id.iv_close, R.id.bt_open_lock_all, R.id.bt_post})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_open_lock_all:
                if (storageEntities.size() == 0) {
                    return;
                }
                List<OpenLockDataBean.Locks> locks = new ArrayList<>();
                for (StorageEntity entity : storageEntities) {
                    OpenLockDataBean.Locks lock = new OpenLockDataBean.Locks();
                    lock.setBoardIndex(Integer.parseInt(entity.getBoardno()));
                    lock.setLockIndex(Integer.parseInt(entity.getLockno()));
                    locks.add(lock);
                }
                if (locks.size() == 0) {
                    return;
                }
                /*多个开锁*/
                openLockType = 2;
                EventBus.getDefault().post(new OpenLockDataBean(1, 0, 1, 1, locks));
                break;
            case R.id.bt_post:
                for (int i = 0; i < storageEntities.size(); i++) {
                    if (TextUtils.isEmpty(storageEntities.get(i).getBarno()) || TextUtils.isEmpty(storageEntities.get(i).getTel())) {
                        showToast(storageEntities.get(i).getShelfno() + " , " + getContext().getResources().getString(R.string.tips_input_barno_tel), 2);
                        BaseActivity.ttsSpeak(storageEntities.get(i).getShelfno() + " , " + getContext().getResources().getString(R.string.tips_input_barno_tel));
                        return;
                    }
                }
                double money = 0;
                storageEntityPostDatas.clear();
                for (StorageEntity entity : storageEntities) {
                    if (!TextUtils.isEmpty(entity.getBarno()) && !TextUtils.isEmpty(entity.getTel())) {
                        if (!TextUtils.isEmpty(entity.getPrice()) && Double.valueOf(entity.getPrice()) > 0) {
                            money += Double.valueOf(entity.getPrice());
                        }
                        storageEntityPostDatas.add(entity);
                    }
                }
                if (storageEntityPostDatas.size() > 0) {
                    if (money > 0) {
                        postDataType = 0;
                        initPaymentDialog(storageEntityPostDatas, null, money);
                    } else {
                        storagebill(storageEntityPostDatas);
                    }
                }
                break;
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
        LogUtils.e(TAG, bean.toString());
        BaseActivity.ttsSpeak(bean.getMessage());
        switch (openLockType) {
            case 0://单个开锁
                if (bean.getType() == 1) {
                    showToast(bean.getMessage(), 0);
                } else {
                    showToast(bean.getMessage(), 2);
                }
                break;
            case 1://单个开锁 并提交
                if (bean.getType() == 1) {
                    showToast(bean.getMessage(), 0);
                    if (null == storageEntityPostData) {
                        return;
                    }
                    if (!TextUtils.isEmpty(storageEntityPostData.getPrice()) || Double.parseDouble(storageEntityPostData.getPrice()) > 0) {
                        postDataType = 1;
                        //需要支付
                        initPaymentDialog(null, storageEntityPostData, Double.parseDouble(storageEntityPostData.getPrice()));
                    } else {
                        //不需要支付
                        storagebill(storageEntityPostData);
                    }
                } else {
                    showToast(bean.getMessage(), 2);
                }
                break;
            case 2://开多个锁
                if (bean.getType() != 1) {
                    showToast(bean.getMessage(), 2);
                }
                break;
            default:
                break;
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
            if (postDataType == 0) {
                storagebill(storageEntityPostDatas);
            } else if (postDataType == 1) {
                storagebill(storageEntityPostData);
            }
            if (null != paymentUrlDialog) {
                paymentUrlDialog.dismiss();
            }
            if (null != paymentCodeDialog) {
                paymentCodeDialog.dismiss();
            }
            if (null != paymentDialog) {
                paymentDialog.dismiss();
            }
        }
    }

    private void initAdapter() {
        if (null != boxEntities && boxEntities.size() > 0) {
            for (int i = 0; i < boxEntities.size(); i++) {
                StorageEntity entity = new StorageEntity();
                entity.setShelfno(boxEntities.get(i).getShelfno());
                entity.setBoardno(boxEntities.get(i).getBoardno());
                entity.setLockno(boxEntities.get(i).getLockno());
                entity.setPosno(boxEntities.get(i).getPosno());
                entity.setKindna(boxEntities.get(i).getKindna());
                entity.setKindno(boxEntities.get(i).getKindno());
                entity.setPrice(boxEntities.get(i).getPrice());
                entity.setExpno(StringUtil.getNumberCode());
                storageEntities.add(entity);
            }
        }
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mStorageInputAdapter = new Storage_Input_Adapter(R.layout.item_storage_input, storageEntities);
        rv.setAdapter(mStorageInputAdapter);
        mStorageInputAdapter.getDeposit(this::inputDepositData);
        mStorageInputAdapter.getOpenLock(item -> {
            /*单个开锁*/
            openLockType = 0;
            //开锁
            EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(item.getBoardno()), Integer.valueOf(item.getLockno()), null));

        });
        mStorageInputAdapter.getDelete(item -> {
            if (null != item) {
                storageEntities.remove(item);
                mStorageInputAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * 弹窗输入 收件人的电话号
     */
    private EditText et_tel;

    /**
     * 录入信息
     *
     * @param item
     */
    private void inputDepositData(StorageEntity item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_edit);
        builder.setTitle(context.getResources().getString(R.string.tips_input_barno_tel));
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_barno_tel_input, null);
        builder.setView(v);

        EditText et_barno = v.findViewById(R.id.et_barno);
        et_barno.setText(item.getBarno());
        et_tel = v.findViewById(R.id.et_tel);
        et_tel.setText(item.getTel());

        et_tel.setOnFocusChangeListener((v1, hasFocus) -> {
            if (hasFocus) {
                if (TextUtils.isEmpty(et_barno.getText())) {
                    return;
                }
                telinfo(et_barno.getText().toString().trim());
            }
        });

        /*@SuppressLint("HandlerLeak")
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    et_tel.requestFocus();
                }
            }
        };
        Runnable runnable = () -> handler.sendEmptyMessage(0);
        et_barno.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(runnable);
                //延迟800ms，如果不再输入字符，则执行该线程的run方法
                handler.postDelayed(runnable, 800);
            }
        });*/

        builder.setNeutralButton(context.getResources().getString(R.string.dialog_item_submission), (dialog, which) -> {
            if (!TextUtils.isEmpty(et_barno.getText()) && !TextUtils.isEmpty(et_tel.getText())) {
                item.setBarno(et_barno.getText().toString());
                item.setTel(et_tel.getText().toString());
                et_barno.setText("");
                et_tel.setText("");
                //当前单个开锁的数据类
                storageEntityPostData = item;
                /*单个开锁 并提交*/
                openLockType = 1;
                //发送开锁信息 并提交
                EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(item.getBoardno()), Integer.valueOf(item.getLockno()), null));
                //EventBus.getDefault().post(new OpenLockDataBean(0, 0, 1, 1, null));
                dialog.dismiss();
                mStorageInputAdapter.notifyDataSetChanged();
            }
        });

        builder.setPositiveButton(context.getResources().getString(R.string.ok_sure), (dialog, which) -> {
            if (!TextUtils.isEmpty(et_barno.getText()) && !TextUtils.isEmpty(et_tel.getText())) {
                item.setBarno(et_barno.getText().toString());
                item.setTel(et_tel.getText().toString());
                et_barno.setText("");
                et_tel.setText("");
                //发送开锁信号
                EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(item.getBoardno()), Integer.valueOf(item.getLockno()), null));
                //EventBus.getDefault().post(new OpenLockDataBean(0, 0, 1, 1, null));
                dialog.dismiss();
                mStorageInputAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.ok_cancel), (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        Objects.requireNonNull(alertDialog.getWindow()).setGravity(Gravity.CENTER);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
    }

    /**
     * 支付选择弹窗
     */
    private AlertDialog paymentDialog;

    /**
     * 支付选择弹窗
     *
     * @param entitys
     * @param entity
     */
    @SuppressLint("SetTextI18n")
    private void initPaymentDialog(List<StorageEntity> entitys, StorageEntity entity, double money) {

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
        ImageView iv_yu_e = view.findViewById(R.id.iv_yu_e);
        iv_yu_e.setVisibility(View.VISIBLE);

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
            payment(entitys, entity, 0, money);
        });
        iv_zhifubao.setOnClickListener(v -> {
            weixin_zhifubao = 1;
            payment(entitys, entity, 0, money);
        });
        iv_niriliya.setOnClickListener(v -> {
            weixin_zhifubao = 2;
            payment(entitys, entity, 0, money);
        });

        iv_yu_e.setOnClickListener(v -> pay(entitys, entity, money));

        builder.setNegativeButton(context.getResources().getString(R.string.ok_cancel), (dialog, which) -> dialog.dismiss());

        paymentDialog = builder.create();
        Objects.requireNonNull(paymentDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentDialog.setCanceledOnTouchOutside(false);
        paymentDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
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
        Objects.requireNonNull(paymentUrlDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentUrlDialog.setCanceledOnTouchOutside(false);
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
        Objects.requireNonNull(paymentCodeDialog.getWindow()).setGravity(Gravity.CENTER);
        paymentCodeDialog.setCanceledOnTouchOutside(false);
        paymentCodeDialog.show();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
    }

    /**
     * 根据单号 查询手机号
     */
    private void telinfo(String billno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("billno", billno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/telinfo", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交已经完成的数据 多个
     */
    private void storagebill(List<StorageEntity> entities) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));
            JSONArray jsonArray = new JSONArray();
            expnos.clear();
            for (int i = 0; i < entities.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("shelfno", entities.get(i).getShelfno());
                object.put("billno", entities.get(i).getBarno());
                object.put("tel", entities.get(i).getTel());
                object.put("expno", entities.get(i).getExpno());
                object.put("kindno", entities.get(i).getKindno());

                jsonArray.put(object);
                LogUtils.e(TAG, "是否拍照上传附件：" + MApp.mSp.getString(UserData.DEPSHOT));
                if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEPSHOT)) && MApp.mSp.getString(UserData.DEPSHOT).equals("1")) {
                    expnos.add(entities.get(i).getExpno());
                }
            }
            if (jsonArray.length() == 0) {
                return;
            }
            jsonObject.put("data", jsonArray);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/storagebill", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提交已经完成的数据 单个
     */
    private void storagebill(StorageEntity entity) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));
            JSONArray jsonArray = new JSONArray();
            expnos.clear();
            JSONObject object = new JSONObject();
            if (!TextUtils.isEmpty(entity.getBarno()) && !TextUtils.isEmpty(entity.getTel())) {
                object.put("shelfno", entity.getShelfno());
                object.put("billno", entity.getBarno());
                object.put("tel", entity.getTel());
                object.put("expno", entity.getExpno());
                object.put("kindno", entity.getKindno());
                jsonArray.put(object);
                LogUtils.e(TAG, "是否拍照上传附件：" + MApp.mSp.getString(UserData.DEPSHOT));
                if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEPSHOT)) && MApp.mSp.getString(UserData.DEPSHOT).equals("1")) {
                    expnos.add(entity.getExpno());
                }
            }
            if (jsonArray.length() == 0) {
                return;
            }
            jsonObject.put("data", jsonArray);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/storagebill", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 支付
     *
     * @param entities
     * @param entity
     * @param weixin_zhifubao
     */
    private void payment(List<StorageEntity> entities, StorageEntity entity, int weixin_zhifubao, double money) {
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
            if (null != entities) {
                for (int i = 0; i < entities.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("expno", entities.get(i).getExpno());
                    object.put("price", entities.get(i).getPrice());
                    mJsonArray.put(object);
                }
            } else if (null != entity) {
                JSONObject object = new JSONObject();
                object.put("expno", entity.getExpno());
                object.put("price", entity.getPrice());
                mJsonArray.put(object);
            }
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
     * 余额支付
     *
     * @param entities
     * @param entity
     * @param money
     */
    private void pay(List<StorageEntity> entities, StorageEntity entity, double money) {
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
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));//余额支付 ：logno 刷卡支付 : cardno
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            if (TextUtils.isEmpty(MApp.systemInfoEntity.getDepotno())) {
                jsonObject.put("depotno", "");
            } else {
                jsonObject.put("depotno", MApp.systemInfoEntity.getDepotno());
            }
            JSONArray mJsonArray = new JSONArray();
            if (null != entities) {
                for (int i = 0; i < entities.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("expno", entities.get(i).getExpno());
                    object.put("price", entities.get(i).getPrice());
                    object.put("shelfno", entities.get(i).getShelfno());
                    mJsonArray.put(object);
                }
            } else if (null != entity) {
                JSONObject object = new JSONObject();
                object.put("expno", entity.getExpno());
                object.put("price", entity.getPrice());
                object.put("shelfno", entity.getShelfno());
                mJsonArray.put(object);
            }
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
            switch (WEB_TYPT) {
                case TYPE_0://根据单号获取手机号
                    if (TextUtils.isEmpty(jsonObject.optString("success")) || !jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    if (TextUtils.isEmpty(jsonObject.optString("data"))) {
                        return;
                    }
                    if (null != et_tel) {
                        et_tel.setText(jsonObject.optString("data"));
                    }
                    break;
                case TYPE_1://单据提交
                    //{"msg":"操作成功!","data":{"shelfno":"A04","boardno":"1","lockno":"4"},"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"));
                        return;
                    }

                    showToast(getContext().getResources().getString(R.string.net_tip_type_4_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_success));

                    if (expnos.size() > 0) {
                        EventBus.getDefault().post(new ExpnoImageBean("0", expnos));
                    }

                    dismiss();
                    if (null != boxListDialog) {
                        boxListDialog.dismiss();
                    }
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
                case TYPE_4://余额支付
                    //{"msg":"更新余额失败!","data":null,"success":"0"}
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
                    if (postDataType == 0) {
                        storagebill(storageEntityPostDatas);
                    } else if (postDataType == 1) {
                        storagebill(storageEntityPostData);
                    }
                    if (null != paymentUrlDialog) {
                        paymentUrlDialog.dismiss();
                    }
                    if (null != paymentCodeDialog) {
                        paymentCodeDialog.dismiss();
                    }
                    if (null != paymentDialog) {
                        paymentDialog.dismiss();
                    }

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
