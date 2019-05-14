package com.futureconcepts.ax.model.data;

import java.util.UUID;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
//import android.util.Log;

public class IncidentRequest extends AddressedTable
{
    public static final String INCIDENT_NAME = "IncidentName";
    public static final String STATUS = "Status";
    public static final String TYPE = "Type";
    public static final String HAS_VICTIMS = "HasVictims";
    public static final String INCIDENT = "Incident";
    public static final String DESCRIPTION = "Description";
    public static final String CREATED = "Created";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/IncidentRequest");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.IncidentRequest";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.IncidentRequest";

    private IncidentType _type;
    private IncidentRequestStatus _status;
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = LAST_MODIFIED + " DESC";

	public IncidentRequest(Context context, Cursor cursor)
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
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	private void closeReferences()
	{
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
	}
	
	public String getIncidentName()
	{
		int idx = getColumnIndex(INCIDENT_NAME);
		assert(idx != -1);
		return getString(idx);
	}

	public String getStatusID()
	{
		return getString(getColumnIndex(STATUS));
	}

	public IncidentRequestStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = IncidentRequestStatus.query(context);
		}
		_status.moveToPosition(getTypeID());
		return _status;
	}

	public String getTypeID()
	{
		return getString(getColumnIndex(TYPE));
	}
	
	public IncidentType getType(Context context)
	{
		if (_type == null)
		{
			_type = IncidentType.query(context);
		}
		_type.moveToPosition(getTypeID());
		return _type;
	}
	
	public boolean hasVictims()
	{
		return getCursorBoolean(HAS_VICTIMS);
	}
	
	public String getIncidentID()
	{
		return getCursorString(INCIDENT);
	}
	
	public String getDescription()
	{
		return getCursorString(DESCRIPTION);
	}
	
	public DateTime getCreatedTime()
	{
		return getCursorDateTime(CREATED);
	}
	
	public static IncidentRequest query(Context context)
	{
		return query(context, CONTENT_URI);
	}

	public static IncidentRequest queryWhere(Context context, String whereClause)
	{
		IncidentRequest result = null;
		try
		{
			result = new IncidentRequest(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static IncidentRequest query(Context context, Uri uri)
	{
		IncidentRequest result = null;
		try
		{
			result = new IncidentRequest(context, context.getContentResolver().query(uri, null, null, null, null));
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
	
	public static Uri createRequest(Context context, String type, String owner)
	{
		String now = DateTime.now().toString();
		ContentValues values = new ContentValues();
		values.put(ID, UUID.randomUUID().toString().toUpperCase());
		values.put(INCIDENT_NAME, now);
		values.put(TYPE, type);
		values.put(STATUS,  IncidentRequestStatus.NEW);
		values.put(HAS_VICTIMS, 0);
		values.put(CREATED, now);
		values.put(OWNER, owner);
		values.put(LAST_MODIFIED, now);
		return context.getContentResolver().insert(CONTENT_URI, values);
	}
}
