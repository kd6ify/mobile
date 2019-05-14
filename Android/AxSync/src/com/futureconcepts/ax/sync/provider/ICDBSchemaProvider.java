package com.futureconcepts.ax.sync.provider;

import com.futureconcepts.ax.model.data.ICDBSchema;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ICDBSchemaProvider extends ContentProvider
{
    private static final String TAG = ICDBSchemaProvider.class.getSimpleName();

    public static final String DATABASE_NAME = "ICDBSchema.db";
    private static final int DATABASE_VERSION = 2;

    
    private SchemaDatabaseHelper _openHelper;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class SchemaDatabaseHelper extends SQLiteOpenHelper
    {
        SchemaDatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate()
    {
        _openHelper = new SchemaDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri,
    		String[] projection,
    		String selection,
    		String[] selectionArgs,
            String sortOrder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (uri.getPathSegments().size() == 1)
        {
            qb.setTables(uri.getPathSegments().get(0));
            // If no sort order is specified use the default
        }
        else
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
    
    @Override
    public String getType(Uri uri)
    {
		int segSize = uri.getPathSegments().size();
		if (segSize == 1)
		{
			return ICDBSchema.CONTENT_ITEM_TYPE;
		}
		else
		{
            throw new IllegalArgumentException("Unknown URI " + uri);
		}
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesList)
    {
    	Log.d(TAG, "bulkInsert " + uri.toString());
    	int result = 0;
    	SQLiteDatabase db = _openHelper.getWritableDatabase();
    	try
    	{
	    	db.beginTransaction();
			createSchemaTable(db, uri);
			for (ContentValues values : valuesList)
			{
				Uri insertedUri = insert(uri, values);
				if (insertedUri != null)
				{
					result++;
				}
			}
			String tableName = uri.getPathSegments().get(0);
			int createTableResult = getContext().getContentResolver().bulkInsert(Uri.parse("content://" + ICDBProvider.AUTHORITY + "/" + tableName + "/schema"), valuesList);
			if (createTableResult != 1)
			{
				throw new SQLException("create table failed");
			}
			db.setTransactionSuccessful();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		db.endTransaction();
    	}
    	return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
    	Uri result = null;
    	Log.d(TAG, "insert " + uri.toString());
    	SQLiteDatabase db = _openHelper.getWritableDatabase();
    	String tableName = uri.getPathSegments().get(0);
	    long rowId = db.insert(tableName, BaseColumns._ID, values);
	    if (rowId > 0) 
	    {
	        result = ContentUris.withAppendedId(uri, rowId);
	        getContext().getContentResolver().notifyChange(uri, null);
	    }
	    else
	    {
	    	throw new SQLException("Failed to insert row into " + uri);
	    }
        return result;
    }
    
    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) 
    {
        int count = 0;
    	Log.d(TAG, "delete " + uri.toString());
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        int segSize = uri.getPathSegments().size();
        String tableName = uri.getPathSegments().get(0);
        if (segSize == 1)
        {
        	db.execSQL("DROP TABLE IF EXISTS " + tableName);
        	count = 1;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs)
    {
        throw new UnsupportedOperationException("update not supported " + uri);
    }
    
    private void createSchemaTable(SQLiteDatabase db, Uri uri)
    {
    	StringBuilder sb = new StringBuilder();
    	String tableName = uri.getPathSegments().get(0);
    	sb.append("CREATE TABLE ");
    	sb.append(tableName);
    	sb.append(" (");
    	sb.append(ICDBSchema.COLUMN_NAME);
    	sb.append(" TEXT, ");
    	sb.append(ICDBSchema.COLUMN_TYPE);
    	sb.append(" TEXT);");
		Log.d(TAG, String.format("trying to execSQL on db %s: %s ", db.getPath(), sb.toString()));
    	db.execSQL(sb.toString());
    }
}
