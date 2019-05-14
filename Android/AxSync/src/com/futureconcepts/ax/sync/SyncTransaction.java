package com.futureconcepts.ax.sync;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.futureconcepts.ax.model.data.BaseTable;
import com.futureconcepts.ax.model.data.Guid;
import com.futureconcepts.ax.model.data.ICDBSchema;
import com.futureconcepts.ax.model.dataset.DataSet;
import com.futureconcepts.ax.model.dataset.DataSetFactory;
import com.futureconcepts.ax.model.dataset.DataSetTable;
import com.futureconcepts.ax.model.dataset.IncidentDataSetTable;
import com.futureconcepts.ax.model.dataset.OwnerDataSetTable;
import com.futureconcepts.ax.sync.client.ISyncTransaction;
import com.futureconcepts.ax.sync.client.ISyncTransactionListener;
import com.futureconcepts.ax.sync.client.SyncError;
import com.futureconcepts.ax.sync.config.Config;
import com.futureconcepts.ax.sync.provider.ICDBProvider;
import com.futureconcepts.ax.sync.tablevalidators.ITableValidate;
import com.futureconcepts.ax.sync.tablevalidators.TableData;
import com.futureconcepts.gqueue.AuthTokenInvalidException;
import com.futureconcepts.gqueue.MercurySettings;
import com.futureconcepts.gqueue.OnReceiveFatalException;
import com.futureconcepts.gqueue.OnRetryException;
import com.futureconcepts.gqueue.PreemptiveAuth;

public class SyncTransaction  extends ISyncTransaction.Stub
{
	private static final String TAG = SyncTransaction.class.getSimpleName();
	
    private RemoteCallbackList<ISyncTransactionListener> mRemoteListeners = new RemoteCallbackList<ISyncTransactionListener>();

    private Context _context;
    private ContentResolver _resolver;
    private String _action;
	private String _dataset;
	private String _query;
	private String _table;
	private SyncError _error;
	private boolean _aborted = false;
	
	private DefaultHttpClient _client;
	private HttpContext _httpContext;
	private String _authToken;

	private int _syncResultsCount;
	private int _syncResultsPosition;
	private Hashtable<String, String> _typeMap;
	private long _lastSyncVersion;
	
	private JsonFactory _jsonFactory;
	
	private ITableValidate _tableValidator;
	private List<TableData> _tablesData = new ArrayList<TableData>();
	
	public SyncTransaction(Context context, String action, String dataset)
	{
		_context = context;
		_action = action;
		_dataset = dataset;
		_resolver = context.getContentResolver();
		_jsonFactory = new JsonFactory();
	}
	
	@Override
	public void registerListener(ISyncTransactionListener listener)	throws RemoteException
	{
    	if (listener != null)
    	{
            mRemoteListeners.register(listener);
        }
	}

	@Override
	public void unregisterListener(ISyncTransactionListener listener) throws RemoteException
	{
		if (listener != null)
		{
			mRemoteListeners.unregister(listener);
		}
	}

	@Override
	public void abort()
	{
		Log.d(TAG, "abort");
		_aborted = true;
	}
	
	@Override
	public String getAction() throws RemoteException
	{
		return _action;
	}

	@Override
	public String getDataset() throws RemoteException
	{
		return _dataset;
	}

	public void setTable(String value)
	{
		_table = value;
		try
		{
			_tableValidator = createValidator();
			_typeMap = ICDBSchema.getTableTypeMap(_context, value);
		}
		catch (Exception e)
		{
			_typeMap = null;
		}
		notifyTableChanged();
	}
	

	private ITableValidate createValidator(){
		try{
			String className = String.format("com.futureconcepts.ax.sync.tablevalidators.TableValidator%s", _table);
			Class<?> clazz = Class.forName(className);
			Constructor<?> ctor = clazz.getConstructor(); 
			Log.e("SycnTransaction","Validator Created");
			return (ITableValidate)ctor.newInstance();			
		}catch (Exception e){
			//class not found Exception/
		}
		return null;	
	}
	
	@Override
	public String getTable() throws RemoteException
	{
		return _table;
	}
	
	@Override
	public SyncError getError()
	{
		return _error;
	}
	
	public void setError(SyncError value)
	{
		if (_error != value)
		{
			_error = value;
			notifyError();
		}
	}
	
