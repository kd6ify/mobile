package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class CollectionType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/CollectionType");

	public CollectionType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static CollectionType query(Context context)
	{
		return query(context, CONTENT_URI);
	}

	public static CollectionType query(Context context, Uri uri)
	{
		CollectionType result = null;
		try
		{
			result = new CollectionType(context, context.getContentResolver().query(uri, null, null, null, null));
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
