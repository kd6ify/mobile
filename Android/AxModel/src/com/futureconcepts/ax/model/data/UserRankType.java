package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class UserRankType extends IndexedType
{
    public static final String PREFIX = "Prefix";
    public static final String ABBR = "Abbr";
    public static final String NUMBER = "Number";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/UserRankType");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.UserRankType";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.UserRankType";

	public UserRankType(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getPrefix()
	{
		return getCursorString(PREFIX);
	}

	public String getABBR()
	{
		return getCursorString(ABBR);
	}	
	
	public int getNumber()
	{
		return getCursorInt(NUMBER);
	}
	
	public static UserRankType query(Context context, Uri uri)
	{
		UserRankType result = null;
		try
		{
			result = new UserRankType(context, context.getContentResolver().query(uri, null, null, null, null));
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
