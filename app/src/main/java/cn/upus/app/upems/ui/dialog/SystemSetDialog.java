package cn.upus.app.upems.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.base.BaseLanguageActivity;
import cn.upus.app.upems.bean.CameraSettingsBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.ui.OpenLockTestActivity;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.language.LanguagesActivity;
import cn.upus.app.upems.util.updata.UpdateManager;

/**
 * 系统设置
 */
public class SystemSetDialog extends BaseDialog {

    private static final String TAG = SystemSetDialog.class.getSimpleName();

    @BindView(R.id.cb_start)
    CheckBox cb_start;
    @BindView(R.id.cb_tts)
    CheckBox cb_tts;
    @BindView(R.id.bt_register)
    Button bt_register;
    @BindView(R.id.bt_dev_updata)
    Button bt_dev_updata;
    @BindView(R.id.bt_test)
    Button bt_test;
    @BindView(R.id.bt_updata)
    Button bt_updata;
    @BindView(R.id.bt_language)
    Button bt_language;
    @BindView(R.id.bt_exit)
    Button bt_exit;
    @BindView(R.id.bt_camera)
    Button bt_camera;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.bt_serialport_set)
    Button bt_serialport_set;
    @BindView(R.id.rg)
    RadioGroup rg;
    @BindView(R.id.rb1)
    RadioButton rb1;
    @BindView(R.id.rb2)
    RadioButton rb2;

    private int type = -1;

    public void setType(int type) {
        this.type = type;
    }

    public SystemSetDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_system_set;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        if (MApp.mSp.getBoolean(UserData.START)) {
            cb_start.setChecked(true);
        }
        if (MApp.mSp.getBoolean(UserData.TTS_OPEN)) {
            cb_tts.setChecked(true);
        }
        if (MApp.mSp.getBoolean(UserData.isHorizontal)) {
            rb2.setChecked(true);
        } else {
            rb1.setChecked(true);
        }

        cb_start.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //是否自动启动程序
            if (isChecked) {
                MApp.mSp.put(UserData.START, true);
                LogUtils.e(TAG, "自动启动");
            } else {
                MApp.mSp.put(UserData.START, false);
                LogUtils.e(TAG, "不自动启动");
            }
        });

        cb_tts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //是否开启语音提示
            if (isChecked) {
                MApp.mSp.put(UserData.TTS_OPEN, true);
                LogUtils.e(TAG, "自动启动");
            } else {
                MApp.mSp.put(UserData.TTS_OPEN, false);
                LogUtils.e(TAG, "不自动启动");
            }
        });

        rg.setOnCheckedChangeListener((radioGroup, i) -> {
            //横屏
            switch (i) {
                case R.id.rb1:
                    LogUtils.e(TAG, "16:9");
                    MApp.mSp.put(UserData.isHorizontal, false);
                    restartApp();
                    break;
                case R.id.rb2:
                    LogUtils.e(TAG, "4:3");
                    MApp.mSp.put(UserData.isHorizontal, true);
                    restartApp();
                    break;
                default:
                    break;
            }
        });
    }

    private RegisterDialog mRegisterDialog;

    @OnClick({R.id.bt_serialport_set, R.id.bt_camera, R.id.iv_close, R.id.bt_register, R.id.bt_dev_updata, R.id.bt_test, R.id.bt_updata, R.id.bt_language, R.id.bt_exit})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.bt_serialport_set:
                BaudrateSetDialog baudrateSetDialog = new BaudrateSetDialog(getContext(), activity);
                baudrateSetDialog.setSystemSetDialog(this);
                baudrateSetDialog.show();
                break;
            case R.id.bt_camera:
                EventBus.getDefault().post(new CameraSettingsBean(1));
                break;
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_register:
                mRegisterDialog = new RegisterDialog(getContext(), activity);
                mRegisterDialog.setType(0);
                mRegisterDialog.show();
                break;
            case R.id.bt_dev_updata:
                mRegisterDialog = new RegisterDialog(getContext(), activity);
                mRegisterDialog.setType(1);
                mRegisterDialog.show();
                break;
            case R.id.bt_test:
                /*if (TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO)) || TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVID))) {
                    showToast(getContext().getResources().getString(R.string.not_register), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.not_register));
                    return;
                }
                if (TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND))) {
                    showToast(getContext().getResources().getString(R.string.not_register), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.not_register));
                    return;
                }*/
                activity.startActivity(new Intent(getContext(), OpenLockTestActivity.class));
                break;
            case R.id.bt_updata:
                try {
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            Looper.prepare();
                            UpdateManager updateManager = new UpdateManager(getContext());
                            UpdateManager.verifyStoragePermissions(activity);
                            updateManager.checkUpdate();
                            Looper.loop();
                        }
                    }.start();
                } catch (Exception e) {
                    showToast("检测更新时发生异常", 2);
                }
                break;
            case R.id.bt_language:
                activity.startActivityForResult(new Intent(getContext(), LanguagesActivity.class), BaseLanguageActivity.CHANGE_LANGUAGE_REQUEST_CODE);
                break;
            case R.id.bt_exit:
                dismiss();
                activity.finish();
                System.exit(0);
                break;
        }
    }

}
