package cn.upus.app.upems.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SpanUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.upus.app.upems.MApp;
import cn.upus.app.upems.R;
import cn.upus.app.upems.data.UserData;
import cn.upus.app.upems.util.http.HttpCallBack;
import cn.upus.app.upems.util.http.HttpUtil;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 弹出基类
 */
public abstract class BaseDialog extends Dialog implements HttpCallBack {

    private static final String TAG = BaseActivity.class.getSimpleName();

    protected Activity activity;
    protected Unbinder unbinder = null;
    protected Context context;
    protected HttpUtil mHttpUtil;
    protected CompositeDisposable compositeDisposable;

    public int WEB_TYPT = -1;
    public final int TYPE_0 = 0;
    public final int TYPE_1 = 1;
    public final int TYPE_2 = 2;
    public final int TYPE_3 = 3;
    public final int TYPE_4 = 4;
    public final int TYPE_5 = 5;
    public final int TYPE_6 = 6;
    public final int TYPE_7 = 7;
    public final int TYPE_8 = 8;
    public final int TYPE_9 = 9;
    public final int TYPE_10 = 10;
    public final int TYPE_11 = 11;
    public final int TYPE_12 = 12;
    public final int TYPE_13 = 13;
    public final int TYPE_14 = 14;
    public final int TYPE_15 = 15;

    protected abstract int initLayoutView();

    public BaseDialog(@NonNull Context context, @NonNull Activity activity) {
        super(context, R.style.Theme_Light_Dialog);
        this.activity = activity;
        Window window = getWindow();
        if (null != window) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            window.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        //布局位于状态栏下方
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        //隐藏导航栏
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                if (Build.VERSION.SDK_INT >= 19) {
                    uiOptions |= 0x00001000;
                } else {
                    uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                window.getDecorView().setSystemUiVisibility(uiOptions);
            });
        }
    }

    protected void hideBottomUIMenu() {
        Window window = getWindow();
        if (null != window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);//API19
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                );
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideBottomUIMenu();
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getWindow()).getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(initLayoutView());

        initWindow();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
        context = getContext();
        unbinder = ButterKnife.bind(this);
        mHttpUtil = new HttpUtil();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        resetToast();
        dispose();
        unbinder.unbind();
        BarUtils.setNavBarVisibility(activity, false);
        BarUtils.setStatusBarVisibility(activity, false);
        super.onStop();
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        try {
            //隐藏软键盘
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && getCurrentFocus() != null) {
                if (getCurrentFocus().getWindowToken() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 重启应用
     */
    protected void restartApp() {
        dismiss();
        activity.finish();
        AppUtils.relaunchApp(true);
    }

    /**
     * 设置窗口显示效果
     */
    protected void initWindow() {
        Window window = getWindow();
        if (null != window) {
            BarUtils.setNavBarVisibility(activity, false);
            BarUtils.setStatusBarVisibility(activity, false);
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams windowParams = window.getAttributes();
            //设置宽度顶满屏幕,无左右留白
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout(dm.widthPixels, window.getAttributes().height);
            windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            //全屏 / 3/1 屏
            if (!MApp.mSp.getBoolean(UserData.isHorizontal)) {
                windowParams.height = dm.heightPixels / 3;
            } else {
                windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            }
            windowParams.dimAmount = 0.0f;
            window.setAttributes(windowParams);
            setCanceledOnTouchOutside(false);

           /* window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(uiOptions);*/
        }
    }

    private void resetToast() {
        ToastUtils.setMsgColor(-0x1000001);
        ToastUtils.setBgColor(-0x1000001);
        ToastUtils.setBgResource(-1);
        ToastUtils.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 64);
    }

    /**
     * 吐司
     *
     * @param message 提示信息
     * @param type    0 成功 1 警告 2 失败
     */
    protected void showToast(String message, int type) {
        resetToast();
        int color;
        switch (type) {
            case 0:
                color = 0xFF1B5E20;
                break;
            case 1:
                color = 0xFFF57F17;
                break;
            case 2:
                color = Color.RED;
                break;
            default:
                color = 0xFF1B5E20;
                break;
        }
        ToastUtils.setBgColor(color);
        ToastUtils.setMsgColor(Color.WHITE);
        ToastUtils.showShort(new SpanUtils()
                .appendImage(R.drawable.ic_tip, SpanUtils.ALIGN_CENTER)
                .append("  ")
                .append(message)
                .setForegroundColor(Color.WHITE)
                .setFontSize(16, true)
                .create()
        );
    }

    /**
     * 将网络请求队列销毁
     */
    public void dispose() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }


    @Override
    public void showDialog() {

    }

    @Override
    public void hideDialog() {

    }

    @Override
    public void addDisposable(@NonNull Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    @Override
    public void onSuccess(String data) {

    }

    @Override
    public void onError(String data) {

    }
}
