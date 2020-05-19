package com.culiu.core.imageloader;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;

/**
 * Created by xujianbo on 2017/6/21.
 */

public class Utils {

    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return new int[]{dm.widthPixels, dm.heightPixels};
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static File getExternalCacheDirectory(Context context) {
        return isExternalStorageMounted() &&
                context != null &&
                null != context.getExternalCacheDir() ?
                context.getExternalCacheDir() :
                new File("/mnt/sdcard/Android/data/" + context.getPackageName() + "/cache/");
    }

    public static boolean isExternalStorageMounted() {
        try {
            boolean e = Environment.getExternalStorageDirectory().canRead();
            boolean onlyRead = Environment.getExternalStorageState().equals("mounted_ro");
            boolean unMounted = Environment.getExternalStorageState().equals("unmounted");
            return e && !onlyRead && !unMounted;
        } catch (Exception var3) {
            return false;
        }
    }

    /**
     * getScreenWidth:得到屏幕宽度(像素点数). <br/>
     *
     * @return
     * @author wangheng
     */
    public static int getScreenWidth(Context context) {
        return getScreenSize(context)[0];
    }

    /**
     * getScreenHeight:得到屏幕高度(像素点数). <br/>
     *
     * @return
     * @author wangheng
     */
    public static int getScreenHeight(Context context) {
        return getScreenSize(context)[1];
    }


}
