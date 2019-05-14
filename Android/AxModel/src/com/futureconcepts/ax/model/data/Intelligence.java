package com.futureconcepts.ax.model.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class Intelligence extends SourcedTable
{
	public static final String INCIDENT = "Incident";
	public static final String STATUS = "Status";
	public static final String NAME = "Name";
	public static final String DETAILS = "Details";
    public static final String COMMENTS = "Comments";
    public static final String ENTRY_TIME = "EntryTime";
    public static final String RELIABILITY = "Reliability";
    public static final String ADDRESS = "Address";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Intelligence");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Intelligence";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Intelligence";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    private IntelligenceStatus _status;
    
	public Intelligence(Context context, Cursor cursor)
	{
		super(context, cursor);
	}
	
	@Override
	public void close()
	{
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
		super.close();
	}
	
	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			setIncidentID(getCursorGuid(INCIDENT));
			setStatusID(getCursorGuid(STATUS));
			setName(getCursorString(NAME));
			setDetails(getCursorString(DETAILS));
			setComments(getCursorString(COMMENTS));
			setEntryTime(getCursorLong(ENTRY_TIME));
			setReliability(getCursorString(RELIABILITY));
			setAddressID(getCursorGuid(ADDRESS));
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getIncidentID()
	{
		return getModelGuid(INCIDENT);
	}
	
	public void setIncidentID(String value)
	{
		setModel(INCIDENT, value);
	}

	public String getStatusID()
	{
		return getModelString(STATUS);
	}
	
	public void setStatusID(String value)
	{
		setModel(STATUS, value);
	}
	
	public IntelligenceStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = IntelligenceStatus.query(context);
		}
		_status.moveToPosition(getStatusID());
		return _status;
	}
	
	public String getName()
	{
		return getModelString(NAME);
	}
	
	public void setName(String value)
	{
		setModel(NAME, value);
	}
	
	public String getDetails()
	{
		return getModelString(DETAILS);
	}
	
	public void setDetails(String value)
	{
		setModel(DETAILS, value);
	}
	
	public String getComments()
	{
		return getModelString(COMMENTS);
	}
	
	public void setComments(String value)
	{
		setModel(COMMENTS, value);
	}
	
	public long getEntryTime()
	{
		return getModelLong(ENTRY_TIME);
	}
	
	public void setEntryTime(long value)
	{
		setModel(ENTRY_TIME, value);
	}

	public String getReliability()
	{
		return getModelString(RELIABILITY);
	}

	public void setReliability(String value)
	{
		setModel(RELIABILITY, value);
	}
	
	public String getAddressID()
	{
		return getModelString(ADDRESS);
	}
	
	public void setAddressID(String value)
	{
		setModel(ADDRESS, value);
	}
	
	public static Uri createDefault(Context context, String incidentID)
	{
		Uri result = null;
		if (incidentID != null)
		{
			ContentValues values = new ContentValues();
			long now = System.currentTimeMillis();
			values.put(ID, Guid.newGuid().toString());
			values.put(NAME, "");
			values.put(INCIDENT, incidentID);
			values.put(ENTRY_TIME, now);
			values.put(STATUS, IntelligenceStatus.ONGOING);
			values.put(SOURCE, SourceType.LARCOPP);
			values.put(SOURCE_DATE, now);
			result = context.getContentResolver().insert(CONTENT_URI, values);
		}
		return result;
	}

	public static Intelligence queryWhere(Context context, String whereClause)
	{
		return new Intelligence(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, Intelligence.ENTRY_TIME + " DESC"));
	}

	public static Intelligence query(Context context, Uri uri)
	{
		Intelligence result = null;
		try
		{
			result = new Intelligence(context, context.getContentResolver().query(uri, null, null, null, null));
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
