package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AgencyType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AgencyType");
    public static final String TABLE_NAME = "AgencyType";

	public AgencyType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static AgencyType query(Context context)
	{
		AgencyType result = null;
		try
		{
			result = new AgencyType(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
