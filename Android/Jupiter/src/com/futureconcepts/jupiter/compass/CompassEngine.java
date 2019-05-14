package com.futureconcepts.jupiter.compass;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.futureconcepts.jupiter.Config;

public class CompassEngine implements SensorEventListener, LocationListener, Closeable, GpsStatus.Listener
{
	private static final String TAG = "CompassEngine";
	
	private Display defaultDisplay;
	
	private SensorManager manager = null;
	private LocationManager locManager = null;
	
	private boolean useTrueNorth = true;
	private GeomagneticField trueNorth = null;
	private Location locationCurrent = null;
	private Location locationWaypoint = null;
	
	private Sensor magnetic;
	private Sensor accelerometer;
	private float[] lastAccelerometer;
	private float[] lastMagnetic;
	
	private static final float filteringFactor = 0.10f;
    private volatile float[] lastOrientation;
    private volatile float[] orientation;
    
	private volatile float[] startR;
	private volatile float[] I;
	private volatile float[] R;
		
	private List<IGpsSatelliteView> viewsGps = new ArrayList<IGpsSatelliteView>();
	private List<ICompassView> viewsCompass = new ArrayList<ICompassView>();
	private List<ICurrentLocationView> viewsCurLoc = new ArrayList<ICurrentLocationView>();
	private List<IWaypointView> viewsWaypoint = new ArrayList<IWaypointView>();
	
	private Config _config;
	private OnSharedPreferenceChangeListener _sharedPreferenceChangeListener;
	
	public CompassEngine(Activity parent)
	{
		Context context = parent.getApplicationContext();
		
		startR = new float[9];
		I = new float[9];
		R = new float[9];
		
		orientation = new float[3];
		lastOrientation = new float[3];
		
		defaultDisplay = parent.getWindowManager().getDefaultDisplay();
		
        //setup compass stuff
        manager = (SensorManager) parent.getSystemService(Context.SENSOR_SERVICE);
        
        magnetic = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        //setup GPS stuff
        locManager = (LocationManager) parent.getSystemService(Context.LOCATION_SERVICE);
        onLocationChanged(locManager.getLastKnownLocation("gps"));
        
        _config = Config.getInstance(context);
        _sharedPreferenceChangeListener = new OnSharedPreferenceChangeListener()
        {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
			{
				if (key.equals(Config.TRUE_NORTH))
				{
					useTrueNorth = sharedPreferences.getBoolean(Config.TRUE_NORTH, true);
					//initTrueNorth();
				}
			}
        };
        _config.getSharedPreferences().registerOnSharedPreferenceChangeListener(_sharedPreferenceChangeListener);
        
        initTrueNorth();
        
        this.useTrueNorth = _config.getSharedPreferences().getBoolean(Config.TRUE_NORTH, true);
	}
	
	private void initTrueNorth()
	{
		Location last = locManager.getLastKnownLocation("gps");
		if(last == null)
		{
			this.trueNorth = null;
			//TODO warning true north not available
		}
		else
		{
			updateTrueNorth(last);
		}
	}
	
	private void updateTrueNorth(Location loc)
	{
		this.trueNorth = new GeomagneticField((float)loc.getLatitude(),
											  (float)loc.getLongitude(),
											  (float)loc.getAltitude(),
											  System.currentTimeMillis());
	}
	
	public void pause()
	{
		detachListeners();
	}
	
	public void resume()
	{
    	manager.registerListener(this, magnetic, SensorManager.SENSOR_DELAY_GAME);
    	manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    	
    	onLocationChanged(locManager.getLastKnownLocation("gps"));

    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0, this);
    	locManager.addGpsStatusListener(this);
    	    	
