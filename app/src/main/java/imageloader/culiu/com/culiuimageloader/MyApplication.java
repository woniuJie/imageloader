package imageloader.culiu.com.culiuimageloader;

import android.app.Application;

import com.culiu.core.imageloader.ImageLoader;

/**
 * Created by xujianbo on 2017/6/21.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoader.getInstance().init(getApplicationContext(), BuildConfig.DEBUG);
    }
}
