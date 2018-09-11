package yanshuo.com.presto_image_gallery;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Bitmap;
/**
 * Created by yanshuo on 9/7/2018.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter {

    private SQLiteDatabase dataBase;
    public int numberOfPhotosDownloaded=0; // declare this as public because it will be set directly from main activity

    private static final String KEY_POSITION ="position";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PHOTO_BITMAP = "photoBitmap";
    private static final String DB_TABLE = "table_flickr_gallery";


    public MyRecyclerViewAdapter(SQLiteDatabase dataBase){
        this.dataBase = dataBase;
    }

    @Override
    public MyPhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        return new MyPhotoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_cell,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        // Extract the photo data from SQLite database according to the current onBind position and display it on the assigned view holder
        try {
            String[] projection = {
                    KEY_POSITION,
                    KEY_TITLE,
                    KEY_PHOTO_BITMAP
            };
            String selection = "position = ?";
            String[] selectionArgs={Integer.toString(position)};
            Cursor cursor = dataBase.query(DB_TABLE, projection, selection, selectionArgs, null,null, null);
            String title;
            byte[] bitmapBytes;
            Bitmap photoBitmap;
            if (cursor!=null){
                cursor.moveToFirst();
                title = cursor.getString(cursor.getColumnIndexOrThrow((KEY_TITLE)));
                bitmapBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(KEY_PHOTO_BITMAP));
                photoBitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                MyPhotoViewHolder viewHolder = (MyPhotoViewHolder) holder;
                viewHolder.photoTitleView.setText("Title: " + title);  // Display title
                viewHolder.photoSizeView.setText("Size: Medium");      // Display size
                viewHolder.photoImageView.setImageBitmap(photoBitmap); // Display bitmap
                viewHolder.photoDimensionView.setText("Dimension: " + Integer.toString(photoBitmap.getWidth()) + "x" + Integer.toString(photoBitmap.getHeight()));//Display dimensions
            }
        }
        catch(Exception e){

        }
    }

    @Override
    public int getItemCount(){
        return numberOfPhotosDownloaded;
    }
}
