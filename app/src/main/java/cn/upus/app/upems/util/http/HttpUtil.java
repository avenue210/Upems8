package cn.upus.app.upems.util.http;

import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;

import java.io.File;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HttpUtil {


    public HttpUtil() {

    }

    /**
     * POST JSON 格式请求
     *
     * @param url
     * @param json
     * @param httpCallBack
     */
    public void postJson(final String url, final String json, final HttpCallBack httpCallBack) {
        httpCallBack.showDialog();
        OkGo.<String>post(url)
                .upJson(json)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<>())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {

                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                httpCallBack.addDisposable(d);
            }

            @Override
            public void onNext(Response<String> response) {
                httpCallBack.onSuccess(response.body());
                httpCallBack.hideDialog();
            }

            @Override
            public void onError(Throwable e) {
                httpCallBack.onError(e.getMessage());
                httpCallBack.hideDialog();
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                httpCallBack.hideDialog();
            }
        });
    }

    /**
     * POST 表单请求
     *
     * @param url
     * @param params
     * @param httpCallBack
     */
    public void postParams(final String url, final Map<String, String> params, final HttpCallBack httpCallBack) {
        httpCallBack.showDialog();
        OkGo.<String>post(url)
                .params(params)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<>())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {

                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                httpCallBack.addDisposable(d);
            }

            @Override
            public void onNext(Response<String> response) {
                httpCallBack.onSuccess(response.body());
                httpCallBack.hideDialog();
            }

            @Override
            public void onError(Throwable e) {
                httpCallBack.onError(e.getMessage());
                httpCallBack.hideDialog();
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                httpCallBack.hideDialog();
            }
        });
    }

    /**
     * 下载
     *
     * @param url                  地址
     * @param filePath             下载保存路径
     * @param fileName             下载文件保存名称
     * @param httpDownloadCallBack 下载回调
     */
    public void download(final String url, final String filePath, final String fileName, final HttpDownloadCallBack httpDownloadCallBack) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        httpDownloadCallBack.showDialog();
        Observable.create((ObservableOnSubscribe<Progress>) e -> OkGo.<File>get(url).execute(new FileCallback(filePath, fileName) {
            @Override
            public void onSuccess(Response<File> response) {
                e.onComplete();
            }

            @Override
            public void onError(Response<File> response) {
                try {
                    e.onError(response.getException());
                } catch (Exception e1) {
                    e1.printStackTrace();
                    e.onError(new Error("网络错误"));
                }
            }

            @Override
            public void downloadProgress(Progress progress) {
                e.onNext(progress);
            }
        })).doOnSubscribe(disposable -> {

        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Progress>() {
            @Override
            public void onSubscribe(Disposable d) {
                httpDownloadCallBack.addDisposable(d);
            }

            @Override
            public void onNext(Progress progress) {
                httpDownloadCallBack.onNext(progress);
                        /*String downloadLength = Formatter.formatFileSize(getApplicationContext(), progress.currentSize);
                        String totalLength = Formatter.formatFileSize(getApplicationContext(), progress.totalSize);
                        tvDownloadSize.setText(downloadLength + "/" + totalLength);
                        String speed = Formatter.formatFileSize(getApplicationContext(), progress.speed);
                        tvNetSpeed.setText(String.format("%s/s", speed));
                        tvProgress.setText(numberFormat.format(progress.fraction));
                        pbProgress.setMax(10000);
                        pbProgress.setProgress((int) (progress.fraction * 10000));*/
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                httpDownloadCallBack.onError(e.getMessage());
                httpDownloadCallBack.hideDialog();
            }

            @Override
            public void onComplete() {
                httpDownloadCallBack.onComplete();
                httpDownloadCallBack.hideDialog();
            }
        });
    }

    /**
     * 上传文件
     *
     * @param url
     * @param files
     */
    public void updata(final String url, final List<File> files, final HttpCallBack httpCallBack) {
        httpCallBack.showDialog();
        OkGo.<String>post(url)
                //.params(params)
                .addFileParams("file", files)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<>())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {

                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                httpCallBack.addDisposable(d);
            }

            @Override
            public void onNext(Response<String> response) {
                httpCallBack.onSuccess(response.body());
                httpCallBack.hideDialog();
            }

            @Override
            public void onError(Throwable e) {
                httpCallBack.onError(e.getMessage());
                httpCallBack.hideDialog();
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                httpCallBack.hideDialog();
            }
        });
    }

    /**
     * 简单GET请求
     *
     * @param url
     */
    public void get(final String url, final HttpCallBack httpCallBack) {
        httpCallBack.showDialog();
        OkGo.<String>get(url)
                .converter(new StringConvert())
                .adapt(new ObservableResponse<>())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {

                }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                httpCallBack.addDisposable(d);
            }

            @Override
            public void onNext(Response<String> response) {
                httpCallBack.onSuccess(response.body());
                httpCallBack.hideDialog();
            }

            @Override
            public void onError(Throwable e) {
                httpCallBack.onError(e.getMessage());
                httpCallBack.hideDialog();
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                httpCallBack.hideDialog();
            }
        });
    }

    /**
     * 拼装 GET 请求时的参数
     *
     * @param beans
     * @return
     */
    public static String getKeyValueString(List<KeyValueBean> beans) {
        StringBuilder sb = new StringBuilder();
        try {
            int i = 0;
            for (KeyValueBean item : beans) {
                if (i > 0) {
                    sb.append("&");
                }
                sb.append(item.getKey());
                sb.append('=');
                //sb.append(URLEncoder.encode(item.getValue(), "utf-8"));
                sb.append(item.getValue());
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * get 参数拼接
     */
    public static class KeyValueBean {

        private String key;
        private String value;

        public KeyValueBean() {
        }

        public KeyValueBean(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "KeyValueEntity{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
}
