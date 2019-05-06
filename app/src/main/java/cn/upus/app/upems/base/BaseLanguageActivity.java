package cn.upus.app.upems.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import cn.upus.app.upems.R;
import cn.upus.app.upems.util.language.AppLanguageUtils;


@SuppressLint("Registered")
public class BaseLanguageActivity extends AppCompatActivity {

    public static final int CHANGE_LANGUAGE_REQUEST_CODE = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(newBase, newBase.getString(R.string.app_language_pref_key)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_LANGUAGE_REQUEST_CODE) {
            recreate();
        }
    }

}