    	initTrueNorth();
	}
	
	private void detachListeners()
	{
		manager.unregisterListener(this);
    	locManager.removeUpdates(this);
    	locManager.removeGpsStatusListener(this);
	}
	
	@Override
	public void close()
	{
		detachListeners();
		
    	closeViews();
	}

	private void closeViews()
	{
		for(IGpsSatelliteView i : viewsGps)
		{
			closeIfCloseable(i);
		}
		viewsGps.clear();
		
		for(ICompassView i : viewsCompass)
		{
			closeIfCloseable(i);
		}
		viewsCompass.clear();
		
		for(ICurrentLocationView i : viewsCurLoc)
		{
			closeIfCloseable(i);
		}
		viewsCurLoc.clear();
		
		for(IWaypointView i : viewsWaypoint)
		{
			closeIfCloseable(i);
		}
		viewsWaypoint.clear();
	}
	
	private void closeIfCloseable(Object obj)
	{
		if(obj instanceof Closeable)
		{
			try
			{
				((Closeable)obj).close();
			}
			catch(Exception e){}
		}
	}
	
	public void addView(Object v)
	{
		if(v instanceof IGpsSatelliteView)
		{
			viewsGps.add((IGpsSatelliteView)v);
		}
		
		if(v instanceof ICompassView)
		{
			viewsCompass.add((ICompassView)v);
		}
		
		if(v instanceof ICurrentLocationView)
		{
			viewsCurLoc.add((ICurrentLocationView)v);
		}
		
		if(v instanceof IWaypointView)
		{
			viewsWaypoint.add((IWaypointView)v);
		}
	}
	
	public void removeView(Object v)
	{
		viewsGps.remove(v);
		viewsCompass.remove(v);
		viewsCurLoc.remove(v);
		viewsWaypoint.remove(v);
	}
	

	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	
	}

	
	@Override
	public void onSensorChanged(SensorEvent e)
	{
		if(viewsCompass.isEmpty()) return;
		
		if(e.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return;
    	
    	if(e.sensor == accelerometer)
    	{
    		lastAccelerometer = e.values.clone();
    		return;
    	}
    	else if(e.sensor == magnetic)
    	{
    		lastMagnetic = e.values.clone();
    	}
    	else
    	{
    		return;
    	}
    	
    	if((lastAccelerometer == null) || (lastMagnetic == null))
    	{
    		return;
    	}

    	if(!SensorManager.getRotationMatrix(startR, I, lastAccelerometer, lastMagnetic))
    	{
    		return;
    	}
   	
    	int orient = defaultDisplay.getOrientation();
    	int xRemap = SensorManager.AXIS_X;
    	int yRemap = SensorManager.AXIS_Y;
    	switch(orient)
    	{
    		case Surface.ROTATION_0:
    			R = startR;
    			break;
    		case Surface.ROTATION_90:
    			xRemap = SensorManager.AXIS_Y;
    			yRemap = SensorManager.AXIS_MINUS_X;
    			break;
    		case Surface.ROTATION_180:
    			xRemap = SensorManager.AXIS_MINUS_X;
    			yRemap = SensorManager.AXIS_MINUS_Y;
    			break;
    		case Surface.ROTATION_270:
    			xRemap = SensorManager.AXIS_MINUS_Y;
    			yRemap = SensorManager.AXIS_MINUS_X;
    			break;
    		default:
    			return;
    	}
    	
    	if((xRemap != SensorManager.AXIS_X) || (yRemap != SensorManager.AXIS_Y))
    	{
	    	if(!SensorManager.remapCoordinateSystem(startR, xRemap, yRemap, R))
			{
				return;
			}
		}
    	
    	SensorManager.getOrientation(R, orientation);
    	
    	filterValues(orientation);
    	lastOrientation = orientation;
 
    	float heading = (float)Math.toDegrees(orientation[0]);
    	
    	//convert MAGNETIC to TRUE
    	if((this.useTrueNorth) && (this.trueNorth != null))
    	{
    		heading += this.trueNorth.getDeclination();
    	}
    	
		for(ICompassView i : viewsCompass)
		{
	    	i.setHeading(heading);
	    	i.setOrientation(orientation);
		}
	}
	
	private void filterValues(float[] values)
	{
		for(int i = 0; i < 3; i++)
		{
			values[i] = values[i] * filteringFactor + (lastOrientation[i] * (1.0f - filteringFactor));
		}
	}
	
	@Override
	public void onLocationChanged(Location location)
	{
		locationCurrent = location;
		for(ICurrentLocationView i : viewsCurLoc)
		{
			i.setCurrentLocation(location);
		}
		updateTrueNorth(location);
		updateWaypoint();
	}

	public void setWaypoint(Location location)
	{
		locationWaypoint = location;
		updateWaypoint();
	}
	
	private void updateWaypoint()
	{
		if((locationWaypoint == null) || (locationCurrent == null))
		{
			for(IWaypointView i : viewsWaypoint)
			{
				i.setShowWaypoint(false);
			}
		}
		else
		{
			float bearing = locationCurrent.bearingTo(locationWaypoint);
			
			//convert TRUE to MAGNETIC
		   	if((!this.useTrueNorth) && (this.trueNorth != null))
	    	{
		   		bearing -= this.trueNorth.getDeclination();
	    	}
			
			float distance = locationCurrent.distanceTo(locationWaypoint);
			Date eta = null;
			if(locationCurrent.hasSpeed())
			{
				if(locationCurrent.getSpeed() > 0)
				{
					long curTime = System.currentTimeMillis();
					long travelTime = (long)(distance / locationCurrent.getSpeed()) * 1000;
					eta = new Date(curTime + travelTime);
				}
			}
			
			for(IWaypointView i : viewsWaypoint)
			{
				i.setWaypointBearing(bearing);
				i.setWaypointDistance(distance);
				i.setWaypointETA(eta);
				i.setShowWaypoint(true);
			}
		}
	}
	
	@Override
	public void onProviderDisabled(String provider)
	{
		
	}

	@Override
	public void onProviderEnabled(String provider)
	{
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		
	}

	@Override
	public void onGpsStatusChanged(int event)
	{
		Log.d(TAG, "GPS Status Changed");
		
		if(viewsGps.isEmpty()) return;

		switch (event)
		{
			case GpsStatus.GPS_EVENT_FIRST_FIX:
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			case GpsStatus.GPS_EVENT_STARTED:
				GpsStatus status = locManager.getGpsStatus(null);
				List<GpsSatStatsSnapshot> data = new ArrayList<GpsSatStatsSnapshot>();
				
				for(GpsSatellite s : status.getSatellites())
				{
					data.add(new GpsSatStatsSnapshot(s));
				}

				for(IGpsSatelliteView i : viewsGps)
				{
					i.setSatelliteLocations(data);
					i.setShowSatelliteLocations(true);
				}
				break;
			case GpsStatus.GPS_EVENT_STOPPED:			// Event sent when the GPS system has stopped.
				for(IGpsSatelliteView i : viewsGps)
				{
					i.setShowSatelliteLocations(false);
				}
				return;
		}
	}
}
