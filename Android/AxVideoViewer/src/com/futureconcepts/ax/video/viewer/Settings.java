package com.futureconcepts.ax.video.viewer;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class Settings
{
	public static String KEY_WEB_SERVICE_ADDRESS = "web_service_address";
	
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
					WifiManager wifiMan = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInf = wifiMan.getConnectionInfo();
					value = wifiInf.getMacAddress().replace(":", "");
//					value = "A0000015D648A8"; // for now, use Droid test phone IMEI
				}
			}
		}
		return value;
	}
	
	public static String getWebServiceAddress(Context context)
	{
		String deviceID = getDeviceId(context);
		String baseUrl = getSharedPreferences(context).getString(KEY_WEB_SERVICE_ADDRESS, "localhost");
		return String.format(baseUrl, deviceID);
	}
	
	public static SharedPreferences getSharedPreferences(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
