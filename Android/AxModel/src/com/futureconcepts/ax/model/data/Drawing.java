package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Drawing extends BaseTable
{
    public static final String NAME = "Name";
    public static final String NOTES = "Notes";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Drawing");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Drawing";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Drawing";

	public Drawing(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public String getName()
	{
		return getModelString(NAME);
	}
	
	public void setName(String value)
	{
		setModel(NAME, value);
	}
	
	public String getNotes()
	{
		return getModelString(NOTES);
	}
	
	public void setNotes(String value)
	{
		setModel(NOTES, value);
	}
	
	public static Drawing queryDrawings(Context context)
	{
		Drawing result = null;
		try
		{
			result = new Drawing(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
		
	}
}
