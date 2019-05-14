package com.futureconcepts.jupiter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.futureconcepts.jupiter.data.Placemark;
import com.futureconcepts.jupiter.data.Folder;
import com.futureconcepts.jupiter.data.Track;
import com.futureconcepts.jupiter.layers.StyleManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class TrackRecorderService extends CompatService
{
	private static final String TAG = "RouteRecorderService";
	public static final String ACTION_CANCEL = "com.futureconcepts.jupiter.action.routerecorder.cancel";

	private LocationManager _locationManager;
	
	private LocationListener _listener;
	private String _interval;
	private long _minTime;
	private float _minDistance;
	private Location _lastTrackLocation;
	
	private static TrackRecorderService _instance = null;
	
	private Folder _folder;
	private String _trackPlacemarkId;
	private Uri _trackPlacemarkUri;
	private Uri _routeUri;
	private int _nextWaypointSequence;
	private int _nextTrackSequence;
	
	public static TrackRecorderService getInstance()
	{
		return _instance;
	}
			
	@Override
	public void onCreate()
	{
		super.onCreate();
		_instance = this;
		_locationManager = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
		startMeAsForeground();
	}
	
	private void startMeAsForeground()
	{
		Notification n = new Notification();
		n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		n.icon = R.drawable.recorder_status_good;
		n.tickerText = "Recording route";
		n.when = System.currentTimeMillis();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, TrackRecorderToggleActivity.class), 0);
		n.setLatestEventInfo(this, "Route Recorder Status", n.tickerText, pendingIntent);
		startForegroundCompat(R.layout.recorder_toggle, n);
	}
		
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopForegroundCompat(R.layout.recorder_toggle);
		if (_routeUri != null)
		{
			createEndWaypoint();
			if (_listener != null)
			{
				Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null)
				{
					_listener.onLocationChanged(location);
				}
			}
			completeTrackPlacemark();
		}
		if (_listener != null)
		{
			_locationManager.removeUpdates(_listener);
			_listener = null;
		}
		if (_folder != null)
		{
			_folder.close();
			_folder = null;
		}
		_instance = null;
	}

	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		String action = intent.getAction();
		String recordMethod = intent.getStringExtra(SetupTrackActivity.EXTRA_RECORD_METHOD);
		_interval = intent.getStringExtra(SetupTrackActivity.EXTRA_INTERVAL);
		Log.d(TAG, "onStart");
		if (action == null)
		{
			_routeUri = intent.getData();
			if (_routeUri != null)
			{
				_folder = new Folder(getContentResolver().query(_routeUri, null, null, null, null));
				if (_folder != null && _folder.getCount() == 1)
				{
					_folder.moveToFirst();
					createBeginWaypoint();
					createRoutePlacemark();
					if (recordMethod.equals("Distance"))
					{
						_listener = new DistanceGpsListener();
					}
					else if (recordMethod.equals("Time"))
					{
						_listener = new TimeGpsListener();
					}
					else
					{
						_listener = new AutoGpsListener();
					}
					_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, _minTime, _minDistance, _listener);
				}
			}
			if (_listener == null)
			{
				cancel();
				stopSelf();
			}
		}
		else if (action.equals(ACTION_CANCEL))
		{
			cancel();
			stopSelf();
		}
	}
				
	public static void startIfNeccessary(Context context, Intent intent)
	{
		if (_instance == null)
		{
			if (intent == null)
			{
				intent = new Intent(context, TrackRecorderService.class);
			}
			else
			{
				intent.setClass(context, TrackRecorderService.class);
			}
			context.startService(intent);
		}
		else
		{
			Log.i(TAG, "start not neccessary - already running");
		}
	}
	
	public static void stopIfNeccessary(Context context)
	{
		if (_instance != null)
		{
			context.stopService(new Intent(context, TrackRecorderService.class));
		}
		else
		{
			Log.i(TAG, "stop not neccessary - already stopped");
		}
	}
	
	private void createBeginWaypoint()
	{
		Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null)
		{
			String id = UUID.randomUUID().toString().toLowerCase();
			ContentValues values = new ContentValues();
			values.put(Placemark.ID, id);
			values.put(Placemark.PARENT_ID, _folder.getId());
			values.put(Placemark.NAME, "Begin");
			values.put(Placemark.TIME, System.currentTimeMillis());
			values.put(Placemark.LATITUDE, location.getLatitude());
			values.put(Placemark.LONGITUDE, location.getLongitude());
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_START);
			getContentResolver().insert(Placemark.CONTENT_URI, values);
		}
	}

	private void createRoutePlacemark()
	{
		_trackPlacemarkId = UUID.randomUUID().toString().toLowerCase();
		ContentValues values = new ContentValues();
		values.put(Placemark.ID, _trackPlacemarkId);
		values.put(Placemark.PARENT_ID, _folder.getId());
		values.put(Placemark.NAME, "Route");
		values.put(Placemark.TIME, System.currentTimeMillis());
		values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_TRACKS);
		_trackPlacemarkUri = getContentResolver().insert(Placemark.CONTENT_URI, values);
	}
	
	private void completeTrackPlacemark()
	{
		ContentValues values = new ContentValues();
		values.put(Placemark.TIME, System.currentTimeMillis() + 1);
		getContentResolver().update(_trackPlacemarkUri, values, null, null);
	}
	
	public static Uri markWaypointOrPlacemark(Context context, Location location)
	{
		Uri result = null;
		if (location == null)
		{
			location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(0);
			location.setLongitude(0);
		}
		if (location != null)
		{
			if (_instance != null)
			{
				result = _instance.markWaypoint(location);
			}
			else
			{
				result = markPlacemark(context, location);
			}
		}
		return result;
	}
	
	public static Uri markPlacemark(Context context, Location location)
	{
		Uri uri = null;
		long now = System.currentTimeMillis();
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		Folder placemarks = Folder.getTripFolder(context, Folder.NAME_PLACEMARKS);
		if (placemarks != null)
		{
			ContentValues values = new ContentValues();
			String id = UUID.randomUUID().toString().toLowerCase();
			values.put(Placemark.ID, id);
			values.put(Placemark.PARENT_ID, placemarks.getId());
			values.put(Placemark.NAME, "Placemark " + dateFormat.format(new Date(now)));
			values.put(Placemark.TIME, now);
			values.put(Placemark.LATITUDE, location.getLatitude());
			values.put(Placemark.LONGITUDE, location.getLongitude());
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_WAYPOINT);
			uri = context.getContentResolver().insert(Placemark.CONTENT_URI, values);
		}
		return uri;
	}
		
	private Uri markWaypoint(Location location)
	{
		Uri result = null;
		int sequence = _nextWaypointSequence++;
		String id = UUID.randomUUID().toString().toLowerCase();
		ContentValues values = new ContentValues();
		values.put(Placemark.ID, id);
		values.put(Placemark.PARENT_ID, _folder.getId());
		values.put(Placemark.NAME, "Waypoint " + sequence);
		values.put(Placemark.TIME, location.getTime());
		values.put(Placemark.LATITUDE, (float)location.getLatitude());
		values.put(Placemark.LONGITUDE, (float)location.getLongitude());
		values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_WAYPOINT);
		result = getContentResolver().insert(Placemark.CONTENT_URI, values);	
		updateRoute();
		return result;
	}
	
	private void createEndWaypoint()
	{
		Location location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null)
		{
			String id = UUID.randomUUID().toString().toLowerCase();
			ContentValues values = new ContentValues();
			values.put(Placemark.ID, id);
			values.put(Placemark.PARENT_ID, _folder.getId());
			values.put(Placemark.NAME, "End");
			values.put(Placemark.TIME, System.currentTimeMillis());
			values.put(Placemark.LATITUDE, location.getLatitude());
			values.put(Placemark.LONGITUDE, location.getLongitude());
			values.put(Placemark.STYLE, StyleManager.STYLE_ROUTE_FINISH);
			getContentResolver().insert(Placemark.CONTENT_URI, values);
			updateRoute();
		}
	}

	private void updateRoute()
	{
		if (_routeUri != null)
		{
			ContentValues values = new ContentValues();
			values.put(Folder.LAST_MODIFIED_TIME, System.currentTimeMillis());
			getContentResolver().update(_routeUri, values, null, null);
		}
	}
	
	private void cancel()
	{
		if ((_folder != null) && (_folder.getCount() == 1))
		{
			getContentResolver().delete(Placemark.CONTENT_URI, Placemark.PARENT_ID + "='" + _folder.getId() + "'", null);
		}
		if (_routeUri != null)
		{
			getContentResolver().delete(_routeUri, null, null);
			_routeUri = null;
		}
	}
	
	private void insertTrack(Location location)
	{
		try
		{
			ContentValues values = new ContentValues();
			values.put(Track.PARENT_ID, _trackPlacemarkId);
			values.put(Track.SEQUENCE, _nextTrackSequence++);
			values.put(Track.LATITUDE, (float)location.getLatitude());
			values.put(Track.LONGITUDE, (float)location.getLongitude());
			getContentResolver().insert(Track.CONTENT_URI, values);
			updateRoute();
			_lastTrackLocation = new Location(location);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private class AutoGpsListener implements LocationListener
	{
		public AutoGpsListener()
		{
			_minTime = 15000;
			_minDistance = 30.0f;
			Location lastKnown = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnown != null)
			{
				onLocationChanged(lastKnown);
			}
		}
		
    	@Override
    	public void onLocationChanged(Location location)
    	{
    		long deltaTime = 0;
    		if (_lastTrackLocation != null)
    		{
    			deltaTime = location.getTime() - _lastTrackLocation.getTime();
    		}
    		if (_lastTrackLocation == null || location.distanceTo(_lastTrackLocation) >= _minDistance || deltaTime > _minTime)
    		{
    			insertTrack(location);
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
	}
	
	private class DistanceGpsListener implements LocationListener
	{
		public DistanceGpsListener()
		{
			_minTime = 15000;
			if (_interval.equals("Most Often"))
			{
				_minDistance = 5.0f;
			}
			else if (_interval.equals("More Often"))
			{
				_minDistance = 15.0f;
				
			}
			else if (_interval.equals("Less Often"))
			{
				_minDistance = 40.f;
			}
			else if (_interval.equals("Least Often"))
			{
				_minDistance = 60.0f;
			}
			else // normal
			{
				_minDistance = 30.0f;
			}
			Location lastKnown = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnown != null)
			{
				onLocationChanged(lastKnown);
			}
		}
		
    	@Override
    	public void onLocationChanged(Location location)
    	{
    		if (_lastTrackLocation == null || location.distanceTo(_lastTrackLocation) >= _minDistance)
    		{
    			insertTrack(location);
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
	}
	
	private class TimeGpsListener implements LocationListener
	{
		public TimeGpsListener()
		{
			_minDistance = 0.0f;
			if (_interval.equals("Most Often"))
			{
				_minTime = 30000;
			}
			else if (_interval.equals("More Often"))
			{
				_minTime = 45000;
				
			}
			else if (_interval.equals("Less Often"))
			{
				_minTime = 90000;
			}
			else if (_interval.equals("Least Often"))
			{
				_minTime = 120000;
			}
			else // normal
			{
				_minTime = 60000;
			}
			Location lastKnown = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnown != null)
			{
				onLocationChanged(lastKnown);
			}
		}
		
    	@Override
    	public void onLocationChanged(Location location)
    	{
    		long deltaTime = 0;
    		if (_lastTrackLocation != null)
    		{
    			deltaTime = location.getTime() - _lastTrackLocation.getTime();
    		}
    		if (_lastTrackLocation == null || deltaTime > _minTime)
    		{
    			insertTrack(location);
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
	}
}
