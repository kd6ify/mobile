package com.futureconcepts.ax.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.futureconcepts.ax.sync.client.ISyncListener;
import com.futureconcepts.ax.sync.client.ISyncManager;
import com.futureconcepts.ax.sync.client.ISyncTransaction;
import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.MercurySettings;
import com.futureconcepts.gqueue.OnReceiveFatalException;
import com.futureconcepts.gqueue.OnRetryException;

public class SyncManager extends ISyncManager.Stub
{
	private static final String TAG = SyncManager.class.getSimpleName();
	private SyncTransaction _currentTransaction;
	public static final int STATE_START = 1;
	public static final int STATE_RESCHEDULE = 2;
	public static final int STATE_STOP = 3;
		
    private final RemoteCallbackList<ISyncListener> mRemoteListeners = new RemoteCallbackList<ISyncListener>();
	
    private Context _context;
    
    private int _state;
    
    public SyncManager(Context context)
    {
    	_context = context;
    }
    
	@Override
	public void registerSyncListener(ISyncListener listener) throws RemoteException
	{
    	if (listener != null)
    	{
            mRemoteListeners.register(listener);
        }
	}

	@Override
	public void unregisterSyncListener(ISyncListener listener) throws RemoteException
	{
        if (listener != null)
        {
            mRemoteListeners.unregister(listener);
        }
	}

	@Override
	public ISyncTransaction getCurrentTransaction() throws RemoteException
	{
		return SyncTransaction.asInterface(_currentTransaction);
	}
	
	public void submitTransaction(String action, String dataset) throws OnRetryException, OnReceiveFatalException
	{
		if (dataset == null)
		{
			Log.d(TAG, "");
		}
		_currentTransaction = new SyncTransaction(_context, action, dataset);
		notifyTransactionCreate();
        _currentTransaction.submit();
        notifyTransactionDestroy();
        _currentTransaction = null;
	}

	public void setState(int value)
	{
		if (value != _state)
		{
			_state = value;
			notifyListeners();
		}
	}
	
	private void notifyListeners()
	{
	    final int N = mRemoteListeners.beginBroadcast();
	    for (int i = 0; i < N; i++)
	    {
	        notifyListener(mRemoteListeners.getBroadcastItem(i));
	    }
	    mRemoteListeners.finishBroadcast();
	}
		
	private void notifyListener(ISyncListener listener)
	{
    	try
    	{
	        if (_state == STATE_START)
	        {
	        	listener.onStart();
	        }
	        else if (_state == STATE_RESCHEDULE)
	        {
	        	listener.onRescheduled();
	        }
	        else if (_state == STATE_STOP)
	        {
	        	listener.onStop();
	        }
    	}
    	catch (Exception e) {}
	}
	
	private void notifyTransactionCreate()
	{
	    final int N = mRemoteListeners.beginBroadcast();
	    for (int i = 0; i < N; i++)
	    {
	        ISyncListener listener = mRemoteListeners.getBroadcastItem(i);
	        try
	        {
	            listener.onTransaction(SyncTransaction.asInterface(_currentTransaction));
	        }
	        catch (RemoteException e)
	        {
	            // The RemoteCallbackList will take care of removing the
	            // dead listeners.
	        }
	    }
	    mRemoteListeners.finishBroadcast();
	}

	private void notifyTransactionDestroy()
	{
	    final int N = mRemoteListeners.beginBroadcast();
	    for (int i = 0; i < N; i++)
	    {
	        ISyncListener listener = mRemoteListeners.getBroadcastItem(i);
	        try
	        {
	            listener.onTransactionComplete(SyncTransaction.asInterface(_currentTransaction));
	        }
	        catch (RemoteException e)
	        {
	            // The RemoteCallbackList will take care of removing the
	            // dead listeners.
	        }
	    }
	    mRemoteListeners.finishBroadcast();
	}
	
	@Override
	public void setCurrentIncidentID(String id) throws RemoteException
	{
		Log.d(TAG, "setCurrentIncidentID: " + id);
		if (_currentTransaction != null)
		{
			_currentTransaction.abort();
		}
		//GQueue.clearQueue(_context, SyncService.class);
		MercurySettings.setCurrentIncidentId(_context, id);
	}
	
	@Override
	public String getCurrentIncidentID()
	{
		return MercurySettings.getCurrentIncidentId(_context);
	}

	@Override
	public void startSyncing() throws RemoteException
	{
		Intent intent = new Intent(_context, SyncService.class);
		_context.startService(intent);
	}

	@Override
	public void syncDataset(String datasetName) throws RemoteException
	{
		Log.d(TAG, "syncDataset: " + datasetName);
		ContentValues values = new ContentValues();
		values.put(GQueue.ACTION, Intent.ACTION_VIEW);
		values.put(GQueue.PARAM1, "sync");
		values.put(GQueue.NOTIFICATION_MESSAGE, datasetName);
		values.put(GQueue.LOCAL_CONTENT_URL, datasetName);
		GQueue.insertMessage(_context, SyncService.class, values);
	}

	@Override
	public void deleteDataset(String datasetName) throws RemoteException
	{
		Log.d(TAG, "deleteDataset: " + datasetName);
		
		// see Mantis 8969 - scenario is this:
		// 1) incident is synced
		// 2) UserListActivity (Personnel) is selected (queues sync UserViewDataset)
		// 3) user launches IncidentSelectorActivity and selects a new Incident
		//	(queues: delete IncidentDataset, sync IncidentDataset behind the previous sync UserViewDataset)
		// 4) sync progress window does not show in the launcher
		// When a delete IncidentDataset is queued, the SyncManager should:
		//	a) clear the sync queue
		//	b) stop any currently running sync transactions	
		//GQueue.clearQueue(_context, SyncService.class);
		//if (_currentTransaction != null)
		//{
		//	_currentTransaction.abort();
	//	}
		ContentValues values = new ContentValues();
		values.put(GQueue.ACTION, Intent.ACTION_VIEW);
		values.put(GQueue.PARAM1, "delete");
		values.put(GQueue.NOTIFICATION_MESSAGE, datasetName);
		values.put(GQueue.LOCAL_CONTENT_URL, datasetName);
		GQueue.insertMessage(_context, SyncService.class, values);
	}

	@Override
	public void dropDataset(String datasetName) throws RemoteException
	{
		ContentValues values = new ContentValues();
		values.put(GQueue.ACTION, Intent.ACTION_VIEW);
		values.put(GQueue.PARAM1, "drop");
		values.put(GQueue.NOTIFICATION_MESSAGE, datasetName);
		values.put(GQueue.LOCAL_CONTENT_URL, datasetName);
		GQueue.insertMessage(_context, SyncService.class, values);
	}

	@Override
	public void uploadInsert(Uri uri) throws RemoteException
	{
    	try
    	{
			Uri uploadQueue = GQueue.getServiceQueueUri(_context, SqlUploadQueueService.class);
			ContentValues qv = new ContentValues();
			qv.put(GQueue.LOCAL_CONTENT_URL, uri.toString());
			qv.put(GQueue.ACTION, Intent.ACTION_INSERT);
			GQueue.insertMessage(_context, uploadQueue, qv);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace(); // TODO - handle error
    	}
	}
}
