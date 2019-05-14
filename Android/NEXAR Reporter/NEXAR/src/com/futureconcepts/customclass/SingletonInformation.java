package com.futureconcepts.customclass;

import java.io.File;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

import com.futureconcepts.database.AnonymousInfoMediaTable;
import com.futureconcepts.database.MediaTable;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

public class SingletonInformation extends Application {

	public LinkedList<String> pathImages= new LinkedList<String>();
	public LinkedList<Bitmap> bitmapImages= new LinkedList<Bitmap>();
	public LinkedList<String> reports= new LinkedList<String>();
	public int dataSizeSend=10;
	public  boolean activitySettingsVisible;//1
	public  boolean activityPendingVisible;//2
	public  boolean activityCategoryVisible;//3
	public Context currentCon;
	
	private static SingletonInformation singleton;
	
	public static SingletonInformation getInstance() {

		return singleton;
	}
	
	@Override
	public void onCreate() {
	super.onCreate();
	singleton = this;
	}
	
	public void saveData(Context context,String reportId, String date, String IDCategory){
	
		if (pathImages.size()>0)
    	{
		for(int a=0; a<pathImages.size(); a++)
			{
	 
	 			UUID id_media = UUID.randomUUID();
	 			String media_id = id_media.toString();
	 			File fileImage = new File(pathImages.get(a));
	 			String fileName = fileImage.getName().toLowerCase(Locale.getDefault());
	 			int size=(int) (fileImage.length());
		        String type="00000000-3b9a-ca00-0000-00003be71540";    
        
	     //Insert data of the image on the Media table and AnonymousInfoMediaTable
        ContentValues values2 = new ContentValues();
		    values2.put(MediaTable.COLUMN_ID, media_id);
		    values2.put(MediaTable.COLUMN_NAME, fileName);	
		    values2.put(MediaTable.COLUMN_FILE, pathImages.get(a));
		    values2.put(MediaTable.COLUMN_TYPE, type);
		    values2.put(MediaTable.COLUMN_SIZE, size);
		    values2.put(MediaTable.COLUMN_Status,"0");
		    values2.put(MediaTable.COLUMN_PartsSent,0);
			values2.put(MediaTable.COLUMN_Date,date);		    
	        context.getContentResolver().insert(MediaTable.CONTENT_URI, values2); 
	        
	    	ContentValues values4 = new ContentValues();
	  		values4.put(AnonymousInfoMediaTable.COLUMN_ID_MEDIA, media_id);
	  		values4.put(AnonymousInfoMediaTable.COLUMN_ID_ANONYMOUS_ACTIVITY, reportId);
	  		values4.put(AnonymousInfoMediaTable.COLUMN_TYPE, IDCategory);
	  	    context.getContentResolver().insert(AnonymousInfoMediaTable.CONTENT_URI, values4);
			}
    	}
	
	}
	
	
	 

}
