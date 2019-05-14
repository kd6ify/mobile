package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class BloodType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/BloodType");

	public BloodType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static BloodType query(Context context, Uri uri)
	{
		BloodType result = null;
		try
		{
			result = new BloodType(context, context.getContentResolver().query(uri, null, null, null, null));
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
