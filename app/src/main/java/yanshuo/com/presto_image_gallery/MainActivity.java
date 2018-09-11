package yanshuo.com.presto_image_gallery;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;



/***The main activity is used to display the gallery list and command network layer to fetch photos***/
public class MainActivity extends AppCompatActivity {

    private Async_Download_Photos asyncDownloadPhotos;   // Asynchronous task to fetch photos

    private RecyclerView recyclerViewPhotos;
    private MyRecyclerViewAdapter myRecyclerViewAdapter;
    private RecyclerView.LayoutManager myLayoutManager;

    private boolean isDownloading = false;

    private static final int MESSAGE_WHAT_DOWNLOAD_COMPLETED = 2; //msg.what to be received from asyncDownloadPhotos indicating the photos for new page have being fetched

    private static final int PHOTO_PER_PAGE=5;                       // download 5 photos at a time

    private static final String DB_DATABASE="flickr_gallery_yanshuo.db"; //string represent SQLite database
    private static final String DB_TABLE = "table_flickr_gallery"; //string represent SQlite database table

    /***keys for storing photo information in SQLite database***/
    private static final String KEY_POSITION ="position";
    private static final String KEY_TITLE = "title";
    private static final String KEY_PHOTO_BITMAP = "photoBitmap";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE + "("+
            KEY_POSITION + " INTEGER primary key," +
            KEY_TITLE + " TEXT,"+
            KEY_PHOTO_BITMAP+ " BLOB);";

    // The base url for fetching photos from flickr endpoint.
    private static final String FLICKR_BASE_URL = "https://api.flickr.com/services/rest/?api_key=949e98778755d1982f537d56236bbb42";

    private int nextPageToDownload;

    private int currentPosition; // the expected current position in the list recyclerview

    private ArrayList<PhotoInfo> photoInfos;

    private SQLiteDatabase dataBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentPosition=0;
        nextPageToDownload = 1; // first page to download is page 1

        File dbFile = getApplicationContext().getDatabasePath(DB_DATABASE);
        String dbFilePath = dbFile.getPath();
        dataBase = SQLiteDatabase.openOrCreateDatabase(dbFilePath, null);
        dataBase.execSQL(CREATE_TABLE);

        recyclerViewPhotos = (RecyclerView)findViewById(R.id.recyclerView);
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(dataBase);
        myLayoutManager = new LinearLayoutManager(this);
        recyclerViewPhotos.setLayoutManager(myLayoutManager);
        recyclerViewPhotos.setAdapter(myRecyclerViewAdapter);

        /***When user scroll down the recycler view to the very bottom, start async task to download more photos from flickr***/
        recyclerViewPhotos.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                int numberOfPhotosDownloaded = ((LinearLayoutManager)myLayoutManager).getItemCount();
                int numberOfPhotosVisible = ((LinearLayoutManager)myLayoutManager).getChildCount();
                int numberOfPhotosPastScrolled = ((LinearLayoutManager)myLayoutManager).findFirstVisibleItemPosition();
                if(!isDownloading && (numberOfPhotosPastScrolled+numberOfPhotosVisible>=numberOfPhotosDownloaded)){
                    nextPageToDownload = numberOfPhotosDownloaded/PHOTO_PER_PAGE+1;
                    isDownloading=true;
                    asyncDownloadPhotos=new Async_Download_Photos(getApplicationContext(),PHOTO_PER_PAGE, messengerFromAsyncTask,FLICKR_BASE_URL,nextPageToDownload);
                    asyncDownloadPhotos.execute();
                }
            }
        });

        /***Download the photos of page 1 first***/
        asyncDownloadPhotos=new Async_Download_Photos(getApplicationContext(),PHOTO_PER_PAGE, messengerFromAsyncTask, FLICKR_BASE_URL, nextPageToDownload);
        asyncDownloadPhotos.execute();
        isDownloading = true;
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

Messenger messengerFromAsyncTask = new Messenger(new handlerFromAsyncDownloadPhotos());

    /***Handler for processing downloaded photos handed over from "Async_Download_Photos" async task***/
    class handlerFromAsyncDownloadPhotos extends Handler{
        @Override
        public void handleMessage(Message msg){
            try{
                if (msg.what==MESSAGE_WHAT_DOWNLOAD_COMPLETED){
                    isDownloading = false;           // At this point we finished downloading current page of photos
                    Bundle bundle = msg.getData();
                    photoInfos = (ArrayList<PhotoInfo>)bundle.getSerializable("photoList");
                    /***For each downloaded photo, assign a new position and store it along with its title and bitmap (in byte array) to the SQLite Database***/
                    for (int i=0; i<photoInfos.size();i++){
                        PhotoInfo photoInfo = photoInfos.get(i);
                        Bitmap bitmap = photoInfo.getPhotoBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArrayBitmap = stream.toByteArray();
                        ContentValues cv = new  ContentValues();
                        cv.put(KEY_POSITION, Integer.toString(currentPosition));
                        cv.put(KEY_TITLE, photoInfo.getTitle());
                        cv.put(KEY_PHOTO_BITMAP, byteArrayBitmap);
                        dataBase.insertWithOnConflict(DB_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                        currentPosition=currentPosition+1; // update the current expected position in the recycler view
                    }
                    /***Update the adapter on data changes (more photos have being fetched!)***/
                    myRecyclerViewAdapter.numberOfPhotosDownloaded=currentPosition;
                    myRecyclerViewAdapter.notifyDataSetChanged();
                }
            }
            catch(Exception e){
            }
        }
    }
}
