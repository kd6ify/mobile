package com.futureconcepts.mercury.provider;

import java.util.List;

import com.futureconcepts.mercury.crypto.SimpleCrypto;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsProvider extends ContentProvider
{
    private static final String TAG = "SettingsProvider";
    
    public static final String AUTHORITY = "com.futureconcepts.settings";
    
	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    
    private static final int TYPE_BOOLEAN = 1;
    private static final int TYPE_FLOAT = 2;
    private static final int TYPE_INT = 3;
    private static final int TYPE_LONG = 4;
    private static final int TYPE_STRING = 5;
    private static final int TYPE_ENCRYPTED_STRING = 6;
    
    private SharedPreferences mSharedPreferences;
    
    private static final UriMatcher sUriMatcher;
    
    /**
     * This class helps open, create, and upgrade the database file.
     */

    @Override
    public boolean onCreate()
    {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri,
    		String[] projection,
    		String selection,
    		String[] selectionArgs,
            String sortOrder)
    {
    	Log.d(TAG, "query " + uri.toString());
    	String key = uri.getPathSegments().get(1);
		MatrixCursor c = new MatrixCursor(new String[] { "value" });
        switch (sUriMatcher.match(uri))
        {
        case TYPE_BOOLEAN:
    		c.addRow(new Object[] { mSharedPreferences.getBoolean(key, false)});
    		break;
        case TYPE_FLOAT:
    		c.addRow(new Object[] { mSharedPreferences.getFloat(key, 0)});
    		break;
        case TYPE_INT:
    		c.addRow(new Object[] { mSharedPreferences.getInt(key, 0)});
    		break;
        case TYPE_LONG:
    		c.addRow(new Object[] { mSharedPreferences.getLong(key, 0)});
    		break;
        case TYPE_STRING:
    		c.addRow(new Object[] { mSharedPreferences.getString(key, null)});
            break;
        case TYPE_ENCRYPTED_STRING:
        	try
        	{
        		String encryptedValue = mSharedPreferences.getString(key, null);
        		String decryptedValue = SimpleCrypto.decrypt(encryptedValue);
				c.addRow(new Object[] { decryptedValue } );
			}
        	catch (Exception e)
        	{
				e.printStackTrace();
			}
        	break;
    	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return c;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
        case TYPE_BOOLEAN:
            return "vnd.android.cursor.item/vnd.futureconcepts.BooleanSetting";
        case TYPE_FLOAT:
            return "vnd.android.cursor.item/vnd.futureconcepts.FloatSetting";
        case TYPE_INT:
            return "vnd.android.cursor.item/vnd.futureconcepts.IntegerSetting";
        case TYPE_LONG:
            return "vnd.android.cursor.item/vnd.futureconcepts.LongSetting";
        case TYPE_STRING:
            return "vnd.android.cursor.item/vnd.futureconcepts.StringSetting";
        case TYPE_ENCRYPTED_STRING:
            return "vnd.android.cursor.item/vnd.futureconcepts.EncryptedStringSetting";
    	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
    	Log.d(TAG, "insert: " + uri.toString() + " " + values.toString());
    	List<String> segs = uri.getPathSegments();
    	String key = segs.get(1);
		SharedPreferences.Editor editor = mSharedPreferences.edit();
        switch (sUriMatcher.match(uri))
        {
        case TYPE_BOOLEAN:
    		editor.putBoolean(key, values.getAsBoolean("value"));
    		break;
        case TYPE_FLOAT:
    		editor.putFloat(key, values.getAsFloat("value"));
    		break;
        case TYPE_INT:
    		editor.putInt(key, values.getAsInteger("value"));
    		break;
        case TYPE_LONG:
    		editor.putLong(key, values.getAsLong("value"));
        case TYPE_STRING:
    		editor.putString(key, values.getAsString("value"));
            break;
    	default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        editor.commit();
        return uri;
    }
            
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) 
    {
    	throw new IllegalArgumentException("delete not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
    	throw new IllegalArgumentException("update not supported");
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "boolean/*", TYPE_BOOLEAN);
        sUriMatcher.addURI(AUTHORITY, "float/*", TYPE_FLOAT);
        sUriMatcher.addURI(AUTHORITY, "int/*", TYPE_INT);
        sUriMatcher.addURI(AUTHORITY, "long/*", TYPE_LONG);
        sUriMatcher.addURI(AUTHORITY, "string/*", TYPE_STRING);
        sUriMatcher.addURI(AUTHORITY, "encrypted_string/*", TYPE_ENCRYPTED_STRING);
    }
}
