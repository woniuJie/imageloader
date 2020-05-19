package imageloader.culiu.com.culiuimageloader;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.culiu.core.imageloader.ImageLoader;
import com.culiu.core.imageloader.Utils;
import com.culiu.core.widget.CustomImageView;

import java.util.List;

/**
 * Created by wangjing on 2018/1/9.
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    private List<String> mDataList;

    public GalleryAdapter(List<String> dataList) {
        mDataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.gallery_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ImageLoader.getInstance().displayFromFile(holder.mCustomImageView,
                mDataList.get(position), R.mipmap.ic_launcher, null);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CustomImageView mCustomImageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mCustomImageView = (CustomImageView) itemView.findViewById(R.id.image_view);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCustomImageView.getLayoutParams();
            layoutParams.height = layoutParams.width = Utils.getScreenWidth(itemView.getContext()) / 3;
            mCustomImageView.setLayoutParams(layoutParams);
        }

    }


}
