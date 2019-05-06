package cn.upus.app.upems.dao.util;

import android.text.TextUtils;

import java.util.List;
import java.util.Random;

import cn.upus.app.upems.dao.DbManager;
import cn.upus.app.upems.dao.User;
import cn.upus.app.upems.dao.UserDao;


/**
 * 用户操作
 */
public class UserUtil {

    /**
     * 获取一个 新的 4 位随机码
     *
     * @return
     */
    public static String getFingerId() {
        String str = "0123456789";
        String uuid;
        while (true) {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                char ch = str.charAt(new Random().nextInt(str.length()));
                buffer.append(ch);
            }
            if (!queryData(buffer.toString())) {
                uuid = buffer.toString();
                break;
            }
        }
        return uuid;
    }

    /**
     * 添加用户
     *
     * @param bean 插入的数据
     * @return 成功  失败
     */
    public static boolean insertData(User bean) {
        try {
            if (null != bean) {
                UserDao userfingDao = DbManager.getInstance().getSession().getUserDao();
                userfingDao.insert(bean);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 查询指纹号是否存在
     *
     * @param finger_id
     * @return
     */
    public static boolean queryData(String finger_id) {
        try {
            if (!TextUtils.isEmpty(finger_id)) {
                UserDao userfingDao = DbManager.getInstance().getSession().getUserDao();
                List<User> userfings = userfingDao.queryBuilder().where(UserDao.Properties.Finger_id.eq(finger_id)).list();
                if (userfings.size() > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据 ID 删除
     *
     * @param finger_id
     * @return
     */
    public static boolean deleteById(String finger_id) {
        try {
            if (!TextUtils.isEmpty(finger_id)) {
                UserDao userDao = DbManager.getInstance().getSession().getUserDao();
                List<User> users = userDao.queryBuilder().where(UserDao.Properties.Finger_id.eq(finger_id)).list();
                for (int i = 0; i < users.size(); i++) {
                    userDao.delete(users.get(i));
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除全部用户
     */
    public static void deleteAllData() {
        try {
            UserDao userDao = DbManager.getInstance().getSession().getUserDao();
            userDao.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
