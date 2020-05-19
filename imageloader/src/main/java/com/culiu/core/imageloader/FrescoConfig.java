package com.culiu.core.imageloader;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.SparseIntArray;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.util.ByteConstants;
import com.facebook.common.webp.WebpSupportStatus;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.common.TooManyBitmapsException;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.memory.PoolParams;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.culiu.core.imageloader.OkHttpConfig.DEFAULT_CONNECT_TIMEOUT;
import static com.culiu.core.imageloader.OkHttpConfig.DEFAULT_READ_TIMEOUT;
import static com.culiu.core.imageloader.OkHttpConfig.DEFAULT_WRITE_TIMEOUT;

/**
 * Created by wangsai on 2015/12/10.
 */
public class FrescoConfig {

    public static final String TAG = "ImageLoader";

    private MemoryTrimManager mtManager = new MemoryTrimManager();

    /**
     * 初始化配置
     */
    public void init(Context context, OkHttpClient okHttpClient) {

        if (okHttpClient != null) { // 防止图片过大，展示不出来，所以延长时间
            okHttpClient = okHttpClient.newBuilder()
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_READ_TIMEOUT * 2, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_WRITE_TIMEOUT * 2, TimeUnit.SECONDS)
                    .build();
        }

        /**
         * setAutoRotateEnabled(true): 自动旋转; 如果你想图片呈现的方向和设备屏幕的方向一致
         */
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(
                        context,
                        okHttpClient == null ? new OkHttpConfig().getOkHttpClient() : okHttpClient)
                .setRequestListeners(genRequestListeners())
                .setMemoryTrimmableRegistry(mtManager)
                .setMainDiskCacheConfig(genDiskCacheConfig(context))
                .setSmallImageDiskCacheConfig(genSmallImageDiskCacheConfig(context))
                .setBitmapsConfig(chooseBitmapConfig())
                .setPoolFactory(genPoolFactory())
                /*
                 * 打开缩放开关，用于图片解析时，将图片按照屏幕大小进行缩放;
                 * 向下采样在大部分情况下比 resize 更快。除了支持 JPEG 图片，
                 * 它还支持 PNG 和 WebP(除动画外) 图片
                 */
                .setDownsampleEnabled(true)
                // 参考 http://blog.csdn.net/honjane/article/details/65629799
                .setBitmapMemoryCacheParamsSupplier(
                        new OverLollipopBitmapMemoryCacheParamsSupplier(context))
                .build();

        Fresco.initialize(context, config);

        handleJELLY_BEAN_MR1();

