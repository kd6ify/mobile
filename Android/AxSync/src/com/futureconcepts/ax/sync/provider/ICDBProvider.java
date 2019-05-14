package com.futureconcepts.ax.sync.provider;

import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.io.JsonStringEncoder;

import com.futureconcepts.ax.model.data.Asset;
import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.ICDBSchema;
import com.futureconcepts.ax.model.data.IncidentAssetView;
import com.futureconcepts.ax.model.data.OperationalPeriodAssetView;
import com.futureconcepts.ax.model.data.OperationalPeriodUserAssetView;
import com.futureconcepts.ax.sync.SqlUploadQueueService;
import com.futureconcepts.gqueue.GQueue;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class ICDBProvider extends BaseProvider
{
    private static final String TAG = ICDBProvider.class.getSimpleName();

    public static final String AUTHORITY = "com.futureconcepts.ax.sync.provider.icdb";

    public static final String DATABASE_NAME = "ICDB.db";
    private static final int DATABASE_VERSION = 2;

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
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new DatabaseHelper(getContext());
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
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (uri.getPathSegments().size() == 1)
        {
        	String tableName = uri.getPathSegments().get(0);
        	if (tableName.startsWith("query_"))
        	{
        		if (tableName.contains("distinct"))
        		{
        			qb.setDistinct(true);
        		}
        		Context context = getContext();
        		int resId = context.getResources().getIdentifier(tableName, "string", context.getPackageName());
        		if (resId == 0)
        		{
        			throw new SQLException("not such table " + tableName);
        		}
        		tableName = context.getString(resId);
        	}
    		qb.setTables(tableName);
            // If no sort order is specified use the default
        }
        else if (uri.getPathSegments().size() == 2)
        {
    		qb.setTables(uri.getPathSegments().get(0));
        	if (uri.getPathSegments().get(1).equals("view") == false)
        	{
        		qb.appendWhere("ID='" + uri.getPathSegments().get(1) + "'");
        	}
        }
        else
        {
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
    public String getType(Uri uri)
    {
		int segSize = uri.getPathSegments().size();
		if (segSize == 1)
		{
			String tableName = uri.getPathSegments().get(0);
			return "vnd.android.cursor.dir/vnd.futureconcepts." + tableName;
		}
		else if (segSize == 2)
		{
			String tableName = uri.getPathSegments().get(0);
			return "vnd.android.cursor.item/vnd.futureconcepts." + tableName;
		}
		else
		{
            throw new IllegalArgumentException("Unknown URI " + uri);
		}
    }
    
    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesList)
    {
    	int result = 0;
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	try
    	{
    		db.beginTransaction();
	    	if (uri.getPathSegments().size() > 1)
	    	{
	    		if (uri.getPathSegments().get(1).equals("schema"))
	    		{
	    			createTable(uri, valuesList);
	    			result = 1;
	    		}
	    	}
	    	else
	    	{
	    		for (ContentValues values : valuesList)
	    		{
	    			try
	    			{
		    			if (insert(uri, values) != null)
		    			{
		    				result++;
		    			}
	    			}
	    			catch (Exception e)
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    	}
	    	db.setTransactionSuccessful();
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
    	String uploadAction = null;
    	if (values.containsKey(BaseTable.UPLOAD))
    	{
    		uploadAction = values.getAsString(BaseTable.UPLOAD);
    		values.remove(BaseTable.UPLOAD);
    	}
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	if (uri.getPathSegments().size() > 1)
    	{
            throw new IllegalArgumentException("Illegal URI for insert " + uri);
    	}
    	String tableName = uri.getPathSegments().get(0);
  //  	Log.d(TAG, "Attempting to insert: " + values.toString());
	    long rowId = db.insertOrThrow(tableName, BaseColumns._ID, values);
	    if (rowId > 0) 
	    {
	        result = Uri.withAppendedPath(uri, values.getAsString(BaseTable.ID));
	        getContext().getContentResolver().notifyChange(uri, null);
		    Log.d(TAG, "inserted: " + result.toString());
	    }
	    if (uploadAction != null)
	    {
	    	uploadInsertToServer(result, values);
	    }
        return result;
    }
    
    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs)
    {
        int count = 0;
    	Log.d(TAG, "delete " + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int segSize = uri.getPathSegments().size();
        String tableName = uri.getPathSegments().get(0);
        if (segSize == 1)
        {
        	if (whereClause != null && whereClause.contains("drop="))
        	{
            	db.execSQL("DROP TABLE IF EXISTS " + tableName);
            	ICDBSchema.deleteTableTypeMap(getContext(), tableName);
            	onDropTable(db, uri);
            	count = 1;
        	}
        	else
        	{
        		count = db.delete(tableName, "1", whereArgs);
        	}
        }
        else if (segSize == 2)
        {
        	
        		String id = uri.getPathSegments().get(1);
        		String extendedWhereClause = "ID='" + id + "'"
                    + (!TextUtils.isEmpty(whereClause) ? " AND (" + whereClause + ')' : "");
            	count = db.delete(tableName, extendedWhereClause, whereArgs);
        }
        else
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
    	uploadDeleteToServer(uri); // possibility record does not exist on server
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs)
    {
    	int result = 0;
    	String uploadAction = null;
    	if (values.containsKey(BaseTable.UPLOAD))
    	{
    		uploadAction = values.getAsString(BaseTable.UPLOAD);
    		values.remove(BaseTable.UPLOAD);
    	}
    	Log.d(TAG, "update " + uri.toString());
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (uri.getPathSegments().size() == 2)
        {
        	String tableName = uri.getPathSegments().get(0);
        	String id = uri.getPathSegments().get(1);
            result = db.update(tableName, values, "ID='" + id + "'"
                    + (!TextUtils.isEmpty(whereClause) ? " AND (" + whereClause + ')' : ""), whereArgs);
        }
        else
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        if (uploadAction != null)
        {
        	try
        	{
        		if (uploadAction.equals(Intent.ACTION_INSERT))
        		{
        			uploadInsertToServer(uri, values);
        		}
        		else if (uploadAction.equals(Intent.ACTION_EDIT))
        		{
        			uploadUpdateToServer(uri, values);
        		}
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        		// TODO ??
        	}
        }
        return result;
    }
    
    private void uploadUpdateToServer(Uri uri, ContentValues values)
    {
    	try
    	{
	    	String tableName = uri.getPathSegments().get(0);
	    	String id = uri.getPathSegments().get(1);
			Uri uploadQueue = GQueue.getServiceQueueUri(getContext(), SqlUploadQueueService.class);
			String serializedValues = serializeContentValues(tableName, values);
			ContentValues qv = new ContentValues();
			qv.put(GQueue.CONTENT, serializedValues.getBytes());
			qv.put(GQueue.ACTION, Intent.ACTION_EDIT);
			qv.put(GQueue.SERVER_URL, String.format("server://ICDB/%s/%s", tableName, id));
			GQueue.insertMessage(getContext(), uploadQueue, qv);
    	}
    	catch (Exception e)
    	{
    		
    	}
    }
    
    private void uploadInsertToServer(Uri uri, ContentValues values)
    {
    	try
    	{
	    	String tableName = uri.getPathSegments().get(0);
			Uri uploadQueue = GQueue.getServiceQueueUri(getContext(), SqlUploadQueueService.class);
			String serializedValues = serializeContentValues(tableName, values);
			ContentValues qv = new ContentValues();
			qv.put(GQueue.CONTENT, serializedValues.getBytes());
			qv.put(GQueue.ACTION, Intent.ACTION_INSERT);
			qv.put(GQueue.SERVER_URL, String.format("server://ICDB/%s", tableName));
			GQueue.insertMessage(getContext(), uploadQueue, qv);
    	}
    	catch (Exception e)
    	{
    		
    	}
    }
    
    private void uploadDeleteToServer(Uri uri)
    {
    	try
    	{
	    	String tableName = uri.getPathSegments().get(0);
	    	String id = uri.getPathSegments().get(1);
			Uri uploadQueue = GQueue.getServiceQueueUri(getContext(), SqlUploadQueueService.class);
			ContentValues qv = new ContentValues();
			qv.put(GQueue.ACTION, Intent.ACTION_DELETE);
			qv.put(GQueue.SERVER_URL, String.format("server://ICDB/%s/%s", tableName, id));
			GQueue.insertMessage(getContext(), uploadQueue, qv);
    	}
    	catch (Exception e)
    	{
    	}
    }
    
    private String serializeContentValues(String tableName, ContentValues values)
    {
    	String result = null;
		StringWriter w = new StringWriter();
		JsonFactory jsonFactory = new JsonFactory();
		JsonGenerator g = null;
		JsonStringEncoder stringEncoder = new JsonStringEncoder();
    	try
    	{
    		g = jsonFactory.createJsonGenerator(w);
    		g.configure(Feature.ESCAPE_NON_ASCII, true);
    		g.setHighestNonEscapedChar(127);
    		g.enable(Feature.ESCAPE_NON_ASCII);
    		Hashtable<String, String> columns = ICDBSchema.getTableTypeMap(getContext(), tableName);
			Enumeration<String> keys = columns.keys();
			g.writeStartArray();
			while (keys.hasMoreElements())
			{
				String columnName = keys.nextElement();
				if (values.containsKey(columnName))
				{
					String columnType = columns.get(columnName);
					g.writeStartObject();
					g.writeStringField("Key", columnName);
					g.writeFieldName("Value");
					if (columnType.equals(ICDBSchema.TYPE_BOOL))
					{
						g.writeNumber(values.getAsInteger(columnName));
					}
					else if (columnType.equals(ICDBSchema.TYPE_BLOB))
					{
						byte[] bytes = values.getAsByteArray(columnName);
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
						long date = values.getAsLong(columnName);
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
						String guidString = values.getAsString(columnName);
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
						g.writeNumber(values.getAsInteger(columnName));
					}
					else if (columnType.equals(ICDBSchema.TYPE_INT64))
					{
						g.writeNumber(values.getAsInteger(columnName));
					}
					else if (columnType.equals(ICDBSchema.TYPE_REAL))
					{
						g.writeNumber(values.getAsDouble(columnName));
					}
					else if (columnType.equals(ICDBSchema.TYPE_TEXT))
					{
						String str = values.getAsString(columnName);
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
						String str = values.getAsString(columnName);
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
    
    private void createTable(Uri uri, ContentValues[] valuesList)
    {
		String tableName = uri.getPathSegments().get(0);
    	StringBuilder sb = new StringBuilder();
    	sb.append("CREATE TABLE ");
    	sb.append(tableName);
    	sb.append(" (");
    	sb.append(BaseColumns._ID);
    	sb.append(" ");
    	sb.append("INTEGER PRIMARY KEY");
    	for (ContentValues values : valuesList)
    	{
			sb.append(",");
			sb.append(values.getAsString(ICDBSchema.COLUMN_NAME));
			sb.append(" ");
			String columnTypeWithOptions = values.getAsString(ICDBSchema.COLUMN_TYPE);
			String[] columnType = columnTypeWithOptions.split(" ");
			if (columnType[0].equals(ICDBSchema.TYPE_DATE))
			{
				sb.append("INTEGER");
			}
			else if (columnType[0].equals(ICDBSchema.TYPE_BLOB))
			{
				sb.append("BLOB");
			}
			else if (columnType[0].equals(ICDBSchema.TYPE_BOOL))
			{
				sb.append("INTEGER");
			}
			else if (columnType[0].equals(ICDBSchema.TYPE_INT32))
			{
				sb.append("INTEGER");
			}
			else if (columnType[0].equals(ICDBSchema.TYPE_INT64))
			{
				sb.append("INTEGER");
			}
			else if (columnType[0].equals(ICDBSchema.TYPE_REAL))
			{
				sb.append("REAL");
			}
			else
			{
				sb.append("TEXT");
			}
			if (columnTypeWithOptions.contains(ICDBSchema.FLAG_UNIQUE))
			{
				sb.append(" UNIQUE");
			}
		}
		sb.append(");");
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		Log.d(TAG, String.format("trying to execSQL on db %s: %s ", db.getPath(), sb.toString()));
		db.execSQL(sb.toString());
		Log.d(TAG, tableName + " was successfully created");
		onCreateTable(db, uri);
    }
    
    private void onCreateTable(SQLiteDatabase db, Uri uri)
    {
    	if (uri.getPathSegments().get(0).equals("Asset"))
    	{
    		createView(db, IncidentAssetView.CONTENT_URI, IncidentAssetView.QUERY);
    		createView(db, OperationalPeriodAssetView.CONTENT_URI, OperationalPeriodAssetView.QUERY);
    		createView(db, OperationalPeriodUserAssetView.CONTENT_URI, OperationalPeriodUserAssetView.QUERY);
    	}
    }

    private void onDropTable(SQLiteDatabase db, Uri uri)
    {
    	if (uri.equals(Asset.CONTENT_URI))
    	{
    		dropView(db, IncidentAssetView.CONTENT_URI);
    		dropView(db, OperationalPeriodAssetView.CONTENT_URI);
    		dropView(db, OperationalPeriodUserAssetView.CONTENT_URI);
    	}
    }
    
    private void createView(SQLiteDatabase db, Uri uri, String query)
    {
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("CREATE VIEW ");
			sql.append(uri.getPathSegments().get(0));
			sql.append(" AS ");
			sql.append(query);
			sql.append(";");
			db.execSQL(sql.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }

    private void dropView(SQLiteDatabase db, Uri uri)
    {
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("DROP VIEW IF EXISTS ");
			sql.append(uri.getPathSegments().get(0));
			sql.append(";");
			db.execSQL(sql.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    }
    
    public static Uri getTableUri(String tableName)
    {
    	return Uri.parse("content://" + BaseTable.AUTHORITY + "/" + tableName);
    }
}
