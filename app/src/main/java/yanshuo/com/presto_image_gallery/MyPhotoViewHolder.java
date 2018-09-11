package yanshuo.com.presto_image_gallery;

import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.support.v7.widget.RecyclerView;
/**
 * Created by yanshuo on 9/7/2018.
 */

public class MyPhotoViewHolder extends RecyclerView.ViewHolder {
    public ImageView photoImageView;
    public TextView photoTitleView;
    public TextView photoSizeView;
    public TextView photoDimensionView;

    public MyPhotoViewHolder(View view){
        super(view);
        photoImageView = (ImageView)view.findViewById(R.id.photoImage);
        photoTitleView = (TextView)view.findViewById(R.id.photoTitle);
        photoSizeView = (TextView)view.findViewById(R.id.photoSize);
        photoDimensionView = (TextView)view.findViewById(R.id.photoDimension);

    }
}
