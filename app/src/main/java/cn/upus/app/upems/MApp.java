package cn.upus.app.upems;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import cn.upus.app.upems.bean.SystemInfoEntity;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.service.OpenLockService;
import cn.upus.app.upems.service.SocketService;
import cn.upus.app.upems.service.TimingTskService;
import cn.upus.app.upems.ui.WelComeActivity;
import cn.upus.app.upems.util.CrashHandler;
import cn.upus.app.upems.util.DESUtil;
import cn.upus.app.upems.util.DateTimeUtil;
import cn.upus.app.upems.util.IO;
import cn.upus.app.upems.util.language.AppLanguageUtils;
import cn.upus.app.upems.util.language.ConstantLanguages;
import cn.upus.app.upems.util.serialport_usb485.DeviceUtils;
import cn.upus.app.upems.util.serialport_usb485.serialport.SerialportUtil;
import cn.upus.app.upems.util.serialport_usb485.usb485.USB485Util;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ServiceUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MApp extends Application {

    public static MApp getApplication;
    public static SPUtils mSp;
    //public static SharedPreUtil mSp;

    public static SystemInfoEntity systemInfoEntity;//设备信息

    public static USB485Util mUsb485Util;
    public static SerialportUtil mSerialportUtil;
    public static SerialportUtil ZHI_WEN_COM;

    private CrashHandler mCrashHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mCrashHandler = CrashHandler.getInstance();
        mCrashHandler.init(getApplicationContext(), WelComeActivity.class);

        getApplication = this;
        mSp = SPUtils.getInstance("UPEMS");
        LogUtils.getConfig().setLogSwitch(true);
        if (null == mUsb485Util) {
            mUsb485Util = new USB485Util();
        }
        if (null == mSerialportUtil) {
            mSerialportUtil = new SerialportUtil();
        }
        if (null == ZHI_WEN_COM) {
            ZHI_WEN_COM = new SerialportUtil();
        }
        onLanguageChange();
        //setlanguage();
        initSystemID();
        initOkGo();
        if (!ServiceUtils.isServiceRunning(OpenLockService.class)) {
            startService(new Intent(this, OpenLockService.class));
        }
        if (!ServiceUtils.isServiceRunning(SocketService.class)) {
            startService(new Intent(this, SocketService.class));
        }
        if (!ServiceUtils.isServiceRunning(TimingTskService.class)) {
            startService(new Intent(this, TimingTskService.class));
        }
    }

    public void setlanguage() {
        //获取系统当前的语言
        String able = getResources().getConfiguration().locale.getLanguage();
        LogUtils.e("语言 " + able);
        Resources resources = getResources();//获得res资源对象
        Configuration config = resources.getConfiguration();//获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();
        //根据系统语言进行设置
        /*if (able.equals("zh")) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);
        } else if (able.equals("en")) {
            config.locale = Locale.US;
            resources.updateConfiguration(config, dm);
        }*/
        if (able.equals("en")) {
            config.locale = Locale.US;
            resources.updateConfiguration(config, dm);
        } else {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            resources.updateConfiguration(config, dm);
        }
    }

    /**
     * 保存硬件设备ID
     */
    private void initSystemID() {
        new Thread(() -> {
            String devid = DeviceUtils.getBTMACAddress(this);
            if (TextUtils.isEmpty(devid)) {
                //DeviceUtils deviceUtils = new DeviceUtils(this);
                //devid = deviceUtils.getUniqueID(this);
                IO io = new IO();
                File file = new File(io.getFilePath() + io.fileName);
                String uuid = io.getFileContent(file);
                if (TextUtils.isEmpty(uuid)) {
                    uuid = DateTimeUtil.getCurDateStr("yyyyMMddHHmmssSSSSSS");//yyyy-MM-dd HH:mm:ss.SSSSSS
                    io.writeData(uuid);
                }
                devid = uuid;
            } else if (devid.equals("22:22:8B:1C:BE:6E")) {
                devid = DeviceUtils.getSerialNumber().replaceAll(":", "");
            }
            LogUtils.e(devid);
            mSp.put(UserData.DEVID, DESUtil.encrypt(devid, "liscjw27"));
        }).start();
    }

    /**
     * 初始化网络请求
     */
    private void initOkGo() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("UPEMS");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   //全局的连接超时时间
        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));            //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失
        //信任所有证书
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        OkGo.getInstance().init(this)                                               //必须调用初始化
                .setOkHttpClient(builder.build())                                   //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)                                   //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)                       //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                                                  //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        assert am != null;
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        for (ActivityManager.RunningTaskInfo taskInfo : list) {
            if (taskInfo.topActivity.getShortClassName().contains(className)) { // 说明它已经启动了
                return true;
            }
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(base, getAppLanguage(base)));
    }

    /**
     * Handling Configuration Changes
     *
     * @param newConfig newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onLanguageChange();
    }

    private void onLanguageChange() {
        AppLanguageUtils.changeAppLanguage(this, AppLanguageUtils.getSupportLanguage(getAppLanguage(this)));
    }

    private String getAppLanguage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.app_language_pref_key), ConstantLanguages.ENGLISH);
    }

}
