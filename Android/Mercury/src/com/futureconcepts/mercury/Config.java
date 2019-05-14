package com.futureconcepts.mercury;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.futureconcepts.mercury.crypto.SimpleCrypto;

public class Config
{
	private Context _context;
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
		_context = context;
		_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
	}
	
	public SharedPreferences getSharedPreferences()
	{
		return _sharedPreferences;
	}

	public String getDeviceId()
	{
		String value = null;
		if ("google_sdk".equals(Build.PRODUCT))
		{
//			value = "000000000000000";
			value = "A0000015D648A8";
		}
		else
		{
			TelephonyManager tmgr = (TelephonyManager)_context.getSystemService(Service.TELEPHONY_SERVICE);
			if (tmgr != null)
			{
				value = tmgr.getDeviceId();
				
				
				if (value == null)
				{
					WifiManager wifiMan = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiInf = wifiMan.getConnectionInfo();
					value = wifiInf.getMacAddress().replace(":", "");
//					value = "A0000015D648A8"; // for now, use Droid test phone IMEI
					Log.d("HI", value);
				}
			}
		}
		return value;
	}
	
	public String getDeviceName()
	{
		return _sharedPreferences.getString("device_name", "Trikorder");
	}
	
	public void setDeviceName(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("device_name", value);
		editor.commit();
	}
		
	public void setPassword(String value) throws Exception
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("password", SimpleCrypto.encrypt(value));
		editor.commit();
	}

	public String getPassword() throws Exception
	{
		String result = null;
		String encryptedPassword = _sharedPreferences.getString("password", null);
		if (encryptedPassword != null)
		{
			result = SimpleCrypto.decrypt(encryptedPassword);
		}
		return result;
	}

	public String getPhoneNumber()
	{
		return _sharedPreferences.getString("phone_number", "1112222");
	}

	public void setPhoneNumber(String phoneNumber)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("phone_number", phoneNumber);
		editor.commit();
	}

	public void setMyEquipmentId(String equipmentId)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("my_equipment_id", equipmentId);
		editor.commit();
	}

	public String getMyEquipmentId()
	{
		return _sharedPreferences.getString("my_equipment_id", null);
	}
	
	public String getWebServiceAddress()
	{
		return _sharedPreferences.getString("web_service_address", "https://tracker2.futurec.net:8280");
	}
	
	public void setWebServiceAddress(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("web_service_address", value);
		editor.commit();
	}

	public String getWsusServiceAddress()
	{
		return _sharedPreferences.getString("wsus_service_address", "https://updates.antaresx.net");
	}
	
	public void setWsusServiceAddress(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("wsus_service_address", value);
		editor.commit();
	}
	
	public String getMediaImagesServerAddress()
	{
		return _sharedPreferences.getString("media_images_server_address", null);
	}
	public void setMediaImagesServerAddressField(String MediaImagesServerAddress)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("media_images_server_address", MediaImagesServerAddress);
		editor.commit();
	}
	
	public String getMediaImagesServerUser()
	{
		return _sharedPreferences.getString("media_images_server_user", null);
	}
	
	public void setMediaImagesServerUserField(String MediaImagesServerUser)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("media_images_server_user", MediaImagesServerUser);
		editor.commit();
	}
	
	public String getMediaImagesServerPassword()
	{
		return _sharedPreferences.getString("media_images_server_password", null);
	}
	
	public void setMediaImagesServerPasswordField(String MediaImagesServerPassword)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("media_images_server_password", MediaImagesServerPassword);
		editor.commit();
	}
		
	public String getAlertMode()
	{
		return _sharedPreferences.getString("alert_mode", "silent");
	}

	public boolean isVibrateAlertEnabled()
	{
		return _sharedPreferences.getBoolean("vibrate_alert", false);
	}
	
	public boolean isSpeakAlertEnabled()
	{
		return _sharedPreferences.getBoolean("speak_alert", false);
	}
	
	public boolean isToneAlertEnabled()
	{
		return _sharedPreferences.getBoolean("tone_alert", false);
	}

	public String getAlertToneUri()
	{
		return _sharedPreferences.getString("alert_tone_uri", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
	}

	public void setAlertToneUri(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("alert_tone_uri", value);
		editor.commit();
	}

	public boolean getTrackerEnabled()
	{
		return _sharedPreferences.getBoolean("tracker_enabled", false);
	}
	
	public void setTrackerEnabled(boolean value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putBoolean("tracker_enabled", value);
		editor.commit();
		if (value)
		{
		//	Intent intent = new Intent(_context, TrackerService.class);
		//	_context.startService(intent);
		}
	}
	
	public String getTrackerMode()
	{
		return _sharedPreferences.getString("tracker_mode", "casual");
	}
	
	public void setTrackerMode(String value)
	{
		SharedPreferences.Editor editor = _sharedPreferences.edit();
		editor.putString("tracker_mode", value);
		editor.commit();
	}
	
	public String getXmppPassword()
	{
		String result = null;
		try
		{
			result = SimpleCrypto.decrypt(_sharedPreferences.getString("xmpp_password", null));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public void setXmppPassword(String cleartext)
	{
		try
		{
			SharedPreferences.Editor editor = _sharedPreferences.edit();
			String encryptedValue = SimpleCrypto.encrypt(cleartext);
			editor.putString("xmpp_password", encryptedValue);
			editor.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
