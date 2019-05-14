package com.futureconcepts.ax.trinity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class PackageReceiver extends BroadcastReceiver
{
	private static final String TAG = "PackageReciever";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "onReceive: " + intent.toString());
		Uri uri = intent.getData();
		String part = uri.getEncodedSchemeSpecificPart();
		Log.d(TAG, "part=" + part);
		Log.d(TAG, "action=" + intent.getAction());
		if (part != null)
		{
			if (part.contains("com.futureconcepts.mercury"))
			{
				if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction()))
				{
					init(context, intent);
				//	transferSettingsToMercury();
					context.sendBroadcast(new Intent("com.futureconcepts.action.RESTART"));
				}
			}
		}
	}
	
	private void init(Context context, Intent intent)
	{
	}
}
