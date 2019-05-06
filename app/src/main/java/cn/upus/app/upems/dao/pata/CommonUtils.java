package cn.upus.app.upems.dao.pata;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

public class CommonUtils {

    public static String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return Environment.getExternalStorageDirectory().toString();
        } else {
            return "";
        }
    }

    /**
     * 文件的路径名称
     *
     * @return
     */
    public static String getDBPath() {
        String sdCardPath = getSDPath();
        if (TextUtils.isEmpty(sdCardPath)) {
            return "";
        } else {
            return sdCardPath + File.separator + "GreenDao" + File.separator + "sqlite";
        }
    }
}
