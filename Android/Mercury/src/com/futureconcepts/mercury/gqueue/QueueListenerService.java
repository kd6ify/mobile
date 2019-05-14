package com.futureconcepts.mercury.gqueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public abstract class QueueListenerService extends Service
{
	private static final String TAG = QueueListenerService.class.getSimpleName();

    protected Uri _myQueueUri;
    private Uri _errorQueueUri;
	
	private WorkerThread _thread;
	
	private Handler _handler = new Handler();
	
	private MyObserver _observer;
	
	public IBinder onBind(Intent intent)
	{
		return null;
	}
			
	@Override
	public void onCreate()
	{
		super.onCreate();
		try
		{
			ServiceInfo serviceInfo = getPackageManager().getServiceInfo(new ComponentName(this, getClass()), PackageManager.GET_META_DATA);
			Bundle metaData = serviceInfo.metaData;
			if (metaData != null)
			{
				String queueUri = metaData.getString("QueueUri");
				if (queueUri != null)
				{
					_myQueueUri = Uri.parse(queueUri);
					_observer = new MyObserver(_handler);
				}
				getContentResolver().registerContentObserver(_myQueueUri, false, _observer);
				String errorService = metaData.getString("ErrorService");
				if (errorService != null)
				{
					_errorQueueUri = GQueue.getServiceQueueUri(this, this.getPackageName(), errorService);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (_observer != null)
		{
			getContentResolver().unregisterContentObserver(_observer);
			_observer = null;
		}
		if (_thread != null)
		{
			_thread.interrupt();
			_thread = null;
		}
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		startBackgroundThreadIfNecessary();
	}
		
	protected abstract void onBegin(Uri queueUri);
	
	protected abstract boolean onReceive(GQueue queue) throws OnReceiveFatalException, OnRetryException;
	
	protected abstract void onProgress(String message, int position, int max);
	
	protected abstract void onFinish();
	
	private synchronized void startBackgroundThreadIfNecessary()
	{
		if (_thread == null)
		{
			_thread = new WorkerThread();
			_thread.start();
		}
	}

	private synchronized void clearBackgroundThread()
	{
		_thread = null;
	}

	private void scheduleRetry(int retryMillis)
	{
		Intent intent = new Intent();
		intent.setClass(this, getClass());
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
		AlarmManager alarmMgr = (AlarmManager)getSystemService(ALARM_SERVICE);
		long scheduledReconnectTime = System.currentTimeMillis() + retryMillis;
		alarmMgr.set(AlarmManager.RTC_WAKEUP, scheduledReconnectTime, pendingIntent);
		Log.d(TAG, "schedule retry");
	}
	
	private final class WorkerThread extends Thread
	{
		public WorkerThread()
		{
			setName(getClass().getSimpleName());
//			setDaemon(true);
//			setPriority(Thread.NORM_PRIORITY - 1);
		}
				
		@Override
		public void run()
	    {
			Log.d(TAG, "WorkerThread.run() - " + _myQueueUri);
			boolean deleteAndContinue = true;
			GQueue queue = null;
			onBegin(_myQueueUri);
			while ( deleteAndContinue && (queue = GQueue.query(QueueListenerService.this, _myQueueUri, null, null)) != null)
			{
				int count = queue.getCount();
				for (int i = 0; deleteAndContinue && (i < count); i++)
				{
					queue.moveToPosition(i);
					onProgress(queue.getNotificationMessage(), i+1, count);
					try
					{
						if (deleteAndContinue = onReceive(queue))
						{
							queue.delete();
						}
					}
					catch (OnRetryException e)
					{
						e.printStackTrace();
						scheduleRetry(e.getRetryMillis());
						deleteAndContinue = false;
						break;
					}
					catch (Exception e)
					{
						e.printStackTrace();
						if (_errorQueueUri != null)
						{
							queue.moveToQueue(_errorQueueUri, e.getMessage(), e.getClass().getName());
						}
						else
						{
							queue.delete();
						}
					}
				}
				queue.close();
				queue = null;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			onFinish();
			if (queue != null)
			{
				queue.close();
			}
			clearBackgroundThread();
	    }
	}
	
	private class MyObserver extends ContentObserver
	{
		public MyObserver(Handler handler)
		{
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange)
		{
			startBackgroundThreadIfNecessary();
		}
	}	
}
