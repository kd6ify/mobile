package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class IncidentType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IncidentType");
    
    public static final String TYPE_LAW = "8A4472CD-6B5E-2BBC-1F0D-524A85EA2615";
    public static final String TYPE_FIRE = "DEC6016B-10CD-C572-FFB3-55D8EC58E74B";

	public IncidentType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static IncidentType query(Context context)
	{
		IncidentType result = null;
		try
		{
			result = new IncidentType(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
