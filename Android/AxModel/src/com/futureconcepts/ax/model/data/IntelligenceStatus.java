package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class IntelligenceStatus extends IndexedType
{
	public static final String ONGOING = "07BACE13-5BAB-47A9-A2A5-5433304FB210";	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IntelligenceStatus");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.IntelligenceStatus";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.IntelligenceStatus";

	public IntelligenceStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	public static IntelligenceStatus query(Context context)
	{
		IntelligenceStatus result = null;
		try
		{
			result = new IntelligenceStatus(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
