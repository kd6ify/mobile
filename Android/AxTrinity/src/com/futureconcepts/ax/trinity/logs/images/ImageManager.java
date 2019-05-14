package com.futureconcepts.ax.trinity.logs.images;

import java.util.Stack;

import com.futureconcepts.ax.trinity.logs.images.DownloadImagesFromServer.DownloadImageNotifier;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

public class ImageManager {

	private LruCache<String, Bitmap> imageMap = null;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());
	private ImageManagerGetImageListener getImageListener;
	private String TAG = ImageManager.class.getSimpleName();
	private boolean clearCacheCalled = false;
	private boolean imageManagerIsRunning = false;
	public interface ImageManagerGetImageListener
	{
		public Bitmap getImage(String imageID, String filePath, int defaultDrawable);
	}
	
	public void removeImageManagerGetImageListener(ImageManagerGetImageListener Listener)
	{
		if(getImageListener == Listener)
			getImageListener=null;
	}
	
	//used just as memory cache.
//	public ImageManager(Context context) {
//		imageMap = new LruCache<String, Bitmap>(getCacheSize());
//	}
//	
	public ImageManager(ImageManagerGetImageListener listener) {
		getImageListener = listener;			
		imageMap = new LruCache<String, Bitmap>(getCacheSize()){
			@Override
			protected int sizeOf(String key, Bitmap value) {				
		         return value.getRowBytes() * value.getHeight();
			 }
			
			@Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldBitmap, Bitmap newBitmap) {
              if(clearCacheCalled)
            	  oldBitmap.recycle();
              	  oldBitmap = null;
            }
		};
		clearCacheCalled = false;
		// Make background thread low priority, to avoid affecting UI
		// performance
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
	}

	private int getCacheSize()
	{
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024)/1024;
		final int total = (int) (Runtime.getRuntime().totalMemory() / 1024)/1024;
		//final int free = (int) (Runtime.getRuntime().freeMemory() / 1024);
		int DEFAULT_MEM_CACHE_SIZE = (int) 1*1024*1024;//1mb
		if((maxMemory-total)<1)//1mib
		{
			DEFAULT_MEM_CACHE_SIZE =(maxMemory-total)/2;//(int)(maxMemory*5)/100;
		}
		 Log.d("ImageManager", "Memory cache assigned : " + DEFAULT_MEM_CACHE_SIZE + " bits");
		return DEFAULT_MEM_CACHE_SIZE;
	}

	/**
     * Clears both the memory and disk cache associated with this ImageCache object.
     */
    public void clearCache() {
        if (imageMap != null) {
        	clearCacheCalled = true;
        	  Log.d(TAG, "Memory Image cache cleared : "+imageMap.size());
        	imageMap.evictAll();        	
        	imageMap=null;
        }
      
    }
    
    public void close()
    {
    	imageManagerIsRunning = false; 	
    	synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.notifyAll();//Wake Up the thread so we can finish it.
		}
    }
    
    public  LruCache<String, Bitmap> getLruImageCache()
    {
    	return imageMap;
    }
    
	public void displayImage(String ID, ImageView imageView,int defaultDrawableId,String filePath) {

		if (imageMap.get(ID)!=null) {
			imageView.setImageBitmap(imageMap.get(ID));
		} else {
			queueImage(ID, imageView, defaultDrawableId,filePath);
			imageView.setImageResource(defaultDrawableId);
		}
	}

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {			
		imageMap.put(key, bitmap);
	}
	
	private void queueImage(String ID, ImageView imageView,	int defaultDrawableId, String filePath) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imageQueue.Clean(imageView);
		ImageRef p = new ImageRef(ID, imageView, defaultDrawableId, filePath);

		synchronized (imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}

		// Start thread if it's not started yet
		if (imageLoaderThread.getState() == Thread.State.NEW) {
			imageManagerIsRunning = true;
			imageLoaderThread.start();
		}
	}
	
	/** Classes **/

	private class ImageRef {
		public String ID;
		public ImageView imageView;
		public int defDrawableId;
		public String filePath;

		public ImageRef(String u, ImageView i, int defaultDrawableId,String fPath) {
			ID = u;
			filePath = fPath;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}
	}

	// stores list of images to download
	private class ImageQueue {
		private Stack<ImageRef> imageRefs = new Stack<ImageRef>();
		// removes all instances of this ImageView
		public void Clean(ImageView view) {
			for (int i = 0; i < imageRefs.size();) {
				//if (imageRefs.size()>i)	{
					if (imageRefs.get(i).imageView == view)
						imageRefs.remove(i);
					else		
						i++;
				//}
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		
		@Override
		public void run() {
			try {
				while (imageManagerIsRunning) {
					// Thread waits until there are images in the
					// queue to be retrieved
					if (imageQueue.imageRefs.size() == 0) {
						synchronized (imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}
					// When we have images to be loaded
					if (imageQueue.imageRefs.size() != 0) {
						ImageRef imageToLoad;
						
						synchronized (imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}
						Bitmap bmp= null;
						if(getImageListener!=null){
							bmp= getImageListener.getImage(imageToLoad.ID,imageToLoad.filePath,imageToLoad.defDrawableId);//createImageViewForImage(imageToLoad.filePath);
						}
						if(bmp!=null && imageMap!=null){
							imageMap.put(imageToLoad.ID,bmp);
						}
						Object tag = imageToLoad.imageView.getTag();
						// Make sure we have the right view - thread safety defender
						if (tag != null
								&& ((String) tag).equals(imageToLoad.ID)) {
							BitmapDisplayer bmpDisplayer = new BitmapDisplayer(bmp, imageToLoad.imageView,imageToLoad.defDrawableId);
							Activity a = (Activity) imageToLoad.imageView.getContext();
							a.runOnUiThread(bmpDisplayer);
						}
						imageToLoad = null;
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;
		int defDrawableId;

		public BitmapDisplayer(Bitmap b, ImageView i, int defaultDrawableId) {
			bitmap = b;
			imageView = i;
			defDrawableId = defaultDrawableId;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);			
			else
				imageView.setImageResource(defDrawableId);
			 bitmap = null;
			 imageView = null;
		}
	}
}