package com.futureconcepts.ax.trinity;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class GPS implements LocationListener{

	private final int GPS_REQUEST_UPDATES_TIME= 30000;// in miliseconds
	private final int GPS_REQUEST_UPDATES_METERS = 30;
	private double currentLat, currentLong;
	private LocationManager locationManager;
	private String provider;
	public static Location lastKnownLocation;
	private final String Tag = "LocationActivity";
	private Context activityCallerContext = null;
	private boolean showLocationUpdateToast = true;
	private List<GpsOnLocationChangeNotifier> listeners = new ArrayList<GpsOnLocationChangeNotifier>();
	
	public interface GpsOnLocationChangeNotifier
	{
		void gpsLocationChange(Location point);
	}
	public GPS(Context context, GpsOnLocationChangeNotifier listener) {
		// Get the location manager
		activityCallerContext = context;
		if(listener!=null){
			listeners.add(listener);
		}		
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		inizializateGPS();
	}
	
	public void removeListener(GpsOnLocationChangeNotifier listener)
	{
		if(listener!=null){
			listeners.remove(listener);
		}	
	}

	private void inizializateGPS()
	{
		getProvider();
		// Define the criteria how to select the locatioin provider -> use
		if (provider != null) {
			lastKnownLocation = locationManager.getLastKnownLocation(provider);
			// Initialize the location fields
			if (lastKnownLocation != null) {
				System.out.println("Provider " + provider+ " has been selected.");
				// onLocationChanged(lastKnownLocation);
				updateLocationValues(lastKnownLocation);
			} else {
				Log.d(Tag, "The location is  null on create");
			}
			requestLocationUpdates();
		}
	}
	  
	  public void getProvider()
	  {
		  provider = LocationManager.GPS_PROVIDER;
		    Log.d(Tag,"provider name: "+provider); 	
	  }
	  
	public static boolean isGPSEnabled(Context context)
	{
		final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
		  if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
		  {
			  return true;
		  }
		  return false;
	}
	
	public  Location getCurrentLocation()
	{
		return lastKnownLocation;
	}
	public String getCurrentLatitude()
	{
		if(currentLat == 0.0)
		{
			return null;
		}
		return ""+currentLat;
	}
	
	public String getCurrentLongitude()
	{
		if(currentLong == 0.0)
		{
			return null;
		}
		return ""+currentLong;
	}
	
	/* Remove the locationlistener updates */
	public void removeLocationUpdates()
	{
		if(locationManager!=null)
			locationManager.removeUpdates(this);
	}
	public void requestLocationUpdates()
	{
		if(locationManager!=null)
			locationManager.requestLocationUpdates(provider, GPS_REQUEST_UPDATES_TIME, GPS_REQUEST_UPDATES_METERS, this);
	}
	
	public void updateLocationValues(Location location)
	{
		 currentLat = (double) (location.getLatitude());
		 currentLong = (double) (location.getLongitude());	
	}
	
	public void showLatLong ()
	{
		 Toast.makeText(activityCallerContext, "You are using " + provider+" Location service.   "+"Location update occur now is: Lat: "+currentLat+" long: "+currentLong, Toast.LENGTH_SHORT).show();
	}
	

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(lastKnownLocation!=null){
			if(location.getLatitude() != lastKnownLocation.getLatitude() || location.getLongitude() != lastKnownLocation.getLongitude())
			{
				currentLat = (double) (location.getLatitude());
				currentLong = (double) (location.getLongitude());	
				Log.d(Tag, "Location is: Lat: "+currentLat+" long: "+currentLong);
				lastKnownLocation = location;
			}
		}else
		{
			lastKnownLocation = location;
		}
		if(showLocationUpdateToast){
			showLocationUpdateToast = false;
			Toast.makeText(activityCallerContext, "Gps: Location Acquired", Toast.LENGTH_SHORT).show();
		}
		for(GpsOnLocationChangeNotifier listener:listeners){
			listener.gpsLocationChange(lastKnownLocation);
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		inizializateGPS();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle arg2) {
		// TODO Auto-generated method stub
	}
}
