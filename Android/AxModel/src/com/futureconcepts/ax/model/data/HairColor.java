package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class HairColor extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/HairColor");

	public HairColor(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static HairColor query(Context context, Uri uri)
	{
		HairColor result = null;
		try
		{
			result = new HairColor(context, context.getContentResolver().query(uri, null, null, null, null));
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
