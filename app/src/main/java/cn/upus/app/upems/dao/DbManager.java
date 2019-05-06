package cn.upus.app.upems.dao;

import android.util.Log;

import cn.upus.app.upems.dao.pata.GreenDaoContext;
import cn.upus.app.upems.dao.pata.MySQLiteOpenHelper;


public class DbManager {

    private static DbManager mInstance;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    //private DaoMaster.DevOpenHelper devOpenHelper;
    private MySQLiteOpenHelper devOpenHelper;

    public DbManager() {
        //创建一个数据库
        //devOpenHelper = new DaoMaster.DevOpenHelper(MyApp.getContext(), "zhiwen", null);
        //devOpenHelper = new DaoMaster.DevOpenHelper(new GreenDaoContext(), "zhiwen", null);
        devOpenHelper = new MySQLiteOpenHelper(new GreenDaoContext(), "UP_ZHI_WEN", null);
        mDaoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        mDaoSession = mDaoMaster.newSession();
        Log.e("DbManager", "初始化");
    }

    public static DbManager getInstance() {
        if (mInstance == null) {
            mInstance = new DbManager();
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }

    /**
     * 关闭数据连接
     */
    public void close() {
        if (devOpenHelper != null) {
            devOpenHelper.close();
        }
    }
}
