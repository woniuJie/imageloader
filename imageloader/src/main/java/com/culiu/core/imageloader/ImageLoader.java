package com.culiu.core.imageloader;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * @author wangheng
 * @describe ImageLoader，对ImageLoader的简单封装.
 * @date: 2014年10月13日 下午8:19:20 <br/>
 */
public class ImageLoader {

    private static final String TAG = FrescoConfig.TAG + "ImageLoader";

    private boolean hasInitialized = false;

    private boolean mDebuggable;

    public static final int INVALID_RES_ID = -1;

    private Context mContext;

    private FrescoConfig mFrescoConfig;

    private static class SingletonHolder {
        private static final ImageLoader mInstance = new ImageLoader();
    }

    private ImageLoader() {
    }

    public static ImageLoader getInstance() {
        return SingletonHolder.mInstance;
    }

    public boolean isDebuggable() {
        return mDebuggable;
    }

    public void init(Context context) {
        init(context, false);
    }

    public void init(Context context, boolean debuggable) {
        init(context, debuggable, null);
    }

    public void init(Context context, boolean debuggable, OkHttpClient okHttpClient) {
        this.mDebuggable = debuggable;
        if (hasInitialized)
            return;

        hasInitialized = true;
        this.mContext = context;

        // 初始化fresco的配置信息
        mFrescoConfig = new FrescoConfig();
        mFrescoConfig.init(context, okHttpClient);
    }

    public Context getContext() {
        return mContext;
    }

    public void displayFromUri(SimpleDraweeView imageView, String uri) {
        imageView.setImageURI(uri);
    }

    public void displayFromRes(SimpleDraweeView imageView, int drawableId) {
        display(imageView, "res://" + getPackageNamePath() + drawableId,
                INVALID_RES_ID, null);
    }

    public void displayFromFile(SimpleDraweeView imageView,
                                String filePath,
                                int resId) {
        displayFromFile(imageView, filePath, resId, null);
    }

    public void displayFromFile(SimpleDraweeView imageView,
                                String filePath,
                                int resId,
                                ImageLoadListener listener) {
        filePath = removePrefix("file://", filePath);
        display(imageView, "file://" + getPackageNamePath() + filePath, resId, listener);
    }

    public void displayFromAssets(SimpleDraweeView imageView,
                                  String assetsPath,
                                  int resId,
                                  ImageLoadListener listener) {
        assetsPath = removePrefix("assets://", assetsPath);
        display(imageView, "asset://" + getPackageNamePath() + assetsPath, resId, listener);
    }

    private String getPackageNamePath() {
        return getContext().getPackageName() + File.separator;
    }

    private String removePrefix(String prefix, String path) {
        path = path.replace(prefix, "");
        while (true) {
            if (path.charAt(0) == File.separatorChar)
                path = path.substring(1, path.length());
            else
                break;
        }
        return path;
    }

    public void display(SimpleDraweeView imageView, String imageUri) {
        display(imageView, imageUri, INVALID_RES_ID, null);
    }

    public void display(SimpleDraweeView imageView, String imageUri, ImageLoadListener listener) {
        display(imageView, imageUri, INVALID_RES_ID, listener);
    }

    public void display(SimpleDraweeView imageView, String imageUri, int resId) {
        display(imageView, imageUri, resId, null);
    }

    public void display(final SimpleDraweeView img,
                        String img_url,
                        final int default_img,
                        final int margins,
                        final int fraction,
                        final float wBh) {
        float width = (Utils.getScreenSize(getContext())[0] - Utils.dip2px(getContext(), margins)) / fraction;

        display(img, img_url, default_img, (int) width, wBh);
    }

    public void display(final SimpleDraweeView img,
                        String img_url,
                        final int default_img,
                        final int width,
                        final float wBh) {
        final LayoutParams layoutParams = img.getLayoutParams();

        layoutParams.width = width;
        layoutParams.height = (int) (width * wBh);

        display(img, img_url, default_img, new SimpleImageLoadListener() {

                    @Override
                    public void onLoadingComplete(Object bitmapInfo) {
                        img.setLayoutParams(layoutParams);  // 进行scale操作；有硬件加速、速度非常快；
                    }

                    @Override
                    public void onLoadingStart() {
                        img.setLayoutParams(layoutParams);
                    }

                    @Override
                    public void onLoadingFailed() {
                        img.setLayoutParams(layoutParams);
                    }
                },
                new ResizeOptions(width, 1), null);//宽度决定自身缩放情况即可
    }

    public void display(SimpleDraweeView imageView,
                        String imageUri,
                        int resId,
                        final ImageLoadListener listener) {
        display(imageView, imageUri, resId, listener, null, null);
    }

    public void display(SimpleDraweeView imageView,
                        String imageUri,
                        int resId,
                        final ImageLoadListener listener,
                        final BasePostprocessor postprocessor) {
        display(imageView, imageUri, resId, listener, null, postprocessor);
    }


