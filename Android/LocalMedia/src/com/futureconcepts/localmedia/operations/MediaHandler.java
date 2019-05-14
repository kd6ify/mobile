package com.futureconcepts.localmedia.operations;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.futureconcepts.localmedia.database.DatabaseHelper;
import com.futureconcepts.localmedia.database.LocalMediaTable;

public class MediaHandler {
	 public static final String PREFS_NAME = "localMediaPreferences";
	//Media Status
	public static final String STATUS_INCOMPLETE="incomplete";
	public static final String STATUS_COMPLETE="complete";
	//Media Action
	public static final String ACTION_UPLOAD="upload";
	public static final String ACTION_DOWNLOAD="download";
	//Media Action
	public static final String CURRENT_STATE_UPLOADING="uploading";
	public static final String CURRENT_STATE_DOWNLOADING="downloading";
	//Media fail Status
	public static final String STATUS_FAIL_NO="no";
	public static final String STATUS_FAIL_YES="yes";
	
	public static final int insertRow=1;
	public static final int updateRow=2;
	
	public final static String rootDirectoryFolderPathForImages = Environment.getExternalStorageDirectory()+"/Trinity/";
	/**
	 * Add new media records to local database for upload or download.
	 */
	
	public static void updateDataBase(Context context)
	{
		 // Restore preferences
	       SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
	       int databaseVersion = settings.getInt("databaseVersion", 1);
	      if(databaseVersion<DatabaseHelper.DATABASE_VERSION)
	      {
	    	 DatabaseHelper dbHelper = new DatabaseHelper(context);
	    	// dbHelper.onUpgrade(database, oldVersion, newVersion)
	    	 SQLiteDatabase newDB = dbHelper.getWritableDatabase();	    	
	    	 LocalMediaTable.onUpgrade(newDB, databaseVersion, DatabaseHelper.DATABASE_VERSION);
	    	 newDB.close();
		   	 SharedPreferences.Editor editor = settings.edit();
		   	 editor.putInt("databaseVersion",DatabaseHelper.DATABASE_VERSION);
	         editor.commit(); 
	         Log.d("MEDIA HANDLER", "the database was updated");
	      }else
	      {
	    	   Log.d("MEDIA HANDLER", "No need update database: "+databaseVersion);
	      }
	}
	
	public static void addMedia(Context context, String ID, String filePath, String action, int fileSize, String journalEntryID) {
		//updateDataBase(context);//check if we need to update the database	
		Cursor localMedia = queryLocalMediaID(context, ID, null);
		if (localMedia.getCount() == 0) {//new media
			String date;
			ContentValues values = new ContentValues();
			values.put(LocalMediaTable.COLUMN_ID, ID);
			values.put(LocalMediaTable.COLUMN_BYTES_STORED, 0);
			if (action == ACTION_DOWNLOAD) {			
				Date lastModified = new Date();
				date = "" + lastModified.getTime();
				values.put(LocalMediaTable.COLUMN_FILE_SIZE, fileSize);
			} else {
				date = getPhotoLastModifiedDate(filePath);
				values.put(LocalMediaTable.COLUMN_FILE_SIZE,
						getFileSize(filePath));
			}
			values.put(LocalMediaTable.COLUMN_FILEPATH, filePath);
			values.put(LocalMediaTable.COLUMN_LASTMODIFIED_DATE, date);
			values.put(LocalMediaTable.COLUMN_ACTION, action);
			values.put(LocalMediaTable.COLUMN_STATUS,STATUS_INCOMPLETE);
			values.put(LocalMediaTable.COLUMN_ENTRY_ID,journalEntryID);
			context.getContentResolver().insert(LocalMediaTable.CONTENT_URI,
					values);
		} else {
			ContentValues values = new ContentValues();
			values.put(LocalMediaTable.COLUMN_ID, ID);
			values.put(LocalMediaTable.COLUMN_FILEPATH, filePath);
			values.put(LocalMediaTable.COLUMN_BYTES_STORED, 0);
			values.put(LocalMediaTable.COLUMN_STATUS, STATUS_INCOMPLETE);
			insertUpdateMediaWithContentValues(LocalMediaTable.CONTENT_URI,context, values, updateRow,LocalMediaTable.COLUMN_ID+"=?",new String[]{ID});
		}
			localMedia.close();
	}
	
	/**
	 * @param context
	 * @param ID
	 * @param bytes total size of the file download or uploaded
	 * @param status complete or incomplete
	 */
	public static void updateMedia(Context context, String ID,long bytes,String status) {
		//updateDataBase(context);//check if we need to update the database
		ContentValues values = new ContentValues();
		values.put(LocalMediaTable.COLUMN_ID, ID);
		values.put(LocalMediaTable.COLUMN_BYTES_STORED, bytes);
		values.put(LocalMediaTable.COLUMN_STATUS, status);
		if(STATUS_COMPLETE.equals(status)){
			values.put(LocalMediaTable.COLUMN_FAIL,STATUS_FAIL_NO);
		}
		context.getContentResolver().update(LocalMediaTable.CONTENT_URI, values, LocalMediaTable.COLUMN_ID+"=?", new String[]{ID});
	}
	
