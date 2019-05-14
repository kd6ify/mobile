package com.futureconcepts.ax.model.data;

import java.util.Hashtable;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Journal extends SourcedTable
{
    public static final String NAME = "Name";
    public static final String TIME = "Time";
    public static final String ADDRESS = "Address";
    public static final String INCIDENT = "Incident";
    public static final String COLLECTION = "Collection";
    public static final String ASSET = "Asset";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Journal");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Journal";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Journal";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    protected Hashtable<String, Integer> _dict;
    
	public Journal(Context context, Cursor cursor)
	{
		super(context, cursor);
		_dict = new Hashtable<String, Integer>();
		for (int i = 0; i < getCount(); i++)
		{
			moveToPosition(i);
			String id = getID();
			_dict.put(id, Integer.valueOf(i));
		}
		moveToPosition(0);
	}

	public void moveToPosition(String id)
	{
		Integer position = _dict.get(id);
		if (position != null)
		{
			moveToPosition(position.intValue());
		}
	}

	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			setName(getCursorString(NAME));
			setTime(getCursorDateTime(TIME));
			setAddressID(getCursorString(ADDRESS));
			setIncidentID(getCursorString(INCIDENT));
			setCollectionID(getCursorString(COLLECTION));
			setAssetID(getCursorString(ASSET));
			setSourceID(getCursorString(SOURCE));
			setSourceDate(getCursorDateTime(SOURCE_DATE));
		}
	}

	public String getName()
	{
		return getModelString(NAME);
	}
	
	public void setName(String value)
	{
		setModel(NAME, value);
	}
	
	public DateTime getTime()
	{
		return getModelDateTime(TIME);
	}

	public void setTime(DateTime value)
	{
		setModel(TIME, value);
	}
	
	public String getAddressID()
	{
		return getModelString(ADDRESS);
	}
	
	public void setAddressID(String value)
	{
		setModel(ADDRESS, value);
	}
	
	public String getIncidentID()
	{
		return getModelString(INCIDENT);
	}
	
	public void setIncidentID(String value)
	{
		setModel(INCIDENT, value);
	}

	public String getCollectionID()
	{
		return getModelString(COLLECTION);
	}
	
	public void setCollectionID(String value)
	{
		setModel(COLLECTION, value);
	}

	public String getAssetID()
	{
		return getModelString(ASSET);
	}
	
	public void setAssetID(String value)
	{
		setModel(ASSET, value);
	}

	public static Journal query(Context context)
	{
		return queryWhere(context, null);
	}

	public static Journal queryWhere(Context context, String whereClause)
	{
		Journal result = null;
		try
		{
			result = new Journal(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, "UPPER("+NAME+") ASC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Uri createDefault(Context context, String incidentID)
	{
		DateTime now = DateTime.now();
		Journal journal = new Journal(context, null);
		journal.beginEdit();
		journal.setID(Guid.newGuid().toString());
		journal.setTime(now);
		journal.setIncidentID(incidentID);
		journal.setName("");
		journal.setSourceID(SourceType.LARCOPP);
		journal.setSourceDate(now);
		return context.getContentResolver().insert(CONTENT_URI, journal.endEdit());
	}
	
	public Uri createDefaultEntry(Context context)
	{
		return JournalEntry.createDefault(context, this);
	}
	public static Journal query(Context context, Uri uri)
	{
		Journal result = null;
		try
		{
			result = new Journal(context, context.getContentResolver().query(uri, null, null, null, null));
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