    private void display(final SimpleDraweeView imageView,
                         String imageUri,
                         int resId,
                         final ImageLoadListener listener,
                         ResizeOptions options,
                         BasePostprocessor postprocessor) {
        if (imageView == null) {
            return;
        }
        init(imageView.getContext());
        /**
         * 避免加载图片闪烁,比如在调用NotifyDataSetChanged的时候
         */
        if (imageView.getTag() != null
                && !TextUtils.isEmpty(imageUri)
                && imageUri.equals(imageView.getTag())) {
            return;
        }
        imageView.setTag(imageUri);
        /**
         * 目的：通过Resize处理网络、本地相册等大尺寸图片的情况；否则，非常消耗内存；
         * 参考：http://fresco-cn.org/docs/resizing-rotating.html#_
         */
        if (options == null) {
            int width = 0;

            //首先按照控件的大小来设置图片大小，但是这里可能会有一个问题，就是控件大小后期被调整了， 会导致图片模糊
            if (imageView.getWidth() > 0)
                width = imageView.getWidth();
            else if (imageView.getLayoutParams() != null && imageView.getLayoutParams().width > 0)
                width = imageView.getLayoutParams().width;
            else
                width = Utils.getScreenSize(getContext())[0];

            options = new ResizeOptions(width, 1);//这里主要是宽度决定最终的图片大小; // 0.9.0: 目前仅支持JPEG格式图片; 开启setDownsampleEnabled(true)开关后，还需要该选项
        }

        ImageRequest request = null;
        if (null != postprocessor) { // 如果后处理器不为null,则设置
            request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(imageUri == null ? "" : imageUri))
                    .setPostprocessor(postprocessor)
                    // http://frescolib.org/docs/requesting-multiple-images.html
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(options)    // 去除Resize功能；
                    .build();
        } else {
            request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(imageUri == null ? "" : imageUri))
                    .setResizeOptions(options)    // 去除Resize功能；
                    // http://frescolib.org/docs/requesting-multiple-images.html
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();
        }

        AbstractDraweeController pipelineDraweeController = Fresco.getDraweeControllerBuilderSupplier().get()
                .setOldController(imageView.getController())    // 这可节省不必要的内存分配
                .setImageRequest(request)
                .setAutoPlayAnimations(true)    // 支持动画图
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onSubmit(String id, Object callerContext) {
                        super.onSubmit(id, callerContext);
                        if (listener != null)
                            listener.onLoadingStart();//不一定可靠
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        super.onFinalImageSet(id, imageInfo, animatable);

                        //处理wrap_content的情况：其他情况，按照XML配置属性自动设置Scale
                        LayoutParams params = imageView.getLayoutParams();
                        if (params.width == LayoutParams.WRAP_CONTENT ^ params.height == LayoutParams.WRAP_CONTENT) {
                            imageView.setAspectRatio(imageInfo.getWidth() * 1.0f / imageInfo.getHeight());
                        } else if (params.width == LayoutParams.WRAP_CONTENT && params.height == LayoutParams.WRAP_CONTENT) {
                            params.width = imageInfo.getWidth();
                            params.height = imageInfo.getHeight();
                            imageView.setLayoutParams(params);
                        }

                        if (listener != null)
                            listener.onLoadingComplete(imageInfo);
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                        super.onFailure(id, throwable);
                        if (listener != null)
                            listener.onLoadingFailed();
                    }
                })
                .build();

        imageView.setController(pipelineDraweeController);

        // 处理站位图
        if (resId > 0) {
            // 如果是圆角图片：居中显示；否则，FIT_XY铺满控件显示；
            ScalingUtils.ScaleType type = imageView.getHierarchy().getRoundingParams() == null ?
                    ScalingUtils.ScaleType.FIT_XY : ScalingUtils.ScaleType.FIT_CENTER;

            imageView.getHierarchy().setPlaceholderImage(imageView.getResources().getDrawable(resId), type);
        }
    }

    /**
     * 加载圆角矩形图片
     *
     * @param imageView
     * @param skuImageUrl
     * @param defaultImageViewID
     * @param CornersRadius
     */
    public void displayRoundCornerPic(SimpleDraweeView imageView, String skuImageUrl,
                                      int defaultImageViewID, float CornersRadius) {
        Uri uri;

        try {
            if (skuImageUrl != null) {
                uri = Uri.parse(skuImageUrl);
                //v3.3设计师需求，添加圆角功能
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(Utils.dip2px(getContext(), CornersRadius));
                //获取GenericDraweeHierarchy对象
                GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getContext().getResources())
                        .setRoundingParams(roundingParams)
                        //构建
                        .build();
                hierarchy.setPlaceholderImage(defaultImageViewID);
                //设置Hierarchy
                imageView.setHierarchy(hierarchy);

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(uri)
                        .setTapToRetryEnabled(true)
                        .build();
                imageView.setController(controller);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 新旧框架切换时，临时使用的接口，提供的调用有限，等框架切换完成后，可以抛弃，使用框架自带接口
     */
    public interface ImageLoadListener {

        void onLoadingStart();

        void onLoadingComplete(Object bitmapInfo);

        void onLoadingFailed();

    }

    public static class SimpleImageLoadListener implements ImageLoadListener {

        @Override
        public void onLoadingStart() {
        }

        @Override
        public void onLoadingComplete(Object bitmapInfo) {
        }

        @Override
        public void onLoadingFailed() {
        }
    }

    public void clearMemoryCaches() {
        if (hasInitialized) {
            Fresco.getImagePipeline().clearMemoryCaches();
        }
    }

    public void clearCache() {
        if (hasInitialized) {
            Fresco.getImagePipeline().clearMemoryCaches();
            Fresco.getImagePipeline().clearDiskCaches();
        }
    }

    public void onTrimMemory(int level) {
        if (mFrescoConfig != null) {
            mFrescoConfig.onTrimMemory(level);
        }
    }

    public void onLowMemory() {
        if (mFrescoConfig != null) {
            mFrescoConfig.onLowMemory();
        }
    }

}
