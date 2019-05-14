package com.futureconcepts.ax.sync;

import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.OnReceiveFatalException;
import com.futureconcepts.gqueue.OnRetryException;
import com.futureconcepts.gqueue.QueueListenerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

public class SyncService extends QueueListenerService
{
	private static final String TAG = SyncService.class.getSimpleName();
	
	private SyncManager _syncManager;
	
	private StatusBarNotifier _notifier;
	
    @Override
    public void onCreate()
    {
    	super.onCreate();
    	_syncManager = new SyncManager(this);
    	_notifier = new StatusBarNotifier(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return _syncManager;
    }

	@Override
	protected void onBegin(Uri queueUri)
	{
		_syncManager.setState(SyncManager.STATE_START);
		_notifier.notifySyncing();
	}

	@Override
	protected void onFinish()
	{
		_syncManager.setState(SyncManager.STATE_STOP);
		_notifier.cancelSyncing();
	}

	@Override
	protected boolean onReceive(GQueue queue) throws OnReceiveFatalException, OnRetryException
	{
		try
		{
			String action = queue.getParam1();
			String dataset = queue.getLocalContentUrl();
			_syncManager.submitTransaction(action, dataset);
		}
		catch (OnRetryException e)
		{
			_syncManager.setState(SyncManager.STATE_RESCHEDULE);
			_notifier.notifyScheduledRetry(e);
			throw e;
		}
		return true;
	}

	public static final class RestartReceiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			Intent startIntent = new Intent(context, SyncService.class);
			context.startService(startIntent);
	    }
	}
}
