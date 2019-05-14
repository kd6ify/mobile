package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class JournalStatus extends IndexedType
{
	public static final String IN_PROGRESS = "1B22AE12-4E17-4513-9F1E-EC22AEE42C44";	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/JournalStatus");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.JournalStatus";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.JournalStatus";

	public JournalStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static JournalStatus query(Context context)
	{
		JournalStatus result = null;
		try
		{
			result = new JournalStatus(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
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
	public static JournalStatus query(Context context, Uri uri)
	{
		JournalStatus result = null;
		try
		{
			result = new JournalStatus(context, context.getContentResolver().query(uri, null, null, null, null));
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
