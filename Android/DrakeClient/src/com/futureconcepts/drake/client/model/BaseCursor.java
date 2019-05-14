package com.futureconcepts.drake.client.model;

import java.util.Date;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.BaseColumns;

public class BaseCursor extends CursorWrapper implements BaseColumns
{
	public BaseCursor(Cursor cursor)
    {
	    super(cursor != null ? cursor : new EmptyCursor());
    }
    	
	public String getCursorString(String key)
	{
		return getString(getColumnIndex(key));
	}
	
	public int getCursorInt(String key)
	{
		return getInt(getColumnIndex(key));
	}
	
	public long getCursorLong(String key)
	{
		return getLong(getColumnIndex(key));
	}
	
	public byte[] getCursorBlob(String key)
	{
		return getBlob(getColumnIndex(key));
	}
	
	public long getCursorDateAsLong(String key)
	{
		return getLong(getColumnIndex(key));
	}
	
	public Date getCursorDateAsDate(String key)
	{
		return new Date(getLong(getColumnIndex(key)));
	}
	
	public boolean getCursorBoolean(String key)
	{
		int value = getInt(getColumnIndex(key));
		return value != 0;
	}
	
	public double getCursorDouble(String key)
	{
		return getDouble(getColumnIndex(key));
	}
		
	public long get_ID()
	{
		return getCursorLong(_ID);
	}
}
