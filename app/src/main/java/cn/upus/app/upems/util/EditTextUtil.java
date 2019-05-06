package cn.upus.app.upems.util;

import android.app.Activity;
import android.text.InputType;
import android.view.WindowManager;
import android.widget.EditText;

import java.lang.reflect.Method;

/**
 * Created by computer on 2017-04-22.
 */

public class EditTextUtil {

    /**
     * 设置软键盘 有光标 不弹出
     *
     * @param context
     * @param edit
     */
    public static void setEditText(Activity context, EditText edit) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            edit.setInputType(InputType.TYPE_NULL);
        } else {
            context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setSoftInputShownOnFocus;
                setSoftInputShownOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setSoftInputShownOnFocus.setAccessible(true);
                setSoftInputShownOnFocus.invoke(edit, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
