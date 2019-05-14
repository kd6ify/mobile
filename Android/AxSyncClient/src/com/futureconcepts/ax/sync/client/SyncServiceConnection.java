package com.futureconcepts.ax.sync.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class SyncServiceConnection
{
	private static final String LOG_TAG = SyncServiceConnection.class.getSimpleName();

	private Context _context;
	private Client _client;
	private ServiceConnection _serviceConnection;
	private ISyncManager _service = null;
	private ConnectivityManager _connectivityManager;
	
	public SyncServiceConnection(Context context, SyncServiceConnection.Client client)
	{
		_context = context;
		_client = client;
	}
	
    public void connect()
    {
    	if (_service == null)
    	{
        	Log.d(LOG_TAG, "connect");
	        _serviceConnection = new ServiceConnection()
	        {
	        	@Override
	            public void onServiceConnected(ComponentName className, IBinder service)
	            {
	                _service = ISyncManager.Stub.asInterface(service);
                    Log.d(LOG_TAG, "service connected");
                    _client.onSyncServiceConnected();
	            }
	        	@Override
	            public void onServiceDisconnected(ComponentName className)
	            {
	                _service = null;
                    Log.d(LOG_TAG, "service disconnected");
                    _client.onSyncServiceDisconnected();
	            }
	        };
	        Intent serviceIntent = start();
	        if (_context.bindService(serviceIntent, _serviceConnection, Context.BIND_AUTO_CREATE) == false)
	        {
	        	Log.d(LOG_TAG, "error: bindService returned false");
	        	_client.onSyncServiceDisconnected();
	        }
    	}
    	else
    	{
    		Log.d(LOG_TAG, "already connected");
    	}
    }

    public Intent start()
    {
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.futureconcepts.ax.sync","com.futureconcepts.ax.sync.SyncService"));
        _context.startService(serviceIntent);
        return serviceIntent;
    }
    
    public void disconnect()
    {
    	if (_service != null)
    	{
    		_context.unbindService(_serviceConnection);
    		_service = null;
    		_serviceConnection = null;
    		_client.onSyncServiceDisconnected();
            Log.d(LOG_TAG, "unbind");
    	}
    	else
    	{
    		Log.d(LOG_TAG, "not connected");
    	}
    }
    
    public void registerSyncListener(ISyncListener listener)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.registerSyncListener(listener);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    	else
    	{
    		Log.d(LOG_TAG, "can't register--SyncServiceConnection not connected");
    	}
    }

    public void unregisterSyncListener(ISyncListener listener)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.unregisterSyncListener(listener);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    	else
    	{
    		Log.d(LOG_TAG, "can't unregister--SyncServiceConnection not connected");
    	}
    }
    
    public boolean isConnected()
    {
    	return _service != null;
    }
    
    public ISyncTransaction getCurrentTransaction()
    {
    	ISyncTransaction result = null;
    	if (_service != null)
    	{
	    	try
			{
				result = _service.getCurrentTransaction();
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    	return result;
    }
    
    public void setCurrentIncidentID(String id)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.setCurrentIncidentID(id);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public void startSyncing()
    {
    	if (_service != null)
    	{
    		try
			{
				_service.startSyncing();
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else
    	{
    		Log.d(LOG_TAG, "startSyncing service not connected");
    	}
    }
    
    public void deleteDataset(String dataset)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.deleteDataset(dataset);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    }

    public void syncDataset(String dataset)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.syncDataset(dataset);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    }
    
    public void syncDatasetIfNetworkIsConnected(String dataset)
    {
    	if (_connectivityManager == null)
    	{
    		_connectivityManager = (ConnectivityManager)_context.getSystemService(Service.CONNECTIVITY_SERVICE);
    	}
		NetworkInfo networkInfo = _connectivityManager.getActiveNetworkInfo();
		if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED))
		{
			syncDataset(dataset);
		}
    }
    
    public void dropDataset(String dataset)
    {
    	if (_service != null)
    	{
    		try
			{
				_service.dropDataset(dataset);
			}
			catch (RemoteException e)
			{
				e.printStackTrace();
			}
    	}
    }
    
    public void uploadInsert(Uri uri)
    {
    	if (_service != null)
    	{
    		try
    		{
    			_service.uploadInsert(uri);
    		}
    		catch (RemoteException e)
    		{
    			e.printStackTrace();
    		}
    	}
    }
    
    public interface Client
    {
    	void onSyncServiceConnected();
    	void onSyncServiceDisconnected();
    }
}
