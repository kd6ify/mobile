package com.futureconcepts.ax.model.data;

import org.joda.time.DateTime;

import android.content.Context;
import android.database.Cursor;

public class SourcedTable extends BaseTable
{
    public static final String SOURCE = "Source";
    public static final String SOURCE_DATE = "SourceDate";
    
    private SourceType _sourceType;
    
	public SourcedTable(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public void beginEdit()
	{
		super.beginEdit();
		if (getCount() != 0)
		{
			setSourceID(getCursorGuid(SOURCE));
			setSourceDate(getCursorDateTime(SOURCE_DATE));
		}
	}

	public String getSourceID()
	{
		return getModelGuid(SOURCE);
	}

	public SourceType getSourceType(Context context)
	{
		if (_sourceType == null)
		{
			_sourceType = SourceType.query(context);
		}
		String id = getSourceID();
		if (id != null)
		{
			_sourceType.moveToPosition(id);
			return _sourceType;
		}
		else
		{
			return null;
		}
	}
	
	public void setSourceID(String value)
	{
		setModel(SOURCE, value);
	}
	
	public DateTime getSourceDate()
	{
		return getModelDateTime(SOURCE_DATE);
	}
	
	public void setSourceDate(DateTime value)
	{
		setModel(SOURCE_DATE, value);
	}
}
