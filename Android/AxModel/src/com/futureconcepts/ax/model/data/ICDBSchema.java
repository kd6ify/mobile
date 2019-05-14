package com.futureconcepts.ax.model.data;

import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

public class ICDBSchema extends CursorWrapper
{
    public static final String AUTHORITY = "com.futureconcepts.ax.sync.provider.icdb.schema";
	
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.icdb.schema";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.icdb.schema";
    
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_TYPE = "Type";
    
    public static final String TYPE_GUID = "guid";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_DATE_TIME_OFFSET = "datetimeoffset";
    public static final String TYPE_BLOB = "blob";
    public static final String TYPE_BOOL = "bool";
    public static final String TYPE_INT32 = "int32";
    public static final String TYPE_INT64 = "int64";
    public static final String TYPE_REAL = "real";

	public static final String FLAG_UNIQUE = "unique";

    private int _nameColumnIndex = -1;
    private int _typeColumnIndex = -1;
    
	public ICDBSchema(Cursor cursor)
	{
		super(cursor);
	}
	
	public String getColumnName()
	{
		if (_nameColumnIndex == -1)
		{
			_nameColumnIndex = getColumnIndex(COLUMN_NAME);
		}
		return getString(_nameColumnIndex);
	}
	
	public String getColumnType()
	{
		if (_typeColumnIndex == -1)
		{
			_typeColumnIndex = getColumnIndex(COLUMN_TYPE);
		}
		return getString(_typeColumnIndex);
	}

	public static Hashtable<String, String> getSchemaTypeMap(Context context)
	{
		Hashtable<String, String> result = new Hashtable<String, String>();
		result = new Hashtable<String, String>();
		result.put(COLUMN_NAME, TYPE_TEXT);
		result.put(COLUMN_TYPE, TYPE_TEXT);
		return result;
	}

	public static void deleteTableTypeMap(Context context, String tableName)
	{
		ContentResolver resolver = context.getContentResolver();
		resolver.delete(getSchemaTableUri(tableName), null, null);
	}
	
	public static Hashtable<String, String> getTableTypeMap(Context context, String tableName)
	{
		Hashtable<String, String> result = new Hashtable<String, String>();
		ContentResolver resolver = context.getContentResolver();
		Cursor rawCursor = resolver.query(getSchemaTableUri(tableName), null, null, null, null);
		if (rawCursor != null)
		{
			ICDBSchema schema = new ICDBSchema(rawCursor);
			int count = schema.getCount();
			if (count > 0)
			{
				for (int i = 0; i < count; i++)
				{
					schema.moveToPosition(i);
					result.put(schema.getColumnName(), schema.getColumnType().split(" ")[0]);
				}
			}
			schema.close();
			schema = null;
			rawCursor.close();
			rawCursor = null;
		}
		return result;
	}
	
	public static Uri getSchemaTableUri(String tableName)
	{
		return Uri.parse("content://" + AUTHORITY + "/" + tableName);
	}
}
