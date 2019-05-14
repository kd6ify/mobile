package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class ISO3166 extends BaseTable
{
	public static final String CODE = "Code";
	public static final String NAME = "Name";
    public static final String IDD = "IDD";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/ISO3166");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.ISO3166";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.ISO3166";

	public ISO3166(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public void close()
	{
		super.close();
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getName()
	{
		return getModelString(NAME);
	}
	
	public static ISO3166 query(Context context, Uri uri)
	{
		ISO3166 result = null;
		try
		{
			result = new ISO3166(context, context.getContentResolver().query(uri, null, null, null, null));
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
