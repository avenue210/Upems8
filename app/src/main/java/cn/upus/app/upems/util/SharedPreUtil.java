package cn.upus.app.upems.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * 使用SharedPreferences存储数据
 */
public class SharedPreUtil {

    Context context;

    public SharedPreUtil(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(SharedPreUtil.class.getSimpleName(), 0);
    }

    private SharedPreferences.Editor getEdit() {
        return getSharedPreferences().edit();
    }

    public void put(String key, Object object) {
        SharedPreferences.Editor editor = getEdit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else if (object instanceof String[]) {
            StringBuilder datas = new StringBuilder();
            String[] data = (String[]) object;

            for (int i = 0; i < data.length; ++i) {
                if (i != 0) {
                    datas.append(":");
                }

                datas.append(data[i]);
            }
            editor.putString(key, datas.toString());
        }

        editor.commit();
    }

    public String getString(String key, String defaultObject) {
        return getSharedPreferences().getString(key, defaultObject);
    }

    public String getString(String key) {
        return getSharedPreferences().getString(key, "");
    }

    public int getInt(String key) {
        return getSharedPreferences().getInt(key, 0);
    }

    public boolean getBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    public float getFloat(String key) {
        return getSharedPreferences().getFloat(key, 0f);
    }

    public long getLong(String key) {
        return getSharedPreferences().getLong(key, 0l);
    }

    public String[] getStringArray(String key) {
        return getString(key).split(":");
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = getEdit();
        editor.remove(key);
        editor.commit();
    }

    public void clear() {
        SharedPreferences.Editor editor = getEdit();
        editor.clear();
        editor.commit();
    }

    public boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    public Map<String, ?> getAll() {
        return getSharedPreferences().getAll();
    }
}
