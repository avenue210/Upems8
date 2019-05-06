package cn.upus.app.upems.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.LogUtils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.util.serialport_usb485.Data;

/**
 * 串口路径 波特率设置
 */
public class BaudrateSetDialog extends BaseDialog {

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.s_device)
    Spinner s_device;
    @BindView(R.id.s_baud_rate)
    Spinner s_baud_rate;
    @BindView(R.id.bt_ok)
    Button bt_ok;

    private SystemSetDialog systemSetDialog;

    private String DEVICE = "";
    private String BAUDRATE = "";

    public void setSystemSetDialog(SystemSetDialog systemSetDialog) {
        this.systemSetDialog = systemSetDialog;
    }

    public BaudrateSetDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_set_baudrate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iv_close.setOnClickListener(v -> dismiss());

        DEVICE = MApp.mSp.getString(Data.DEVICE);
        BAUDRATE = MApp.mSp.getString(Data.BAUDRATE);

        bt_ok.setOnClickListener(v -> {
            MApp.mSp.put(Data.DEVICE, DEVICE);
            MApp.mSp.put(Data.BAUDRATE, BAUDRATE);
            restartApp();
        });

        SerialPortFinder mSerialPortFinder = MApp.mSerialportUtil.mSerialPortFinder;

        //mSerialPortFinder.getAllDevices()
        //mSerialPortFinder.getAllDevicesPath()
        List<String> devices = Arrays.asList(mSerialPortFinder.getAllDevicesPath());
        List<String> baudRates = Arrays.asList(getContext().getResources().getStringArray(R.array.baudrates_value));

        ArrayAdapter entriesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, devices);
        entriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_device.setAdapter(entriesAdapter);
        if (!TextUtils.isEmpty(MApp.mSp.getString(Data.DEVICE))) {
            for (int i = 0; i < devices.size(); i++) {
                if (MApp.mSp.getString(Data.DEVICE).equals(devices.get(i))) {
                    s_device.setSelection(i, true);
                }
            }
        }
        s_device.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.e("device", devices.get(position));
                DEVICE = devices.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter entryValuesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, baudRates);
        entryValuesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_baud_rate.setAdapter(entryValuesAdapter);
        if (!TextUtils.isEmpty(MApp.mSp.getString(Data.BAUDRATE))) {
            for (int i = 0; i < baudRates.size(); i++) {
                if (MApp.mSp.getString(Data.BAUDRATE).equals(baudRates.get(i))) {
                    s_baud_rate.setSelection(i, true);
                }
            }
        }

        s_baud_rate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.e("baud rate", baudRates.get(position));
                BAUDRATE = baudRates.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void initWindow() {
        Window window = getWindow();
        if (null != window) {
            BarUtils.setNavBarVisibility(activity, false);
            BarUtils.setStatusBarVisibility(activity, false);
            window.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams windowParams = window.getAttributes();
            //设置宽度顶满屏幕,无左右留白
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout(dm.widthPixels, window.getAttributes().height);
            windowParams.width = dm.widthPixels / 3 * 2;
            windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            windowParams.dimAmount = 0.0f;
            window.setAttributes(windowParams);
            setCanceledOnTouchOutside(false);
        }
    }

    /**
     * 重启应用
     */
    protected void restartApp() {
        dismiss();
        if (null != systemSetDialog) {
            systemSetDialog.dismiss();
        }
        if (null != activity) {
            activity.finish();
        }
        AppUtils.relaunchApp(true);
    }
}
