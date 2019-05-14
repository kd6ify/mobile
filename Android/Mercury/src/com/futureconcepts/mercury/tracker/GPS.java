package com.futureconcepts.mercury.tracker;

import android.content.Context;
import android.location.LocationManager;

public class GPS{
	
	
	public static boolean isGPSEnabled(Context context)
	{
		final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
		  if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
		  {
			  return true;
		  }
		  return false;
	}
}
