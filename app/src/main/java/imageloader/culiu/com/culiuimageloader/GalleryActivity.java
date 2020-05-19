package imageloader.culiu.com.culiuimageloader;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private List<String> mImageUrls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);


        GridLayoutManager linearLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        loadLocalUrls();

        mRecyclerView.setAdapter(new GalleryAdapter(mImageUrls));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                switch (newState) {
//                    case RecyclerView.SCROLL_STATE_DRAGGING:
//                    case RecyclerView.SCROLL_STATE_IDLE:
//                        Fresco.getImagePipeline().resume();
//                        break;
//                    case RecyclerView.SCROLL_STATE_SETTLING:
//                        Fresco.getImagePipeline().pause();
//                        break;
//                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

    }


    private void loadLocalUrls() {
        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(externalContentUri, projection, null, null, null);

            mImageUrls.clear();

            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

            String imageId;
            Uri imageUri;
            while (cursor.moveToNext()) {
                imageId = cursor.getString(columnIndex);
                imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);

                String realPathFromUri = UriUtil.getRealPathFromUri(getContentResolver(), imageUri);

                mImageUrls.add(realPathFromUri);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
