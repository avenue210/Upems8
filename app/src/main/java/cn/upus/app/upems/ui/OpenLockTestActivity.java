package cn.upus.app.upems.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.noober.background.BackgroundLibrary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.service.OpenLockService;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.serialport_usb485.Data;

/**
 * 锁控板测试页面
 */
public class OpenLockTestActivity extends AppCompatActivity {

    private RadioGroup rg;
    private RadioButton rb1;
    private RadioButton rb2;
    private RadioButton rb3;

    private RadioGroup rg_usb_serialport;
    private RadioButton rb_usb;
    private RadioButton rb_serialport;

    private TextView tv;

    private RecyclerView rv1;
    private RecyclerView rv2;
    private RecyclerView rv3;

    private EditText et1;
    private EditText et2;
    private EditText et3;
    private EditText et4;
    private LinearLayout ll_path;

    private BtAdapter adapter1;
    private BtAdapter adapter2;
    private BtAdapter adapter3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        /*屏幕常亮*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        BackgroundLibrary.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_open_lock);
        EventBus.getDefault().register(this);

        tv = findViewById(R.id.tv);

        rg_usb_serialport = findViewById(R.id.rg_usb_serialport);
        rb_usb = findViewById(R.id.rb_usb);
        rb_serialport = findViewById(R.id.rb_serialport);

        rg = findViewById(R.id.rg);
        rb1 = findViewById(R.id.rb1);
        rb2 = findViewById(R.id.rb2);
        rb3 = findViewById(R.id.rb3);

        rv1 = findViewById(R.id.rv1);
        rv2 = findViewById(R.id.rv2);
        rv3 = findViewById(R.id.rv3);
        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        ll_path = findViewById(R.id.ll_path);

        //OpenLockService.protocol_type = 0;
        //OpenLockService.usb485_serialport = 0;

        initAdapter1();
        initAdapter2();
        initAdapter3();

        switch (OpenLockService.protocol_type) {
            case 0:
                rv1.setVisibility(View.VISIBLE);
                rv2.setVisibility(View.GONE);
                rv3.setVisibility(View.GONE);
                rb1.setChecked(true);
                break;
            case 1:
                rv1.setVisibility(View.GONE);
                rv2.setVisibility(View.VISIBLE);
                rv3.setVisibility(View.GONE);
                rb2.setChecked(true);
                break;
            case 2:
                rv1.setVisibility(View.GONE);
                rv2.setVisibility(View.GONE);
                rv3.setVisibility(View.VISIBLE);
                ll_path.setVisibility(View.VISIBLE);
                rb3.setChecked(true);
                break;
            default:
                rv1.setVisibility(View.VISIBLE);
                rv2.setVisibility(View.GONE);
                rv3.setVisibility(View.GONE);
                rb1.setChecked(true);
                OpenLockService.protocol_type = 0;
                break;
        }

        switch (OpenLockService.usb485_serialport) {
            case 0:
                rb_usb.setChecked(true);
                break;
            case 1:
                rb_serialport.setChecked(true);
                break;
            default:
                rb_usb.setChecked(true);
                OpenLockService.usb485_serialport = 0;
                break;
        }

        rg_usb_serialport.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_usb:
                    OpenLockService.usb485_serialport = 0;
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
                case R.id.rb_serialport:
                    OpenLockService.usb485_serialport = 1;
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
                default:
                    OpenLockService.usb485_serialport = 0;
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
            }
        });

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb1:
                    OpenLockService.protocol_type = 0;
                    rv1.setVisibility(View.VISIBLE);
                    rv2.setVisibility(View.GONE);
                    rv3.setVisibility(View.GONE);
                    ll_path.setVisibility(View.GONE);
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
                case R.id.rb2:
                    OpenLockService.protocol_type = 1;
                    rv1.setVisibility(View.GONE);
                    rv2.setVisibility(View.VISIBLE);
                    rv3.setVisibility(View.GONE);
                    ll_path.setVisibility(View.GONE);
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
                case R.id.rb3:
                    OpenLockService.protocol_type = 2;
                    rv1.setVisibility(View.GONE);
                    rv2.setVisibility(View.GONE);
                    rv3.setVisibility(View.VISIBLE);
                    ll_path.setVisibility(View.VISIBLE);
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
                default:
                    OpenLockService.protocol_type = 0;
                    rv1.setVisibility(View.VISIBLE);
                    rv2.setVisibility(View.GONE);
                    rv3.setVisibility(View.GONE);
                    restartDriver(OpenLockService.usb485_serialport, OpenLockService.protocol_type);
                    break;
            }
        });

    }

    /**
     * 重启 启动
     */
    private void restartDriver(int usb485_serialport, int protocol_type) {
        if (MApp.mUsb485Util.driverOpen) {
            MApp.mUsb485Util.close();
        }
        if (MApp.mSerialportUtil.driverOpen) {
            MApp.mSerialportUtil.close();
        }
        initDriver(usb485_serialport, protocol_type);
    }

