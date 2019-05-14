package com.futureconcepts.gqueue;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MercurySettings
{
	public static final String KEY_PHONE_NUMBER = "phone_number";
	public static final String KEY_ALERT_MODE = "alert_mode";	
	public static final String KEY_VIBRATE_ALERT = "vibrate_alert";
	public static final String KEY_SPEAK_ALERT = "speak_alert";
	public static final String KEY_TONE_ALERT = "tone_alert";
	public static final String KEY_ALERT_TONE_URI = "alert_tone_uri";
	public static final String KEY_CAMERA_HORIZONTAL_ANGLE = "camera_horizontal_angle";
	public static final String KEY_CAMERA_VERTICAL_ANGLE = "camera_vertical_angle";
	public static final String KEY_COMPASS_OFFSET = "compass_offset";
	public static final String KEY_USER_MESSAGE = "user_message";
	public static final String KEY_USER_MESSAGE_TITLE = "user_message_title";
	public static final String KEY_CURRENT_INCIDENT_ID = "current_incident_id";
	public static final String KEY_CURRENT_OPERATIONAL_PERIOD_ID = "current_operational_period_id";
	public static final String KEY_MEDIA_IMAGES_SERVER_ADDRESS = "media_images_server_address";
	public static final String KEY_MEDIA_IMAGES_SERVER_USER = "media_images_server_user";
	public static final String KEY_MEDIA_IMAGES_SERVER_PASSWORD = "media_images_server_password";
	
	
	
	private MercurySettings() {}

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
					if (wifiInf.getMacAddress() != null)
					{
						value = wifiInf.getMacAddress().replace(":", "");
						// value = "A0000015D648A8"; // for now, use Droid test phone IMEI
					}
					else
					{
						value = "A0000015D648A8";
					}
				}
			}
		}
		return value;
	}
	
	public static String getWebServiceAddress(Context context)
	{
		return getStringValue(context, "web_service_address", "https://tracker.antaresx.net");
	}

	public static String getPassword(Context context)
	{
		return getEncryptedStringValue(context, "password", null);
	}

	public static String getMyEquipmentId(Context context)
	{
		return getStringValue(context, "my_equipment_id", null);
	}

	public static float getCameraHorizontalViewAngle(Context context)
	{
		return Float.parseFloat(getStringValue(context, KEY_CAMERA_HORIZONTAL_ANGLE, "62.1"));
	}
	
	public static float getCameraVerticalViewAngle(Context context)
	{
		return Float.parseFloat(getStringValue(context, KEY_CAMERA_VERTICAL_ANGLE, "48.2"));
	}
			
	public static float getCompassOffset(Context context)
	{
		return Float.parseFloat(getStringValue(context, KEY_COMPASS_OFFSET, "0"));
	}
	
	public static String getCurrentIncidentId(Context context)
	{
		return getStringValue(context, KEY_CURRENT_INCIDENT_ID, null);
	}
	
	public static void setCurrentIncidentId(Context context, String value)
	{
		putStringValue(context, KEY_CURRENT_INCIDENT_ID, value);
		putStringValue(context, KEY_CURRENT_OPERATIONAL_PERIOD_ID, null);
	}

	public static String getCurrentOperationalPeriodId(Context context)
	{
		return getStringValue(context, KEY_CURRENT_OPERATIONAL_PERIOD_ID, null);
	}
	
	public static void setCurrentOperationalPeriodId(Context context, String value)
	{
		putStringValue(context, KEY_CURRENT_OPERATIONAL_PERIOD_ID, value);
	}
	
	public static String getMediaImagesServerAddress(Context context)
	{
		return getStringValue(context, KEY_MEDIA_IMAGES_SERVER_ADDRESS, null);
	}
	
	public static String getMediaImagesServerPassword(Context context)
	{
		return getStringValue(context, KEY_MEDIA_IMAGES_SERVER_PASSWORD, null);
	}
	
	public static String getMediaImagesServerUser(Context context)
	{
		return getStringValue(context, KEY_MEDIA_IMAGES_SERVER_USER, null);
	}
	
	public static String getStringValue(Context context, String key, String defaultValue)
	{
		String value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/string"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = cursor.getString(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
		}
		return value;
	}
	
	public static void putStringValue(Context context, String key, String value)
	{
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/string"), key);
		ContentValues values = new ContentValues();
		values.put("value", value);
		context.getContentResolver().insert(uri, values);
	}

	public static String getEncryptedStringValue(Context context, String key, String defaultValue)
	{
		String value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/encrypted_string"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = cursor.getString(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
		}
		return value;
	}

	public static int getIntValue(Context context, String key, int defaultValue)
	{
		int value = defaultValue;
		Uri uri = Uri.withAppendedPath(Uri.parse("content://com.futureconcepts.settings/integer"), key);
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if (cursor != null)
		{
			if (cursor.getCount() == 1)
			{
				try
				{
					cursor.moveToFirst();
					value = cursor.getInt(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			cursor.close();
		}
		return value;
	}
}
