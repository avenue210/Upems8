package cn.upus.app.upems.util.glide;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;

import java.security.MessageDigest;

/**
 * Glide图片加载器
 * Created by computer on 2018-03-01.
 */

public class LoadImageUtil {

    private Context context;

    public LoadImageUtil(Context context) {
        this.context = context;
    }

    /**
     * 基本图片加载
     *
     * @param url
     * @param imageView
     */
    public void load(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    /**
     * 图片加载
     *
     * @param url
     * @param placeholder 加载占位
     * @param error       加载失败
     * @param imageView
     */
    public void load(String url, int placeholder, int error, ImageView imageView) {
        GlideApp.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .into(imageView);
    }

    /**
     * 加载为圆形图片
     *
     * @param url
     * @param imageView
     */
    public void loadCircle(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    /**
     * 加载为圆形图片
     *
     * @param url
     * @param placeholder 加载占位
     * @param error       加载失败
     * @param imageView
     */
    public void loadCircle(String url, int placeholder, int error, ImageView imageView) {
        GlideApp.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    /**
     * 高斯模糊图片
     *
     * @param url
     * @param imageView
     */
    public void loadGaussianBlur(String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions.bitmapTransform(new GlideBlurformation(context)))
                .into(imageView);
    }

    /**
     * 高斯模糊图片
     *
     * @param url
     * @param placeholder 加载占位
     * @param error       加载失败
     * @param imageView
     */
    public void loadGaussianBlur(String url, int placeholder, int error, ImageView imageView) {
        GlideApp.with(context)
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .apply(RequestOptions.bitmapTransform(new GlideBlurformation(context)))
                .into(imageView);
    }

    /**
     * 高斯模糊
     */
    class GlideBlurformation extends BitmapTransformation {
        private Context context;

        public GlideBlurformation(Context context) {
            this.context = context;
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            return BlurBitmapUtil.instance().blurBitmap(context, toTransform, 20, outWidth, outHeight);
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
        }
    }

    /**
     * 高斯模糊
     */
    static class BlurBitmapUtil {
        private static BlurBitmapUtil sInstance;

        private BlurBitmapUtil() {
        }

        public static BlurBitmapUtil instance() {
            if (sInstance == null) {
                synchronized (BlurBitmapUtil.class) {
                    if (sInstance == null) {
                        sInstance = new BlurBitmapUtil();
                    }
                }
            }
            return sInstance;
        }

        /**
         * @param context   上下文对象
         * @param image     需要模糊的图片
         * @param outWidth  输入出的宽度
         * @param outHeight 输出的高度
         * @return 模糊处理后的Bitmap
         */
        @SuppressLint("ObsoleteSdkInt")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public Bitmap blurBitmap(Context context, Bitmap image, float blurRadius, int outWidth, int outHeight) {
            // 将缩小后的图片做为预渲染的图片
            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
            // 创建一张渲染后的输出图片
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
            // 创建RenderScript内核对象
            RenderScript rs = RenderScript.create(context);
            // 创建一个模糊效果的RenderScript的工具对象
            ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
            // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            // 设置渲染的模糊程度, 25f是最大模糊度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                blurScript.setRadius(blurRadius);
            }
            // 设置blurScript对象的输入内存
            blurScript.setInput(tmpIn);
            // 将输出数据保存到输出内存中
            blurScript.forEach(tmpOut);
            // 将数据填充到Allocation中
            tmpOut.copyTo(outputBitmap);
            return outputBitmap;
        }
    }

}
