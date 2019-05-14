package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Agency extends BaseTable
{
    public static final String TYPE = "Type";
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";
    public static final String FIPS_CODE = "FIPSCode";
    public static final String ICON = "Icon";
    public static final String ADDRESS = "Address";
    public static final String ABBR = "Abbr";
    public static final String STATE_ABBR = "Stateabbr";
    public static final String CONTACT_PHONE = "ContactPhone";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Agency");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Agency";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Agency";

	public Agency(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public void close()
	{
		super.close();
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getName()
	{
		return getModelString(NAME);
	}
	
	public static Agency query(Context context, Uri uri)
	{
		Agency result = null;
		try
		{
			result = new Agency(context, context.getContentResolver().query(uri, null, null, null, null));
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
