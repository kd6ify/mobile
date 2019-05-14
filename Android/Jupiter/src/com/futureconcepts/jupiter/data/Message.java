package com.futureconcepts.jupiter.data;

import com.futureconcepts.jupiter.data.BaseTable;

import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Message extends BaseTable
{
    public static final String AUTHORITY = "com.futureconcepts.trinity.messaging";

    public static final String TIME_SENT = "TimeSent";
    public static final String SENDER = "Sender";
    public static final String TEXT = "Text";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/message");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Message";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Message";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "TimeSent DESC";

	public Message(Cursor cursor)
	{
		super(cursor);
	}

	public String get_id()
	{
		return getString(getColumnIndex(_ID));
	}
	
	
	public long getTimeSent()
	{
		return getLong(getColumnIndex(TIME_SENT));
	}
	
	public String getSender()
	{
		return getString(getColumnIndex(SENDER));
	}

	public String getText()
	{
		return getString(getColumnIndex(TEXT));
	}
}
