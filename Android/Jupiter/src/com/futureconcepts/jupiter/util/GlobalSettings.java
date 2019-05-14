package com.futureconcepts.jupiter.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class GlobalSettings
{
//	private static String TAG = "GlobalSettings";
	
	private Context _context;

	public static GlobalSettings _instance = null;
	
	public static synchronized GlobalSettings getInstance(Context context)
	{
		if (_instance == null)
		{
			_instance = new GlobalSettings(context);
		}
		return _instance;
	}
	
	private GlobalSettings(Context context)
	{
		_context = context;
	}
	
	public String getStringValue(String key, String defaultValue)
	{
		String value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.provider.settings/string"), key);
		Cursor cursor = _context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = cursor.getString(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
		}
		return value;
	}

	public int getIntValue(String key, int defaultValue)
	{
		int value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.provider.settings/integer"), key);
		Cursor cursor = _context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = cursor.getInt(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
		}
		return value;
	}
	
	public int getMapIconSizeBump()
	{
		return getIntValue("map_icon_size_bump", 0);
	}
}
