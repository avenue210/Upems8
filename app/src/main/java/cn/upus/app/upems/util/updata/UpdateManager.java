package cn.upus.app.upems.util.updata;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import cn.upus.app.upems.R;

/**
 * 检查更新
 */
public class UpdateManager {

    private final String TAG = "UpdateManager";

    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;
    private TextView mUpdataTextView, tvBack, tvUpdata;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 6.0 以上必须添加！！
     * 否则回报 Caused by: android.system.ErrnoException: open failed: EACCES (Permission denied)
     * 这个错误
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //正在下载
                case DOWNLOAD:
                    //设置进度条位置
                    mProgress.setProgress(progress);
                    mUpdataTextView.setText(mContext.getResources().getString(R.string.downloads) + " " + progress + "/100");
                    break;
                case DOWNLOAD_FINISH:
                    //安装文件
                    installApk();
                    break;
                default:
                    break;
            }
        }
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {

        if (isUpdate()) {
            // 显示提示对话框
            showNoticeDialog();
            //Toast.makeText(mContext, "自动更新启动", Toast.LENGTH_LONG).show();
            LogUtils.e(TAG, "检测到新的版本");
        } else {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.its_the_latest_version), Toast.LENGTH_SHORT).show();
            LogUtils.e(TAG, "已经是最新版本:" + getLocalVersionName(mContext));
        }
    }

    /**
     * 检查软件是否有更新版本
     *
     * @return
     */
    public boolean isUpdate() {

        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);

        try {
            List<UpdateInfo> date = ParseXmlService.getDate();
            for (UpdateInfo updateInfo : date) {
                int serviceCode = Integer.valueOf(updateInfo.getVersion());
                String url = updateInfo.getUrl();
                LogUtils.e(TAG, "版本:" + String.valueOf(serviceCode) + "   " + versionCode + "   " + url);
                // 版本判断
                if (serviceCode > versionCode) {
                    LogUtils.e(TAG, "版本:" + String.valueOf(serviceCode) + "   " + versionCode + "   " + url);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "版本比较出错了" + e.toString());
        }

        return false;
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    @SuppressLint("SetTextI18n")
    private void showNoticeDialog() {
        String content = null;
        try {
            List<UpdateInfo> date = ParseXmlService.getDate();
            assert date != null;
            for (UpdateInfo updateInfo : date) {
                content = updateInfo.getContent();
                // 版本判断
                LogUtils.e(TAG, "更新内容:" + content);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "更新内容获取失败: " + e.toString());
        }
        // 构造对话框
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.ok_notifyTitle));
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_softupdate_text, null);
        mUpdataTextView = (TextView) v.findViewById(R.id.update_text);
        if (null != content) {
            mUpdataTextView.setText(content);
        } else {
            mUpdataTextView.setText("");
        }
        tvBack = (TextView) v.findViewById(R.id.tv1);
        tvUpdata = (TextView) v.findViewById(R.id.tv2);
        builder.setView(v);

        final Dialog noticeDialog = builder.create();
        Objects.requireNonNull(noticeDialog.getWindow()).setGravity(Gravity.CENTER);
        noticeDialog.show();
        noticeDialog.setCanceledOnTouchOutside(false);

        tvBack.setText(mContext.getResources().getString(R.string.dialog_cancel));
        tvUpdata.setPadding(12, 0, 0, 0);
        tvUpdata.setTextColor(Color.RED);
        tvUpdata.setText(mContext.getResources().getString(R.string.dialog_item_click_update));

        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticeDialog.dismiss();
            }
        });

        tvUpdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noticeDialog.dismiss();
                showDownloadDialog();
            }
        });
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        // 构造软件下载对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        //更新时候显示的文字
        builder.setTitle(mContext.getResources().getString(R.string.downloads));
        //更新内容
        builder.setMessage("");
        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.dialog_softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        mUpdataTextView = (TextView) v.findViewById(R.id.update_text);
        //tvBack = (TextView) v.findViewById(R.id.tv1);
        builder.setView(v);

        mDownloadDialog = builder.create();
        mDownloadDialog.show();
        Objects.requireNonNull(mDownloadDialog.getWindow()).setGravity(Gravity.CENTER);
        mDownloadDialog.setCanceledOnTouchOutside(false);

        /*tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDownloadDialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });*/
        //下载文件
        downloadApk();
    }

    /**
     * 下载apk文件
     */
    private void downloadApk() {
        // 启动新线程下载软件
        new downloadApkThread().start();
    }

    /**
     * 下载文件线程
     */
    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = sdpath + "download";

                    List<UpdateInfo> date = null;
                    String url1 = null;
                    String name = null;
                    try {
                        date = ParseXmlService.getDate();
                        LogUtils.e(TAG, date.toString());
                        assert date != null;
                        for (UpdateInfo updateInfo : date) {
                            url1 = updateInfo.getUrl();
                            name = updateInfo.getName();
                        }
                    } catch (XmlPullParserException e) {
                        LogUtils.e(TAG, "获取地址失败" + e.toString());
                        e.printStackTrace();
                    }
                    URL url = new URL(url1);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }

                    File apkFile = new File(mSavePath, name);

                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOAD);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }

    /**
     * 安装APK文件
     */
    private void installApk() {

        List<UpdateInfo> date = null;
        String name = null;
        try {
            date = ParseXmlService.getDate();
            for (UpdateInfo updateInfo : date) {
                name = updateInfo.getName();
            }
        } catch (IOException | XmlPullParserException e) {
            LogUtils.e(TAG, "获取地址失败 " + e.toString());
            e.printStackTrace();
        }

        File apkfile = new File(mSavePath, name);
        if (!apkfile.exists()) {
            return;
        }

        installApk(apkfile);

        /*if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(mContext, "cn.upus.palmsystem.fileprovider", apkfile);//在AndroidManifest中的android:authorities值
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(install);
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(install);
        }*/

        // 通过Intent安装APK文件
        /*Intent i = new Intent(Intent.ACTION_VIEW);
        //2016-09-26 如果没有这一步，最后安装好了，点打开，是不会打开新版本应用的。
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
        //2016-09-26 如果没有这一句最后不会提示完成、打开
        android.os.Process.killProcess(android.os.Process.myPid());*/

    }

    /**
     * 安装 apk 文件
     *
     * @param apkFile
     */
    public void installApk(File apkFile) {
        Intent installApkIntent = new Intent();
        installApkIntent.setAction(Intent.ACTION_VIEW);
        installApkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            installApkIntent.setDataAndType(FileProvider.getUriForFile(mContext, "cn.upus.palmsystem.fileprovider", apkFile), "application/vnd.android.package-archive");
            installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            installApkIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }

        if (mContext.getPackageManager().queryIntentActivities(installApkIntent, 0).size() > 0) {
            mContext.startActivity(installApkIntent);
        }
    }
}