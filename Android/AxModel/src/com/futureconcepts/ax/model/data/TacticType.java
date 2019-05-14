package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TacticType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/TacticType");

	public TacticType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static TacticType query(Context context, Uri uri)
	{
		TacticType result = null;
		try
		{
			result = new TacticType(context, context.getContentResolver().query(uri, null, null, null, TacticType.NAME));
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
