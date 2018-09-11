package yanshuo.com.presto_image_gallery;

/**
 * Created by yanshuo on 9/7/2018.
 */

import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Messenger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;


import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;


/*** This AsyncTask serves as the network layer that fetches photos and other meta data from flickr***/
public class Async_Download_Photos extends AsyncTask<String, Void, Integer> {

    private String flickrBaseURL;
    private String flickrPageURL;
    private String flickrFullURL;
    private String responseInXML = null;
    private String numberOfPagesInTotal=null;

    private ArrayList<PhotoInfo> photoInfos;
    private Context localContext;

    private HttpURLConnection httpURLConnection = null;
    private JSONObject responseInJson;
    private JSONObject photosJsonObject;
    private JSONArray photoJsonArray;

    private Messenger messengerAsyncToMainActivity;
    private static final int MESSAGE_WHAT_DOWNLOAD_COMPLETED = 2;     // msg.what to be send back to main activity
    private static final int MESSAGE_WHAT_DOWNLOAD_PHOTO_COMPLETED=3; // msg.what to be received from sub threads in downloading photo bitmap

    private int nextPageToDownload=0;
    private int numberOfPhotoDownloaded;
    private int photoPerPage;

    public Async_Download_Photos(Context appContext, int photoPerPage, Messenger messengerAsyncToMainActivity, String flickerBaseURL, int nextPageToDownload){

        this.localContext = appContext;
        this.messengerAsyncToMainActivity = messengerAsyncToMainActivity;
        this.nextPageToDownload=nextPageToDownload;
        this.flickrBaseURL = flickerBaseURL;
        this.photoPerPage = photoPerPage;
    }

    @Override
    protected void onPreExecute(){
        if (nextPageToDownload==1) {
            Toast.makeText(localContext, "Downloading photos...", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(localContext, "Downloading more photos...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected Integer doInBackground(String...params){

        numberOfPhotoDownloaded = 0;
        photoInfos = new ArrayList<>(); // create an array list to store photos of the new page (nextPageToDownload) to be downloaded
        flickrPageURL = "&method=flickr.photos.search&tags=photos&per_page="+Integer.toString(photoPerPage)+"&page="+Integer.toString(nextPageToDownload);
        flickrFullURL = flickrBaseURL+flickrPageURL;
        try{
            /***Connect to the end point***/
            URL urlObject = new URL(flickrFullURL);
            httpURLConnection = (HttpURLConnection)urlObject.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(15*1000); //set time out of 15 seconds.
            httpURLConnection.connect();

            /***Get XML response of new page (contains 5 photos in this case)***/
            BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String input;
            StringBuffer responseBuffer = new StringBuffer();

            while((input=in.readLine())!=null){
                responseBuffer.append(input);
            }
            in.close();
            responseInXML = responseBuffer.toString();

            /***Convert xml response to json response***/
            responseInJson = XML.toJSONObject(responseInXML);
            photosJsonObject = responseInJson.getJSONObject("rsp").getJSONObject("photos");
            numberOfPagesInTotal = photosJsonObject.getString("pages"); // Get the total number of pages of this end point
            if (nextPageToDownload<=Integer.parseInt(numberOfPagesInTotal)){  // Parse the photos if we have not reached the last page
                photoJsonArray = photosJsonObject.getJSONArray("photo");
                // For each photo in the new page, store the id, secret, server, farm and title
                for (int i=0; i <photoJsonArray.length();i++){
                    JSONObject photoObject = photoJsonArray.getJSONObject(i);
                    String id = photoObject.getString("id");
                    String secret = photoObject.getString("secret");
                    String server = photoObject.getString("server");
                    String farm = photoObject.getString("farm");
                    String title = photoObject.getString("title");
                    PhotoInfo photoInfo = new PhotoInfo(id, secret, server, farm, title);
                    photoInfos.add(photoInfo); // add the photo to the list
                    // create and start a thread to download the bitmap of the photo
                    new DownloadPhotoThread(photoInfo, messengerFromDownloadThreadToAsyncTask).start();
                }
                while (numberOfPhotoDownloaded<photoJsonArray.length()){
                     // wait while all photos's bitmap have being successfully downloaded
                }
            }


        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }


    @Override
    protected void onProgressUpdate(Void...values){

    }

    @Override
    protected void onPostExecute(Integer result){
        /***send the downloaded photos back to main thread***/
        Message messageToMainActivity = Message.obtain(null, MESSAGE_WHAT_DOWNLOAD_COMPLETED,1,1);
        Bundle bundle = new Bundle();
        bundle.putSerializable("photoList", (Serializable)photoInfos);
        messageToMainActivity.setData(bundle);
        try {
            messengerAsyncToMainActivity.send(messageToMainActivity);
        }
        catch(RemoteException e){
        }

        if (nextPageToDownload==1) {
            Toast.makeText(localContext, "Photos downloaded!!!", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(localContext, "More photos downloaded!!!",Toast.LENGTH_SHORT).show();
        }
    }

    /***thread for downloading bitmap of photo***/
    public class DownloadPhotoThread extends Thread {
        PhotoInfo photoInfo;
        Messenger messengerFromDownloadThreadToAsyncTask;

        public DownloadPhotoThread(PhotoInfo photoInfo, Messenger messengerFromDownloadThreadToAsyncTask) {
            this.photoInfo = photoInfo;
            this.messengerFromDownloadThreadToAsyncTask = messengerFromDownloadThreadToAsyncTask;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                // construct the url to the photo to be downloaded. In my case, I choose to download medium size photo
                String urlToPhoto = "http://farm" + photoInfo.getFarm() + ".staticflickr.com/" + photoInfo.getServer() + "/" + photoInfo.getId() + "_" + photoInfo.getSecret() + ".jpg";
                URL urlToPhotoObject = new URL(urlToPhoto);

                // download the photo bitmap
                Bitmap photoBitmap = BitmapFactory.decodeStream(urlToPhotoObject.openConnection().getInputStream());
                photoInfo.setPhotoBitmap(photoBitmap);
            }
            catch(Exception e){

            }
            // send the message back to asynchronous task stating the bitmap of the passed in photo has being finished downloading
            Message messageFromAsyncTaskToDownloadThread = Message.obtain(null, MESSAGE_WHAT_DOWNLOAD_PHOTO_COMPLETED,1,1);
            try {
                messengerFromDownloadThreadToAsyncTask.send(messageFromAsyncTaskToDownloadThread);
            }
            catch(RemoteException e){
            }
        }

    }


    Messenger messengerFromDownloadThreadToAsyncTask = new Messenger(new handlerFromDownloadThread());
    // Handler for collecting message from child threads for the completion of bitmap downloading.
    class handlerFromDownloadThread extends Handler{
        @Override
        public void handleMessage(Message msg){

            if (msg.what==MESSAGE_WHAT_DOWNLOAD_PHOTO_COMPLETED){
                numberOfPhotoDownloaded++;
            }
        }
    }

}