	public void submit() throws OnRetryException, OnReceiveFatalException
	{
		if (_action != null)
		{
			if (_action.equals("sync"))
			{
				handleSync();
			}
			else if (_action.equals("delete"))
			{
				handleDelete();
			}
			else if (_action.equals("drop"))
			{
				handleDrop();
			}
		}
	}

	private void handleSync() throws OnReceiveFatalException, OnRetryException
	{
		DataSet dataSet = DataSetFactory.get(_dataset);
		for (DataSetTable dataSetTable : dataSet.getList())
		{
			if (_aborted == false)
			{		
				_query = dataSetTable.getQuery();		
				//Save sync version of each query in case of database inconsistency.
				_tablesData.add(new TableData(_query,Config.getLastSyncVersion(_context, _query)));
				Log.e("SyncTransaction","handleSyncMethod: query: "+_query+"  syncVersion: "+Config.getLastSyncVersion(_context, _query));
				postIt(getServerUrl(dataSetTable), null, null, null);
			}
		}
	}

	private void handleDelete()
	{
		DataSet dataSet = DataSetFactory.get(_dataset);
		for (DataSetTable dataSetTable : dataSet.getList())
		{
			try
			{
				_resolver.delete(dataSetTable.getUri(), null, null);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			String query = dataSetTable.getQuery();
			Config.setLastSyncVersion(_context, query, -1);
		}
	}

	private void handleDrop()
	{
		DataSet dataSet = DataSetFactory.get(_dataset);
		for (DataSetTable dataSetTable : dataSet.getList())
		{
			try
			{
				dropTable(dataSetTable.getUri());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			String query = dataSetTable.getQuery();
			Config.setLastSyncVersion(_context, query, -1);
		}
	}

	private void dropTable(Uri uri)
	{
		_resolver.delete(uri, "drop='true'", null);
		_resolver.delete(ICDBSchema.getSchemaTableUri(uri.getPathSegments().get(0)), null, null);
	}

	private void onReceiveResponse(HttpEntity ent) throws JsonParseException, IllegalStateException, IOException, MobileServerException
	{
		if (ent != null)
		{
			Header responseContentType = ent.getContentType();
			if (responseContentType != null)
			{
				if (responseContentType.getValue().contains("application/json"))
				{
					JsonParser p = _jsonFactory.createJsonParser(ent.getContent());
					parseSyncResults(p);
				}
				else if (responseContentType.getValue().contains("application/vnd.futureconcepts.mobileserver.exception-v1+json"))
				{
					JsonParser p;
					p = _jsonFactory.createJsonParser(ent.getContent());
					handleServerException(p);
				}
			}
		}
	}
	
	private boolean postIt(String serverUrlUnfixed, byte[] content, String contentType, String action ) throws OnReceiveFatalException, OnRetryException
	{
		try
		{
			if (_client == null)
			{
				BasicScheme basicAuth = new BasicScheme();
				_client = new DefaultHttpClient();
				_httpContext = new BasicHttpContext();
				_httpContext.setAttribute("preemptive-auth", basicAuth);
				_client.addRequestInterceptor(new PreemptiveAuth(), 0);
				setCredentials();
			}
			HttpRequestBase request = null;
			HttpEntityEnclosingRequestBase enclosingRequest = null;			
			String serverUrl = serverUrlUnfixed.replace("server:/", MercurySettings.getWebServiceAddress(_context));
			if (action == null)
			{
				action = Intent.ACTION_VIEW;
			}
			if (action.equals(Intent.ACTION_VIEW))
			{
				HttpGet get = new HttpGet(serverUrl);
				request = get;
			}
			else if (action.equals(Intent.ACTION_INSERT))
			{
				HttpPut put = new HttpPut(serverUrl);
				enclosingRequest = put;
				request = put;
			}
			else if (action.equals(Intent.ACTION_EDIT))
			{
				HttpPost post = new HttpPost(serverUrl);
				enclosingRequest = post;
				request = post;
			}
			else if (action.equals(Intent.ACTION_DELETE))
			{
				HttpDelete delete = new HttpDelete(serverUrl);
				request = delete;
			}
			if (enclosingRequest != null)
			{
				if (content != null)
				{
					enclosingRequest.setEntity(new StringEntity(new String(content)));
					if (contentType == null)
					{
						request.addHeader("Content-Type", "application/json; charset=utf-8");
					}
					else
					{
						request.addHeader("Content-Type", contentType);
					}
				}
			}
			request.getParams().setIntParameter("http.socket.timeout", 60 * 1000); // 1 minute
			request.addHeader("DeviceId", MercurySettings.getDeviceId(_context));
			Log.i(TAG, request.getRequestLine().toString());
			notifyStatusChanged(request.getRequestLine().toString());
			notifyServerFetch();
			HttpResponse response = _client.execute(request, _httpContext);
			notifyServerFetchDone();
			HttpEntity ent = response.getEntity();
			StatusLine statusLine = response.getStatusLine();
			notifyStatusChanged(statusLine.toString());
			Log.i(TAG, statusLine.toString());
			int statusCode = statusLine.getStatusCode();
			if (statusCode != 200)
			{
				 // handle gateway timeout (504) and temp unavailable (503) like a socket exception
				// socket exceptions are auto retried
				if (statusCode == 504 || statusCode == 503 || statusCode == 502)
				{
					throw new SocketException();
				}
				else if (statusCode == 403)
				{
					if (_authToken != null)
					{
						_authToken = null;
						setCredentials();
						throw new AuthTokenInvalidException();
					}
					else
					{
						throw new HttpException(statusLine.toString());
					}
				}
				else
				{
					throw new HttpException(statusLine.toString());
				}
			}
			if (ent != null && _aborted == false)
			{
				onReceiveResponse(ent);
				ent.consumeContent();
			}
			Header authTokenHeader = response.getFirstHeader("AuthToken");
			if (authTokenHeader != null)
			{
				_authToken = new String(authTokenHeader.getValue()); 
				_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(MercurySettings.getDeviceId(_context), _authToken));
			}
		}
		catch (AuthTokenInvalidException e)
		{
			throw new OnRetryException(e, 1000);
		}
		catch (IOException e)
		{
			throw new OnRetryException(e, 60 * 1000);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new OnReceiveFatalException(e);
		}
		return true;
	}
	
	private void setCredentials()
	{
		if (_client != null)
		{
			String deviceId = MercurySettings.getDeviceId(_context);
			String password = MercurySettings.getPassword(_context);
			_client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, "tracker.antaresx.net"), new UsernamePasswordCredentials(deviceId, password));
		}
	}

	private String getServerUrl(DataSetTable dataSetTable)
	{
		StringBuilder sb = new StringBuilder(MercurySettings.getWebServiceAddress(_context));
		sb.append("/ICDB/");
		sb.append(dataSetTable.getQuery());
		sb.append("/");
		long lastSyncVersion = Config.getLastSyncVersion(_context, dataSetTable.getQuery());		
		if (lastSyncVersion == -1)
		{
			 // needs initial sync			
			if (dataSetTable instanceof IncidentDataSetTable)
			{
				sb.append(MercurySettings.getCurrentIncidentId(_context));
				sb.append("/");
			}
			else if (dataSetTable instanceof OwnerDataSetTable)
			{
				sb.append(Config.getDeviceId(_context));
				sb.append("/");
			}
			sb.append("Initial");
		}
		else
		{
			// incremental sync
			sb.append(lastSyncVersion);
			if (dataSetTable instanceof IncidentDataSetTable)
			{
				sb.append("/");
				sb.append(MercurySettings.getCurrentIncidentId(_context));
			}
			else if (dataSetTable instanceof OwnerDataSetTable)
			{
				sb.append("/");
				sb.append(Config.getDeviceId(_context));
			}
		}
		return sb.toString();
	}

	private void parseSyncResults(JsonParser p) throws JsonParseException, IOException, MobileServerException
	{
		_table = null;
		_lastSyncVersion = 0;
		_error = null;
		_typeMap = null;
		_syncResultsCount = 0;
		_syncResultsPosition = 0;
		if (p.nextToken() == JsonToken.START_OBJECT)
		{
			while (p.nextToken() != JsonToken.END_OBJECT)
			{
				if (p.getCurrentToken() == JsonToken.FIELD_NAME)
				{
					String key = p.getText();
					p.nextToken();
					if (key.equals("TableName"))
					{
						setTable(p.getText());
					}
					else if (key.equals("Version"))
					{
						_lastSyncVersion = p.getLongValue();						
					}
					else if (key.equals("ErrorCode"))
					{
						setError(new SyncError(String.valueOf(p.getIntValue())));
					}
					else if (key.equals("ResyncUrl"))
					{
						dropTable(ICDBProvider.getTableUri(_table));
						Config.setLastSyncVersion(_context, _table, -1);
						try
						{
							// TODO - if the server sends back resync and the resync throws an exception??
							postIt(p.getText(), null, null, null);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (key.equals("Exception"))
					{
						handleServerException(p);
					}
					else if (key.equals("Count"))
					{
						_syncResultsCount = p.getIntValue();
						notifyProgress(0, _syncResultsCount);
					}
					else if (key.equals("Schema"))
					{
						handleSchema(p);
					}
					else if (key.equals("Inserts"))
					{
						try
						{
							handleInserts(p);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (key.equals("Updates"))
					{
						try
						{
							handleUpdates(p);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (key.equals("Deletes"))
					{
						try
						{
							handleDeletes(p);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						throw new UnsupportedOperationException("parse sync results field " + key);
					}
				}
			}
		}
		if (_aborted == false)
		{
			Log.e("SyncTransaction","saving For table: "+_table +" syncVersion: "+_lastSyncVersion);
			Config.setLastSyncVersion(_context, _query, _lastSyncVersion);
		}
		Log.e("SyncTransaction","===========================================================");
	}

	private void handleServerException(JsonParser p) throws JsonParseException, IOException, MobileServerException
	{
		Log.d(TAG, "handleServerException");
		String tableName = null;
		long version = 0;
		int errorCode = 0;
		
		if (p.nextToken() == JsonToken.START_OBJECT)
		{
			while (p.nextToken() != JsonToken.END_OBJECT)
			{
				if (p.getCurrentToken() == JsonToken.FIELD_NAME)
				{
					String key = p.getText();
					p.nextToken();
					if (key.equals("TableName"))
					{
						tableName = p.getText();
					}
					else if (key.equals("Version"))
					{
						version = p.getLongValue();
					}
					else if (key.equals("ErrorCode"))
					{
						errorCode = p.getIntValue();
						setError(new SyncError(String.valueOf(p.getIntValue())));
					}
					else if (key.equals("ResyncUrl"))
					{
						dropTable(ICDBProvider.getTableUri(_table));
						Config.setLastSyncVersion(_context, _table, -1);
						try
						{
							// TODO - if the server sends back resync and the resync throws an exception??
							postIt(p.getText(), null, null, null);
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (key.equals("Exception"))
					{
						throw handleInnerException(p);					
					}
					else if (key.equals("Count"))
					{
						p.getIntValue();
					}
					else if (key.equals("Schema"))
					{
						parseRows(p, null);
					}
					else if (key.equals("Inserts"))
					{
						parseRows(p, null);
					}
					else if (key.equals("Updates"))
					{
						parseRows(p, null);
					}
					else if (key.equals("Deletes"))
					{
						parseRows(p, null);
					}
					else
					{
						throw new UnsupportedOperationException("parse sync results field " + key);
					}
				}
			}
		}
	}

	private MobileServerException handleInnerException(JsonParser p)
	{
		MobileServerException result = null;
		Log.d(TAG, "handleInnerException");
		Hashtable<String, String> typeMap = new Hashtable<String, String>();
		typeMap = new Hashtable<String, String>();
		typeMap.put("Message", ICDBSchema.TYPE_TEXT);
		typeMap.put("HelpLink", ICDBSchema.TYPE_TEXT);
		typeMap.put("Source", ICDBSchema.TYPE_TEXT);
		typeMap.put("Data", ICDBSchema.TYPE_TEXT);
		typeMap.put("StackTrace", ICDBSchema.TYPE_TEXT);
		try
		{
			ContentValues[] valuesList = parseRows(p, typeMap);
			for (ContentValues values : valuesList)
			{
				String message = values.getAsString("Message");
				result = new MobileServerException(message);
				if (message != null)
				{
					Log.d(TAG, "Message: " + message);
				}
				String helpLink = values.getAsString("HelpLink");
				if (helpLink != null)
				{
					Log.d(TAG, "HelpLink: " + helpLink);
				}
				String source = values.getAsString("Source");
				if (source != null)
				{
					Log.d(TAG, "Source: "  + source);
				}
				String data = values.getAsString("Data");
				if (data != null)
				{
					Log.d(TAG, "Data: " + data);
				}
				values.toString();
				String stackTrace = values.getAsString("StackTrace");
				if (stackTrace != null)
				{
					Log.d(TAG, "StackTrace: " + stackTrace);
				}
			}
		}
		catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private void handleSchema(JsonParser p) throws JsonParseException, IOException
	{
		Hashtable<String, String> schemaTypeMap = ICDBSchema.getSchemaTypeMap(_context);
		ContentValues[] valuesList = parseRows(p, schemaTypeMap);
		notifyProgress(_syncResultsPosition, _syncResultsCount);
		if (_typeMap == null)
		{
			Uri uri = ICDBSchema.getSchemaTableUri(_table);
			_resolver.bulkInsert(uri, valuesList);
			_typeMap = ICDBSchema.getTableTypeMap(_context, _table);
		}
		_syncResultsPosition += valuesList.length;
		notifyProgress(_syncResultsPosition, _syncResultsCount);
	}
	
	private void handleInserts(JsonParser p) throws JsonParseException, IOException
	{
		Uri tableUri = ICDBProvider.getTableUri(_table);
		ContentValues[] valuesList = parseRows(p, _typeMap);
	//	try
	//	{
			for (ContentValues values : valuesList)
			{
				try
				{
					 validateContent(values);
						if (_aborted == false &&_resolver.insert(tableUri, values) != null)
						{
							_syncResultsPosition++;
							notifyProgress(_syncResultsPosition, _syncResultsCount);
						}
					
				}
				catch (SQLiteConstraintException e)
				{
					try
					{
						doUpdateRow(tableUri, values);
					}
					catch (Exception e2) {}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		
	//		getContentResolver().bulkInsert(tableUri, valuesList);
	//	}
	//	catch (SQLiteConstraintException e1)
	//	{
	//		// Ignore constraint errors -- this probably means the row already exists--it's not expected
	//		// but should not cause the sync operation to fail
	//	}
	//	catch (SQLException e2)
	//	{
	//		Log.d(TAG, "caught SQLException trying to insert into " + tableUri.toString());
	//		e2.printStackTrace();
	//	}
	}
	
	
	private void validateContent(ContentValues values){
		if (_tableValidator != null){
			if(!_tableValidator.valid(values,_context)){
				abort();
				for(TableData table :_tablesData){
					Log.e("SyncTransaction","rolling back: "+ table.getQueryName()+"  sync version From "+
							Config.getLastSyncVersion(_context, table.getQueryName())+
							" to version: -1");
					Config.setLastSyncVersion(_context, table.getQueryName(), -1);
				}
				_tablesData.clear();
				_tableValidator=null;
			}
		}
	}

	private void handleUpdates(JsonParser p) throws JsonParseException, IOException
	{
		Uri tableUri = ICDBProvider.getTableUri(_table);
		ContentValues[] valuesList = parseRows(p, _typeMap);
		for (ContentValues values : valuesList)
		{
			if (_aborted == false)
			{
				doUpdateRow(tableUri, values);
			}
		}
	}
	
	private void doUpdateRow(Uri tableUri, ContentValues values)
	{		
		Uri uri = Uri.withAppendedPath(tableUri, values.getAsString(BaseTable.ID));
		_resolver.update(uri, values, null, null);
		_syncResultsPosition++;
		notifyProgress(_syncResultsPosition, _syncResultsCount);
	}

	private void handleDeletes(JsonParser p) throws JsonParseException, IOException
	{
		Uri tableUri = ICDBProvider.getTableUri(_table);
		ContentValues[] valuesList = parseRows(p, _typeMap);
		for (ContentValues values : valuesList)
		{
			Uri uri = Uri.withAppendedPath(tableUri, values.getAsString(BaseTable.ID));
			try
			{
				if (_aborted == false)
				{
					_resolver.delete(uri, null, null);
					_syncResultsPosition++;
					notifyProgress(_syncResultsPosition, _syncResultsCount);
				}
			}
			catch (Exception e)
			{
				// ignore delete errors
			}
		}
	}
	
	private ContentValues[] parseRows(JsonParser p, Hashtable<String, String> typeMap) throws JsonParseException, IOException
	{
		ContentValues[] result = null;
		ArrayList<ContentValues> list = new ArrayList<ContentValues>();
		if (p.getCurrentToken() == JsonToken.START_ARRAY)
		{
			while (p.nextToken() != JsonToken.END_ARRAY)
			{
				list.add(parseRow(p, typeMap));
			}
		}
		result = new ContentValues[list.size()];
		return list.toArray(result);
	}
	
	private ContentValues parseRow(JsonParser p, Hashtable<String, String> typeMap) throws JsonParseException, IOException
	{
		ContentValues result = new ContentValues();
		if (p.getCurrentToken() == JsonToken.START_ARRAY)
		{
			while (p.nextToken() != JsonToken.END_ARRAY)
			{
				if (p.getCurrentToken() == JsonToken.START_OBJECT)
				{
					String columnName = null;
					String columnType = null;
					while (p.nextToken() != JsonToken.END_OBJECT)
					{
						if (p.getCurrentToken() == JsonToken.FIELD_NAME)
						{
							String key = p.getText();
							p.nextToken();
							if (key.equals("Key"))
							{
								columnName = p.getText();
								columnType = typeMap.get(columnName);
							}
							else if (key.equals("Value"))
							{
								if (p.getCurrentToken() != JsonToken.VALUE_NULL)
								{
									if (columnType != null)
									{
										if (columnType.equals(ICDBSchema.TYPE_GUID))
										{
											Guid guid = new Guid(p.getText());
											result.put(columnName, guid.toString());
										}
										else if (columnType.equals(ICDBSchema.TYPE_TEXT))
										{
											result.put(columnName, p.getText());
										}
										else if (columnType.equals(ICDBSchema.TYPE_DATE))
										{
											result.put(columnName, p.getLongValue());
										}
										else if (columnType.equals(ICDBSchema.TYPE_DATE_TIME_OFFSET))
										{
											result.put(columnName,  p.getText());
										}
										else if (columnType.equals(ICDBSchema.TYPE_BLOB))
										{
											byte[] bytes = Base64.decode(p.getText());
											result.put(columnName, bytes);
										}
										else if (columnType.equals(ICDBSchema.TYPE_BOOL))
										{
											result.put(columnName, p.getIntValue());
										}
										else if (columnType.equals(ICDBSchema.TYPE_INT32))
										{
											result.put(columnName, p.getIntValue());
										}
										else if (columnType.equals(ICDBSchema.TYPE_INT64))
										{
											result.put(columnName, p.getLongValue());
										}
										else if (columnType.equals(ICDBSchema.TYPE_REAL))
										{
											result.put(columnName, p.getDoubleValue());
										}
										else
										{
											throw new UnsupportedOperationException("parse column type " + columnType);
										}
									}
									else
									{
										throw new UnsupportedOperationException("column not found in type map " + columnName);
									}
								}
								else
								{
									result.put(columnName, (String)null);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	private void notifyActionChanged()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onActionChanged(_action);
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}

	private void notifyStatusChanged(String status)
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onStatusChanged(status);
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}

	private void notifyServerFetch()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onServerFetch();
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}

	private void notifyServerFetchDone()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onServerFetchDone();
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}
	
	private void notifyDatasetChanged()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onDatasetChanged(_dataset);
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}

	private void notifyTableChanged()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onTableChanged(_table);
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}
	
	private void notifyProgress(int position, int count)
	{
	    final int N = mRemoteListeners.beginBroadcast();
	    for (int i = 0; i < N; i++)
	    {
	        ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
	        try
	        {
	            listener.onProgress(position, count);
	        }
	        catch (RemoteException e)
	        {
	            // The RemoteCallbackList will take care of removing the
	            // dead listeners.
	        }
	    }
	    mRemoteListeners.finishBroadcast();
	}
	
	private void notifyError()
	{
        final int N = mRemoteListeners.beginBroadcast();
        for (int i = 0; i < N; i++)
        {
            ISyncTransactionListener listener = mRemoteListeners.getBroadcastItem(i);
            try
            {
                listener.onError(_error);
            }
            catch (RemoteException e)
            {
                // The RemoteCallbackList will take care of removing the
                // dead listeners.
            }
        }
        mRemoteListeners.finishBroadcast();
	}
}
