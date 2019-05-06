package cn.upus.app.upems.util.glide;

import android.annotation.SuppressLint;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.annotation.GlideExtension;
import com.bumptech.glide.annotation.GlideOption;
import com.bumptech.glide.annotation.GlideType;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestOptions;

/**
 * Created by computer on 2018-02-10.
 */
@GlideExtension
public class CustomGlideExtension {

    /**
     * 缩略图的最小尺寸，单位：px
     */
    private static final int MINI_THUMB_SIZE = 100;

    /**
     * 将构造方法设为私有，作为工具类使用
     */
    private CustomGlideExtension() {
    }

    /**
     * 1.自己新增的方法的第一个参数必须是RequestOptions options
     * 2.方法必须是静态的
     *
     * @param options
     */
    @SuppressLint("CheckResult")
    @GlideOption
    public static void miniThumb(RequestOptions options) {
        options.fitCenter().override(MINI_THUMB_SIZE);
    }

    private static final RequestOptions DECODE_TYPE_GIF = GlideOptions.decodeTypeOf(GifDrawable.class).lock();

    /**
     * 加载GIF图片
     *
     * @param requestBuilder
     */
    @SuppressLint("CheckResult")
    @GlideType(GifDrawable.class)
    public static void asGIF(RequestBuilder<GifDrawable> requestBuilder) {
        requestBuilder
                .transition(new DrawableTransitionOptions())
                .apply(DECODE_TYPE_GIF);
    }
}
