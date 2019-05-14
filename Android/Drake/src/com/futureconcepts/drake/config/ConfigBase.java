package com.futureconcepts.drake.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public class ConfigBase
{
	public static SharedPreferences getPreferences(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences;
	}
	
	public static SharedPreferences.Editor getPreferencesEditor(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		return editor;
	}
	
	public static boolean hasKey(Context context, String key)
	{
		return getPreferences(context).contains(key);
	}
	
	public static String getGlobalStringValue(Context context, String key, String defaultValue)
	{
		String value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/string"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					String columnValue = cursor.getString(0);
					if (columnValue != null)
					{
						value = columnValue;
					}
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

	public static String getGlobalEncryptedStringValue(Context context, String key, String defaultValue)
	{
		String value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/encrypted_string"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					String columnValue = cursor.getString(0);
					if (columnValue != null)
					{
						value = columnValue;
					}
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

	public static int getGlobalIntValue(Context context, String key, int defaultValue)
	{
		int value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/int"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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

	public static boolean getGlobalBooleanValue(Context context, String key, boolean defaultValue)
	{
		boolean value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/boolean"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = Boolean.parseBoolean(cursor.getString(0));
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
}
