package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Icon extends IndexedType
{
    public static final String NAME = "Name";
    public static final String IMAGE = "Image";
    public static final String FILENAME = "FileName";
    public static final String SORT = "Sort";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Icon");
    
	public Icon(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public String getName()
	{
		int idx = getColumnIndex(NAME);
		assert(idx != -1);
		return getString(idx);
	}

	public byte[] getImage()
	{
		return getBlob(getColumnIndex(IMAGE));
	}
	
	public String getFileName()
	{
		return getString(getColumnIndex(FILENAME));
	}
	
	public int getSort()
	{
		return getInt(getColumnIndex(SORT));
	}
	
	public static Icon query(Context context)
	{
		Icon result = null;
		try
		{
			result = new Icon(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Icon query(Context context, Uri uri)
	{
		Icon result = null;
		try
		{
			result = new Icon(context, context.getContentResolver().query(uri, null, null, null, null));
			if (result.getCount() == 1)
			{
				result.moveToFirst();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
