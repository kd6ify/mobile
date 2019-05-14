package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SourceType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/SourceType");

    public static final String LARCOPP = new String("FFD9A747-A409-2386-AF45-E92EBFB526A8");
    
	public SourceType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static SourceType query(Context context)
	{
		SourceType result = null;
		try
		{
			result = new SourceType(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
