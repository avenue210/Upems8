package cn.upus.app.upems.util.http;

import android.support.annotation.NonNull;

import io.reactivex.disposables.Disposable;

public interface HttpCallBack {

    void showDialog();

    void hideDialog();

    void addDisposable(@NonNull Disposable disposable);

    void onSuccess(String data);

    void onError(String data);

}
