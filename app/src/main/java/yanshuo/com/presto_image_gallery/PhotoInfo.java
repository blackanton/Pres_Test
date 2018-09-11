package yanshuo.com.presto_image_gallery;

/**
 * Created by yanshuo on 9/7/2018.
 */
import android.graphics.Bitmap;
import android.os.Parcelable;

/***This class is used to store fetched photos' info (including bitmap) before storing it in SQLite database***/
public class PhotoInfo {
   private String id;
   private String secret;
   private String server;
   private String farm;
   private String title;
   private Bitmap photoBitmap;

   public PhotoInfo(String id, String secret, String server, String farm, String title) {
       this.id = id;
       this.secret = secret;
       this.server = server;
       this.farm = farm;
       this.title = title;
   }

    public String getId(){
       return id;
   }
    public void setId(String id){
       this.id = id;
   }
    public String getSecret(){
        return secret;
    }
    public void setSecret(String secret){
        this.secret = secret;
    }
    public String getServer(){
        return server;
    }
    public void setServer(String server){
        this.server = server;
    }
    public String getFarm(){
        return farm;
    }
    public void setFarm(String farm){
        this.farm = farm;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public Bitmap getPhotoBitmap(){return photoBitmap;}
    public void setPhotoBitmap(Bitmap photoBitmap){this.photoBitmap = photoBitmap;}
}
