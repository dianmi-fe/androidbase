package com.arnold.common.architecture.integration.imageloader.glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.arnold.common.architecture.di.component.AppComponent;
import com.arnold.common.architecture.utils.ArnoldUtilsKt;
import com.arnold.common.repository.utils.DataHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.AppGlideModule;

import java.io.File;

/**
 * ================================================
 * {@link AppGlideModule} 的默认实现类
 * 用于配置缓存文件夹,切换图片请求框架等操作
 * <p>
 * Created by JessYan on 16/4/15.
 * <a href="mailto:jess.yan.effort@gmail.com">Contact me</a>
 * <a href="https://github.com/JessYanCoding">Follow me</a>
 * ================================================
 */
//@Excludes(value = OkHttpLibraryGlideModule.class)
//@GlideModule(glideName = "GlideArms")
public class GlideConfiguration extends AppGlideModule {
    public static final int IMAGE_DISK_CACHE_MAX_SIZE = 100 * 1024 * 1024;//图片缓存文件最大值为100Mb

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        final AppComponent appComponent = ArnoldUtilsKt.obtainAppComponentFromContext(context);
        builder.setDiskCache(() -> {
            // Careful: the external cache directory doesn't enforce permissions
            return DiskLruCacheWrapper.create(DataHelper.INSTANCE.makeDirs(new File(appComponent.cacheFile(), "Glide")), IMAGE_DISK_CACHE_MAX_SIZE);
        });

        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context).build();
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        int customMemoryCacheSize = (int) (1.2 * defaultMemoryCacheSize);
        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
        //设置内存缓存
        builder.setMemoryCache(new LruResourceCache(customMemoryCacheSize));
        //设置Bitmap的缓存池
        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
        //设置读取磁盘缓存中资源的线程
        builder.setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor());

    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        //Glide 默认使用 HttpURLConnection 做网络请求,在这切换成 Okhttp 请求
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .dispatcher(new Dispatcher(executorService)) // <-线程池
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .addInterceptor(new ProgressInterceptor())
//                .build();
//        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(ProgressManager.getOkHttpClient()));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