	public static void insertUpdateMediaWithContentValues(Uri Table,Context context,ContentValues values, int type,String where, String[] whereArgs) {
		//updateDataBase(context);//check if we need to update the database
		//String ID = values.getAsString(LocalMediaTable.COLUMN_ID);
		switch(type)
		{
			case updateRow:
				context.getContentResolver().update(Table, values,where ,whereArgs);
				break;
			case insertRow:
				context.getContentResolver().insert(Table, values);
				break;
			default:
				throw new UnsupportedOperationException(
						"Unknown Action to execute in updateMediaWithContentValues Action: " + type);
		}		
	}
	
	public static void updateMediaError(Context context, String currentImageID, String failStatus)
	{
		//updateDataBase(context);//check if we need to update the database
		ContentValues values = new ContentValues();
		values.put(LocalMediaTable.COLUMN_ID, currentImageID);
		values.put(LocalMediaTable.COLUMN_FAIL, failStatus);
		context.getContentResolver().update(LocalMediaTable.CONTENT_URI, values,  LocalMediaTable.COLUMN_ID+"=?", new String[]{currentImageID});
	}
	
	public static void updateMediaFilePath(Context context, String ID, String FilePath)
	{
		//updateDataBase(context);//check if we need to update the database
		ContentValues values = new ContentValues();
		values.put(LocalMediaTable.COLUMN_ID, ID);
		values.put(LocalMediaTable.COLUMN_FILEPATH, FilePath);
		context.getContentResolver().update(LocalMediaTable.CONTENT_URI, values,  LocalMediaTable.COLUMN_ID+"=?", new String[]{ID});
	}
	
	public static Cursor queryLocalMediaIdWithError(Context context, String ID) {	
		//updateDataBase(context);//check if we need to update the database
		return context.getContentResolver().query(LocalMediaTable.CONTENT_URI, LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION, LocalMediaTable.COLUMN_ID+"=? AND " +LocalMediaTable.COLUMN_FAIL+" =?", new String []{ID,STATUS_FAIL_YES}, null);
	}
	
	public static Cursor queryLocalMediaID(Context context, String ID,String sortOrder) {	
		//updateDataBase(context);//check if we need to update the database
		return context.getContentResolver().query(LocalMediaTable.CONTENT_URI, LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION, LocalMediaTable.COLUMN_ID+"=?", new String []{ID}, sortOrder);
	}
	
	public static Cursor queryMedia(Uri table, String [] projection,String where, String[] selectionArgs,Context context, String sortOrder) {	
		//updateDataBase(context);//check if we need to update the database
		return context.getContentResolver().query(table, projection, where, selectionArgs, sortOrder);
	}
	
	public static void deleteMediaWithID(Uri Table,Context context, String where,String[] whereArgs) {
		//updateDataBase(context);//check if we need to update the database
		context.getContentResolver().delete(Table, where,whereArgs);
	}
	
	public static void madeFailImagesAvailable(Context context, String action, String journalEntryID)
	{
		//updateDataBase(context);//check if we need to update the database
		Cursor images = context.getContentResolver().query(LocalMediaTable.CONTENT_URI,
				LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,
				LocalMediaTable.COLUMN_ACTION+"=? AND "+
				LocalMediaTable.COLUMN_STATUS+"=? AND "+
				LocalMediaTable.COLUMN_ENTRY_ID+"=? AND "+
				LocalMediaTable.COLUMN_FAIL+"=?",new String[]{action,STATUS_INCOMPLETE,journalEntryID,STATUS_FAIL_YES},null);
		if(images.getCount()>0)
		{
			images.moveToFirst();
			do
			{				
				updateMediaError(context,images.getString(images.getColumnIndex(LocalMediaTable.COLUMN_ID)),STATUS_FAIL_NO);
			}while(images.moveToNext());
			images.close();
		}
	}
	
	/**
	 * @param context
	 * @param action download || upload
	 * @return a list of ImageObject found on the local database based on action
	 */
	public static LinkedList<ImageObject> getAllMediaToDownload(Context context, String action, String journalEntryID)
	{
		//updateDataBase(context);//check if we need to update the database
		LinkedList<ImageObject> toUpload = new LinkedList<ImageObject>();
		Cursor images = context.getContentResolver().query(LocalMediaTable.CONTENT_URI, LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,LocalMediaTable.COLUMN_ACTION+"=? AND "+LocalMediaTable.COLUMN_STATUS+"=? AND "+LocalMediaTable.COLUMN_ENTRY_ID+"=? AND "+LocalMediaTable.COLUMN_FAIL+"=?",new String[]{action,STATUS_INCOMPLETE,journalEntryID,STATUS_FAIL_NO},null);
		if(images.getCount()>0){
		images.moveToFirst();			
				do {
					toUpload.add(createImageObject(images));
				} while (images.moveToNext());
		}
		images.close();
		return toUpload;		
	}
	
