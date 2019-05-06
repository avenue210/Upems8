package cn.upus.app.upems.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.service.OpenLockService;
import cn.upus.app.upems.ui.dialog.BaudrateSetDialog;
import cn.upus.app.upems.util.serialport_usb485.Data;
import cn.upus.app.upems.util.serialport_usb485.GuoHeOpenLockUtil;

public class MainActivity extends BaseActivity {

    @BindView(R.id.bt0)
    Button bt0;
    @BindView(R.id.bt1)
    Button bt1;
    @BindView(R.id.bt2)
    Button bt2;

    @Override
    protected int initLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @OnClick({R.id.bt0, R.id.bt1, R.id.bt2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt0:
                BaudrateSetDialog mBaudrateSetDialog = new BaudrateSetDialog(this,this);
                mBaudrateSetDialog.show();
                break;
            case R.id.bt1:
                if (!MApp.mSerialportUtil.driverOpen) {
                    if (OpenLockService.protocol_type == 1){
                        MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE),"115200");
                    }else {
                        MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE),MApp.mSp.getString(Data.BAUDRATE));
                    }
                }
                break;
            case R.id.bt2:
                MApp.mSerialportUtil.sendData(GuoHeOpenLockUtil.openLock(1));
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        MApp.mSerialportUtil.close();
        super.onDestroy();
    }
}
