package com.futureconcepts.jupiter.provider;

import com.futureconcepts.jupiter.data.BaseTable;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public abstract class BaseProvider extends ContentProvider
{
//	private static final String TAG = "BaseProvider";

	protected SQLiteOpenHelper mOpenHelper;

	public static long getLastModifiedTime(ContentValues values)
	{
		return values.getAsLong(BaseTable.LAST_MODIFIED_TIME);
	}

	public static long getLastUpdateTime(ContentValues values)
	{
		return values.getAsLong(BaseTable.LAST_UPDATE_TIME);
	}
	
	public static boolean needsSyncing(ContentValues values)
	{
		return values.containsKey(BaseTable.LAST_UPDATE_TIME) == false;
	}
	
    protected Uri insertRow(String tableName, Uri contentUri, ContentValues values)
    {
	    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	    long rowId = db.insert(tableName, BaseColumns._ID, values);
	    if (rowId > 0) 
	    {
	        Uri insertedUri = ContentUris.withAppendedId(contentUri, rowId);
	        getContext().getContentResolver().notifyChange(contentUri, null);
	        return insertedUri;
	    }
	    throw new SQLException("Failed to insert row into " + contentUri);
    }
    
    protected int updateRow(String tableName, Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String id = uri.getPathSegments().get(1);
        int count = db.update(tableName, values, BaseTable._ID + "=" + id
                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
        return count;
    }
}
