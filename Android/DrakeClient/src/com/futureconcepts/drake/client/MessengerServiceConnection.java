package com.futureconcepts.drake.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.futureconcepts.drake.client.constants.ImServiceConstants;

public class MessengerServiceConnection
{
	private static final String LOG_TAG = MessengerServiceConnection.class.getSimpleName();

	private Context _context;
	private Client _client;
	private ServiceConnection _serviceConnection;
	private IRemoteImService _service = null;
	
	public MessengerServiceConnection(Context context, MessengerServiceConnection.Client client)
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
	            public void onServiceConnected(ComponentName className, IBinder service)
	            {
	                _service = IRemoteImService.Stub.asInterface(service);
                    Log.d(LOG_TAG, "service connected");
                    _client.onMessengerServiceConnected();
	            }
	            public void onServiceDisconnected(ComponentName className)
	            {
	                _service = null;
                    Log.d(LOG_TAG, "service disconnected");
                    _client.onMessengerServiceDisconnected();
	            }
	        };
	        Intent serviceIntent = new Intent();
	        serviceIntent.setComponent(ImServiceConstants.IM_SERVICE_COMPONENT);
	        if (_context.bindService(serviceIntent, _serviceConnection, Context.BIND_AUTO_CREATE) == false)
	        {
	        	_client.onMessengerServiceDisconnected();
	        }
    	}
    }

    public void disconnect()
    {
    	if (_service != null)
    	{
    		_context.unbindService(_serviceConnection);
            Log.d(LOG_TAG, "unbind");
    	}
    }
    
    public boolean isConnected()
    {
    	return _service != null;
    }
    
    public IRemoteImService getServiceInterface()
    {
    	return _service;
    }
    
    public IImConnection getConnection() throws RemoteException
    {
    	IImConnection result = null;
    	if (_service != null)
    	{
			result = _service.getConnection();
    	}
    	return result;
    }
    
    public interface Client
    {
    	void onMessengerServiceConnected();
    	void onMessengerServiceDisconnected();
    }
}
