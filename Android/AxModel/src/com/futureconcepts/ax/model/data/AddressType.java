package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AddressType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AddressType");
    public static final String BOOKMARK = "C575F919-3ED3-47E7-B4BE-800BC2339E8E";    
    public static final String MOBILE = "107C656F-94A7-4ADE-9302-543CA32C604C";    
    public static final String NOTE  = "AE1DBA51-566D-4210-AD04-1AA617CA0054";

	public AddressType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static AddressType query(Context context, Uri uri)
	{
		AddressType result = null;
		try
		{
			result = new AddressType(context, context.getContentResolver().query(uri, null, null, null, null));
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
