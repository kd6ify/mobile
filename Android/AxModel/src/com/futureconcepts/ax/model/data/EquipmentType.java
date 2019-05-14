package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class EquipmentType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/EquipmentType");

	public EquipmentType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static EquipmentType query(Context context)
	{
		EquipmentType result = null;
		try
		{
			result = new EquipmentType(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static EquipmentType query(Context context, Uri uri)
	{
		EquipmentType result = null;
		try
		{
			result = new EquipmentType(context, context.getContentResolver().query(uri, null, null, null, null));
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
