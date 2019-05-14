package com.futureconcepts.mercury.tracker;

import java.io.Closeable;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.futureconcepts.mercury.CompatService;
import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.Intents;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.gqueue.GQueue;

public class TrackerService extends CompatService implements OnSharedPreferenceChangeListener
{
	private static final String TAG = TrackerService.class.getSimpleName();
		
	private LocationManager _locationManager;
	private ConnectivityManager _connectivityManager;
	private GpsListener _gpsListener;
	private Location _lastPostedLocation;
	private Uri _queueUri;
	
//  See Mantis 4346
//	private BatteryReceiver _batteryReceiver;
	
//	private boolean _isBatteryLow = false;
	private boolean _gpsAvailable = false;
//	private String _gpsMessage;
	
//	private int _batteryLevel;
	
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private Config _config;
		
	public boolean isGpsAvailable()
	{
		return _gpsAvailable;
	}
	
	public void setGpsAvailable(boolean value, String message)
	{
//		_gpsMessage = message;
		if (value != _gpsAvailable)
		{
			_gpsAvailable = value;
			notifyStateChanged();
		}
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "onCreate");
		_config = Config.getInstance(this);
//		configureXMPP();
		startMeAsForeground();
		if ( _config.getWebServiceAddress() == null )
		{
			Log.d(TAG, "needs configuration");
			setConfigurationNotification();
			stopSelf();
			return;
		}
		_queueUri = GQueue.getServiceQueueUri(this, TrackerQueueService.class);
		
//		_batteryChangedReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent)
//			{
//				int rawlevel = intent.getIntExtra("level", -1);
//               int scale = intent.getIntExtra("scale", -1);
//               _batteryLevel = -1;
//                if (rawlevel >= 0 && scale > 0)
//                {
//                    _batteryLevel = (rawlevel * 100) / scale;
//                }
//			}
//		};
//		_batteryReceiver = new BatteryReceiver();
//		registerReceiver(_batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		_config.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	private void startMeAsForeground()
	{
		Notification n = new Notification();
		n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		n.icon = R.drawable.tracker_status_nogps; // make warning
		n.tickerText = "Tracker is starting";
		n.when = System.currentTimeMillis();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToggleActivity.class), 0);
		n.setLatestEventInfo(this, "AntaresX Tracker Status", n.tickerText, pendingIntent);
		startForegroundCompat(R.layout.tracker_toggle, n);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG, "onDestroy");
//		if (_batteryChangedReceiver != null)
//		{
//			unregisterReceiver(_batteryChangedReceiver);
//			_batteryChangedReceiver = null;
//		}
		stopGpsListener();
		notifyStateChanged();
		stopForegroundCompat(R.layout.tracker_toggle);
