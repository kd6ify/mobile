package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AssetStatus extends IndexedType
{
    /// <summary>
    /// Fixed id for the Asset's Unknown status
    /// </summary>
    public static final String UNKNOWN = "D497F299-AF3A-1956-7E21-84B1B3947FD5";
	
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AssetStatus");

	public AssetStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static AssetStatus query(Context context)
	{
		return query(context, CONTENT_URI);
	}

	public static AssetStatus query(Context context, Uri uri)
	{
		AssetStatus result = null;
		try
		{
			result = new AssetStatus(context, context.getContentResolver().query(uri, null, null, null, null));
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
