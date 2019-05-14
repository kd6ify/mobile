package com.futureconcepts.ax.rex.config;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class Config
{
	public static final String KEY_IS_FIRST_LAUNCH = "is_first_launch";
	
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
	
	public static boolean isFirstApplicationLaunch(Context context)
	{
		boolean result = false;
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		result = sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true);
		if (result)
		{
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putBoolean(KEY_IS_FIRST_LAUNCH, false);
			editor.commit();
		}
		return result;
	}
	
	public static void resetFirstApplicationLaunch(Context context)
	{
		SharedPreferences.Editor editor = getPreferencesEditor(context);
		editor.remove(KEY_IS_FIRST_LAUNCH);
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
			}
		}
		return value;
	}
	
	
}
