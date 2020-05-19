package imageloader.culiu.com.culiuimageloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.culiu.core.imageloader.ImageLoader;
import com.facebook.drawee.view.SimpleDraweeView;

public class MainActivity extends AppCompatActivity {

    private SimpleDraweeView simpleDraweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        simpleDraweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);
    }

    public void onButtonClick(View view) {
        ImageLoader.getInstance().display(simpleDraweeView,
                "https://ads-cdn.chuchujie.com/FuE4r7Fo3sZsF8JmuE4PLb6A0_yO.png?imageView2/2/format/webp/q/90&");
    }

    public void onOpenGallery(View view) {
        startActivity(new Intent(this, GalleryActivity.class));
    }

}