        handleDefaultScaleType();
    }

    /**
     * https://github.com/facebook/fresco/issues/902
     *
     * @return
     */
    private PoolFactory genPoolFactory() {
        int MaxRequestPerTime = 64;
        SparseIntArray defaultBuckets = new SparseIntArray();
        defaultBuckets.put(16 * ByteConstants.KB, MaxRequestPerTime);
        PoolParams smallByteArrayPoolParams = new PoolParams(
                16 * ByteConstants.KB * MaxRequestPerTime,
                2 * ByteConstants.MB,
                defaultBuckets);
        return new PoolFactory(PoolConfig.newBuilder()
                .setSmallByteArrayPoolParams(smallByteArrayPoolParams)
                .build());
    }

    /**
     * 内存较小的手机采取质量较低的图片格式
     *
     * @return
     */
    private Bitmap.Config chooseBitmapConfig() {
        int maxMemory = (int) Math.min(Runtime.getRuntime().maxMemory(), Integer.MAX_VALUE);
        if (maxMemory < 64 * 1024 * 1024) {
            return Bitmap.Config.RGB_565;
        }

        return null;
    }

    private DiskCacheConfig genSmallImageDiskCacheConfig(final Context context) {
        DiskCacheConfig diskCacheConfig = null;

        File cacheFile = null;
        try {
            cacheFile = Utils.getExternalCacheDirectory(context);
            if (cacheFile != null) {
                if (!cacheFile.exists()) {
                    cacheFile.mkdirs();
                }
            }
        } catch (Exception e) { //反馈显示这里会有bug，catch一下
            Log.e(TAG, e.getMessage());
        }

        if (cacheFile != null && cacheFile.exists() && Utils.isExternalStorageMounted()) {
            diskCacheConfig = DiskCacheConfig
                    .newBuilder(context)
                    .setBaseDirectoryPathSupplier(new Supplier<File>() {
                        @Override
                        public File get() {
                            return Utils.getExternalCacheDirectory(context);
                        }
                    })
                    .setBaseDirectoryName("image_cache_small")
                    .setMaxCacheSize(200 * ByteConstants.MB)
                    .setMaxCacheSizeOnLowDiskSpace(50 * ByteConstants.MB)
                    .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB)
                    .build();
        } else {
            // CrashUtils.logCrashException(new Exception("No External Cache Dir!"));
            Log.e(TAG, "未获取到SD卡缓存目录");
        }

        return diskCacheConfig;
    }

    /**
     * 生成磁盘缓存配置
     *
     * @param context
     * @return
     */
    private DiskCacheConfig genDiskCacheConfig(final Context context) {
        DiskCacheConfig diskCacheConfig = null;

        File cacheFile = null;
        try {
            cacheFile = Utils.getExternalCacheDirectory(context);
            if (cacheFile != null) {
                if (!cacheFile.exists()) {
                    cacheFile.mkdirs();
                }
            }
        } catch (Exception e) { //反馈显示这里会有bug，catch一下
            Log.e(TAG, e.getMessage());
        }

        if (cacheFile != null && cacheFile.exists() && Utils.isExternalStorageMounted()) {
            diskCacheConfig = DiskCacheConfig
                    .newBuilder(context)
                    .setBaseDirectoryPathSupplier(new Supplier<File>() {
                        @Override
                        public File get() {
                            return Utils.getExternalCacheDirectory(context);
                        }
                    })
                    .setBaseDirectoryName("image_cache")
                    .setMaxCacheSize(200 * ByteConstants.MB)
                    .setMaxCacheSizeOnLowDiskSpace(50 * ByteConstants.MB)
                    .setMaxCacheSizeOnVeryLowDiskSpace(5 * ByteConstants.MB)
                    .build();
        } else {
            // CrashUtils.logCrashException(new Exception("No External Cache Dir!"));
            Log.e(TAG, "未获取到SD卡缓存目录");
        }

        return diskCacheConfig;
    }

    /**
     * 创建请求的观察者，监视图片的生产流程
     *
     * @return
     */
    private Set<RequestListener> genRequestListeners() {
        //请求的事件监听类
        Set<RequestListener> listeners = new HashSet<RequestListener>();
        listeners.add(new RequestListener() {

            @Override
            public void onRequestStart(ImageRequest request, Object callerContext, String requestId, boolean isPrefetch) {

            }

            @Override
            public void onRequestSuccess(ImageRequest request, String requestId, boolean isPrefetch) {

            }

            @Override
            public void onRequestFailure(ImageRequest request, String requestId,
                                         Throwable throwable, boolean isPrefetch) {

            }

            @Override
            public void onRequestCancellation(String requestId) {
            }

            @Override
            public void onProducerStart(String requestId, String producerName) {
            }

            @Override
            public void onProducerEvent(String requestId, String producerName, String eventName) {
            }

            @Override
            public void onProducerFinishWithSuccess(String requestId, String producerName,
                                                    Map<String, String> extraMap) {
            }

            @Override
            public void onProducerFinishWithFailure(String requestId, String producerName,
                                                    Throwable t, Map<String, String> extraMap) {
                // 内存策略没有调整前，可能会出现图像停止加载的情况； 后续优化后，就很少出现这种场景！
                if (t != null && t.getClass().equals(TooManyBitmapsException.class)) {//释放一下内存
                    mtManager.trimMemory(MemoryTrimType.OnSystemLowMemoryWhileAppInForeground);
                }
            }

            @Override
            public void onProducerFinishWithCancellation(String requestId, String producerName,
                                                         Map<String, String> extraMap) {
            }

            @Override
            public void onUltimateProducerReached(String requestId, String producerName, boolean successful) {

            }

            @Override
            public boolean requiresExtraMap(String requestId) {
                return false;
            }
        });

        return listeners;
    }

    /**
     * 部分4.2机型对于带有透明度的webp支持有问题，只好强制将webp转为png
     */
    private void handleJELLY_BEAN_MR1() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.JELLY_BEAN_MR1)
            return;

        try {
            Field isExtendedWebpSupported = WebpSupportStatus.class.getDeclaredField("sIsExtendedWebpSupported");
            isExtendedWebpSupported.setAccessible(true);
            isExtendedWebpSupported.setBoolean(isExtendedWebpSupported, false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 处理图片的默认缩放类型
     */
    private void handleDefaultScaleType() {
        try {
            Field defaultScaleType = GenericDraweeHierarchyBuilder.class.getDeclaredField("DEFAULT_SCALE_TYPE");
            defaultScaleType.setAccessible(true);
            defaultScaleType.set(defaultScaleType, ScalingUtils.ScaleType.FIT_CENTER);

            Field defaultActualImageScaleType = GenericDraweeHierarchyBuilder.class.getDeclaredField("DEFAULT_ACTUAL_IMAGE_SCALE_TYPE");
            defaultActualImageScaleType.setAccessible(true);
            defaultActualImageScaleType.set(defaultActualImageScaleType, ScalingUtils.ScaleType.FIT_CENTER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 处理内存回收，接口Activity、Application的onTrimMemory()事件
     * <p>
     * 参考:https://developer.android.com/reference/android/content/ComponentCallbacks2.html
     * <p>
     * http://frescolib.org/docs/configure-image-pipeline.html
     *
     * @param level
     */
    public void onTrimMemory(int level) {
        if (mtManager == null)
            return;
        switch (level) {
            // When your app is running
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
                mtManager.trimMemory(MemoryTrimType.OnCloseToDalvikHeapLimit);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
                mtManager.trimMemory(MemoryTrimType.OnCloseToDalvikHeapLimit);
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                mtManager.trimMemory(MemoryTrimType.OnSystemLowMemoryWhileAppInForeground);
                break;
            // When your app's visibility changes
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                mtManager.trimMemory(MemoryTrimType.OnSystemLowMemoryWhileAppInBackground);
                break;
            // When your app's process resides in the background LRU list
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                mtManager.trimMemory(MemoryTrimType.OnAppBackgrounded);
                break;
        }
    }

    /**
     * 当系统onLowMemory()通知时，直接清掉Fresco所有内存缓存
     */
    public void onLowMemory() {
        Fresco.getImagePipeline().clearMemoryCaches();
    }

}