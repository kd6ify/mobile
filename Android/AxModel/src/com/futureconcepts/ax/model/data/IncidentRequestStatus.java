package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class IncidentRequestStatus extends IndexedType
{
    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IncidentRequestStatus");
	
    public static final String NEW = "CB75C7B6-F431-4A79-8889-4C4994CAF7CE";
    public static final String IN_REVIEW = "A842EAAA-D83B-4FA5-A1C7-0AFAE0A306A3";
    public static final String APPROVED = "3E3318E8-71AA-4183-8DBB-D23C45FB9658";
    public static final String DISMISSED = "D4404300-5367-4477-B5B7-4A446B2B383D";
    public static final String SHARED = "035927F5-0C24-49B3-A84E-BF66F5362B35";
    
	public IncidentRequestStatus(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	public static IncidentRequestStatus query(Context context)
	{
		IncidentRequestStatus result = null;
		try
		{
			result = new IncidentRequestStatus(context, context.getContentResolver().query(CONTENT_URI, null, null, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
}
