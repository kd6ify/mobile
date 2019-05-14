package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Objective extends BaseTable
{
    public static final String NAME = "Name";
    public static final String STATUS = "Status";
    public static final String PROJECTED_START = "ProjectedStart";
    public static final String PROJECTED_END = "ProjectedEnd";
    public static final String ACTUAL_START = "ActualStart";
    public static final String ACTUAL_END = "ActualEnd";
    public static final String PERCENT_COMPLETE = "PercentComplete";
    public static final String NOTES = "Notes";
    public static final String OPERATIONAL_PERIOD = "OperationalPeriod";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Objective");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Objective";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Objective";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = ACTUAL_START + " DESC";

    private ObjectiveStatus _status;
    
	public Objective(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public void close()
	{
		closeReferences();
		super.close();
	}
	
	@Override
	public boolean requery()
	{
		closeReferences();
		return super.requery();
	}
	
	@Override
	public boolean moveToPosition(int position)
	{
		closeReferences();
		return super.moveToPosition(position);
	}
	
	private void closeReferences()
	{
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
	}
	
	public String getName()
	{
		return getCursorString(NAME);
	}

	public String getStatusID()
	{
		return getCursorGuid(STATUS);
	}
	
	public ObjectiveStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = ObjectiveStatus.query(context);
		}
		_status.moveToPosition(getStatusID());
		return _status;
	}

	public DateTime getProjectedStart()
	{
		return getCursorDateTime(PROJECTED_START);
	}
	public DateTime getProjectedEnd()
	{
		return getCursorDateTime(PROJECTED_END);
	}
	
	public DateTime getActualStart()
	{
		return getCursorDateTime(ACTUAL_START);
	}
	
	public DateTime getActualEnd()
	{
		return getCursorDateTime(ACTUAL_END);
	}

	public double getPercentComplete()
	{
		return getCursorDouble(PERCENT_COMPLETE);
	}

	public String getNotes()
	{
		return getString(getColumnIndex(NOTES));
	}

	public String getOperationalPeriod()
	{
		return getString(getColumnIndex(OPERATIONAL_PERIOD));
	}
	
	public static Objective query(Context context, Uri uri)
	{
		Objective result = null;
		try
		{
			result = new Objective(context, context.getContentResolver().query(uri, null, null, null, null));
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

	public static Objective queryWhere(Context context, String whereClause)
	{
		Objective result = null;
		try
		{
			result = new Objective(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
