package cn.upus.app.upems.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.bean.FixplusEntity;
import cn.upus.app.upems.bean.event_bus.OpenLockDataBean;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.gson.GsonUtil;
import cn.upus.app.upems.util.http.HttpCallBack;
import cn.upus.app.upems.util.http.HttpUtil;
import io.reactivex.disposables.Disposable;

/**
 * 定时任务
 */
@SuppressLint("Registered")
public class TimingTskService extends Service {

    private Map<String, FixplusEntity> fixplusEntityMap = new HashMap<>();
    public static List<FixplusEntity> fixplusEntities = new ArrayList<>();//定时开关任务
    public static List<FixplusEntity> fixplusEntitiesAD1 = new ArrayList<>();//半屏图片
    public static List<FixplusEntity> fixplusEntitiesAD2 = new ArrayList<>();//全屏图片

    private Thread mThread;
    private HttpUtil mHttpUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        mHttpUtil = new HttpUtil();
        mThread = new Thread(mRunnable, "TimingTsk");
        mThread.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (null != mThread) {
            mThread.interrupt();
            mThread = null;
        }
        /*定时任务*/
        if (null != thread) {
            thread.interrupt();
            thread = null;
        }
        super.onDestroy();
    }

    private Runnable mRunnable = () -> {
        while (true) {
            if (NetworkUtils.isConnected() && !TextUtils.isEmpty(MApp.mSp.getString(UserData.WEB_URL)) && !TextUtils.isEmpty(MApp.mSp.getString(UserData.DEVNO))) {
                fixplus();
            }
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public Map<String, FixplusEntity> traditionalWay(List<FixplusEntity> list) {
        @SuppressLint("UseSparseArrays")
        Map<String, FixplusEntity> map = new HashMap<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                FixplusEntity value = list.get(i);
                map.put(value.getStatetime(), value);
            }
        }
        return map;
    }

    /**
     * 根据设备号获取该设备所有零部件开关时间 广告轮播图片
     */
    private void fixplus() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("compno", MApp.mSp.getString(UserData.COMPNO));
            jsonObject.put("devno", MApp.mSp.getString(UserData.DEVNO));
            mHttpUtil.postJson(MApp.mSp.getString(UserData.WEB_URL) + "upus_APP/app/expressbox/fixplus", jsonObject.toString(), mHttpCallBack);

//            jsonObject.put("compno","80280001");
//            jsonObject.put("devno", "000301");
//            mHttpUtil.postJson( "http://119.3.214.51:8091/upus_APP/app/expressbox/fixplus", jsonObject.toString(), mHttpCallBack);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private HttpCallBack mHttpCallBack = new HttpCallBack() {
        @Override
        public void showDialog() {

        }

        @Override
        public void hideDialog() {

        }

        @Override
        public void addDisposable(@NonNull Disposable disposable) {

        }

        @Override
        public void onSuccess(String data) {
            if (TextUtils.isEmpty(data)) {
                return;
            }
            LogUtils.e("onSuccess", "定时任务：" + data);
            //{"msg":"查询成功!","data":[{"statetime":"00:00","pluskindna":"半屏广告","endtime":"23:59","stateno":"1","fileurl":"http://www.upus.cn:8091/Userfile/Otherpic/194201143-085033.jpg","plusna":null,"pluskind":"2","plusno":null,"boardno":null},
            // {"statetime":"11:59","pluskindna":"灯","endtime":"12:00","stateno":"1","fileurl":null,"plusna":null,"pluskind":"0","plusno":"2","boardno":"1"},
            // {"statetime":"12:00","pluskindna":"灯","endtime":"12:00","stateno":"0","fileurl":null,"plusna":null,"pluskind":"0","plusno":"2","boardno":"1"}],"success":"1"}
            try {
                JSONObject jsonObject = new JSONObject(data);
                if (TextUtils.isEmpty(jsonObject.optString("success"))) {
                    return;
                }
                if (!jsonObject.optString("success").equals("1")) {
                    return;
                }
                JSONArray jsonArray = jsonObject.optJSONArray("data");
                if (null == jsonArray || jsonArray.length() == 0) {
                    return;
                }
                fixplusEntityMap.clear();
                fixplusEntities.clear();
                fixplusEntitiesAD1.clear();
                fixplusEntitiesAD2.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.opt(i);
                    FixplusEntity fixplusEntity = (FixplusEntity) GsonUtil.stringToObject(object.toString(), FixplusEntity.class);
                    switch (fixplusEntity.getPluskind()) {
                        case "2":
                            if (!TextUtils.isEmpty(fixplusEntity.getFileurl())) {
                                LogUtils.e("添加 半屏广告 定时任务");
                                fixplusEntitiesAD1.add(fixplusEntity);
                            }
                            break;
                        case "3":
                            if (!TextUtils.isEmpty(fixplusEntity.getFileurl())) {
                                LogUtils.e("添加 全屏广告 定时任务");
                                fixplusEntitiesAD2.add(fixplusEntity);
                            }
                            break;
                        default:
                            LogUtils.e("添加 设备开关 定时任务");
                            fixplusEntities.add(fixplusEntity);
                            break;
                    }
                }
                fixplusEntityMap = traditionalWay(fixplusEntities);
                if (fixplusEntityMap.size() == 0) {
                    return;
                }
                if (null != thread) {
                    thread.interrupt();
                    thread = null;
                }
                thread = new Thread(runnable, "timerTask");
                thread.start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String data) {

        }
    };

    private Thread thread;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                for (String key : fixplusEntityMap.keySet()) {
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("date", key);
                            message.setData(bundle);
                            handler.sendMessage(message);
                            cancel();
                        }
                    };
                    String date = DateTimeUtil.getCurDateStr("yyyy-MM-dd");
                    date = date + " " + key;
                    LogUtils.e("date: " + date);
                    Timer timer = new Timer(true);
                    timer.schedule(timerTask, DateTimeUtil.strToDateLong1(date));
                    //timer.schedule(timerTask, DateTimeUtil.strToDateLong(key));
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        LogUtils.e("Thread.sleep(200)：" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                LogUtils.e("定时任务异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String date = bundle.getString("date");
            LogUtils.e("执行了:" + date);
            if (fixplusEntities.size() == 0 || TextUtils.isEmpty(date)) {
                LogUtils.e("没有获取到任务");
                return;
            }
            for (int i = 0; i < fixplusEntities.size(); i++) {
                if (fixplusEntities.get(i).getStatetime().equals(date)) {

                    String stateno = fixplusEntities.get(i).getStateno();//开关 0 关 1 开
                    String plusno = fixplusEntities.get(i).getPlusno();//锁号
                    String boardno = fixplusEntities.get(i).getBoardno();//板号
                    if (DateTimeUtil.compareNowTime(fixplusEntities.get(i).getStatetime())) {
                        if (TextUtils.isEmpty(stateno)) {
                            return;
                        }
                        if (stateno.equals("0")) {
                            //关灯
                            EventBus.getDefault().post(new OpenLockDataBean(0, 3, Integer.valueOf(boardno), Integer.valueOf(plusno), null));
                        } else if (stateno.equals("1")) {
                            //开灯
                            EventBus.getDefault().post(new OpenLockDataBean(0, 2, Integer.valueOf(boardno), Integer.valueOf(plusno), null));
                        }
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    };

}
