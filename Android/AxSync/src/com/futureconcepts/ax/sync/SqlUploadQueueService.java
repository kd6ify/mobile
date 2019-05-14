package com.futureconcepts.ax.sync;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;

import com.futureconcepts.ax.model.data.ICDBSchema;
import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.HttpQueueService;
import com.futureconcepts.gqueue.OnReceiveFatalException;
import com.futureconcepts.gqueue.OnRetryException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SqlUploadQueueService extends HttpQueueService
{
	private static final String TAG = SqlUploadQueueService.class.getSimpleName();
			
    @Override
    public void onCreate()
    {
    	super.onCreate();
    }

	@Override
	protected void onBegin(Uri queueUri)
	{
	}

	@Override
	protected void onFinish()
	{
	}	

	@Override
	protected boolean onReceive(GQueue queue) throws OnReceiveFatalException, OnRetryException
	{
		boolean result = false;
		String contentUrl = queue.getLocalContentUrl();
		if (contentUrl != null)
		{
			Uri uri = Uri.parse(contentUrl);
			if (uri != null)
			{
				String tableName = uri.getPathSegments().get(0);
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				if (cursor != null && cursor.getCount() == 1)
				{
					cursor.moveToFirst();
					String content = serializeContent(tableName, cursor);
					cursor.close();
					String serverUrlUnfixed = String.format("server://ICDB/%s", tableName);
					String action = queue.getAction();
					result = postTransaction(serverUrlUnfixed, content.getBytes(), "application/json", action);
				}
			}
		}
		else
		{
			result = super.onReceive(queue);
		}
		return result;
	}
    
	@Override
	protected void onReceiveResponse(HttpEntity ent) throws OnReceiveFatalException, OnRetryException
	{
		Header contentType = ent.getContentType();
		Log.d(TAG, contentType.getValue());
	}
	
    private String serializeContent(String tableName, Cursor cursor)
    {
    	String result = null;
		StringWriter w = new StringWriter();
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator g = null;
    	try
    	{
    		g = jsonFactory.createJsonGenerator(w);
    		g.configure(Feature.ESCAPE_NON_ASCII, true);
    		g.setHighestNonEscapedChar(127);
    		g.enable(Feature.ESCAPE_NON_ASCII);
    		Hashtable<String, String> columns = ICDBSchema.getTableTypeMap(this, tableName);
			Enumeration<String> keys = columns.keys();
			g.writeStartArray();
			while (keys.hasMoreElements())
			{
				String columnName = keys.nextElement();
				int columnIndex = cursor.getColumnIndex(columnName);
				if (columnIndex != -1)
				{
					String columnType = columns.get(columnName);
					g.writeStartObject();
					g.writeStringField("Key", columnName);
					g.writeFieldName("Value");
					if (columnType.equals(ICDBSchema.TYPE_BOOL))
					{
						g.writeNumber(cursor.getInt(columnIndex));
					}
					else if (columnType.equals(ICDBSchema.TYPE_BLOB))
					{
						byte[] bytes = cursor.getBlob(columnIndex);
						if (bytes != null)
						{
							g.writeBinary(bytes);
						}
						else
						{
							g.writeNull();
						}
					}
					else if (columnType.equals(ICDBSchema.TYPE_DATE))
					{
						long date = cursor.getLong(columnIndex);
						if (date != 0)
						{
							g.writeNumber(date);
						}
						else
						{
							g.writeNull();
						}
					}
					else if (columnType.equals(ICDBSchema.TYPE_GUID))
					{
						String guidString = cursor.getString(columnIndex);
						if (guidString != null)
						{
							g.writeString(guidString);
						}
						else
						{
							g.writeNull();
						}
					}
					else if (columnType.equals(ICDBSchema.TYPE_INT32))
					{
						g.writeNumber(cursor.getInt(columnIndex));
					}
					else if (columnType.equals(ICDBSchema.TYPE_INT64))
					{
						g.writeNumber(cursor.getInt(columnIndex));
					}
					else if (columnType.equals(ICDBSchema.TYPE_REAL))
					{
						g.writeNumber(cursor.getDouble(columnIndex));
					}
					else if (columnType.equals(ICDBSchema.TYPE_TEXT))
					{
						String str = cursor.getString(columnIndex);
						if (str != null)
						{
							g.writeString(str);
						}
						else
						{
							g.writeNull();
						}
					}
					else
					{
						String str = cursor.getString(columnIndex);
						if (str != null)
						{
							g.writeString(str);
						}
						else
						{
							g.writeNull();
						}
					}
					g.writeEndObject();
				}
			}
			g.writeEndArray();
			g.flush();
			w.flush();
			result = new String(w.toString());
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	finally
    	{
    		try
    		{
    			if (g != null)
    			{
    				g.close();
    			}
				w.close();
    		}
    		catch (Exception e)
    		{
    		}
    	}
    	return result;
    }
    
	public static final class RestartReceiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			Intent startIntent = new Intent(context, SqlUploadQueueService.class);
			context.startService(startIntent);
	    }
	}
}
