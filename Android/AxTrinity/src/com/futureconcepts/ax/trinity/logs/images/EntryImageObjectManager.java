package com.futureconcepts.ax.trinity.logs.images;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.futureconcepts.ax.model.data.Guid;
import com.futureconcepts.ax.model.data.JournalEntryMedia;
import com.futureconcepts.ax.model.data.Media;
import com.futureconcepts.localmedia.operations.MediaHandler;

public class EntryImageObjectManager {
	public static  ArrayList<EntryImageObject> images = new ArrayList<EntryImageObject>();
	public static  HashMap<String,Integer> relationIDPosition  = new HashMap<String,Integer>();
	public static String currentJournalEntry;
	public static final int NO_NEED_DOWNLOAD = 0;
	public static final int NEED_DOWNLOAD = 1;
	public static final int IS_DOWNALODING = 2;
	public static final int SendImagesService = 0;
	public static final int DownloadImageService = 1;
	
	public static int imagesOnDatabase=0;
	public static boolean isLiteVersion = false;	
	
	public static void imagesOnDatabase(int totalImages)
	{
		imagesOnDatabase =totalImages;
	}

	/**
	 * Insert all image information on Media table; save relation with JournalEntry on JournalEntryMedia Table
	 *@param context: who call method
	 *@param JournalEntryID
	 **/	
	public static List<String> insertMedia(Context context, String journalEntrID,String action) {
		List<String> medias = new ArrayList<String>();//new String[images.size()-imagesOnDatabase];
		for (int i = imagesOnDatabase; i < images.size(); i++) {
			String media_id = null;
			String idOfSavedMedia = MediaHandler.verifyIsNewMedia(context,images.get(i).getImagePath());
			if (idOfSavedMedia == null) {
				media_id=Guid.newGuid().toString();
				Uri uri = Media.insertMedia(context, media_id, images.get(i).getImagePath(), action);
				if (uri != null) {
					Log.d("EditModelActivity", "Images saved");
					MediaHandler.addMedia(context, media_id,images.get(i).getImagePath(),MediaHandler.ACTION_UPLOAD,0,journalEntrID);
					// Log.i("EntryImageObjects","Media moved: "+moveMediaFile(media_id,images.get(i).getImagePath(),context));
				} else {
					Log.d("EditModelActivity", "Images not saved");
				}
			} else {
				Log.d("EditModelActivity", "Image uploaded in previous log");
				media_id = idOfSavedMedia;
			}
			medias.add(media_id);
		}
		return medias;
	}
	
	
	public static void insertJournalEntryMedia(Context context,List<String> medias, String journalEntrID,String action) {
		for (String mediaId : medias) {
			Uri uri2 = JournalEntryMedia.insertMediaRelation(context, mediaId,journalEntrID, action);
			if (uri2 != null) {
				Log.d("EditModelActivity", "Images Relation saved");
			} else {
				Log.d("EditModelActivity", "Images Relation not saved");
			}
		}
	}

	/**
	 * @param context
	 * @param service 0: SendImagesToServer service; 1:DownloadImageFromServer service.
	 */
	public static void callService(Context context, int service, String journalEntryID) {
			switch(service)
			{
			case SendImagesService:					
						Intent intent = new Intent(context,SendImageToServer.class);
						context.startService(intent);
			break;
			case DownloadImageService:
					if (MediaHandler.getAllMediaToDownload(context,MediaHandler.ACTION_DOWNLOAD,journalEntryID).size() > 0) {
						Toast.makeText(context, "Downloading Image....", Toast.LENGTH_SHORT).show();
						Intent msgIntent = new Intent(context,DownloadImagesFromServer.class);
						msgIntent.putExtra("JournalEntryID",journalEntryID );
						context.startService(msgIntent);
					} else {
						Log.i("EntryImageObject", "No images to download");
					}										
				break;
			}			
	}

	public static void verifyNetworkAndDownloadImage(Context context,String mediaID,String filePath,int fileSize, String journalEntryID)
	{
		if(!isLiteVersion){
			MediaHandler.addMedia(context, mediaID, MediaHandler.localPathForDownloadedImage(mediaID,filePath), MediaHandler.ACTION_DOWNLOAD,fileSize,journalEntryID);				
			callService(context,DownloadImageService,journalEntryID);							
		}else{
		Toast.makeText(context, "Upgrade to full version to perform this action.", Toast.LENGTH_SHORT).show();
		}
	}
}
