package cn.upus.app.upems.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.blankj.utilcode.util.BarUtils;
import com.youth.banner.Banner;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.TimingTskService;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.banner.BannerLoad;

/**
 * 全屏广告
 */
@SuppressLint("Registered")
public class ADActivity extends AppCompatActivity {

    private Banner banner;

    private BannerLoad mBannerLoad;//轮播
    private Thread mADThread;//广告定时器

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
        BarUtils.setNavBarVisibility(this, false);
        BarUtils.setStatusBarVisibility(this, false);
        banner = findViewById(R.id.banner);
        initBanner();
    }

    private void initBanner() {
        mBannerLoad = new BannerLoad();
        banner.setFocusable(true);
        banner.setFocusableInTouchMode(true);
        banner.requestFocus();
        List<Object> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(MApp.mSp.getString(UserData.fixplusEntitiesAD2))) {
            try {
                JSONArray jsonArray = new JSONArray(MApp.mSp.getString(UserData.fixplusEntitiesAD2));
                if (jsonArray.length() == 0) {
                    urls.add(R.drawable.ic_ld1);
                    urls.add(R.drawable.ic_ld2);
                    urls.add(R.drawable.ic_ld3);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    urls.add(String.valueOf(jsonArray.get(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            urls.add(R.drawable.ic_ld1);
            urls.add(R.drawable.ic_ld2);
            urls.add(R.drawable.ic_ld3);
        }
        mBannerLoad.init(banner, urls);
        //启动广告定时器
        mADThread = new Thread(mADRunnable, "ADThread");
        mADThread.start();
    }

    /**
     * 定时广告
     */
    private Runnable mADRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                mADHandler.sendEmptyMessage(0);
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 定时广告
     */
    @SuppressLint("HandlerLeak")
    private Handler mADHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (null == TimingTskService.fixplusEntitiesAD2 || TimingTskService.fixplusEntitiesAD2.size() == 0) {
                    return;
                }
                List<Object> urls = new ArrayList<>();
                try {
                    JSONArray urlsArray = new JSONArray();
                    for (int i = 0; i < TimingTskService.fixplusEntitiesAD2.size(); i++) {
                        if (!TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD2.get(i).getStatetime())//开始时间
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD2.get(i).getEndtime())//结束时间
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD2.get(i).getStateno())//开关 0 关 1 开
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD2.get(i).getFileurl())//图片地址
                                && !TextUtils.isEmpty(TimingTskService.fixplusEntitiesAD2.get(i).getPluskind())//子设备号
                        ) {
                            Date nowTime = DateTimeUtil.strToDateLong(DateTimeUtil.getCurDateStr("HH:mm"));
                            Date startTime = DateTimeUtil.strToDateLong(TimingTskService.fixplusEntitiesAD2.get(i).getStatetime());
                            Date endTime = DateTimeUtil.strToDateLong(TimingTskService.fixplusEntitiesAD2.get(i).getEndtime());
                            if (DateTimeUtil.isEffectiveDate(nowTime, startTime, endTime)
                                    && TimingTskService.fixplusEntitiesAD2.get(i).getStateno().equals("1")
                                    && TimingTskService.fixplusEntitiesAD2.get(i).getPluskind().equals("3")) {
                                urls.add(TimingTskService.fixplusEntitiesAD2.get(i).getFileurl());
                                urlsArray.put(TimingTskService.fixplusEntitiesAD2.get(i).getFileurl());
                            }
                        }
                    }
                    MApp.mSp.put(UserData.fixplusEntitiesAD2, urlsArray.toString());
                    if (urls.size() > 0) {
                        mBannerLoad.stop(banner);
                        mBannerLoad.update(banner, urls);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mBannerLoad.start(banner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBannerLoad.stop(banner);
    }

    @Override
    protected void onDestroy() {
        if (null != mADThread) {
            mADThread.interrupt();
            mADThread = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        this.finish();
        return super.dispatchTouchEvent(ev);
    }
}
