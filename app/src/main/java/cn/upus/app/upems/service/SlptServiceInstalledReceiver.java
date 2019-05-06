package cn.upus.app.upems.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.LogUtils;

import java.util.Locale;
import java.util.Objects;

import cn.upus.app.upems.ui.WelComeActivity;


/**
 * 开机启动服务
 */
public class SlptServiceInstalledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            /* 应用开机自启动 */
            Intent intent_n = new Intent(context, WelComeActivity.class);
            intent_n.setAction("android.intent.action.MAIN");
            intent_n.addCategory("android.intent.category.LAUNCHER");
            intent_n.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent_n);
            //restartApp();
        }
    }

    /**
     * 重启应用
     */
    protected void restartApp() {
        LogUtils.e("重启应用");
        AppUtils.relaunchApp(true);
    }

}
