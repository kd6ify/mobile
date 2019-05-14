package com.futureconcepts.jupiter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config
{
	public static final String FORMAT_DISTANCE = "distance_format";
	public static final String FORMAT_VELOCITY = "velocity_format";
	public static final String FORMAT_LOCATION = "location_format";
	public static final String FOLLOW_ME = "follow_me";
	public static final String LAYER_ENABLED = "layer_enabled_";
	public static final String TRUE_NORTH = "true_north";
	
	public static final String DISTANCE_FORMAT_METERS = "meters";
	public static final String DISTANCE_FORMAT_FEET = "feet";
	public static final String DISTANCE_FORMAT_MILES = "miles";
	
	public static final String VELOCITY_FORMAT_KPH = "kph";
	public static final String VELOCITY_FORMAT_MPH = "mph";
	public static final String VELOCITY_FORMAT_METERS_PER_SECOND = "mps";
	
	public static final String LOCATION_FORMAT_DEGREES = "degrees";
	public static final String LOCATION_FORMAT_MINUTES = "minutes";
	public static final String LOCATION_FORMAT_SECONDS = "seconds";
	public static final String LOCATION_FORMAT_NAD27 = "nad27";

	private SharedPreferences _sharedPreferences;
	
	public static Config _instance = null;
	
	public static synchronized Config getInstance(Context context)
	{
		if (_instance == null)
		{
			_instance = new Config(context);
		}
		return _instance;
	}
	
	private Config(Context context)
	{
		_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public SharedPreferences getSharedPreferences()
	{
		return _sharedPreferences;
	}
		
	public boolean getTrueNorthEnabled()
	{
		return _sharedPreferences.getBoolean(Config.TRUE_NORTH, true);
	}
		
	public void setLayerEnabledById(String id, boolean value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putBoolean(LAYER_ENABLED + id, value);
		editor.commit();
	}

	public boolean isLayerEnabledById(String id)
	{
		boolean value = _sharedPreferences.getBoolean(LAYER_ENABLED + id, false);
		return value;
	}

	public float getLastMapScale()
	{
		return _sharedPreferences.getFloat("last_map_scale", 0.0f);
	}

	public String getDistanceFormat()
	{
		return _sharedPreferences.getString(FORMAT_DISTANCE, DISTANCE_FORMAT_FEET);
	}
	
	public String getVelocityFormat()
	{
		return _sharedPreferences.getString(FORMAT_VELOCITY, VELOCITY_FORMAT_MPH);
	}

	public String getLocationFormat()
	{
		return _sharedPreferences.getString(FORMAT_LOCATION, LOCATION_FORMAT_DEGREES);
	}
	
	public void setLastMapScale(float value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putFloat("last_map_scale", value);
		editor.commit();
	}

	public float getLastMapLatitude()
	{
		return _sharedPreferences.getFloat("last_map_latitude", 0.0f);
	}

	public void setLastMapLatitude(float value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putFloat("last_map_latitude", value);
		editor.commit();
	}

	public float getLastMapLongitude()
	{
		return _sharedPreferences.getFloat("last_map_longitude", 0.0f);
	}

	public void setLastMapLongitude(float value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putFloat("last_map_longitude", value);
		editor.commit();
	}

	public String getTripId()
	{
		return _sharedPreferences.getString("selected_trip_id", null);
	}

	public void setTripId(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("selected_trip_id", value);
		editor.commit();
	}

	public String getTripUri()
	{
		return _sharedPreferences.getString("selected_trip_uri", null);
	}

	public void setTripUri(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("selected_trip_uri", value);
		editor.commit();
	}
}
