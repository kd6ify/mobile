package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class IncidentDrawing extends BaseTable {
	
	public static final String Incident = "Incident";
	public static final String Drawing = "Drawing";

	/**
	* The content:// style URL for this table
	*/
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IncidentDrawing");
    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.IncidentDrawing";
    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.IncidentDrawing";
 
	public IncidentDrawing(Context context, Cursor cursor) {
		super(context, cursor);
		// TODO Auto-generated constructor stub
	}

	public  String getIncidentID() {
		return getModelString(Incident);
	}

	public  String getDrawingID() {
		return getModelString(Drawing);
	}
	
	public static IncidentDrawing queryDrawings(Context context)
	{
		IncidentDrawing result = null;
		try
		{
			result = new IncidentDrawing(context, context.getContentResolver().query(CONTENT_URI, null,null, null,null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
		
	}

}
