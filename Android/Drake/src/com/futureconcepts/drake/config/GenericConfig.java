package com.futureconcepts.drake.config;

import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

public class GenericConfig extends ConfigBase
{
	public static class KEY
	{
		private static final String PHONE_NUMBER = "phone_number";
	}
	
	private GenericConfig()
	{
		// all methods static
	}

	public static String getDeviceId(Context context)
	{
		String value = null;
		if ("google_sdk".equals(Build.PRODUCT))
		{
			value = "000000000000000";
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
	
	public static String getPhoneNumber(Context context)
	{
		return getGlobalStringValue(context, KEY.PHONE_NUMBER, null);
	}
}
