package com.futureconcepts.mercury.tracker;

import org.apache.http.HttpEntity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.futureconcepts.mercury.Intents;
import com.futureconcepts.mercury.gqueue.HttpQueueService;
import com.futureconcepts.mercury.gqueue.OnReceiveFatalException;

public class TrackerQueueService extends HttpQueueService
{
	@Override
	protected void onReceiveResponse(HttpEntity ent) throws OnReceiveFatalException
	{
	}

	@Override
	protected void onBegin(Uri queueUri)
	{
	}

	@Override
	protected void onProgress(String message, int position, int max) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onFinish()
	{
	}
	
	public static final class Receiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			String action = intent.getAction();
			if (action != null)
			{
				if ( action.equals(Intent.ACTION_BOOT_COMPLETED) ||
						action.equals(Intents.ACTION_LOGIN_COMPLETED) ||
						action.equals(Intents.ACTION_START_SERVICES) )
				{
					Intent startIntent = new Intent(context, TrackerQueueService.class);
					context.startService(startIntent);
				}
			}
	    }
	}
}
