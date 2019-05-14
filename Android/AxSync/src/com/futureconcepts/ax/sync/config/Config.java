package com.futureconcepts.ax.sync.config;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Config
{
	public static final String KEY_LAST_SYNC_VERSION = "last_sync_version_";
	public static final String KEY_LAST_SYNC_ERROR_MESSAGE = "last_sync_error_message";
	public static final String KEY_CURRENT_INCIDENT_ID = "current_incident_id";
		
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
	
	public static long getLastSyncVersion(Context context, String query)
	{
		return getPreferences(context).getLong(KEY_LAST_SYNC_VERSION + query, -1);
	}
		
	public static void setLastSyncException(Context context, Exception e)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		if (e != null)
		{
			editor.putString(KEY_LAST_SYNC_ERROR_MESSAGE, e.getMessage());
		}
		else
		{
			editor.remove(KEY_LAST_SYNC_ERROR_MESSAGE);
		}
		editor.commit();
	}
	
	public static String getLastSyncErrorMessage(Context context)
	{
		return getPreferences(context).getString(KEY_LAST_SYNC_ERROR_MESSAGE, null);
	}
	
	public static void setLastSyncVersion(Context context, String query, long value)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.putLong(KEY_LAST_SYNC_VERSION + query, value);
		editor.commit();
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
				if (value == null)
				{
					WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInf = wifiMan.getConnectionInfo();
					value = wifiInf.getMacAddress().replace(":", "");
//					value = "A0000015D648A8"; // for now, use Droid test phone IMEI
				}
				
			}
		}
		return value;
	}
}
