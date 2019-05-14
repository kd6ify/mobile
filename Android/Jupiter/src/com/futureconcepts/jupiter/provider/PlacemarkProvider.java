package com.futureconcepts.jupiter.provider;

import java.io.File;
import java.util.HashMap;

import com.futureconcepts.jupiter.data.BaseTable;
import com.futureconcepts.jupiter.data.Placemark;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class PlacemarkProvider extends BaseProvider
{
    private static final String TAG = "PlacemarkProvider";

    public static final String DATABASE_NAME = "placemark.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "placemark";

    private static HashMap<String, String> sProjectionMap;

    private static final int PLACEMARK = 1;
    private static final int PLACEMARK_ID = 2;

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
                    + Placemark._ID + " INTEGER PRIMARY KEY,"
                    + Placemark.PARENT_ID + " TEXT,"
                    + Placemark.ID + " TEXT UNIQUE,"
                    + Placemark.NAME + " TEXT,"
                    + Placemark.ADDRESS + " TEXT,"
                    + Placemark.DESCRIPTION + " TEXT,"
                    + Placemark.MEDIA_URL + " TEXT,"
                    + Placemark.LATITUDE + " REAL,"
                    + Placemark.LONGITUDE + " REAL,"
                    + Placemark.TIME + " INTEGER,"
                    + Placemark.ACCURACY + " INTEGER,"
                    + Placemark.ALTITUDE + " INTEGER,"
                    + Placemark.BEARING + " INTEGER,"
                    + Placemark.SPEED + " INTEGER,"
                    + Placemark.STYLE + " INTEGER"
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
        case PLACEMARK:
            count = db.delete(TABLE_NAME, "1", whereArgs);
            break;
        case PLACEMARK_ID:
        	count = deletePlacemark(uri, where, whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}

	private int deletePlacemark(Uri uri, String where, String[] whereArgs)
	{
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
		Placemark placemark = new Placemark(query(uri, null, null, null, null));
		if (placemark != null)
		{
			count = placemark.getCount();
			if (count == 1)
			{
				placemark.moveToFirst();
	            String id = uri.getPathSegments().get(1);
	            count = db.delete(TABLE_NAME, BaseTable._ID + "=" + id
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            String mediaUrl = placemark.getMediaUrl();
	            if (mediaUrl != null)
	            {
	            	deletePlacemarkMedia(Uri.parse(mediaUrl));
	            }
			}
			placemark.close();
		}
		return count;
	}
	
	private void deletePlacemarkMedia(Uri uri)
	{
    	Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
    	if (cursor != null)
    	{
    		if (cursor.getCount() == 1)
    		{
    			cursor.moveToFirst();
    			String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
    			if (path != null)
    			{
    				File delFile = new File(path);
    				if (delFile.exists())
    				{
    					delFile.delete();
    				}
    			}
    		}
    		cursor.close();
    		cursor = null;
    	}
    	try
    	{
    		getContext().getContentResolver().delete(uri, null, null);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	@Override
	public String getType(Uri uri)
	{
        switch (sUriMatcher.match(uri))
        {
        case PLACEMARK:
            return Placemark.CONTENT_TYPE;
        case PLACEMARK_ID:
            return Placemark.CONTENT_ITEM_TYPE;
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
    	if (match == PLACEMARK)
    	{
    		insertedUri = insertRow(TABLE_NAME, Placemark.CONTENT_URI, values);
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
        case PLACEMARK:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            // If no sort order is specified use the default
            break;
        case PLACEMARK_ID:
            qb.setTables(TABLE_NAME);
            qb.setProjectionMap(sProjectionMap);
            qb.appendWhere(BaseTable._ID + "=" + uri.getPathSegments().get(1));
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
        case PLACEMARK_ID:
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
        sUriMatcher.addURI(Placemark.AUTHORITY, "placemark", PLACEMARK);
        sUriMatcher.addURI(Placemark.AUTHORITY, "placemark/#", PLACEMARK_ID);

        sProjectionMap = new HashMap<String, String>();
        sProjectionMap.put(BaseTable._ID, BaseTable._ID);
        sProjectionMap.put(Placemark.ID, Placemark.ID);
        sProjectionMap.put(Placemark.NAME, Placemark.NAME);
        sProjectionMap.put(Placemark.DESCRIPTION, Placemark.DESCRIPTION);
        sProjectionMap.put(Placemark.MEDIA_URL, Placemark.MEDIA_URL);
        sProjectionMap.put(Placemark.PARENT_ID, Placemark.PARENT_ID);
        sProjectionMap.put(Placemark.LATITUDE, Placemark.LATITUDE);
        sProjectionMap.put(Placemark.LONGITUDE, Placemark.LONGITUDE);
        sProjectionMap.put(Placemark.TIME, Placemark.TIME);
        sProjectionMap.put(Placemark.ACCURACY, Placemark.ACCURACY);
        sProjectionMap.put(Placemark.ALTITUDE, Placemark.ALTITUDE);
        sProjectionMap.put(Placemark.BEARING, Placemark.BEARING);
        sProjectionMap.put(Placemark.SPEED, Placemark.SPEED);
        sProjectionMap.put(Placemark.STYLE, Placemark.STYLE);
    }
}
