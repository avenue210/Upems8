package cn.upus.app.upems.ui.dialog.storage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.adapter.BoxgroupAdapter;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.bean.BoxgroupEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.gson.GsonUtil;

/**
 * 配餐柜 分组选择
 */
public class BoxGroupDialog extends BaseDialog {

    private static final String TAG = BoxGroupDialog.class.getSimpleName();

    @BindView(R.id.iv_close)
    ImageView iv_close;
    @BindView(R.id.rv)
    RecyclerView rv;
    @BindView(R.id.s_detail)
    Spinner s_detail;
    @BindView(R.id.tv_detail)
    TextView tv_detail;
    @BindView(R.id.bt_distribution)
    Button bt_distribution;

    private ArrayAdapter boxgroupGroupnoAdapter;
    private List<String> boxgroupGroupnos = new ArrayList<>();

    private BoxgroupAdapter boxgroupAdapter;
    private List<BoxgroupEntity> boxgroupEntities = new ArrayList<>();
    private List<BoxgroupEntity.DetailBean> detailBeans = new ArrayList<>();
    private List<BoxgroupEntity.DetailBean> detailBeansOpens = new ArrayList<>();

    private int mPosition = -1;

    public BoxGroupDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_box_group;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBoxgroupAdapter();
        initGroupnoAdapter();
        boxgroup();
    }

    @OnClick({R.id.iv_close, R.id.bt_distribution})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_distribution:
                if (detailBeansOpens.size() == 0) {
                    showToast(getContext().getResources().getString(R.string.net_tip_type_3_error_4), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_4));
                    return;
                }
                LogUtils.e(detailBeansOpens.size() + "  " + detailBeansOpens.toString());
                List<BoxgroupEntity.DetailBean> beans = new ArrayList<>();
                for (int i = 0; i < detailBeansOpens.size(); i++) {
                    int size = detailBeansOpens.get(i).getAddSize();
                    for (int k = 0; k < size; k++) {
                        beans.add(detailBeans.get(i));
                    }
                }
                LogUtils.e(beans.size() + "  " + beans.toString());
                StorageHbDialog storageHbDialog = new StorageHbDialog(getContext(), activity);
                storageHbDialog.setBoxGroupDialog(this);
                storageHbDialog.setBeans(beans);
                storageHbDialog.show();
                break;
        }
    }

    private void initGroupnoAdapter() {
        boxgroupGroupnoAdapter = new ArrayAdapter<>(getContext(), R.layout.item_simple_spinner, boxgroupGroupnos);
        boxgroupGroupnoAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);//android.R.layout.simple_spinner_dropdown_item
        s_detail.setAdapter(boxgroupGroupnoAdapter);
        s_detail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    mPosition = position;
                    detailBeansOpens.clear();
                    detailBeans.clear();
                    tv_detail.setText(boxgroupEntities.get(position).getUsecnt() + "/" + boxgroupEntities.get(position).getSumcnt());
                    Map<String, BoxgroupEntity.DetailBean> map = new HashMap<>();
                    BoxgroupEntity.DetailBean mBoxEntity;
                    for (BoxgroupEntity.DetailBean entity : boxgroupEntities.get(position).getDetail()) {
                        mBoxEntity = map.get(entity.getKindno());
                        if (mBoxEntity != null) {
                            mBoxEntity.setSize(mBoxEntity.getSize() + 1);
                        } else {
                            entity.setSize(1);
                            map.put(entity.getKindno(), entity);
                        }
                    }
                    detailBeans.clear();
                    for (String key : map.keySet()) {
                        map.get(key).setType(mPosition);
                        map.get(key).setGroupno(boxgroupGroupnos.get(position));
                        detailBeans.add(map.get(key));
                    }
                    boxgroupAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initBoxgroupAdapter() {
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        boxgroupAdapter = new BoxgroupAdapter(R.layout.item_detail_list, detailBeans);
        boxgroupAdapter.setInputComplete(activity, mInputComplete);
        rv.setAdapter(boxgroupAdapter);
    }

    private BoxgroupAdapter.InputComplete mInputComplete = (detailBean, number) -> {
        if (null == detailBean) {
            return;
        }
        Iterator<BoxgroupEntity.DetailBean> it = detailBeansOpens.iterator();
        while (it.hasNext()) {
            BoxgroupEntity.DetailBean value = it.next();
            if (detailBean.getKindna().equals(value.getKindna())) {
                it.remove();
            }
        }
        int openLockSize = 0;
        for (int i = 0; i < detailBeans.size(); i++) {
            if (openLockSize < number && detailBeans.get(i).getKindna().equals(detailBean.getKindna())) {
                detailBeansOpens.add(detailBeans.get(i));
                openLockSize += 1;
            }
        }
    };

    /**
     * 获取可用货格列表 //校园取餐柜 货格列表
     */
    public void boxgroup() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            jsonObject.put("devid", MApp.mSp.getString(UserData.DEVID));
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/boxgroup", jsonObject.toString(), this);
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
                    boxgroupEntities.clear();
                    boxgroupGroupnos.clear();
                    //{"msg":"Query Success!","data":[{"usecnt":"24","groupno":null,"sumcnt":"26","detail":[{"kindno":"0","kindna":"小格","price":"0.5","lockno":"5","posno":"A05","shelfno":"A05","boardno":"1"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"7","posno":"A09","shelfno":"A09","boardno":"1"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"12","posno":"A10","shelfno":"A10","boardno":"1"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"12","posno":"B12","shelfno":"B12","boardno":"2"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"13","posno":"B13","shelfno":"B13","boardno":"2"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"14","posno":"B14","shelfno":"B14","boardno":"2"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"15","posno":"B15","shelfno":"B15","boardno":"2"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"9","posno":"B09","shelfno":"B09","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"10","posno":"B10","shelfno":"B10","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"7","posno":"B07","shelfno":"B07","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"6","posno":"B16","shelfno":"B16","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"17","posno":"B17","shelfno":"B17","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"18","posno":"B18","shelfno":"B18","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"19","posno":"B19","shelfno":"B19","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"20","posno":"B20","shelfno":"B20","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"11","posno":"A11","shelfno":"A11","boardno":"1"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"10","posno":"A12","shelfno":"A12","boardno":"1"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"8","posno":"B08","shelfno":"B08","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"9","posno":"A07","shelfno":"A07","boardno":"1"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"8","posno":"A08","shelfno":"A08","boardno":"1"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"6","posno":"A06","shelfno":"A06","boardno":"1"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"3","posno":"A03","shelfno":"A03","boardno":"1"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"4","posno":"A04","shelfno":"A04","boardno":"1"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"11","posno":"B11","shelfno":"B11","boardno":"2"}]},{"usecnt":"5","groupno":"1","sumcnt":"6","detail":[{"kindno":"0","kindna":"小格","price":"0.5","lockno":"2","posno":"B02","shelfno":"B02","boardno":"2"},{"kindno":"0","kindna":"小格","price":"0.5","lockno":"5","posno":"B05","shelfno":"B05","boardno":"2"},{"kindno":"1","kindna":"中格","price":"0.6","lockno":"6","posno":"B06","shelfno":"B06","boardno":"2"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"3","posno":"B03","shelfno":"B03","boardno":"2"},{"kindno":"2","kindna":"大格","price":"0.7","lockno":"1","posno":"B01","shelfno":"B01","boardno":"2"}]}],"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_7), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_7));
                        return;
                    }
                    mJsonArray = jsonObject.optJSONArray("data");
                    if (mJsonArray.length() == 0) {
                        showToast(getContext().getString(R.string.net_tip_type_3_error_7), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_3_error_7));
                        return;
                    }
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject o = (JSONObject) mJsonArray.opt(i);
                        BoxgroupEntity boxgroupEntity = (BoxgroupEntity) GsonUtil.stringToObject(o.toString(), BoxgroupEntity.class);
                        for (int k = 0; k < boxgroupEntity.getDetail().size(); k++) {
                            boxgroupEntity.getDetail().get(k).setType(i);
                            boxgroupEntity.getDetail().get(k).setSize(1);
                            boxgroupEntity.getDetail().get(k).setAddSize(0);
                        }
                        boxgroupEntities.add(boxgroupEntity);
                        String groupno = boxgroupEntity.getGroupno();
                        if (TextUtils.isEmpty(groupno)) {
                            groupno = String.valueOf(i + 1);
                        }
                        boxgroupGroupnos.add(groupno);
                    }
                    boxgroupGroupnoAdapter.notifyDataSetChanged();
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
