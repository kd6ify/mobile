package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Gender extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Gender");

    public static final String UNKNOWN = new String("5F630000-D713-645D-6262-93AC9431C556");

	public Gender(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	public static Gender query(Context context)
	{
		Gender result = null;
		try
		{
			result = new Gender(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	public static Gender query(Context context, Uri uri)
	{
		Gender result = null;
		try
		{
			result = new Gender(context, context.getContentResolver().query(uri, null, null, null, null));
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
