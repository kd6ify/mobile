package com.futureconcepts.ax.globalclases;

import com.futureconcepts.ax.model.data.Icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.Log;

public class LruImageCache {
	
	private LruCache<String, Bitmap>lruImageCache;
	private String TAG = LruImageCache.class.getSimpleName();
	public LruImageCache() {
		super();
		// Get max available VM memory, exceeding this amount will throw an
	    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
	    // int in its constructor.
	    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
	    // Use 1/8th of the available memory for this memory cache. get 20% of memory
	    	double memory = maxMemory*.20;
	    	int DEFAULT_MEM_CACHE_SIZE = (int)memory;
	     Log.d(TAG, "Memory cache assigned : " + DEFAULT_MEM_CACHE_SIZE + " Kb");
	    lruImageCache = new LruCache<String, Bitmap>(DEFAULT_MEM_CACHE_SIZE);		  
	}

	public void addBitmapIconToMemoryCache(String key, Context context) {		
		Bitmap bitmap2 = generateBitmapFromIconTableWithID(context, key);
		lruImageCache.put(key, bitmap2);
	}

	public Bitmap getBitmapIconFromMemCache(String key,Context context) {
	    if(lruImageCache.get(key)==null)
	    {
	    	addBitmapIconToMemoryCache(key,context);
	    	return lruImageCache.get(key);
	    }
	    return lruImageCache.get(key);
	}
	
	private Bitmap generateBitmapFromIconTableWithID(Context context, String key){
		Icon i = Icon.query(context, Uri.withAppendedPath(Icon.CONTENT_URI,key));
		byte [] bytes = i.getImage();
		i.close();
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);		
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
	    return lruImageCache.get(key);
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {			
		lruImageCache.put(key, bitmap);
	}
	
	/**
     * Clears both the memory and disk cache associated with this ImageCache object.
     */
    public void clearCache() {
        if (lruImageCache != null) {
            lruImageCache.evictAll();
            lruImageCache=null;
            Log.d(TAG, "Memory Image cache cleared");
         
        }
    }
}
