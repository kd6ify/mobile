package com.futureconcepts.jupiter.provider;

import java.util.HashMap;

import com.futureconcepts.jupiter.data.Track;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class TrackProvider extends BaseProvider
{
    private static final String TAG = "TrackProvider";

    public static final String DATABASE_NAME = "track.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "track";

    private static HashMap<String, String> sProjectionMap;

    private static final int TRACK = 1;
    private static final int TRACK_ID = 2;

    private static final UriMatcher sUriMatcher;

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
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + Track._ID + " INTEGER PRIMARY KEY,"
                    + Track.PARENT_ID + " TEXT,"
                    + Track.SEQUENCE + " INTEGER,"
                    + Track.LATITUDE + " REAL,"
                    + Track.LONGITUDE + " REAL"
                    + ");");
            Log.d(TAG, "created " + TABLE_NAME);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

	@Override
	public int delete(Uri uri, String where, String[] whereArgs)
	{
    	Log.d(TAG, "delete " + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        int match = sUriMatcher.match(uri);
        switch (match) 
        {
        case TRACK:
            count = db.delete(TABLE_NAME, "1", whereArgs);
            break;
        case TRACK_ID:
        	count = deleteTrack(uri, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	private int deleteTrack(Uri uri, String where, String[] whereArgs)
	{
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
		Track track = new Track(query(uri, null, null, null, null));
		if (track != null)
		{
			count = track.getCount();
			if (count == 1)
			{
				track.moveToFirst();
	            String id = uri.getPathSegments().get(1);
	            count = db.delete(TABLE_NAME, Track._ID + "=" + id
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			}
			track.close();
		}
		return count;
	}
	
	@Override
	public String getType(Uri uri)
	{
        switch (sUriMatcher.match(uri))
        {
        case TRACK:
            return Track.CONTENT_TYPE;
        case TRACK_ID:
            return Track.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
    	Log.d(TAG, "insert " + uri.toString());
    	Uri insertedUri = null;
    	int match = sUriMatcher.match(uri);
    	if (match == TRACK)
    	{
    		insertedUri = insertRow(TABLE_NAME, Track.CONTENT_URI, values);
    	}
    	else
    	{
    		throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    	return insertedUri;
	}

	@Override
	public boolean onCreate()
	{
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder)
	{
    	Log.d(TAG, "query " + uri.toString());
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri))
        {
        case TRACK:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            // If no sort order is specified use the default
            break;
        case TRACK_ID:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(Track._ID + "=" + uri.getPathSegments().get(1));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
	{
    	Log.d(TAG, "update " + uri.toString());
        int count;
        switch (sUriMatcher.match(uri))
        {
        case TRACK_ID:
        	count = updateRow(TABLE_NAME, uri, values, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Track.AUTHORITY, "track", TRACK);
        sUriMatcher.addURI(Track.AUTHORITY, "track/#", TRACK_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(Track._ID, Track._ID);
        sProjectionMap.put(Track.PARENT_ID, Track.PARENT_ID);
        sProjectionMap.put(Track.SEQUENCE, Track.SEQUENCE);
        sProjectionMap.put(Track.LATITUDE, Track.LATITUDE);
        sProjectionMap.put(Track.LONGITUDE, Track.LONGITUDE);
    }
}
