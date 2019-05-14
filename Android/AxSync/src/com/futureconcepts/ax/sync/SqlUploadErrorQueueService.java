package com.futureconcepts.ax.sync;

import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.QueueListenerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SqlUploadErrorQueueService extends QueueListenerService
{
//	private static final String TAG = SqlUploadErrorQueueService.class.getSimpleName();
	
	private StatusBarNotifier _notifier;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		_notifier = new StatusBarNotifier(this);
	}
	
	@Override
	protected void onBegin(Uri queueUri) {
		// TODO Auto-generated method stub
	}

	@Override
	protected boolean onReceive(GQueue queue)
	{
		_notifier.notifyUploadError(Uri.withAppendedPath(_myQueueUri, Integer.toString(queue.get_ID())), queue.getNotificationMessage());
		return false;
	}

	@Override
	protected void onFinish()
	{
	}	
    
	public static final class RestartReceiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			Intent startIntent = new Intent(context, SqlUploadErrorQueueService.class);
			context.startService(startIntent);
	    }
	}
}
