package cn.upus.app.upems.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import cn.bertsir.zbar.utils.QRUtils;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DESUtil;

/**
 * 寄存
 */
public class DepositDialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;

    @BindView(R.id.iv1)
    ImageView iv1;
    @BindView(R.id.iv2)
    ImageView iv2;

    public DepositDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_deposit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iv_close.setOnClickListener(v -> dismiss());
        getdate();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                    String fixno = MApp.mSp.getString(UserData.DEVNO);
                    String compno = MApp.mSp.getString(UserData.COMPNO);
                    String devkind = "23"; //23 柜子设备类型
                    String inUrl = "http://www.upus.cn/Locker/a.html?fixno=" + fixno + "&compno=" + compno + "&devkind=" + devkind + "&optkind=" + "0" + "&m=5&t=" + DESUtil.encrypt(time);
                    Bitmap qrCode1 = QRUtils.getInstance().createQRCode(inUrl);
                    iv1.setImageBitmap(qrCode1);

                    String outUrl = "http://www.upus.cn/Locker/a.html?fixno=" + fixno + "&compno=" + compno + "&devkind=" + devkind + "&optkind=" + "1" + "&m=5&t=" + DESUtil.encrypt(time);
                    Bitmap qrCode2 = QRUtils.getInstance().createQRCode(outUrl);
                    iv2.setImageBitmap(qrCode2);
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
