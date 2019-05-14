package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class DrawingAddress extends AddressedTable
{
    public static final String DRAWING = "Drawing";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/DrawingAddress");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.DrawingAddress";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.DrawingAddress";

	public DrawingAddress(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public String getDrawingID()
	{
		return getCursorString(DRAWING);
	}
	
	public static DrawingAddress queryDrawings(Context context)
	{
		DrawingAddress result = null;
		try
		{
			result = new DrawingAddress(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
		
	}
	
	public static DrawingAddress queryDrawingAddress(Context context, String DrawingID)
	{
		DrawingAddress result = null;
		try
		{
			result = new DrawingAddress(context, context.getContentResolver().query(CONTENT_URI, null,DRAWING+"='"+DrawingID+"'", null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static Address getAddressCursor(Context context, String id)
	{
		Address result = null;
		try
		{
			result = new Address(context, context.getContentResolver().query(Address.CONTENT_URI, null,"ID='"+id+"'", null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
