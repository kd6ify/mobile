package com.futureconcepts.jupiter.data;

import java.io.IOException;

import org.w3c.util.InvalidDateException;

import com.futureconcepts.jupiter.data.BaseTable;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class MediaMetadata extends BaseTable
{
    public static final String AUTHORITY = "com.futureconcepts.provider.media";

    public static final String ID = "Id";
    public static final String NAME = "Name";
    public static final String NOTES = "Notes";
    public static final String MIME_TYPE = "MimeType";
    public static final String SERVER_PATH = "ServerPath";
    public static final String DEVICE_PATH = "DevicePath";
    public static final String LENGTH = "Length";
    public static final String CHECKSUM = "Checksum";
    public static final String EXPIRATION_DATE = "ExpirationDate";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/media");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.MediaMetadata";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.MediaMetadata";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

	public MediaMetadata(Cursor cursor)
	{
		super(cursor);
	}

	public String get_id()
	{
		return getString(getColumnIndex(_ID));
	}
	
	public String getId()
	{
		return getString(getColumnIndex(ID));
	}

	public String getName()
	{
		return getString(getColumnIndex(NAME));
	}

	public String getNotes()
	{
		return getString(getColumnIndex(NOTES));
	}

	public String getMimeType()
	{
		return getString(getColumnIndex(MIME_TYPE));
	}
	
	public String getServerPath()
	{
		return getString(getColumnIndex(SERVER_PATH));
	}
	
	public String getDevicePath()
	{
		return getString(getColumnIndex(DEVICE_PATH));
	}
		
	public long getLength()
	{
		return getInt(getColumnIndex(LENGTH));
	}
	
	public long getChecksum()
	{
		return getLong(getColumnIndex(CHECKSUM));
	}
	
	public long getExpirationDate()
	{
		return getLong(getColumnIndex(EXPIRATION_DATE));
	}
}
