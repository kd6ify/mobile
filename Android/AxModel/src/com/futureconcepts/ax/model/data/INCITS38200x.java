package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class INCITS38200x extends BaseTable
{
	public static final String NAME = "Name";
	public static final String NUM_CODE = "NumCode";
    public static final String STATE_CODE = "StateCode";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/INCITS38200x");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.INCITS38200x";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.INCITS38200x";

	public INCITS38200x(Context context, Cursor cursor)
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
		return getCursorString(NAME);
	}

	public int getNumCode()
	{
		return getCursorInt(NUM_CODE);
	}
	
	public String getStateCode()
	{
		return getCursorString(STATE_CODE);
	}
	
	public static INCITS38200x query(Context context, Uri uri)
	{
		INCITS38200x result = null;
		try
		{
			result = new INCITS38200x(context, context.getContentResolver().query(uri, null, null, null, null));
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
