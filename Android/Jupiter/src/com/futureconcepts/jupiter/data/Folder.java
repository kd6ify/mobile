package com.futureconcepts.jupiter.data;

import java.util.UUID;

import com.futureconcepts.jupiter.Config;
import com.futureconcepts.jupiter.data.BaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Folder extends BaseTable
{
	public static final String ID = "id";
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";

	public static final String AUTHORITY = "com.futureconcepts.jupiter.provider.folder";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/folder");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.jupiter.Folder";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.jupiter.Folder";
    
    public static final String NAME_TRIPS = "Trips";
    public static final String NAME_ROUTES = "Routes";
    public static final String NAME_TRACKS = "Tracks";
    public static final String NAME_MEDIA = "Media";
    public static final String NAME_PLACEMARKS = "Placemarks";
        
    public Folder(Cursor cursor)
    {
    	super(cursor);
    }

    public String getId()
    {
    	return getString(getColumnIndex(ID));
    }
    
    public String getName()
    {
    	return getString(getColumnIndex(NAME));
    }

    public String getDescription()
    {
    	return getString(getColumnIndex(DESCRIPTION));
    }
    
    public static Folder getFolder(Context context, Uri uri)
    {
    	Folder folder = null;
    	if (uri != null)
    	{
    		folder = new Folder(context.getContentResolver().query(uri, null, null, null, null));
    		if (folder != null)
    		{
    			if (folder.getCount() == 1)
    			{
    				folder.moveToFirst();
    			}
    			else
    			{
    				folder.close();
    				folder = null;
    			}
    		}
    	}
    	return folder;
    }
    
    public static Folder getFolderById(Context context, String id)
    {
    	Folder folder = new Folder(context.getContentResolver().query(CONTENT_URI, null, ID + "='" + id + "'", null, null));
    	if (folder != null)
    	{
	    	if (folder.getCount() == 1)
	    	{
	    		folder.moveToFirst();
	    	}
	    	else
	    	{
	    		folder.close();
	    		folder = null;
	    	}
    	}
    	return folder;
    }

    public static Folder getFoldersByParentId(Context context, String id)
    {
		String whereClause = PARENT_ID + "='" + id + "'";
    	return new Folder(context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
    }

    public static Folder getFolderByParentIdAndName(Context context, String parentId, String name)
    {
    	Folder folder = null;
    	String whereClause = null;
    	if (parentId != null)
    	{
    		whereClause = PARENT_ID + "='" + parentId + "' and " + NAME + "='" + name + "'";
    	}
    	else
    	{
    		whereClause = PARENT_ID + " ISNULL and " + NAME + "='" + name + "'";
    	}
    	folder = new Folder(context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
    	if (folder != null)
    	{
    		if (folder.getCount() == 1)
    		{
    			folder.moveToFirst();
    		}
    		else
    		{
    			folder.close();
    			folder = null;
    		}
    	}
    	return folder;
    }

    public static Folder getFolderByParentId(Context context, String parentId, String name)
    {
    	Folder folder = getFolderByParentIdAndName(context, parentId, name);
    	if (folder != null)
    	{
    		if (folder.getCount() != 1)
    		{
    			folder.close();
    			folder = null;
    		}
    	}
    	if (folder == null)
    	{
			ContentValues values = new ContentValues();
			String id = UUID.randomUUID().toString().toLowerCase();
			values.put(Folder.ID, id);
			values.put(Folder.NAME, name);
			values.put(Folder.DESCRIPTION, name);
			values.put(Folder.PARENT_ID, parentId);
			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
			Uri uri = context.getContentResolver().insert(Folder.CONTENT_URI, values);
			if (uri != null)
			{
		    	folder = new Folder(context.getContentResolver().query(uri, null, null, null, null));
			}
    	}
    	if (folder != null && folder.getCount() == 1)
    	{
    		folder.moveToFirst();
    	}
    	return folder;
    }

    public static Folder getTripFolder(Context context, String name)
    {
    	String tripId = Config.getInstance(context).getTripId();
    	return getFolderByParentId(context, tripId, name);
    }
}
