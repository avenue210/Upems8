package cn.upus.app.upems.util.http;

import android.support.annotation.NonNull;

import com.lzy.okgo.model.Progress;

import io.reactivex.disposables.Disposable;

public interface HttpDownloadCallBack {

    void showDialog();

    void hideDialog();

    void addDisposable(@NonNull Disposable disposable);

    void onNext(@NonNull Progress progress);

    void onError(String error);

    void onComplete();

}
