package cn.upus.app.upems.ui.dialog.storage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.adapter.BoxAdapter;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.BoxEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.FastClickUtil;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 格子列表
 */
public class BoxListDialog extends BaseDialog {

    private static final String TAG = BoxListDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.bt_distribution)
    Button bt_distribution;

    //货格列表
    private List<BoxEntity> boxEntitiesAll = new ArrayList<>();
    private List<BoxEntity> boxEntities = new ArrayList<>();
    private List<BoxEntity> boxEntitiesOpen = new ArrayList<>();
    private BoxAdapter mBoxAdapter;

    public BoxListDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_box_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initAdapter();
        if (NetworkUtils.isConnected()) {
            getBoxlist();
        }
    }

    @OnClick({R.id.iv_close, R.id.bt_distribution})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_distribution:
                String devkind = MApp.mSp.getString(UserData.DEVKIND);
                if (!TextUtils.isEmpty(devkind) && devkind.equals(BaseActivity.TYPE_25) || devkind.equals(BaseActivity.TYPE_26)) {
                    //手动输入
                    StorageInputDialog storage_input_dialog = new StorageInputDialog(getContext(), activity);
                    storage_input_dialog.setBoxEntities(boxEntitiesOpen);
                    storage_input_dialog.setBoxListDialog(this);
                    storage_input_dialog.show();
                } else {
                    //蓝牙扫描
                    StorageBluetoothDialog storage_bluetooth_dialog = new StorageBluetoothDialog(getContext(), activity);
                    storage_bluetooth_dialog.setBoxEntities(boxEntitiesOpen);
                    storage_bluetooth_dialog.setBoxListDialog(this);
                    storage_bluetooth_dialog.show();
                }
                break;
            default:
                break;
        }
    }

    private void initAdapter() {
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBoxAdapter = new BoxAdapter(R.layout.item_box_list, boxEntities);
        mBoxAdapter.setInputComplete(activity, mInputComplete);
        rv.setAdapter(mBoxAdapter);
    }

    /**
     * 格子数量 输入完成回调
     */
    private BoxAdapter.InputComplete mInputComplete = (boxEntity, number) -> {
        if (null == boxEntity) {
            return;
        }
        Iterator<BoxEntity> it = boxEntitiesOpen.iterator();
        while (it.hasNext()) {
            BoxEntity value = it.next();
            if (boxEntity.getKindna().equals(value.getKindna())) {
                it.remove();
            }
        }
        int openLockSize = 0;
        for (int i = 0; i < boxEntitiesAll.size(); i++) {
            if (openLockSize < number && boxEntitiesAll.get(i).getKindna().equals(boxEntity.getKindna())) {
                boxEntitiesOpen.add(boxEntitiesAll.get(i));
                openLockSize += 1;
            }
        }
        LogUtils.e(TAG, "当前格子总数： " + boxEntitiesOpen.size() + "");
    };

    /**
     * 获取可用货格列表
     */
    public void getBoxlist() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("devid", MApp.mSp.getString(UserData.DEVID));
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/boxlist", jsonObject.toString(), this);
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
            JSONArray mJsonArray;
            if (TextUtils.isEmpty(jsonObject.toString())) {
                showToast(getContext().getString(R.string.net_error_1), 2);
                BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_error_1));
                return;
            }
            switch (WEB_TYPT) {
                case TYPE_0:
                    //{"retcode":"1","list":"2","listTask":[{"shelfno":"A02","kindna":"小格","price":"0.5000"},{"shelfno":"A03","kindna":"小格","price":"0.5000"}]}
                    boxEntities.clear();
                    boxEntitiesAll.clear();
                    if (TextUtils.isEmpty(jsonObject.optString("retcode"))) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_1));
                        return;
                    }
                    if (!jsonObject.optString("retcode").equals("1")) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_7), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_7));
                        return;
                    }
                    mJsonArray = jsonObject.optJSONArray("listTask");
                    if (mJsonArray.length() == 0) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_7), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_7));
                        return;
                    }
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject object = (JSONObject) mJsonArray.opt(i);
                        BoxEntity boxEntity = (BoxEntity) GsonUtil.stringToObject(object.toString(), BoxEntity.class);
                        boxEntity.setSize(1);
                        boxEntitiesAll.add(boxEntity);
                    }
                    Map<String, BoxEntity> map = new HashMap<>();
                    BoxEntity mBoxEntity;
                    for (BoxEntity entity : boxEntitiesAll) {
                        mBoxEntity = map.get(entity.getKindna());
                        if (mBoxEntity != null) {
                            mBoxEntity.setSize(mBoxEntity.getSize() + 1);
                        } else {
                            map.put(entity.getKindna(), entity);
                        }
                    }
                    boxEntities.addAll(map.values());
                    mBoxAdapter.notifyDataSetChanged();
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
