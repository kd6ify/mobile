package com.futureconcepts.ax.model.data;

import java.io.File;
import org.joda.time.DateTime;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.util.Log;

public class Media extends SourcedTable {
	
	  public static final String MEDIA_NAME = "Name";
	  public static final String MEDIA_NOTES = "Notes";
	  public static final String MEDIA_MIMETYPE = "MimeType";
//	  public static final String MEDIA_APPLICATION = "Application";
	  public static final String MEDIA_CREATIONDATE = "CreationDate";
//	  public static final String MEDIA_EXPIRATIONDATE = "ExpirationDate";
	  public static final String MEDIA_LENGTH = "Length";
//	  public static final String MEDIA_THUMBNAILDATA = "ThumbnailData";
//	  public static final String MEDIA_THUMBNAILMIMETYPE = "ThumbnailMimeType";
//	  public static final String MEDIA_APPLICATIONEXTENSION = "ApplicationExtension";
//	  public static final String MEDIA_METADATAEXTENSION = "MetadataExtension";
//	  public static final String MEDIA_CREATOR = "Creator";
//	  public static final String MEDIA_ORIGIN = "Origin";
//	  public static final String MEDIA_DATA = "MediaData";
//	  public static final String MEDIA_CHECKSUM = "CheckSum";
//	  public static final String MEDIA_LASTMODIFIED = "LastModified";
	  public static final String MEDIA_OWNER = "Owner";
	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Media");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Media";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Media";

 
	public Media(Context context, Cursor cursor) {
		super(context, cursor);
	}
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			//setMediaId(getCursorGuid(ID));
			setMediaName(getCursorString(MEDIA_NAME));
			setMediaNotes(getCursorString(MEDIA_NOTES));
			setMediaType(getCursorString(MEDIA_MIMETYPE));
			setMediaSize(getCursorInt(MEDIA_LENGTH));
			setMediaCreationDate(getCursorDateTime(MEDIA_CREATIONDATE));			
			//setMediaLastModifiedDate(getCursorString(MEDIA_LASTMODIFIED));
		}
	}
	
	/**
	 * Get, Set Media_id
	 * 
	*/
//	public void setMediaId(String id)
//	{
//		setModel(ID,id);
//	}
//	
	/**
	 * Get, Set Media_name
	 * 
	*/
	public String getMediaName()
	{
		return getModelString(MEDIA_NAME);
	}
	
	public void setMediaName(String name)
	{
		setModel(MEDIA_NAME,name);
	}
	/**
	 * Get, Set Media_filepath
	 * 
	*/
	public String getMediaNotes()
	{
		return getModelString(MEDIA_NOTES);
	}
	
	public void setMediaNotes(String filepath)
	{
		setModel(MEDIA_NOTES,filepath);
	}
	/**
	 * Get, Set Media_type
	 * 
	*/
	public String getMediaType()
	{
		return getModelString(MEDIA_MIMETYPE);
	}
	
	public void setMediaType(String type)
	{
		setModel(MEDIA_MIMETYPE,type);
	}
	/**
	 * Get, Set Media_size
	 * 
	*/
	public int getMediaSize()
	{
		return getModelInt(MEDIA_LENGTH);
	}
	
	public void setMediaSize(int size)
	{
		setModel(MEDIA_LENGTH,size);
	}

	/**
	 * Get, Set Media_date
	 * 
	*/
	public DateTime getMediaCreationDate()
	{
		return getModelDateTime(MEDIA_CREATIONDATE);
	}
	
	public void setMediaCreationDate(DateTime date)
	{
		setModel(MEDIA_CREATIONDATE,date);
	}
	
	/**
	 * Get, Set OWNER
	 * 
	*/
	public String getMediaOwner()
	{
		return getModelString(MEDIA_OWNER);
	}
	
	public void setMediaOwner(String owner)
	{
		setModel(MEDIA_OWNER,owner);
	}
	
	public static Media queryMedia(Context context)
	{
		Media result = null;
		try
		{
			result = new Media(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static Media queryMediaId(Context context,String ID)
	{
		return new Media(context, context.getContentResolver().query(CONTENT_URI, null,"ID='"+ID+"'", null,null));
	}
	
	public static Uri insertMedia(Context context,String id, String _filePath, String action)
	{
		//Create id for Media Table      		
	    File fileImage = new File(_filePath);
        String fileName = fileImage.getName();
        int size=(int) (fileImage.length());
        String filenameArray[] = fileName.split("\\.");
        String extension = filenameArray[filenameArray.length-1];		
		Uri result =null;
		DateTime now = DateTime.now();
		Media media = new Media(context,null);
		media.beginEdit();			
		media.setID(id);
		media.setMediaName(fileName);
		media.setMediaNotes("ImagesServer/MobileMedia/Trinity/"+id+"."+extension);
		media.setMediaType(extension);
		media.setMediaSize(size);
		media.setMediaCreationDate(now);
		//media.setOwner(Secure.getString(context.getContentResolver(),Secure.ANDROID_ID));
		//media.setMediaLastModifiedDate(now.toLocalDateTime().toString("MMddyyyy"));
		try
		{
			result = context.getContentResolver().insert(CONTENT_URI, media.endEditAndUpload(action));
		}catch (Exception e)
		{
			Log.i("MediaModelError",""+e.toString());
		}
		//media.endEditAndUpload(uploadAction)
		return result;
	}
	
	
}
