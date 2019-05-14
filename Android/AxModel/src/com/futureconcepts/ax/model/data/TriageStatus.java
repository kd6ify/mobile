package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TriageStatus extends IndexedType
{
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/TriageStatus");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.TriageStatus";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.TriageStatus";

    public static final String UNKNOWN = new String("5BB89AAF-F865-49B9-BD8B-44244E395F9C");

	public TriageStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static TriageStatus query(Context context)
	{
		TriageStatus result = null;
		try
		{
			result = new TriageStatus(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	public static TriageStatus query(Context context, Uri uri)
	{
		TriageStatus result = null;
		try
		{
			result = new TriageStatus(context, context.getContentResolver().query(uri, null, null, null, null));
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
