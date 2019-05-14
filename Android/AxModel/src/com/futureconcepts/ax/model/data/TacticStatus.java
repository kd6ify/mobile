package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class TacticStatus extends IndexedType
{
	public static final String ACTIVE = "AF97CF51-5BB0-072F-13C0-1AC365D9AB27";
	public static final String COMPLETE = "71E68D20-5F6A-37C7-2DB2-5048661A197F";
	public static final String PENDING = "D4342B89-BC90-4EF9-8577-F49F61DCEBD0";
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/TacticStatus");

	public TacticStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	public static TacticStatus query(Context context, Uri uri)
	{
		TacticStatus result = null;
		try
		{
			result = new TacticStatus(context, context.getContentResolver().query(uri, null, null, null, null));
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
