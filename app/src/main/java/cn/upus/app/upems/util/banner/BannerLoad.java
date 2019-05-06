package cn.upus.app.upems.util.banner;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.List;

/**
 * 轮播图片加载
 * Created by computer on 2018-02-10.
 */
public class BannerLoad<T> {

    public BannerLoad() {

    }

    /**
     * 初始化轮播
     */
    public void init(Banner banner, List<T> urls) {
        //设置banner样式
        banner.setBannerStyle(BannerConfig.NUM_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        //设置图片集合
        banner.setImages(urls);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Stack);
        //设置标题集合（当banner样式有显示title时）
        //banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(3000);
        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.CENTER);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    public void start(Banner banner) {
        //开始轮播
        banner.startAutoPlay();
    }

    public void stop(Banner banner) {
        //结束轮播
        banner.stopAutoPlay();
    }

    public void update(Banner banner, List<String> urls) {
        //更新图片
        banner.update(urls);
    }

    /**
     * 图片加载器
     */
    class GlideImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {

            Glide.with(context).load(path).into(imageView);

        }
    }

}
