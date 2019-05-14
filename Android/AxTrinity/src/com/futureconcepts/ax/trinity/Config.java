package com.futureconcepts.ax.trinity;

import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.model.data.OperationalPeriod;
import com.futureconcepts.gqueue.MercurySettings;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class Config
{
	public static final String KEY_LAYER_ENABLED = "layer_enabled_";
	public static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
	public static final String KEY_MAP_ICON_SIZE_BUMP = "map_icon_size_bump";
	public static final String KEY_ALERT_INCIDENT_ONLY = "alert_incident_only";
	public static final String KEY_LAYER = "layer_";
	public static final String KEY_LAYER_SELECTED = "_selected";
	public static final String KEY_LAST_MAP_SCALE = "last_map_scale";
	public static final String KEY_LAST_MAP_LATITUDE = "last_map_latitude";
	public static final String KEY_LAST_MAP_LONGITUDE = "last_map_longitude";
	
	private Config()
	{
		// all methods static
	}

	public static SharedPreferences getPreferences(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences;
	}
	
	public static SharedPreferences.Editor getPreferencesEditor(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		return editor;
	}
	
	public static boolean hasKey(Context context, String key)
	{
		return getPreferences(context).contains(key);
	}

	public static String getDeviceId(Context context)
	{
		String value = null;
		if ("google_sdk".equals(Build.PRODUCT))
		{
//			value = "000000000000000";
			value = "A0000015D648A8";
		}
		else
		{
			TelephonyManager tmgr = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
			if (tmgr != null)
			{
				value = tmgr.getDeviceId();
			}
		}
		return value;
	}
		
	public static boolean isFirstApplicationLaunch(Context context)
	{
		boolean result = false;
		if (MercurySettings.getPassword(context) != null)
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			result = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
			if (result)
			{
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);
				editor.commit();
			}
		}
		return result;
	}
	
	public static void resetFirstApplicationLaunch(Context context)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.remove(KEY_IS_FIRST_LAUNCH);
		editor.commit();
	}
	
	public static String getCurrentIncidentName(Context context)
	{
		String result = null;
		String id = MercurySettings.getCurrentIncidentId(context);
		if (id != null)
		{
			Uri uri = Uri.withAppendedPath(Incident.CONTENT_URI, id);
			try
			{
				Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
				if (cursor != null)
				{
					if (cursor.getCount() == 1)
					{
						cursor.moveToFirst();
						result = cursor.getString(cursor.getColumnIndex(Incident.NAME));
					}
					cursor.close();
				}
			}
			catch (Exception e)
			{
				result = "PLEASE RESET DATA";
			}
		}
		return result;
	}
		
	public static String getCurrentOperationalPeriodName(Context context)
	{
		String result = "";
		String id = MercurySettings.getCurrentOperationalPeriodId(context);
		if (id != null)
		{
			Uri uri = Uri.withAppendedPath(OperationalPeriod.CONTENT_URI, id);
			try
			{
				Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
				if (cursor != null)
				{
					if (cursor.getCount() == 1)
					{
						cursor.moveToFirst();
						result = cursor.getString(cursor.getColumnIndex(OperationalPeriod.NAME));
					}
					cursor.close();
				}
			}
			catch (Exception e)
			{
				result = "PLEASE RESET DATA";
			}
		}
		return result;
	}
				
	public static int getMapIconSizeBump(Context context)
	{
		return getPreferences(context).getInt(KEY_MAP_ICON_SIZE_BUMP, 0);
	}

	public static void setMapIconSizeBump(Context context, int value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putInt(KEY_MAP_ICON_SIZE_BUMP, value);
		editor.commit();
	}
	
	public static boolean alertIncidentOnly(Context context)
	{
		return getPreferences(context).getBoolean(KEY_ALERT_INCIDENT_ONLY, false);
	}

	public static void setLayerSelected(Context context, String name, boolean value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putBoolean(KEY_LAYER + name + KEY_LAYER_SELECTED, value);
		editor.commit();
	}
	
	public static boolean isLayerSelected(Context context, String name)
	{
		return getPreferences(context).getBoolean(KEY_LAYER + name + KEY_LAYER_SELECTED, false);
	}

	public static float getLastMapScale(Context context)
	{
		return getPreferences(context).getFloat(KEY_LAST_MAP_SCALE, 0.0f);
	}

	public static void setLastMapScale(Context context, float value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putFloat(KEY_LAST_MAP_SCALE, value);
		editor.commit();
	}

	public static float getLastMapLatitude(Context context)
	{
		return getPreferences(context).getFloat(KEY_LAST_MAP_LATITUDE, 0.0f);
	}

	public static void setLastMapLatitude(Context context, float value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putFloat(KEY_LAST_MAP_LATITUDE, value);
		editor.commit();
	}

	public static float getLastMapLongitude(Context context)
	{
		return getPreferences(context).getFloat(KEY_LAST_MAP_LONGITUDE, 0.0f);
	}

	public static void setLastMapLongitude(Context context, float value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putFloat(KEY_LAST_MAP_LONGITUDE, value);
		editor.commit();
	}
	
	public static void setLayerEnabledById(Context context, String id, boolean value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putBoolean(KEY_LAYER_ENABLED + id, value);
		editor.commit();
	}

	public static boolean isLayerEnabledById(Context context, String id)
	{
		return getPreferences(context).getBoolean(KEY_LAYER_ENABLED + id, false);
	}
}
