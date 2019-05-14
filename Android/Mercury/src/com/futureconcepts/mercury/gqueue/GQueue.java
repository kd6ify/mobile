package com.futureconcepts.mercury.gqueue;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;

public class GQueue extends CursorWrapper implements BaseColumns
{
	private static final String TAG = "GQueue";
	
    public static final String AUTHORITY = "com.futureconcepts.provider.gqueue";

    /**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI_ROOT = Uri.parse("content://" + AUTHORITY + "/queue");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of queue entries.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.gqueue";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.gqueue";
	
	public static final String QUEUED_TIME = "QueuedTime";
	public static final String ACTION = "Action";
    public static final String LOCAL_CONTENT_URL = "ClientUrl";
    public static final String CONTENT = "Content";
    public static final String EXPIRATION_TIME = "ExpirationTime";
    public static final String CONTENT_MIME_TYPE = "ContentType";
    public static final String SERVER_URL = "ServerURL";
    public static final String PRIORITY = "Priority";
    public static final String MESSAGE_ID = "MessageId";
    public static final String NOTIFICATION_MESSAGE = "NotificationMessage";
    public static final String EXCEPTION_TYPE = "ExceptionType";
    public static final String PARAM1 = "Param1";
    
    public static final int PRIORITY_LOWEST = 10;
    public static final int PRIORITY_LOW = 7;
    public static final int PRIORITY_NORMAL = 5;
    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_HIGHEST = 0;
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = QUEUED_TIME + " DESC";

    private ContentResolver _resolver;
    
    private Uri _contentUri;
    
    public GQueue(Cursor cursor)
    {
    	super(cursor);
    }
    
    public ContentResolver getContentResolver()
    {
    	return _resolver;
    }
    
    public void setContentResolver(ContentResolver resolver)
    {
    	_resolver = resolver;
    }
    
    public Uri getContentUri()
    {
    	return _contentUri;
    }

    public void setContentUri(Uri value)
    {
    	_contentUri = value;
    }
    
    public int get_ID()
    {
    	return getInt(getColumnIndex(_ID));
    }
    
    public int getQueuedTime()
    {
    	return getInt(getColumnIndex(QUEUED_TIME));
    }
    
    public String getAction()
    {
    	return getString(getColumnIndex(ACTION));
    }
    
    public byte[] getContent()
    {
    	return getBlob(getColumnIndex(CONTENT));
    }
    
    public long getExpirationTime()
    {
    	return getLong(getColumnIndex(EXPIRATION_TIME));
    }
    
    public String getLocalContentUrl()
    {
    	return getString(getColumnIndex(LOCAL_CONTENT_URL));
    }
    
    public String getContentMimeType()
    {
    	int idx = getColumnIndex(CONTENT_MIME_TYPE);
    	if (idx > 0)
    	{
    		return getString(idx);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public String getServerUrl()
    {
    	int idx = getColumnIndex(SERVER_URL);
    	if (idx != -1)
    	{
    		return getString(idx);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    public int getPriority()
    {
    	return getInt(getColumnIndex(PRIORITY));
    }
    
    public String getMessageId()
    {
    	return getString(getColumnIndex(MESSAGE_ID));
    }
    
    public String getNotificationMessage()
    {
    	return getString(getColumnIndex(NOTIFICATION_MESSAGE));
    }
    
    public String getExceptionType()
    {
    	return getString(getColumnIndex(EXCEPTION_TYPE));
    }
    
    public String getParam1()
    {
    	return getString(getColumnIndex(PARAM1));
    }
    
    public static Uri getServiceQueueUri(Context context, Class<?> theClass)
    {
    	return getServiceQueueUri(context, context.getPackageName(), theClass.getName());
    }
    
    public static Uri getServiceQueueUri(Context context, String packageName, String className)
    {
    	Uri result = null;
    	PackageManager pm = context.getPackageManager();
    	if (pm != null)
    	{
			try
			{
				ComponentName compName = new ComponentName(packageName, className);
				ServiceInfo serviceInfo = pm.getServiceInfo(compName, PackageManager.GET_META_DATA);
				if (serviceInfo != null)
				{
					Bundle metaData = serviceInfo.metaData;
					if (metaData != null)
					{
						String queueUri = metaData.getString("QueueUri");
						if (queueUri != null)
						{
							result = Uri.parse(queueUri);
							Intent intent = new Intent();
							intent.setComponent(compName);
							context.startService(intent);
						}
					}
				}
			}
			catch (NameNotFoundException e)
			{
				e.printStackTrace();
			}
    	}
		return result;
    }
    
    public static int delete(Context context, Uri uri)
    {
    	if (context != null)
    	{
    		ContentResolver resolver = context.getContentResolver();
    		return resolver.delete(uri, null, null);
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    public int delete()
    {
    	if (_contentUri != null)
    	{
    		int rowId = get_ID();
    		Uri deleteUri = Uri.withAppendedPath(_contentUri, String.valueOf(rowId));
    		return _resolver.delete(deleteUri, null, null);
    	}
    	else
    	{
    		return 0;
    	}
    }
    
    public static Uri insertMessage(Context context, Uri uri, String content)
    {
    	ContentValues values = new ContentValues();
    	values.put(CONTENT, content);
    	return insertMessage(context, uri, values);
    }

    public static Uri insertMessage(Context context, Uri uri, String action, String serverUrl)
    {
    	ContentValues values = new ContentValues();
    	values.put(LOCAL_CONTENT_URL, uri.toString());
    	values.put(ACTION, action);
    	values.put(SERVER_URL, serverUrl);
    	return insertMessage(context, uri, values);
    }

    public static Uri insertMessage(Context context, Class<?> theClass, ContentValues values)
    {
    	return insertMessage(context, context.getPackageName(), theClass.getName(), values);
    }
    
    public static Uri insertMessage(Context context, String packageName, String className, ContentValues values)
    {
    	return insertMessage(context, getServiceQueueUri(context, packageName, className), values);
    }

    public static Uri insertMessage(Context context, Uri uri, ContentValues values)
    {
    	Uri result = null;
    	if (values.containsKey(QUEUED_TIME) == false)
    	{
    		values.put(QUEUED_TIME, System.currentTimeMillis());
    	}
    	if (values.containsKey(PRIORITY) == false)
    	{
    		values.put(PRIORITY, PRIORITY_NORMAL);
    	}
    	if (values.containsKey(CONTENT_MIME_TYPE) == false && values.containsKey(CONTENT))
    	{
    		values.put(CONTENT_MIME_TYPE, "application/json");
    	}
    	if (values.containsKey(PARAM1) == false)
    	{
    		Log.d(TAG, "param1 is null");
    	}
    	result = context.getContentResolver().insert(uri, values);
    	Log.d(TAG, String.format("insertMessage into %s", uri.toString()));
    	Log.d(TAG, values.toString());
    	Log.d(TAG, String.format("inserted Uri=%s", result));
    	return result;
    }
    
    public static GQueue query(Context context, Uri uri, String where, String[] whereArgs)
    {
    	GQueue result = null;
    	try
    	{
	    	ContentResolver resolver = context.getContentResolver();
	    	Cursor cursor = resolver.query(uri, null, where, whereArgs, PRIORITY + " ASC, " + QUEUED_TIME + " ASC");
	        if (cursor != null)
	        {
	        	if (cursor.getCount() > 0)
	        	{
	        		result = new GQueue(cursor);
	        		result.setContentResolver(resolver);
	        		result.setContentUri(uri);
	        	}
	        	else
	        	{
	        		cursor.close();
	        	}
	        }
    	}
    	catch (Exception e)
    	{
    	}
    	return result;
    }
    
    public void moveToQueue(Uri destinationQueueUri, String message, String exceptionType)
    {
    	int rowId = get_ID();
    	if (_resolver != null && _contentUri != null && rowId != 0)
    	{
	    	ContentValues values = new ContentValues();
			values.put(QUEUED_TIME, getQueuedTime());
	    	values.put(ACTION, getAction());
	    	values.put(LOCAL_CONTENT_URL, getLocalContentUrl());
	    	values.put(CONTENT, getContent());
	    	values.put(EXPIRATION_TIME, getExpirationTime());
	    	values.put(CONTENT_MIME_TYPE, getContentMimeType());
	    	values.put(SERVER_URL, getServerUrl());
	    	values.put(PRIORITY, getPriority());
	    	values.put(MESSAGE_ID, getMessageId());
	    	values.put(NOTIFICATION_MESSAGE, message);
	    	values.put(EXCEPTION_TYPE, getExceptionType());
	    	if (_resolver.insert(destinationQueueUri, values) != null)
	    	{
	    		Uri deleteUri = Uri.withAppendedPath(_contentUri, String.valueOf(rowId));
	    		_resolver.delete(deleteUri, null, null);
	    	}
    	}
    }
}
