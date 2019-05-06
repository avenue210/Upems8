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
 * 指纹柜 取
 */
public class OutDialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.tv_message)
    TextView tv_message;
    @BindView(R.id.tv_time)
    TextView tv_time;

    private int mVISAWrite = -1;
    private MThread mThread;
    private String mFingerId;
    private String mReadData = "";

    private String expno;//单号 服务器返回
    private String shelfno;
    private String boardno;
    private String lockno;
    private String posno;//用于语音提示 格子编号

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

    public OutDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_zhiwen_out;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start.start();
        EventBus.getDefault().register(this);
        iv_close.setOnClickListener(v -> dismiss());

        BaseActivity.ttsSpeak("请长按指纹器");
        /*验证指纹*/
        postData(TYPE_1, ZhiWenUtil.w_12());
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
            update();
        } else {
            showToast(bean.getMessage(), 2);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (mVISAWrite) {
                case TYPE_1://验证用户是否存在
                    if (!TextUtils.isEmpty(mReadData) && mReadData.length() == 16 && mReadData.substring(0, 4).equals("f50c")) {
                        mFingerId = mReadData.substring(4, 8);
                        if (UserUtil.queryData(mFingerId)) {
                            tv_message.setText("验证成功 正在获取存件信息...");
                            //BaseActivity.ttsSpeak("正在获取存件信息");
                            take();
                        } else {
                            BaseActivity.ttsSpeak("验证失败");
                            showToast("验证失败", 2);
                            dismiss();
                            /*tv_message.setText("验证失败 请重新按压指纹器");
                            BaseActivity.ttsSpeak("验证失败");
                            *//*验证指纹*//*
                            postData(TYPE_1, ZhiWenUtil.w_12());*/
                        }
                    }
                    break;
                case TYPE_8://将指定用户移除
                    if (!TextUtils.isEmpty(mReadData) && mReadData.substring(0, 4).equals("f504") && mReadData.substring(8, 10).equals("00")) {
                        LogUtils.e("删除指纹ID成功");
                        /*将该用户移除*/
                        UserUtil.deleteById(mFingerId);
                        dismiss();
                    } else {
                        LogUtils.e("删除指纹ID失败");
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
     * 指纹柜取件获取货格号
     */
    private void take() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("barno", mFingerId);
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
                        showToast("获取信息失败", 2);
                        BaseActivity.ttsSpeak("获取信息失败");
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast("获取信息失败", 2);
                        BaseActivity.ttsSpeak("获取信息失败");
                        return;
                    }
                    jsonObject = jsonObject.optJSONObject("data");
                    expno = jsonObject.optString("expno");
                    lockno = jsonObject.optString("lockno");
                    boardno = jsonObject.optString("boardno");
                    shelfno = jsonObject.optString("shelfno");
                    posno = jsonObject.optString("posno");
                    if (TextUtils.isEmpty(lockno) || TextUtils.isEmpty(boardno) || TextUtils.isEmpty(expno) || TextUtils.isEmpty(posno)) {
                        showToast("获取信息失败", 2);
                        BaseActivity.ttsSpeak("获取信息失败");
                        return;
                    }
                    tv_message.setText("获取成功 [ " + posno + " ] 正在开锁 请稍后...");
                    showToast("正在开锁", 0);
                    //BaseActivity.ttsSpeak("正在开锁");
                    EventBus.getDefault().post(new OpenLockDataBean(0, 0, Integer.valueOf(boardno), Integer.valueOf(lockno), null));
                    break;
                case TYPE_1:
                    //{"msg":"操作成功!","data":"数据更新成功!","success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    /*根据指纹号删除指纹信息*/
                    postData(TYPE_8, ZhiWenUtil.w_8(mFingerId));
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
        dismiss();
    }

}
