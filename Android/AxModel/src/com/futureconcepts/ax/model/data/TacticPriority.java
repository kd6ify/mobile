package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TacticPriority extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/TacticPriority");
    
    public static final String NONE = "E6C6E51E-3940-4C42-9A19-8EAD673605D3";
    public static final String NORMAL = "52A5CEE3-4624-429F-9562-A7E620B448FD";
    public static final String IMMEDIATE = "D28260DF-7368-4FAE-8E44-60D61B00007C";

	public TacticPriority(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static TacticPriority query(Context context, Uri uri)
	{
		TacticPriority result = null;
		try
		{
			result = new TacticPriority(context, context.getContentResolver().query(uri, null, null, null, null));
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
