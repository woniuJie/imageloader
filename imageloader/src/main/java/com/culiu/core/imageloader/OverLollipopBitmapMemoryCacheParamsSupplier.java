package com.culiu.core.imageloader;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.cache.MemoryCacheParams;

/**
 * 5.0以上不能让fresco无限制的使用C内存
 * http://blog.csdn.net/honjane/article/details/65629799
 */
public class OverLollipopBitmapMemoryCacheParamsSupplier implements Supplier<MemoryCacheParams> {
//    private static final int MAX_CACHE_ENTRIES = 56;
//    private static final int MAX_CACHE_ASHM_ENTRIES = 128;
//    private static final int MAX_CACHE_EVICTION_SIZE = 5;
//    private static final int MAX_CACHE_EVICTION_ENTRIES = 5;
//
//    private ActivityManager mActivityManager;
//
//    public OverLollipopBitmapMemoryCacheParamsSupplier(Context context) {
//        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//    }
//
//    @Override
//    public MemoryCacheParams get() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return new MemoryCacheParams(getMaxCacheSize(),
//                    MAX_CACHE_ENTRIES,
//                    MAX_CACHE_EVICTION_SIZE,
//                    MAX_CACHE_EVICTION_ENTRIES,
//                    1);
//        } else {
//            return new MemoryCacheParams(
//                    getMaxCacheSize(),
//                    MAX_CACHE_ASHM_ENTRIES,
//                    Integer.MAX_VALUE,
//                    Integer.MAX_VALUE,
//                    Integer.MAX_VALUE);
//        }
//    }
//
//    private int getMaxCacheSize() {
//        final int maxMemory =
//                Math.min(mActivityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);
//        if (maxMemory < 32 * ByteConstants.MB) {
//            return 4 * ByteConstants.MB;
//        } else if (maxMemory < 64 * ByteConstants.MB) {
//            return 6 * ByteConstants.MB;
//        } else {
//            return maxMemory / 5;
//        }
//    }


    private ActivityManager activityManager;

    public OverLollipopBitmapMemoryCacheParamsSupplier(Context context) {
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    @Override
    public MemoryCacheParams get() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new MemoryCacheParams(
                    getMaxCacheSize(),
                    56,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        } else {
            return new MemoryCacheParams(
                    getMaxCacheSize(),
                    256,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE,
                    Integer.MAX_VALUE);
        }
    }

    private int getMaxCacheSize() {
        final int maxMemory = Math.min(activityManager.getMemoryClass() * ByteConstants.MB, Integer.MAX_VALUE);

        if (maxMemory < 32 * ByteConstants.MB) {
            return 4 * ByteConstants.MB;
        } else if (maxMemory < 64 * ByteConstants.MB) {
            return 6 * ByteConstants.MB;
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD) {
                return 8 * ByteConstants.MB;
            } else {
                return maxMemory / 4;
            }
        }
    }

}