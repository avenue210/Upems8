package cn.upus.app.upems.ui.dialog.take;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.adapter.PayByCardTakeListAdapter;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.PartParameterEntity;
import cn.upus.app.upems.bean.event_bus.OpenLockCallBackBean;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;

/**
 * 刷卡取件列表
 */
public class PayByCardTakeListDialog extends BaseDialog {

    private static final String TAG = PayByCardTakeListDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.rv)
    RecyclerView rv;

    private PayByCardTakeListAdapter adapter;
    private List<PartParameterEntity> partParameterEntities;
    private String shelfno;
    private String expno;

    public void setPartParameterEntities(List<PartParameterEntity> partParameterEntities) {
        this.partParameterEntities = partParameterEntities;
    }

    public PayByCardTakeListDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_pay_by_card_take_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        iv_close.setOnClickListener(v -> dismiss());
        initAdapter();
    }

    private void initAdapter() {
        if (null == partParameterEntities) {
            return;
        }
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new PayByCardTakeListAdapter(R.layout.item_pay_by_card_take_list, partParameterEntities);
        rv.setAdapter(adapter);
        adapter.getOpenLock(item -> {
            shelfno = item.getShelfno();
            expno = item.getExpno();
            EventBus.getDefault().post(new OpenLockDataBean(0, 0, 1, 1, null));
        });
    }

    @Override
    protected void onStop() {
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
            BaseActivity.ttsSpeak(bean.getMessage());
            showToast(getContext().getResources().getString(R.string.take_ok_tip), 0);
            updboxstate(shelfno, expno);
        } else {
            BaseActivity.ttsSpeak(bean.getMessage());
            showToast(bean.getMessage(), 2);
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
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/updboxstate", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(String data) {
        super.onSuccess(data);

    }

    @Override
    public void onError(String data) {
        super.onError(data);

    }
}