//		if (_batteryReceiver != null)
//		{
//			unregisterReceiver(_batteryReceiver);
//			_batteryReceiver = null;
//		}
//		_asmack.close(this);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		Log.d(TAG, "onStart");
		if (_config.getTrackerEnabled())
		{
			restartGpsListener();
		}
		notifyStateChanged();
	}
	
	private boolean isConnectedAndAuthenticated()
	{
		boolean result = false;
		NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
		String password = null;
		try {
			password = _config.getPassword();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED) && (password != null))
		{
			result = true;
		}
		return result;
	}
		
	public void setConfigurationNotification()
	{
        // Set the icon, scrolling text and timestamp
	    Notification notification = new Notification(R.drawable.icon, "Requires Configuration", System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
	    Intent locationIntent = new Intent(this, com.futureconcepts.mercury.main.SettingsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, locationIntent, 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "AntaresX Needs Configuration", "Please run DownloadConfiguration", contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        getNotificationManager().notify(R.layout.download_configuration, notification);
    }
	
	private LocationManager getLocationManager()
	{
		if (_locationManager == null)
		{
			_locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		}
		return _locationManager;
	}
	
	private ConnectivityManager getConnectivityManager()
	{
		if (_connectivityManager == null)
		{
			_connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		}
		return _connectivityManager;
	}
	
	private void notifyStateChanged()
	{
		Notification n = new Notification();
		n.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		if (_config.getTrackerEnabled())
		{
			if (_gpsAvailable)
			{
				n.icon = R.drawable.tracker_status_good;
				n.tickerText = "Tracker is running " + _config.getTrackerMode();
			}
			else
			{
				n.icon = R.drawable.tracker_status_nogps; // make warning
				n.tickerText = "Tracker is paused " + _config.getTrackerMode();
			}
		}
		else
		{
			n.icon = R.drawable.tracker_status_bad;
			n.tickerText = "Tracker is stopped";
		}
		if (_config.isVibrateAlertEnabled())
		{
			n.vibrate = new long[] { 200, 300 };
		}
		n.when = System.currentTimeMillis();
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, ToggleActivity.class), 0);
		n.setLatestEventInfo(this, "AntaresX Tracker Status", n.tickerText, pendingIntent);
		NotificationManager nmgr = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
		nmgr.notify(R.layout.tracker_toggle, n);
		sendBroadcast(new Intent(Intents.ACTION_STATE_CHANGED));
		notifyServerOfStateChange();
	}
	
	private void notifyServerOfStateChange()
	{
		try
		{
			if (isConnectedAndAuthenticated())
			{
				ContentValues values = new ContentValues();
				values.put(GQueue.CONTENT, getStateAsContent());
				values.put(GQueue.NOTIFICATION_MESSAGE, "Update Tracker State");
				values.put(GQueue.EXPIRATION_TIME, 0);
				values.put(GQueue.ACTION, Intent.ACTION_INSERT);
				values.put(GQueue.SERVER_URL, "server://Tracker/State");
				GQueue.insertMessage(this, _queueUri, values);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private String getStateAsContent() throws JSONException
	{
		Config config = Config.getInstance(this);
		JSONObject data = new JSONObject();
		data.put("State", _config.getTrackerEnabled() ? "Running" : "Stopped");
		data.put("Mode", config.getTrackerMode());
//		data.put("BatteryLow", _isBatteryLow);
		String result = data.toString();
		return result;
	}
	
	private void postLocationIfPossible(Location location) throws JSONException
	{
		NetworkInfo networkInfo = getConnectivityManager().getActiveNetworkInfo();
		String password = null;
		try {
			password = _config.getPassword();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED) && (password != null))
		{
			ContentValues values = new ContentValues();
			String jsonString = getLocationData(location).toString();
			values.put(GQueue.CONTENT, jsonString);
			values.put(GQueue.EXPIRATION_TIME, System.currentTimeMillis() + (5 * 60 * 1000));
			values.put(GQueue.ACTION, Intent.ACTION_INSERT);
			values.put(GQueue.NOTIFICATION_MESSAGE, "Update Tracker Location");
			values.put(GQueue.SERVER_URL, "server://Tracker/Location");
			GQueue.insertMessage(TrackerService.this, _queueUri, values);
			_lastPostedLocation = new Location(location);
		}
		else
		{
			Log.d(TAG, "postLocationIfPossible ignoring location -- not connected or authenticated");
			if (networkInfo != null)
			{
				Log.d(TAG, networkInfo.toString());
			}
		}
	}

	public void onProviderDisabled(String provider)
	{
		Log.d(TAG, "onProviderDisabled " + provider);
	}

	public void onProviderEnabled(String provider)
	{
		Log.d(TAG, "onProviderEnabled " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		switch (status)
		{
		case LocationProvider.AVAILABLE:
			setGpsAvailable(true, null);
			if (extras != null)
			{
				Log.d(TAG, "Satellites: " + extras.getInt("satellites"));
			}
			break;
		case LocationProvider.OUT_OF_SERVICE:
			setGpsAvailable(false, "OUT_OF_SERVICE");
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			setGpsAvailable(false, "TEMPORARILY_UNAVAILABLE");
			break;
		}
	}

	private Object getLocationData(Location location) throws JSONException
	{
		JSONObject data = new JSONObject();
		Bundle extras = location.getExtras();
		if (extras != null)
		{
			if (extras.containsKey("satellites"))
			{
				data.put("Satellites", Integer.toString(extras.getInt("satellites")));
			}
			if (extras.containsKey("name"))
			{
				data.put("Name", extras.getString("name"));
			}
			if (extras.containsKey("description"))
			{
				data.put("Description", extras.getString("description"));
			}
		}
		data.put("Time", location.getTime());
		data.put("Latitude", location.getLatitude());
		data.put("Longitude", location.getLongitude());
		if (location.hasAccuracy())
		{
			data.put("HasAccuracy", location.hasAccuracy());
			data.put("Accuracy", location.getAccuracy());
		}
		if (location.hasAltitude())
		{
			data.put("HasAltitude", location.hasAltitude());
			data.put("Altitude", location.getAltitude());
		}
		if (location.hasBearing())
		{
			data.put("HasBearing", location.hasBearing());
			data.put("Bearing", location.getBearing());
		}
		if (location.hasSpeed())
		{
			data.put("HasSpeed", location.hasSpeed());
			data.put("Speed", location.getSpeed());
		}
//		data.put("BatteryLevel", _batteryLevel);
		return data;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals("tracker_mode") || key.equals("tracker_enabled"))
		{
			if (_config.getTrackerEnabled() == false)
			{
				stopGpsListener();
			}
			else
			{
				restartGpsListener();
			}
		}
	}

	private void stopGpsListener()
	{
		if (_gpsListener != null)
		{
			try
			{
				_gpsListener.close();
				_gpsListener = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		notifyStateChanged();
	}
	
	private void restartGpsListener()
	{
		stopGpsListener();
		_gpsListener = new GpsListener();
		notifyStateChanged();
	}

	public static final class Receiver extends BroadcastReceiver
	{
		@Override
	    public void onReceive(Context context, Intent intent)
	    {
			String action = intent.getAction();
			if (action != null)
			{
				Log.d(TAG, "received " + action);
				if ( action.equals(Intent.ACTION_BOOT_COMPLETED) ||	action.equals(Intents.ACTION_START_SERVICES))
				{
					context.startService(new Intent(context, TrackerService.class));
				}
			}
	    }
	}
	
//	public final class BatteryReceiver extends BroadcastReceiver
//	{
//		@Override
//	    public void onReceive(Context context, Intent intent)
//	    {
//			String action = intent.getAction();
//			if (action.equals(Intent.ACTION_BATTERY_LOW))
//			{
//				_isBatteryLow = true;
//				notifyServerOfStateChange();
//			}
//			else if (action.equals(Intent.ACTION_BATTERY_OKAY))
//			{
//				_isBatteryLow = false;
//				notifyServerOfStateChange();
//			}
//	    }
//   }
	
	private class GpsListener implements LocationListener, Closeable
	{
		private int _minTime;
		private float _minDistance;
		
		public GpsListener()
		{
			Location lastKnown = getLocationManager().getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (lastKnown != null)
			{
				onLocationChanged(lastKnown);
			}
			String trackerMode = _config.getTrackerMode();
			if (trackerMode != null)
			{
				if (trackerMode.equals("frequent"))
				{
					_minTime = 1000 * 60;
					_minDistance = 50.0f;
				}
				else
				{
					_minTime = 1000 * 60 * 15;
					_minDistance = 100.0f; // 100 meters
				}
			}
// Had no luck using any value for mMinTime > 0
//				getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, mMinTime, 0, mGpsListener);
			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}
		
		@Override
		public void onLocationChanged(Location location)
		{
			Log.d(TAG, "onLocationChanged");
			try
			{
				if (_lastPostedLocation != null)
				{
					float distanceTo = location.distanceTo(_lastPostedLocation);
					long deltaTime = location.getTime() - _lastPostedLocation.getTime();
					if ( distanceTo >= _minDistance ||
							deltaTime >= _minTime ||
							(location.hasAccuracy() && _lastPostedLocation.hasAccuracy() && location.getAccuracy() < _lastPostedLocation.getAccuracy())) 
					{
						postLocationIfPossible(location);
					}
					else
					{
						Log.d(TAG, "onLocationChanged skipping distance=" + distanceTo + " deltaTime=" + deltaTime);
					}
				}
				else
				{
					postLocationIfPossible(location);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws IOException
		{
			getLocationManager().removeUpdates(this);
		}

		@Override
		public void onProviderDisabled(String provider)
		{
			Log.d(TAG, "onProviderDisabled " + provider);
		}

		@Override
		public void onProviderEnabled(String provider)
		{
			Log.d(TAG, "onProviderEnabled " + provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			switch (status)
			{
			case LocationProvider.AVAILABLE:
				setGpsAvailable(true, null);
				if (extras != null)
				{
					Log.d(TAG, "Satellites: " + extras.getInt("satellites"));
				}
				break;
			case LocationProvider.OUT_OF_SERVICE:
				setGpsAvailable(false, "Out of service");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				setGpsAvailable(false, "temporarily unavailable");
				break;
			}
		}
	}
}
