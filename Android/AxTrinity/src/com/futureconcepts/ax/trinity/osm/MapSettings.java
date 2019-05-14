package com.futureconcepts.ax.trinity.osm;

import android.content.Context;
import android.content.SharedPreferences;


public class MapSettings {
	
	private Context _context;
	private SharedPreferences _sharedPreferences;
	public static final String PREFERENCE_MAP_MODE ="map_mode";
	public static final String PREFERENCE_TRACKER_MODE ="tracker_mode";
	public static final String PREFERENCE_MAP_FILE ="map_file";
	public static final String PREFERENCE_MAP_SCALE ="scale";
	public static final int MAP_ONLINE =1;
	public static final int MAP_OFFLINE =0;
	public static MapSettings _instance = null;
	
	public static synchronized MapSettings getInstance(Context context)
	{
		if (_instance == null)
		{
			_instance = new MapSettings(context);
		}
		return _instance;
	}
	
	private MapSettings(Context context)
	{
		_context = context;
		_sharedPreferences =_context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE);//PreferenceManager.//.getDefaultSharedPreferences(_context);
	}
	
	public SharedPreferences getSharedPreferences()
	{
		return _sharedPreferences;
	}
	
	public int getMapMode()
	{		
		return _sharedPreferences.getInt(PREFERENCE_MAP_MODE, MAP_ONLINE);		
	}
	
	public String getMapModeAsOfflineOnline()
	{
		if(_sharedPreferences.getString(PREFERENCE_MAP_MODE, "0").equals("0"))
			return "Offline";
		else
			return "Online";
	}
	
	public void setMapMode(int value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putInt(PREFERENCE_MAP_MODE, value);
		editor.commit();
	}
	
	public String getMapFile()
	{		
		return _sharedPreferences.getString(PREFERENCE_MAP_FILE, "No file");		
	}	
	public void setMapFile(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString(PREFERENCE_MAP_FILE, value);
		editor.commit();
	}

	public  String getPreferenceTrackerMode() {
		return  _sharedPreferences.getString(PREFERENCE_TRACKER_MODE, "Normal: 1min/50m");	
	}
	
	public void setPreferenceTrackerMode(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString(PREFERENCE_TRACKER_MODE, value);
		editor.commit();
	}
	
	public  String getPreferenceMapScale() {
		return  _sharedPreferences.getString(PREFERENCE_MAP_SCALE, "1.0");//1.0 is defaul for mapsForge	
	}
	public  Float getPreferenceMapScaleAsFloat() {
		return  Float.valueOf(_sharedPreferences.getString(PREFERENCE_MAP_SCALE, "1.0"));//1.0 is defaul for mapsForge	
	}
	
	public void setPreferenceMapScale(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString(PREFERENCE_MAP_SCALE, value);
		editor.commit();
	}


}
