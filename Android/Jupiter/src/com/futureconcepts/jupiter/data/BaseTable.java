package com.futureconcepts.jupiter.data;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.BaseColumns;

import com.futureconcepts.jupiter.data.EmptyCursor;

public class BaseTable extends CursorWrapper implements BaseColumns
{
	public static final String PARENT_ID = "ParentId";
	public static final String LAST_UPDATE_TIME = "LastUpdateTime";
    public static final String LAST_MODIFIED_TIME = "LastModifiedTime";
	
	public BaseTable(Cursor cursor)
    {
	    super(cursor != null ? cursor : new EmptyCursor());
    }
    
    public String get_ID()
    {
    	return getString(getColumnIndex(_ID));
    }
    
    public String getParentId()
    {
    	return getString(getColumnIndex(PARENT_ID));
    }
    
    public long getLastUpdateTime()
    {
    	return getLong(getColumnIndex(LAST_UPDATE_TIME));
    }

    public long getLastModifiedTime()
    {
    	return getLong(getColumnIndex(LAST_MODIFIED_TIME));
    }
}
