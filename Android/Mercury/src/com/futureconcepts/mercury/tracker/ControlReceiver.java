package com.futureconcepts.mercury.tracker;

import com.futureconcepts.mercury.Config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ControlReceiver extends BroadcastReceiver
{
	private static final String TAG = "tracker.ControlReceiver";
	
	public static final String ACTION_TRACKER_START = "com.futureconcepts.action.tracker.START";
	public static final String ACTION_TRACKER_STOP = "com.futureconcepts.action.tracker.STOP";
	public static final String ACTION_TRACKER_SET_MODE = "com.futureconcepts.action.tracker.SET_MODE";
	
	@Override
    public void onReceive(Context context, Intent intent)
    {
		Log.d(TAG, "onReceive " + intent.getAction());
		String action = intent.getAction();
		if (action != null)
		{
			if (action.equals(ACTION_TRACKER_START))
			{
				context.startService(new Intent(context, TrackerService.class));
			}
			else if (action.equals(ACTION_TRACKER_STOP))
			{
				context.stopService(new Intent(context, TrackerService.class));
			}
			else if (action.equals(ACTION_TRACKER_SET_MODE))
			{
				Bundle extras = intent.getExtras();
				if (extras != null)
				{
					String trackerMode = extras.getString("tracker_mode");
					if (trackerMode != null)
					{
						Config.getInstance(context).setTrackerMode(trackerMode);
					}
					else
					{
						Log.i(TAG, "tracker mode not specified");
					}
				}
				else
				{
					Log.i(TAG, "extra not found in intent extras");
				}
			}
			else
			{
				Log.i(TAG, "unknown intent dispatched");
			}
		}
    }
}
