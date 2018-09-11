A. Problems
   Problem 1: Calling the endpoint URL    "https://api.flickr.com/services/rest/api_key=949e98778755d1982f537d56236bbb42&method=flickr.photos.search" directly would raise no parameter exeception error as documented in the flickr API. 
   Problem 2: The photos from end point "https://api.flickr.com/services/rest/api_key=949e98778755d1982f537d56236bbb42&method=flickr.photos.search" are in a number of hundreds of thousands. Download all these photo at once would be impractical. 
   Problem 3: Downloading photo bitmap is a relatively time consuming task.
   Problem 4: The downloaded photos have to be all displayed without occupying too much memory
   
B. Design Considerations
   1. Regarding problem 1, "photos" is being specified as value of "tags" argument. The reason for this is because all photos are categorized under "photos" tag. 
   2. Regarding problem 2, I found out that all the photos are organized into pages and you can specify the # of photos/page as argument in the flickr end point call.Therefore, I designed the app in such a way that it downloads one page (of photos) at a time.
   3. Regarding problem 3, I created separate thread to download each photo's bitmap.
   4. Regarding problem 4, I choose to use linear recyclerview in displaying photos. Recyclerview will use designated view holders for photo display, which will certainly save memory.
   
C. App Architecture/Reasoning behind technical choices
   1. MainActivity: The main activity is the activity that first launches. The main responsibilities of the MainActivity is to set up the recyclerview, start asynchronous task for photo downloading, collect and store downloaded photos, and react to the event which user have scrolled down to the bottom of the recyclerview. That's when more photos should be downloaded.
   2. Async_Download_Photos: This class extends the AsyncTask class and is used to download photos in background threads. It first receives parameters from MainActivity specifying which page to download and how many photos in the page to download. Then it calls flickr endpoint for the xml response. After the xml response is converted to jsonResponse, metadata (id, server, farm, secret and title) of each photo in downloaded page will be extracted, stored as a PhotoInfo entity and appended to the list. After that, a sub-thread is created to download the bitmap of that photo. The reason to create a sub-thread for downloading each photo's bitmap is because bitmap downloading takes time, so it is best to make the process as parallel as possible. Finally, the downloaded photos (along with their bitmap) will be send back to MainActivity for displaying.
   3. SQLite Storage: The SQLite is used as the base data storage for the recyclerview instead of array list. After the photos are fetched from network layer(async task), their title and bitmap are stored in SQLite with the assigned primary key "position". So whenever the recyclerview's adapter assigns the view holder to a certain position, the photo's title and bitmap stored with that position value as primary key will be extracted and displayed on the recyclerview. The reason why SQLite is chosen for photo data 
storage is due to the limitation in array list's capacity in holding large amount of data. The app has being tested to found out that
the app terminates itself after filling the array list with just over 2000 photos (1000 Megabytes of data). However, SQLite offers
much more storage space than array list and would save up to millions of photos' data.

D. Tradeoff
   1. Eventhough SQLite stores more data than array list, on the otherhand it is slower in read/insert data from SQLite comparing to 
    array list. However, this little lagging is still of less concern than running out of memory.
   2. The number of photos to download in a time is another tradeoff. Downloading more photos at a time would reduce number of times 
   new photos should be downloaded, but it means more threads have to be started by async task in downloading photo bitmap. This could
   cause lagging in UI response. At last, the number of photos to be downloaded each time is limited to 5.
   
E. Leftout/Potential extention
   1. As for now the recyler view display the title, dimension and size together with the image(bitmap) in the list. A better way of displaying the entire thing would be using a master-detailed layout. All images are listed in one fragment and have another fragment displaying the detailed image and other data (size, dimension, title) of the chosen image.
   2. Another leftout is to coming up with a better solution in reaching parallelism. One possible improvement is dividing the threads of downloading photo bitmap into different groups so that one group of threads will be executed after another group.
 




