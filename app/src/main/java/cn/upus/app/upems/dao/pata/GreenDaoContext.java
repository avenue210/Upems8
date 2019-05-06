package cn.upus.app.upems.dao.pata;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.IOException;

import cn.upus.app.upems.MApp;


/**
 * 描述：主要用于基于GreenDao框架自定义数据库路径
 */
public class GreenDaoContext extends ContextWrapper {

    private String currentUserId = "greendao";//一般用来针对一个用户一个数据库，以免数据混乱问题
    private Context mContext;

    public GreenDaoContext() {
        super(MApp.getApplication);
        this.mContext = MApp.getApplication;
    }

    /**
     * 获得数据库路径，如果不存在，则创建对象
     *
     * @param name
     * @return
     */
    @Override
    public File getDatabasePath(String name) {
        String dbDir = CommonUtils.getDBPath();
        LogUtils.d("GreenDaoContext", "数据库地址：" + dbDir);
        if (TextUtils.isEmpty(dbDir)) {
            Log.e("SD卡管理：", "SD卡不存在，请加载SD卡");
            return null;
        }
        File baseFile = new File(dbDir);
        // 目录不存在则自动创建目录
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(baseFile.getPath());
        buffer.append(File.separator);
        buffer.append(currentUserId);
        dbDir = buffer.toString();// 数据库所在目录
        buffer.append(File.separator);
        buffer.append(name);
        String dbPath = buffer.toString();// 数据库路径
        // 判断目录是否存在，不存在则创建该目录
        File dirFile = new File(dbDir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        // 数据库文件是否创建成功
        boolean isFileCreateSuccess = false;
        // 判断文件是否存在，不存在则创建该文件
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            try {
                isFileCreateSuccess = dbFile.createNewFile();// 创建文件
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            isFileCreateSuccess = true;
        // 返回数据库文件对象
        if (isFileCreateSuccess) {
            return dbFile;
        } else {
            return super.getDatabasePath(name);
        }
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
    }

}
