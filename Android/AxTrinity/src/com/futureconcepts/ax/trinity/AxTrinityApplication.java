package com.futureconcepts.ax.trinity;

import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.model.DisplayModel;

import com.futureconcepts.ax.trinity.osm.MapSettings;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

//we need this class to make work the OSM (new map API)
public class AxTrinityApplication extends Application {

	  public static final String TAG = "AxTrinity APP";
	  
	  public void onCreate()
	  {
	    super.onCreate();
	    AndroidGraphicFactory.createInstance(this);
	    Log.e(TAG, "Device scale factor " + Float.toString(DisplayModel.getDeviceScaleFactor()));
	    MapSettings preferences = MapSettings.getInstance(getApplicationContext());//PreferenceManager.getDefaultSharedPreferences(this);
	    float fs = preferences.getPreferenceMapScaleAsFloat();//getString(SETTING_SCALE, Float.toString(DisplayModel.getDefaultUserScaleFactor()))).floatValue();
	    Log.e(TAG, "User ScaleFactor " + Float.toString(fs));
	    if (fs != DisplayModel.getDefaultUserScaleFactor()) {
	      DisplayModel.setDefaultUserScaleFactor((float) fs);
	     }
	  }
}
