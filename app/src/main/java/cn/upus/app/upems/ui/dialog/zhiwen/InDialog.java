package cn.upus.app.upems.ui.dialog.zhiwen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.dao.User;
import cn.upus.app.upems.dao.util.UserUtil;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.serialport_usb485.ZhiWenUtil;
import cn.upus.app.upems.util.serialport_usb485.bean.Received;
import com.blankj.utilcode.util.LogUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 指纹柜 存
 */
public class InDialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_message)
    TextView tv_message;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private String lockno;
    private String shelfno;
    private String boardno;
    private String posno;//用于语音提示 格子编号

    private int mVISAWrite = -1;
    private MThread mThread;
    private int userType;
    private String mFingerId;
    private String mReadData;

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

    public InDialog(@NonNull Context context, @NonNull Activity activity, String lockno, String shelfno, String boardno, String posno) {
        super(context, activity);
        this.lockno = lockno;
        this.shelfno = shelfno;
        this.boardno = boardno;
        this.posno = posno;
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_zhiwen_in;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start.start();
        EventBus.getDefault().register(this);
        iv_close.setOnClickListener(v -> dismiss());
        userType = 2;
        mFingerId = UserUtil.getFingerId();
        LogUtils.e("指纹编号：" + mFingerId);

        BaseActivity.ttsSpeak("请长按指纹器");
        /*录入指纹 1*/
        postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));
    }

    private void postData(int type, byte[] mBuffer) {
        if (null != mThread) {
            mThread.setStop(true);
            mThread.interrupt();
            mThread = null;
        }
        mVISAWrite = type;
        mThread = new MThread(mBuffer);
        mThread.start();
    }

    @Override
    protected void onStop() {
        if (null != start) {
            start.cancel();
            start = null;
        }
        EventBus.getDefault().unregister(this);
        if (null != mThread) {
            mThread.setStop(true);
            mThread.interrupt();
            mThread = null;
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
        BaseActivity.ttsSpeak(bean.getMessage());
        if (bean.getType() == 1) {
            showToast(bean.getMessage(), 0);
            /*将信息提交*/
            storage();
        } else {
            showToast(bean.getMessage(), 2);
            /*根据指纹号删除指纹信息*/
            postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (mVISAWrite) {
                case TYPE_1://录入指纹 1
                    if (!TextUtils.isEmpty(mReadData) && mReadData.length() == 16 && mReadData.substring(0, 8).equals("f5010000")) {
                        switch (mReadData.substring(8, 10)) {
                            case "00"://操作成功
                                tv_message.setText("操作成功 请二次按压指纹器");
                                //BaseActivity.ttsSpeak("请二次按压指纹器");

                                /*录入指纹 2*/
                                postData(TYPE_2, ZhiWenUtil.w_6(mFingerId, userType));
                                break;
                            case "01"://操作失败
                                tv_message.setText("操作失败 请重新按压指纹器");
                                BaseActivity.ttsSpeak("操作失败 请重新按压指纹");

                                /*录入指纹 1*/
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));
                                break;
                            case "02"://指纹采集成功
                                break;
                            case "04"://指纹数据库已满
                                tv_message.setText(getContext().getResources().getString(R.string.the_fingerprint_database_is_full));
                                break;
                            case "05"://无此用户
                                break;
                            case "06"://用户已存在
                                tv_message.setText(getContext().getResources().getString(R.string.fingerprints_already_exist));
                                break;
                            case "07"://指纹已存在
                                BaseActivity.ttsSpeak("指纹已存在");
                                showToast("指纹已存在", 2);
                                dismiss();
                                /* BaseActivity.ttsSpeak("指纹已存在 请重新按压指纹器");
                                 *//*录入指纹 1*//*
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));*/
                                break;
                        }
                    }
                    break;
                case TYPE_2://第二次录入
                    if (!TextUtils.isEmpty(mReadData) && mReadData.length() == 16 && mReadData.substring(0, 8).equals("f5020000")) {
                        switch (mReadData.substring(8, 10)) {
                            case "00"://操作成功
                                tv_message.setText("操作成功 请三次按压指纹器");
                                //BaseActivity.ttsSpeak("请三次按压指纹器");
                                /*录入指纹 3*/
                                postData(TYPE_3, ZhiWenUtil.w_7(mFingerId, userType));
                                break;
                            case "01"://操作失败
                                tv_message.setText("操作失败 请重新按压指纹器");
                                BaseActivity.ttsSpeak("操作失败 请重新按压指纹器");

                                /*录入指纹 1*/
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));
                                break;
                            case "02"://指纹采集成功
                                break;
                            case "04"://指纹数据库已满
                                tv_message.setText(getContext().getResources().getString(R.string.the_fingerprint_database_is_full));
                                break;
                            case "05"://无此用户
                                break;
                            case "06"://用户已存在
                                tv_message.setText(getContext().getResources().getString(R.string.fingerprints_already_exist));
                                break;
                            case "07"://指纹已存在
                                BaseActivity.ttsSpeak("指纹已存在");
                                showToast("指纹已存在", 2);
                                dismiss();
                               /* tv_message.setText("指纹已存在 请重新按压指纹器");
                                BaseActivity.ttsSpeak("指纹已存在 请重新按压指纹器");
                                *//*录入指纹 1*//*
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));*/
                                break;
                        }
                    }
                    break;
                case TYPE_3://第三次录入
                    if (!TextUtils.isEmpty(mReadData) && mReadData.length() == 16 && mReadData.substring(0, 8).equals("f5030000")) {
                        switch (mReadData.substring(8, 10)) {
                            case "00"://操作成功
                                tv_message.setText("正在开锁");
                                //BaseActivity.ttsSpeak("正在开锁");
                                EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
                                break;
                            case "01"://操作失败
                                tv_message.setText("操作失败 请重新按压指纹器");
                                BaseActivity.ttsSpeak("操作失败 请重新按压指纹器");
                                /*录入指纹 1*/
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));
                                break;
                            case "02"://指纹采集成功
                                break;
                            case "04"://指纹数据库已满
                                tv_message.setText(getContext().getResources().getString(R.string.the_fingerprint_database_is_full));
                                break;
                            case "05"://无此用户
                                break;
                            case "06"://用户已存在
                                tv_message.setText(getContext().getResources().getString(R.string.fingerprints_already_exist));
                                break;
                            case "07"://指纹已存在
                                BaseActivity.ttsSpeak("指纹已存在");
                                showToast("指纹已存在", 2);
                                dismiss();
                                /*tv_message.setText("指纹已存在 请重新按压指纹器");
                                BaseActivity.ttsSpeak("指纹已存在 请重新按压指纹器");
                                *//*录入指纹 1*//*
                                postData(TYPE_1, ZhiWenUtil.w_5(mFingerId, userType));*/
                                break;
                        }
                    }
                    break;
                default:
                    BaseActivity.ttsSpeak("超时退出");
                    dismiss();
                    break;
            }
        }

    };

    class MThread extends Thread {

        private byte[] mBuffer;
        private boolean isStop = false;

        public void setStop(boolean stop) {
            this.isStop = stop;
        }

        MThread(byte[] mBuffer) {
            this.mBuffer = mBuffer;
        }

        @Override
        public void run() {
            super.run();
            Received mReceived = MApp.ZHI_WEN_COM.callBack(mBuffer);
            if (null != mReceived && !TextUtils.isEmpty(mReceived.getReadData())) {
                mReadData = mReceived.getReadData();
                LogUtils.e("readData : " + mReadData);
                mHandler.sendEmptyMessage(mVISAWrite);
            } else {
                mHandler.sendEmptyMessage(99);
            }
        }
    }

    /**
     * 指纹柜存件开锁成功后调用接口上传数据
     */
    private void storage() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("barno", mFingerId);
            jsonObject.put("shelfno", shelfno);
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressextra/finger/storage", jsonObject.toString(), this);
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
                case TYPE_0:
                    //{"msg":"操作成功!","data":"数据更新成功!","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        /*根据指纹号删除指纹信息*/
                        postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
                        showToast("数据提交异常", 2);
                        BaseActivity.ttsSpeak("提交失败");
                        dismiss();
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        /*根据指纹号删除指纹信息*/
                        postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
                        showToast("数据提交失败", 2);
                        BaseActivity.ttsSpeak("提交失败");
                        dismiss();
                        return;
                    }
                    /*将信息保存本地数据库*/
                    User user = new User();
                    user.setUser_id(mFingerId);
                    user.setFinger_id(mFingerId);
                    user.setFinger_lev("3");
                    UserUtil.insertData(user);
                    showToast("数据提交成功", 0);
                    BaseActivity.ttsSpeak("提交成功");
                    dismiss();
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            /*根据指纹号删除指纹信息*/
            postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
            showToast("数据提交异常", 2);
            BaseActivity.ttsSpeak("提交失败");
        }
    }

    @Override
    public void onError(String data) {
        super.onError(data);
        showToast(getContext().getString(R.string.net_error_1), 2);
        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
        /*根据指纹号删除指纹信息*/
        postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
        dismiss();
    }

}
