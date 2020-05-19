package com.culiu.core.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;

/**
 * Created by yxb on 2017/9/4.
 */

public class ImageUtils {

    public  static Bitmap getCachedImageOnDisk(Context context, Uri loadUri, int reqWidth, int reqHeight) {
        File localFile = null;
        if (loadUri != null) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(loadUri), context);
            BinaryResource resource = null;
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
            }

            if (resource == null){
                return null;
            }
            localFile = ((FileBinaryResource) resource).getFile();
        }

        if (localFile != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
            options.inJustDecodeBounds = false;
            return  BitmapFactory.decodeFile(localFile.getAbsolutePath(), options);
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