	/**
	 * @param context
	 * @param action upload
	 * @return a list of ImageObject found on the local database based on action
	 */
	public static LinkedList<ImageObject> getAllMedia(Context context, String action)
	{
		//updateDataBase(context);//check if we need to update the database
		LinkedList<ImageObject> toUpload = new LinkedList<ImageObject>();
		Cursor images = context.getContentResolver().query(LocalMediaTable.CONTENT_URI, LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,LocalMediaTable.COLUMN_ACTION+"=? AND "+LocalMediaTable.COLUMN_STATUS+"=?",new String[]{action,STATUS_INCOMPLETE},null);
		if(images.getCount()>0){
			images.moveToFirst();
			do {
				toUpload.add(createImageObject(images));
			} while (images.moveToNext());
		}
		images.close();
		return toUpload;		
	}
	
	/**
	 * @param context
	 * @param action upload
	 * @return a list of ImageObject found on the local database based on action
	 */
	public static ImageObject getOneMedia(Context context, String action, String ID)
	{		
		Cursor images = context.getContentResolver().query(LocalMediaTable.CONTENT_URI, LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,LocalMediaTable.COLUMN_ID+"=? AND "+LocalMediaTable.COLUMN_ACTION+"=? AND "+LocalMediaTable.COLUMN_STATUS+"=?",new String[]{ID,action,STATUS_INCOMPLETE},null);
		if(images.getCount()>0){
			images.moveToFirst();			
			return createImageObject(images);			
		}
		images.close();
		return null;		
	}
	
	/**
	 * Create ImageObject with the given cursor.
	 * @param image
	 * @return 
	 */
	public static ImageObject createImageObject(Cursor image)
	{
		return new ImageObject(image.getString(image.getColumnIndex(LocalMediaTable.COLUMN_ID)),
				image.getInt(image.getColumnIndex(LocalMediaTable.COLUMN_BYTES_STORED)),
				image.getInt(image.getColumnIndex(LocalMediaTable.COLUMN_FILE_SIZE)),
				image.getString(image.getColumnIndex(LocalMediaTable.COLUMN_FILEPATH)),
				image.getString(image.getColumnIndex(LocalMediaTable.COLUMN_LASTMODIFIED_DATE)),
				image.getString(image.getColumnIndex(LocalMediaTable.COLUMN_ACTION)),
				image.getString(image.getColumnIndex(LocalMediaTable.COLUMN_STATUS)));
	}
	
	/**
	 * @param context
	 * @param filePath
	 * @return id of the media or null if the media was never send in other log entry
	 */
	public static String verifyIsNewMedia(final Context context, final String filePath) {
		String id = null; 
		Cursor image;
		if (!filePath.contains(Environment.getExternalStorageDirectory()+ "/Trinity/")) {//path of media in phone
			final long size = getFileSize(filePath);
			final String creationDate = getPhotoLastModifiedDate(filePath);
			image = context.getContentResolver().query(LocalMediaTable.CONTENT_URI,LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,
					LocalMediaTable.COLUMN_FILE_SIZE+"=? AND "+
							LocalMediaTable.COLUMN_FILEPATH+"=? AND "+
							LocalMediaTable.COLUMN_LASTMODIFIED_DATE+"=?",
							new String[] { String.valueOf(size), filePath,creationDate }, null);
		} else {//the path is from server
			image = context.getContentResolver().query(LocalMediaTable.CONTENT_URI,LocalMediaTable.MEDIA_ACTIVITY_TABLE_PROJECTION,
					LocalMediaTable.COLUMN_FILEPATH+"=?", new String[] { filePath }, null);
			}
		try {
			if (image.getCount() > 0) {
				image.moveToFirst();
				id = image.getString(image.getColumnIndex("ID"));
			}
			image.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return id;
	}
	
	/**
	 * @param filePath
	 * @return the lastModified date of the file
	 */
	private static String getPhotoLastModifiedDate(String filePath) {
		File file = new File(filePath);
		 Date lastModified = null;
		if (file.exists()) {
			lastModified = new Date(file.lastModified());
		}
		if(lastModified !=null)
		{
			return ""+lastModified.getTime();
		}else {
			lastModified = new Date();
			file.setLastModified(lastModified.getTime());
			return ""+lastModified.getTime();					
		}
		
	}
	
	/**
	 * @param filepath 
	 * @return the size of the file or 0 if the file not exists.
	 */
	public static long getFileSize(String filepath)
	{
		File image = new File(filepath);
		if(image.exists()){
			return image.length();
		}else
		{
			return 0;
		}
	}
	
	
	/**
	 * @param id: used as name of the image.
	 * @param filePath: used to get the extension of the media.
	 * @return The path for this media based on phone paths.
	 */
	public static String localPathForDownloadedImage(String id, String filePath)
	{
		File file = new File(filePath);
		String filename = file.getName();
		String mimeType = filename.substring(filename.lastIndexOf('.') + 1);		
		//Create path for a Temp file.. when the file get complete is renamed.
		String pathOfDownloadedImages =rootDirectoryFolderPathForImages+"TEMP"+id+"."+mimeType;
		return pathOfDownloadedImages;
	}
}
