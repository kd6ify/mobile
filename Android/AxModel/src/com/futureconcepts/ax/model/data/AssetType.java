package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AssetType extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/AssetType");

    public static final String USER = "41E78F40-C6DF-E47F-2901-45DE2693EC0B";
    public static final String EQUIPMENT = "AAC09D04-4A8A-D6B4-7A01-3471B926CE49";
    public static final String INFRASTRUCTURE = "2A6337A5-6D55-C2B6-059F-F159F10EDC6F";
    
	public AssetType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static AssetType query(Context context)
	{
		return query(context, CONTENT_URI);
	}

	public static AssetType query(Context context, Uri uri)
	{
		AssetType result = null;
		try
		{
			result = new AssetType(context, context.getContentResolver().query(uri, null, null, null, null));
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
