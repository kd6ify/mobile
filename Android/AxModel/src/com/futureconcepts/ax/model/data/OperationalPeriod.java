package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class OperationalPeriod extends BaseTable
{
    public static final String NAME = "Name";
    public static final String INCIDENT = "Incident";
    public static final String ACTUAL_START = "ActualStart";
    public static final String ACTUAL_END = "ActualEnd";
    public static final String PROJECTED_START = "ProjectedStart";
    public static final String PROJECTED_END = "ProjectedEnd";
    public static final String NOTES = "Notes";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/OperationalPeriod");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.OperationalPeriod";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.OperationalPeriod";
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = LAST_MODIFIED + " DESC";

	public OperationalPeriod(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	public String getName()
	{
		return getCursorString(NAME);
	}

	public String getIncidentID()
	{
		return getCursorGuid(INCIDENT);
	}
	
	public String getNotes()
	{
		return getCursorString(NOTES);
	}
	
	public DateTime getActualStart()
	{
		return getCursorDateTime(ACTUAL_START);
	}
	
	public DateTime getActualEnd()
	{
		return getCursorDateTime(ACTUAL_END);
	}
	
	public DateTime getProjectedStart()
	{
		return getCursorDateTime(PROJECTED_START);
	}
	
	public DateTime getProjectedEnd()
	{
		return getCursorDateTime(PROJECTED_END);
	}
	
	public static OperationalPeriod queryIncident(Context context, String incidentId)
	{
		OperationalPeriod result = null;
		try
		{
			result = new OperationalPeriod(context, context.getContentResolver().query(CONTENT_URI, null, INCIDENT + "='" + incidentId + "'", null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static OperationalPeriod queryWhere(Context context, String where)
	{
		OperationalPeriod result = null;
		try
		{
			result = new OperationalPeriod(context, context.getContentResolver().query(CONTENT_URI, null,where, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
