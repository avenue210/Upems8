package cn.upus.app.upems.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
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
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.base.BaseActivity;
import cn.upus.app.upems.base.BaseDialog;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.FastClickUtil;

/**
 * 设备注册 / 更新
 */
public class RegisterDialog extends BaseDialog {

    private static final String TAG = RegisterDialog.class.getSimpleName();

    @BindView(R.id.tv_title)
    TextView tv_title;
    @BindView(R.id.iv_close)
    ImageView ivClose;
    @BindView(R.id.et_company)
    TextInputEditText et_company;
    @BindView(R.id.et_devno)
    TextInputEditText et_devno;
    @BindView(R.id.bt_register)
    Button bt_register;
    @BindView(R.id.s_type)
    Spinner s_type;

    private String company;
    private String devid;//设备ID
    private String devno;//设备编号
    private String devkind = null;//设备类型
    private List<Map<String, String>> devkindlst = new ArrayList<>();
    private List<String> hasscnLsList = new ArrayList<>();
    private List<String> kindnoLsList = new ArrayList<>();
    private List<String> kindnaLsList = new ArrayList<>();
    private ArrayAdapter kindnaAdapter = null;

    private int type = -1;

    public void setType(int type) {
        this.type = type;
    }

    public RegisterDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, activity);
    }

    @Override
    protected int initLayoutView() {
        return R.layout.dialog_register;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        et_company.setText(MApp.mSp.getString(UserData.COMPANY));
        et_devno.setText(MApp.mSp.getString(UserData.DEVNO));
        et_company.setEnabled(false);
        if (type == 1) {
            et_devno.setEnabled(false);
            tv_title.setText(getContext().getResources().getString(R.string.dialog_item_dev_updata));
        }
        kindnaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, kindnaLsList);
        kindnaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_type.setAdapter(kindnaAdapter);

        devkindlst();

        s_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LogUtils.e(TAG, "选取了:" + kindnaLsList.get(position) + " " + kindnoLsList.get(position));
                devkind = kindnoLsList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @OnClick({R.id.iv_close, R.id.bt_register})
    public void onClick(View view) {
        if (!FastClickUtil.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;
            case R.id.bt_register:
                if (TextUtils.isEmpty(et_company.getText())) {
                    et_company.requestFocus();
                    showToast(getContext().getString(R.string.login_tip_a), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_a));
                    return;
                }
                if (TextUtils.isEmpty(et_devno.getText())) {
                    et_devno.requestFocus();
                    showToast(getContext().getString(R.string.login_tip_d), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_d));
                    return;
                }
                devid = MApp.mSp.getString(UserData.DEVID);

                if (TextUtils.isEmpty(devkind)) {
                    showToast(getContext().getString(R.string.login_tip_f), 2);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.login_tip_f));
                    return;
                }
                devno = et_devno.getText().toString().trim();
                company = et_company.getText().toString().trim();
                if (type == 1) {
                    //类型修改
                    updkind();
                } else {
                    insertRegister(et_company.getText().toString().trim(), et_devno.getText().toString().trim(), devkind);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设备注册获取所有设备类型列表
     */
    private void devkindlst() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("hasscn", "1");
            jsonObject.put("cateno", "2");
            WEB_TYPT = TYPE_0;
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/devkindlst", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册
     *
     * @param cocode
     * @param devno
     * @param devkind
     */
    private void insertRegister(String cocode, String devno, String devkind) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("apiName", "insertRegister");
            jsonObject.put("cocode", cocode);
            jsonObject.put("devcate", "0");
            jsonObject.put("devno", devno);
            jsonObject.put("devid", devid);
            jsonObject.put("devkind", devkind);
            WEB_TYPT = TYPE_1;
            //mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/regdev", jsonObject.toString(), this);
            mHttpUtil.postJson("http://www.upus.cn:8091/" + "upus_APP/app/expressbox/regdev", jsonObject.toString(), this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新
     */
    private void updkind() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", devno);
            jsonObject.put("devkind", devkind);
            WEB_TYPT = TYPE_1;
            //mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/updkind", jsonObject.toString(), this);
            mHttpUtil.postJson("http://www.upus.cn:8091/" + "upus_APP/app/expressbox/updkind", jsonObject.toString(), this);
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
                case TYPE_0://设备类型列表
                    //"msg":"查询成功!","data":[{"hasscn":"1","kindno":"10","kindna":"弹簧售货机"},{"hasscn":"1","kindno":"11","kindna":"格子售货机"},{"hasscn":"1","kindno":"20","kindna":"有屏快递柜"},{"hasscn":"1","kindno":"22","kindna":"有屏存包柜"},{"hasscn":"1","kindno":"23","kindna":"有屏扫码存包柜"},{"hasscn":"1","kindno":"24","kindna":"无屏扫码存包柜"}],"success":"1"}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getString(R.string.net_tip_type_2_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_2_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        return;
                    }
                    mJsonArray = jsonObject.optJSONArray("data");
                    if (mJsonArray.length() == 0) {
                        return;
                    }
                    //devkindlst
                    for (int i = 0; i < mJsonArray.length(); i++) {
                        JSONObject object = (JSONObject) mJsonArray.opt(i);
                        Map<String, String> m = new HashMap<>();
                        String hasscn = object.optString("hasscn");
                        String kindno = object.optString("kindno");
                        String kindna = object.optString("kindna");
                        m.put("hasscn", hasscn);
                        m.put("kindno", kindno);
                        m.put("kindna", kindna);
                        hasscnLsList.add(hasscn);
                        kindnoLsList.add(kindno);
                        kindnaLsList.add(kindna);
                        devkindlst.add(m);
                    }
                    kindnaAdapter.notifyDataSetChanged();
                    if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVKIND))) {
                        for (int i = 0; i < kindnoLsList.size(); i++) {
                            if (MApp.mSp.getString(UserData.DEVKIND).equals(kindnoLsList.get(i))) {
                                s_type.setSelection(i, true);
                                break;
                            }
                        }
                    }
                    break;
                case TYPE_1:
                    //{"msg":"操作成功!","success":1}
                    if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                        showToast(getContext().getString(R.string.net_tip_type_2_error_1), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_2_error_1));
                        return;
                    }
                    if (!jsonObject.optString("success").equals("1")) {
                        showToast(getContext().getString(R.string.net_tip_type_2_error_2) + jsonObject.optString("msg"), 2);
                        BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_2_error_2) + jsonObject.optString("msg"));
                        return;
                    }
                    MApp.mSp.put(UserData.COMPANY, company);
                    MApp.mSp.put(UserData.DEVNO, devno);
                    MApp.mSp.put(UserData.DEVKIND, devkind);
                    showToast(getContext().getString(R.string.net_tip_type_2_success), 0);
                    BaseActivity.ttsSpeak(getContext().getResources().getString(R.string.net_tip_type_2_success));
                    restartApp();
                    break;
                default:
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
