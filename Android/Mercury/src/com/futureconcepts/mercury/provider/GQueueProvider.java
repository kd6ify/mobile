package com.futureconcepts.mercury.provider;

import java.util.HashMap;
import java.util.UUID;

import com.futureconcepts.mercury.gqueue.GQueue;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class GQueueProvider extends ContentProvider
{
    private static final String TAG = "GQueueProvider";

    public static final String DATABASE_NAME = "gqueue.db";
    private static final int DATABASE_VERSION = 2;

    private static final int QUEUE_ALL = 1;
    private static final int QUEUE_ID = 2;

    private static final UriMatcher sUriMatcher;
        
    private static HashMap<String, String> sProjectionMap;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            // TODO: get list of all tables and drop ???
            onCreate(db);
        }
    }

    private DatabaseHelper _openHelper;
    
	@Override
	public boolean onCreate()
	{
        _openHelper = new DatabaseHelper(getContext());
		return true;
	}
    
	@Override
	public int delete(Uri uri, String where, String[] whereArgs)
	{
    	Log.d(TAG, "delete " + uri.toString());
        int count = 0;
        switch (sUriMatcher.match(uri)) 
        {
        case QUEUE_ALL:
        	deleteAllQueueMessages(uri, where, whereArgs);
            break;
        case QUEUE_ID:
        	deleteQueueMessage(uri, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	private int deleteAllQueueMessages(Uri uri, String where, String[] whereArgs)
	{
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        String tableName = uri.getPathSegments().get(1);
        return db.delete(tableName, where, whereArgs);
	}
	
	private int deleteQueueMessage(Uri uri, String where, String[] whereArgs)
	{
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        String tableName = uri.getPathSegments().get(1);
        String rowId = uri.getPathSegments().get(2);
        return db.delete(tableName, GQueue._ID + "='" + rowId + "'", whereArgs);
	}
	
	@Override
	public String getType(Uri uri)
	{
        switch (sUriMatcher.match(uri)) {
        case QUEUE_ALL:
            return GQueue.CONTENT_TYPE;
        case QUEUE_ID:
            return GQueue.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
    	Log.d(TAG, "insert " + uri.toString());
    	if (sUriMatcher.match(uri) == QUEUE_ALL)
    	{
			if (values.containsKey(GQueue.MESSAGE_ID) == false)
			{
				values.put(GQueue.MESSAGE_ID, UUID.randomUUID().toString());
			}
			if (values.containsKey(GQueue.QUEUED_TIME) == false)
			{
				values.put(GQueue.QUEUED_TIME, System.currentTimeMillis());
			}
			if (values.containsKey(GQueue.CONTENT_MIME_TYPE) == false)
			{
				values.put(GQueue.CONTENT_MIME_TYPE, "text/plain");
			}
		    SQLiteDatabase db = _openHelper.getWritableDatabase();
		    String tableName = uri.getPathSegments().get(1);
		    long rowId = db.insert(tableName, GQueue._ID, values);
		    if (rowId == -1)
		    {
		    	createQueueTable(db, tableName);
		    	rowId = db.insert(tableName, GQueue._ID, values);
		    }
		    if (rowId > 0) 
		    {
		        Uri insertedUri = ContentUris.withAppendedId(uri, rowId);
		        getContext().getContentResolver().notifyChange(uri, null);
		        return insertedUri;
		    }
		    throw new SQLException("Failed to insert row into " + uri);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Unknown URI " + uri);
    	}
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
    	Log.d(TAG, "query " + uri.toString());
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String tableName = null;

        switch (sUriMatcher.match(uri))
        {
        case QUEUE_ALL:
        	tableName = uri.getPathSegments().get(1);
            qb.setTables(tableName);
            qb.setProjectionMap(sProjectionMap);
            // If no sort order is specified use the default
            break;
        case QUEUE_ID:
        	tableName = uri.getPathSegments().get(1);
            qb.setTables(tableName);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(2));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        try
        {
        	Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            // Tell the cursor what uri to watch, so it knows when its source data changes
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }
        catch (SQLiteException e)
        {
        	e.printStackTrace();
        	return null;
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void createQueueTable(SQLiteDatabase db, String tableName)
	{
	    db.execSQL("CREATE TABLE " + tableName + " ("
	            + GQueue._ID + " INTEGER PRIMARY KEY,"
	            + GQueue.QUEUED_TIME + " INTEGER,"
	            + GQueue.ACTION + " TEXT,"
	            + GQueue.LOCAL_CONTENT_URL + " TEXT,"
	            + GQueue.CONTENT + " BLOB,"
	            + GQueue.EXPIRATION_TIME + " INTEGER,"
	            + GQueue.SERVER_URL + " TEXT,"
	            + GQueue.CONTENT_MIME_TYPE + " TEXT,"
	            + GQueue.PRIORITY + " INTEGER,"
	            + GQueue.MESSAGE_ID + " TEXT,"
	            + GQueue.NOTIFICATION_MESSAGE + " TEXT,"
	            + GQueue.EXCEPTION_TYPE + " TEXT,"
	            + GQueue.PARAM1 + " TEXT"
	            + ");");
	    Log.d(TAG, "created queue " + tableName);
	}
	
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(GQueue.AUTHORITY, "queue/*", QUEUE_ALL);
        sUriMatcher.addURI(GQueue.AUTHORITY, "queue/*/#", QUEUE_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(GQueue._ID, GQueue._ID);
        sProjectionMap.put(GQueue.QUEUED_TIME, GQueue.QUEUED_TIME);
        sProjectionMap.put(GQueue.ACTION, GQueue.ACTION);
        sProjectionMap.put(GQueue.LOCAL_CONTENT_URL, GQueue.LOCAL_CONTENT_URL);
        sProjectionMap.put(GQueue.CONTENT, GQueue.CONTENT);
        sProjectionMap.put(GQueue.EXPIRATION_TIME, GQueue.EXPIRATION_TIME);
        sProjectionMap.put(GQueue.SERVER_URL, GQueue.SERVER_URL);
        sProjectionMap.put(GQueue.PRIORITY, GQueue.PRIORITY);
        sProjectionMap.put(GQueue.MESSAGE_ID, GQueue.MESSAGE_ID);
        sProjectionMap.put(GQueue.NOTIFICATION_MESSAGE, GQueue.NOTIFICATION_MESSAGE);
        sProjectionMap.put(GQueue.EXCEPTION_TYPE, GQueue.EXCEPTION_TYPE);
        sProjectionMap.put(GQueue.PARAM1, GQueue.PARAM1);
    }
}
