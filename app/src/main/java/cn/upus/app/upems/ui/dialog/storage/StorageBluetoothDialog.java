package cn.upus.app.upems.ui.dialog.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.adapter.Storage_Bluetooth_Adapter;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.StorageEntity;
import cn.upus.app.upems.bean.BoxEntity;
import cn.upus.app.upems.bean.ExpnoImageBean;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.EditTextUtil;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.serialport_usb485.StringUtil;

/**
 * 存件 蓝牙扫入
 */
public class StorageBluetoothDialog extends BaseDialog {

    private static final String TAG = StorageBluetoothDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.bt_open_lock_all)
    Button bt_open_lock_all;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.bt_post)
    Button bt_post;
    @BindView(R.id.et_scanning)
    EditText et_scanning;

    private boolean scanShelfno = true;
    private String shelfno;

    private List<BoxEntity> boxEntities;
    private List<StorageEntity> storageEntities = new ArrayList<>();
    private Storage_Bluetooth_Adapter mStorageBluetoothAdapter;

    /*生成的单据名称列表*/
    private ArrayList<String> expnos = new ArrayList<>();

    private int openLockType = -1;

    public void setBoxEntities(List<BoxEntity> boxEntities) {
        this.boxEntities = boxEntities;
    }

    private BoxListDialog boxListDialog;

    public void setBoxListDialog(BoxListDialog boxListDialog) {
        this.boxListDialog = boxListDialog;
    }

    public StorageBluetoothDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_storage_bluetooth;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        EditTextUtil.setEditText(activity, et_scanning);
        initAdapter();
        initEtScanning();
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
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 初始化扫描信息
     */
    private void initEtScanning() {
        @SuppressLint("HandlerLeak")
        Handler etBarCodeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 0) {
                    String barCode = et_scanning.getText().toString().trim();
                    et_scanning.setText("");
                    et_scanning.requestFocus();
                    initEtData(barCode);
                }
            }
        };

        Runnable etBarCodeRunnable = () -> etBarCodeHandler.sendEmptyMessage(0);

        et_scanning.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etBarCodeHandler.removeCallbacks(etBarCodeRunnable);
                if (!TextUtils.isEmpty(et_scanning.getText()) && Objects.requireNonNull(et_scanning.getText()).length() > 0) {
                    /*800毫秒没有输入认为输入完毕*/
                    etBarCodeHandler.postDelayed(etBarCodeRunnable, 800);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initEtData(String barcode) {
        for (int i = 0; i < storageEntities.size(); i++) {
            if (barcode.equals(storageEntities.get(i).getShelfno())) {
                shelfno = barcode;
                scanShelfno = true;
                showToast(getContext().getResources().getString(R.string.scanning_cabinet_number) + shelfno, 0);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_cabinet_number) + shelfno);
                mStorageBluetoothAdapter.setShelfno(shelfno);
                mStorageBluetoothAdapter.notifyDataSetChanged();
                break;
            }
            scanShelfno = false;
        }
        if (TextUtils.isEmpty(shelfno)) {
            showToast(getContext().getResources().getString(R.string.scanning_tips_a), 2);
            BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_tips_a));
            return;
        }
        if (!scanShelfno) {
            String tel;
            String barno;
            try {
                if (!scanShelfno) {
                    String[] str = barcode.split("_");
                    if (!scanShelfno && str.length > 0) {
                        barno = barcode.split("_")[0];
                        tel = barcode.split("_")[1];
                        LogUtils.e(TAG, "tel: " + tel + " barno:" + barno);
                    } else {
                        showToast(getContext().getResources().getString(R.string.scanning_exception_tip), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_exception_tip));
                        return;
                    }
                } else {
                    showToast(getContext().getResources().getString(R.string.scanning_exception_tip), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_exception_tip));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(getContext().getResources().getString(R.string.scanning_exception), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_exception));
                return;
            }
            if (TextUtils.isEmpty(tel) || TextUtils.isEmpty(barno)) {
                showToast(getContext().getResources().getString(R.string.scanning_error), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.scanning_error));
                return;
            }
            if (!scanShelfno && !TextUtils.isEmpty(shelfno)) {
                for (int i = 0; i < storageEntities.size(); i++) {
                    if (shelfno.equals(storageEntities.get(i).getShelfno())) {
                        storageEntities.get(i).setTel(tel);
                        storageEntities.get(i).setBarno(barno);
                        mStorageBluetoothAdapter.notifyDataSetChanged();
                    }
                }
                shelfno = null;
            }
        }
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
                boolean isNull = true;
                for (int i = 0; i < storageEntities.size(); i++) {
                    if (!TextUtils.isEmpty(storageEntities.get(i).getBarno()) && !TextUtils.isEmpty(storageEntities.get(i).getTel())) {
                        isNull = false;
                        break;
                    }
                }
                if (isNull) {
                    return;
                }
                for (int i = 0; i < storageEntities.size() - 1; i++) {
                    String temp = storageEntities.get(i).getBarno();
                    for (int j = i + 1; j < storageEntities.size(); j++) {
                        if (!TextUtils.isEmpty(temp) && !TextUtils.isEmpty(storageEntities.get(j).getBarno()) && temp.equals(storageEntities.get(j).getBarno())) {
                            showToast(storageEntities.get(i).getShelfno() + " / " + storageEntities.get(j).getShelfno() + " " + getContext().getResources().getString(R.string.tips_b), 2);
                            BaseActivity.ttsSpeak(storageEntities.get(i).getShelfno() + " / " + storageEntities.get(j).getShelfno() + " " + getContext().getResources().getString(R.string.tips_b));
                            return;
                        }
                    }
                }
                if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.CARDNO)) && MApp.mSp.getString(UserData.DEVKIND).equals(BaseActivity.TYPE_22)) {
                    //储物柜存件提交
                    storagegoods(MApp.mSp.getString(UserData.CARDNO));
                } else {
                    //快递柜存件提交
                    storagebill(storageEntities);
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
            case 2://开多个锁
                if (bean.getType() != 1) {
                    showToast(bean.getMessage(), 2);
                }
                break;
            default:
                break;
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
                entity.setPrice(boxEntities.get(i).getPrice());
                entity.setExpno(StringUtil.getNumberCode());
                storageEntities.add(entity);
            }
        }
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mStorageBluetoothAdapter = new Storage_Bluetooth_Adapter(R.layout.item_storage_bluetooth, storageEntities);
        rv.setAdapter(mStorageBluetoothAdapter);

        mStorageBluetoothAdapter.getOpenLock(item -> {
            /*单个开锁*/
            openLockType = 0;
            //开锁
            EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(item.getBoardno()), Integer.valueOf(item.getLockno()), null));
        });
        mStorageBluetoothAdapter.getDelete(item -> {
            if (null != item) {
                storageEntities.remove(item);
                mStorageBluetoothAdapter.notifyDataSetChanged();
            }
        });
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
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/storagebill", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 储物柜存件提交
     *
     * @param cardno 卡号
     */
    private void storagegoods(String cardno) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("logno", MApp.mSp.getString(UserData.USER_NAME));
            jsonObject.put("cardno", cardno);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < storageEntities.size(); i++) {
                JSONObject object = new JSONObject();
                object.put("shelfno", storageEntities.get(i).getShelfno());
                jsonArray.put(object);
            }
            jsonObject.put("data", jsonArray);
            WEB_TYPT = TYPE_1;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/storagegoods", jsonObject.toString(), this);
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
                case TYPE_0://快递柜提交
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
                case TYPE_1://储物柜提交
                    //{"msg":"操作成功!","success":1}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_5_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_5_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_error_2) + jsonObject.optString("msg"));
                        return;
                    }

                    showToast(getContext().getResources().getString(R.string.net_tip_type_4_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_4_success));
                    dismiss();
                    if (null != boxListDialog) {
                        boxListDialog.dismiss();
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
