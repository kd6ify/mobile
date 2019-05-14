package com.futureconcepts.ax.trinity.osm;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidBitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;

import android.support.v4.util.LruCache;
import android.util.Log;

public class LruOSMIconsCache {

	private LruCache<String, AndroidBitmap> iconsCache;


	public LruOSMIconsCache() {				
		iconsCache = new LruCache<String, AndroidBitmap>(getCacheSize());
	}
	
	private int getCacheSize()
	{
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024)/1024;
		final int total = (int) (Runtime.getRuntime().totalMemory() / 1024)/1024;
		//final int free = (int) (Runtime.getRuntime().freeMemory() / 1024);
		int DEFAULT_MEM_CACHE_SIZE = (int) 700*1024;//Defualt cache =.5mb
		if((maxMemory-total)<.700)//1mib
		{
			DEFAULT_MEM_CACHE_SIZE =(maxMemory-total)/2;//(int)(maxMemory*5)/100;
		}
		 Log.d("ImageManager", "Memory cache assigned : " + DEFAULT_MEM_CACHE_SIZE + " bits");
		return DEFAULT_MEM_CACHE_SIZE;
	}
	
	public LruCache<String, AndroidBitmap> getIconsCache() {
		return iconsCache;
	}
	
	public  void addIconToCache(String key,AndroidBitmap bitmap)
	{
		this.iconsCache.put(key, bitmap);
	}
	public  AndroidBitmap getIconFromCache(String key)
	{
		return this.iconsCache.get(key);
	}
	
}