    /**
     * 初始化驱动
     *
     * @param usb485_serialport 0 USB / 1 串口
     * @param protocol_type     0 默认 / 1 银龙 / 2 果核
     */
    private void initDriver(int usb485_serialport, int protocol_type) {
        switch (usb485_serialport) {
            case 0://USB
                switch (protocol_type) {
                    case 0://默认
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(9600);
                        }
                        break;
                    case 1://银龙
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(115200);
                        }
                        break;
                    case 2://果核
                        if (!MApp.mUsb485Util.driverOpen) {
                            MApp.mUsb485Util.start(9600);
                        }
                        break;
                    default:
                        break;
                }
                break;
            case 1://串口
                if (!MApp.mSerialportUtil.driverOpen) {
                    if (!TextUtils.isEmpty(MApp.mSp.getString(Data.DEVICE)) && !TextUtils.isEmpty(MApp.mSp.getString(Data.BAUDRATE))){
                        if (protocol_type == 1) {
                            MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE), "115200");
                        } else {
                            MApp.mSerialportUtil.start(MApp.mSp.getString(Data.DEVICE), MApp.mSp.getString(Data.BAUDRATE));
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void initAdapter1() {
        List<String> bts = new ArrayList<>();
        bts.add(getResources().getString(R.string.dialog_item_open_lock));
        bts.add(getResources().getString(R.string.all_unlocked));
        bts.add(getResources().getString(R.string.query_state));
        bts.add(getResources().getString(R.string.query_all_state));
        bts.add(getResources().getString(R.string.open_electricity));
        bts.add(getResources().getString(R.string.turn_off_electricity));

        rv1.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter1 = new BtAdapter(R.layout.item_bt, bts);
        rv1.setAdapter(adapter1);
        rv1.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText())) {
                    return;
                }
                int boardIndex = Integer.parseInt(et1.getText().toString().trim());
                int lockIndex = Integer.parseInt(et2.getText().toString().trim());

                switch (position) {
                    case 0://开锁
                        EventBus.getDefault().post(new OpenLockDataBean(0, 0, boardIndex, lockIndex, null));
                        break;
                    case 1://全部开锁
                        EventBus.getDefault().post(new OpenLockDataBean(0, 5, boardIndex, lockIndex, null));
                        break;
                    case 2://查询状态
                        EventBus.getDefault().post(new OpenLockDataBean(0, 1, boardIndex, lockIndex, null));
                        break;
                    case 3://查询全部状态
                        EventBus.getDefault().post(new OpenLockDataBean(0, 4, boardIndex, lockIndex, null));
                        break;
                    case 4://开电
                        EventBus.getDefault().post(new OpenLockDataBean(0, 2, boardIndex, lockIndex, null));
                        break;
                    case 5://关电
                        EventBus.getDefault().post(new OpenLockDataBean(0, 3, boardIndex, lockIndex, null));
                        break;
                }
            }
        });
    }

    private void initAdapter2() {
        List<String> bts = new ArrayList<>();
        bts.add(getResources().getString(R.string.dialog_item_open_lock));
        bts.add(getResources().getString(R.string.query_all_state));

        rv2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter2 = new BtAdapter(R.layout.item_bt, bts);
        rv2.setAdapter(adapter2);
        rv2.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText())) {
                    return;
                }
                int boardIndex = Integer.parseInt(et1.getText().toString().trim());
                int lockIndex = Integer.parseInt(et2.getText().toString().trim());
                switch (position) {
                    case 0://开锁
                        EventBus.getDefault().post(new OpenLockDataBean(0, 0, boardIndex, lockIndex, null));
                        break;
                    case 1://查询全部状态
                        EventBus.getDefault().post(new OpenLockDataBean(0, 4, boardIndex, lockIndex, null));
                        break;
                }
            }
        });
    }

    private void initAdapter3() {
        List<String> bts = new ArrayList<>();
        bts.add("单个开锁(锁号1)");
        bts.add("查询状态(锁号1)");
        bts.add("写入锁号(A地址，B地址)");

        rv3.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter3 = new BtAdapter(R.layout.item_bt, bts);
        rv3.setAdapter(adapter3);
        rv3.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (TextUtils.isEmpty(et1.getText()) || TextUtils.isEmpty(et2.getText())) {
                    return;
                }
                int boardIndex = Integer.parseInt(et1.getText().toString().trim());
                int lockIndex = Integer.parseInt(et2.getText().toString().trim());
                switch (position) {
                    case 0://开锁
                        EventBus.getDefault().post(new OpenLockDataBean(0, 0, boardIndex, lockIndex, null));
                        break;
                    case 1://查询状态
                        EventBus.getDefault().post(new OpenLockDataBean(0, 1, boardIndex, lockIndex, null));
                        break;
                    case 2://写入锁号
                        if (TextUtils.isEmpty(et3.getText()) || TextUtils.isEmpty(et4.getText())) {
                            return;
                        }
                        int pata1 = Integer.parseInt(et3.getText().toString().trim());
                        int pata2 = Integer.parseInt(et4.getText().toString().trim());
                        EventBus.getDefault().post(new OpenLockDataBean(0, 2, pata1, pata2, null));
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
        tv.append("\n" + DateTimeUtil.getCurDateStr() + " : " + bean.getReadData() + " - " + bean.getMessage());
    }

    class BtAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        public BtAdapter(int layoutResId, @Nullable List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(R.id.bt, item);
        }
    }

}
