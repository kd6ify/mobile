package com.futureconcepts.ax.sync;

import com.futureconcepts.gqueue.GQueue;
import com.futureconcepts.gqueue.QueueListenerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class SqlReplicationErrorQueueService extends QueueListenerService
{
	private static final String TAG = "SqlReplicationErrorQueueService";

	@Override
	protected void onBegin(Uri queueUri)
	{
	}

	@Override
	protected boolean onReceive(GQueue queue)
	{
		String content = new String(queue.getContent());
		if (content != null)
		{
			Log.d(TAG, "ignoring error queue content: " + content);
		}
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
			Intent startIntent = new Intent(context, SqlReplicationErrorQueueService.class);
			context.startService(startIntent);
	    }
	}
}
