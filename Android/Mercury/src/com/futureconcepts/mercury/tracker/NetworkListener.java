package com.futureconcepts.mercury.tracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class NetworkListener implements LocationListener
{
//	private static final String TAG = "trinity.tracker.NetworkListener";

	private TrackerService mContext;
	
	public NetworkListener(TrackerService context)
	{
		mContext = context;
	}
	
	public void destroy()
	{
		mContext = null;
	}

	public void onLocationChanged(Location location)
	{
		if (mContext.isGpsAvailable() == false)
		{
//			mContext.getClient().submit(location);
		}
	}

	public void onProviderDisabled(String provider)
	{
	}

	public void onProviderEnabled(String provider)
	{
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}
}
