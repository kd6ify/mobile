package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class JournalEntry extends SourcedTable
{
    public static final String JOURNAL = "Journal";
    public static final String JOURNAL_TIME = "JournalTime";
    public static final String SEQUENCE = "Sequence";
    public static final String STATUS = "Status";
    public static final String TYPE = "Type";
    public static final String PRIORITY = "Priority";
    public static final String TEXT = "Text";
    public static final String ADDRESS = "Address";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/JournalEntry");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.JournalEntry";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.JournalEntry";

    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = "modified DESC";

    private JournalType _type;
    private JournalStatus _status;
    private Journal _journal;
    
	public JournalEntry(Context context, Cursor cursor)
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
	
	private void closeReferences()
	{
		if (_type != null)
		{
			_type.close();
			_type = null;
		}
		if (_status != null)
		{
			_status.close();
			_status = null;
		}
		if (_journal != null)
		{
			_journal.close();
			_journal = null;
		}
	}
	
	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			setJournalID(getCursorGuid(JOURNAL));
			setJournalTime(getCursorDateTime(JOURNAL_TIME));
			setSequence(getCursorInt(SEQUENCE));
			setStatusID(getCursorGuid(STATUS));
			setTypeID(getCursorGuid(TYPE));
			setPriority(getCursorInt(PRIORITY));
			setText(getCursorString(TEXT));
			setAddressID(getCursorGuid(ADDRESS));
		}
	}
	
	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}
	
	public String getJournalID()
	{
		return getModelString(JOURNAL);
	}

	public Journal getJournal(Context context)
	{
		if (_journal == null)
		{
			_journal = Journal.query(context);
		}
		String id = getJournalID();
		if (id != null)
		{
			_journal.moveToPosition(id);
			return _journal;
		}
		else
		{
			return null;
		}
	}
	
	public void setJournalID(String value)
	{
		setModel(JOURNAL, value);
	}
	
	public DateTime getJournalTime()
	{
		return getModelDateTime(JOURNAL_TIME);
	}
	
	public void setJournalTime(DateTime value)
	{
		setModel(JOURNAL_TIME, value);
	}
	
	public int getSequence()
	{
		return getModelInt(SEQUENCE);
	}
	
	public void setSequence(int value)
	{
		setModel(SEQUENCE, value);
	}
	
	public String getStatusID()
	{
		return getModelString(STATUS);
	}
	
	public JournalStatus getStatus(Context context)
	{
		if (_status == null)
		{
			_status = JournalStatus.query(context);
		}
		String statusID = getStatusID();
		if (statusID != null)
		{
			_status.moveToPosition(statusID);
			return _status;
		}
		else
		{
			return null;
		}
	}
	
	public void setStatusID(String value)
	{
		setModel(STATUS, value);
	}
	
	public String getTypeID()
	{
		return getModelString(TYPE);
	}
	
	public JournalType getType(Context context)
	{
		if (_type == null)
		{
			_type = JournalType.query(context);
		}
		String typeID = getTypeID();
		if (typeID != null)
		{
			_type.moveToPosition(typeID);
			return _type;
		}
		else
		{
			return null;
		}
	}
	
	public void setTypeID(String value)
	{
		setModel(TYPE, value);
	}
	
	public int getPriority()
	{
		return getModelInt(PRIORITY);
	}
	
	public void setPriority(int value)
	{
		setModel(PRIORITY, value);
	}
	
	public String getAddressID()
	{
		return getModelGuid(ADDRESS);
	}
	
	public void setAddressID(String value)
	{
		setModel(ADDRESS, value);
	}
		
	public String getText()
	{
		return getModelString(TEXT);
	}
	
	public void setText(String value)
	{
		setModel(TEXT, value);
	}

	public static JournalEntry queryWhere(Context context, String whereClause)
	{
		JournalEntry result = null;
		try
		{
			result = new JournalEntry(context, context.getContentResolver().query(CONTENT_URI, null, whereClause, null, JOURNAL_TIME + " DESC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static JournalEntry queryJournal(Context context, String id)
	{
		JournalEntry result = null;
		try
		{
			result = new JournalEntry(context, context.getContentResolver().query(CONTENT_URI, null, JOURNAL+"='"+id+"'", null, JOURNAL_TIME + " DESC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static JournalEntry queryMaster(Context context)
	{
		JournalEntry result = null;
		try
		{
			result = new JournalEntry(context, context.getContentResolver().query(CONTENT_URI, null, null, null, JOURNAL_TIME + " DESC"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public static Uri createDefault(Context context, Journal journal)
	{
		Uri result = null;
		ContentValues values = new ContentValues();
		DateTime now = DateTime.now();
		values.put(ID, Guid.newGuid().toString());
		values.put(JOURNAL_TIME, now.toString());
		values.put(JOURNAL, journal.getID());
		values.put(STATUS, JournalStatus.IN_PROGRESS);
		values.put(TYPE, JournalType.UNIT_LOG);
		values.put(PRIORITY, 0);
		values.put(TEXT, "");
		values.put(SOURCE, journal.getSourceID());
		values.put(SOURCE_DATE, now.toString());
		result = context.getContentResolver().insert(CONTENT_URI, values);
		return result;
	}
	public static JournalEntry query(Context context, Uri uri)
	{
		JournalEntry result = null;
		try
		{
			result = new JournalEntry(context, context.getContentResolver().query(uri, null, null, null, null));
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
