package com.futureconcepts.mercury.main;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.Intents;
import com.futureconcepts.mercury.R;
import com.futureconcepts.mercury.maps.DownloadMapFileActivity;
import com.futureconcepts.mercury.tracker.GPS;
import com.futureconcepts.mercury.update.CheckUpdateActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
//	private static final String TAG = "SettingsActivity";
	private static final String MAGIC_COOKIE = "admin";
	private static final String XMPP_USERNAME = "xmpp_username";
	private Config _config;
	private int _cookieIndex = 0;
	private AlertDialog gpsAlert = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        _config = Config.getInstance(this);
        addPreferencesFromResource(R.xml.settings);
    	findPreference("device_id").setSummary(_config.getDeviceId());
    	SharedPreferences sharedPreferences = _config.getSharedPreferences();
    	if (sharedPreferences != null)
    	{
    		onSharedPreferenceChanged(sharedPreferences, "device_name");
    		onSharedPreferenceChanged(sharedPreferences, "web_service_address");
    		onSharedPreferenceChanged(sharedPreferences, "wsus_service_address");
    		onSharedPreferenceChanged(sharedPreferences, XMPP_USERNAME);
    	}
    	_config.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        sendBroadcast(new Intent(Intents.ACTION_START_SERVICES));
    }
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_config.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		verifyGPS();
	}
	
			
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
		if (preference.getKey().equals("antaresx_updates"))
		{
			if (_config.getMyEquipmentId() == null)
			{
				AlertDialog.Builder ab = new AlertDialog.Builder(this);
				ab.setTitle("Configuration Required");
				ab.setMessage("Do you want to download configuration now?");
				ab.setPositiveButton("Yes", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent(SettingsActivity.this, DownloadConfigurationActivity.class);
						startActivity(intent);
					}
				});
				ab.setNegativeButton("No", new OnClickListener() {
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
				AlertDialog dialog = ab.create();
				dialog.show();
			}
			else
			{
				Intent intent = new Intent(this, CheckUpdateActivity.class);
				startActivity(intent);
			}
		}
		else if (preference.getKey().equals("antaresx_set_password"))
		{
			Intent intent = new Intent(this, SetPasswordActivity.class);
			startActivity(intent);
		}
		else if (preference.getKey().equals("set_xmpp_password"))
		{
			Intent intent = new Intent(this, SetXmppPasswordActivity.class);
			startActivity(intent);
		}
		else if (preference.getKey().equals("antaresx_download_configuration"))
		{
			Intent intent = new Intent(this, DownloadConfigurationActivity.class);
			startActivity(intent);
		}
		else if(preference.getKey().equals("download_map_file"))
		{
			Intent intent = new Intent(this, DownloadMapFileActivity.class);
			startActivity(intent);
		}else if(preference.getKey().equals("view_legal_info"))
		{
			Intent intent = new Intent(this, LegalInfoActivity.class);
			startActivity(intent);
		}
		///test only
//		else if(preference.getKey().equals("tracker_enabled"))
//		{
//			if(!GPS.isGPSEnabled(getApplicationContext()))
//			{
//				CheckBoxPreference box = (CheckBoxPreference)findPreference("tracker_enabled");
//				box.setChecked(false);
//				buildAlertMessageNoGps();
//			}
//		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
	{
		if (keyEvent.getUnicodeChar() == MAGIC_COOKIE.codePointAt(_cookieIndex))
		{
			_cookieIndex++;
			if (_cookieIndex == MAGIC_COOKIE.length())
			{
				_cookieIndex = 0;
				Intent intent = new Intent(this, AdminSettings.class);
				startActivity(intent);
				return true;
			}
			else
			{
				return true;
			}
		}
		else
		{
			_cookieIndex = 0;
			return super.onKeyDown(keyCode, keyEvent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settings_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_backdoor:
			onMenuSoftKeyboard();
			break;
		case R.id.menu_start_service:
			onMenuStartService();
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals("device_name"))
		{
	    	findPreference("device_name").setSummary(_config.getDeviceName());
		}
		else if (key.equals("web_service_address"))
		{
			findPreference("web_service_address_readonly").setSummary(_config.getWebServiceAddress());
		}
		else if (key.equals("wsus_service_address"))
		{
			findPreference("wsus_service_address_readonly").setSummary(_config.getWsusServiceAddress());
		}
		else if (key.equals(XMPP_USERNAME))
		{
			findPreference(key).setSummary(sharedPreferences.getString(key, null));
		}
		// add here the validation for the GPS..
		else if (key.equals("tracker_enabled") && Config.getInstance(this).getTrackerEnabled())
		{
			verifyGPS();
		}
		
	}

	private void onMenuSoftKeyboard()
	{
		final View view = getLayoutInflater().inflate(R.layout.get_password, null);
		view.findViewById(R.id.password).setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
				{
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
				else
				{
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				}
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Backdoor");
		builder.setView(view);
		builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TextView passwordView = (TextView)view.findViewById(R.id.password);
				String passwordText = new String(passwordView.getText().toString());
				if ("admin".equals(passwordText))
				{
					Intent intent = new Intent(SettingsActivity.this, AdminSettings.class);
					startActivity(intent);
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	
	private void verifyGPS()
	{
		if(!GPS.isGPSEnabled(getApplicationContext()))
		{
			CheckBoxPreference box = (CheckBoxPreference)findPreference("tracker_enabled");
			box.setChecked(false);
			buildAlertMessageNoGps();
		}
	}
	
	private void buildAlertMessageNoGps() {
		if (gpsAlert == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Your GPS is disabled, do you want to enable it?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(
										@SuppressWarnings("unused") final DialogInterface dialog,
										@SuppressWarnings("unused") final int id) {
									startActivity(new Intent(
											android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
									gpsAlert = null;
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										@SuppressWarnings("unused") final int id) {
									dialog.cancel();
									gpsAlert = null;
								}
							});
			gpsAlert = builder.create();
			gpsAlert.show();
		}
	}


	private void onMenuStartService()
	{
	//	PushReceiverService.actionStart(this);
//		TrackerServiceXMPP.startIfNeccessary(this, null);
	}
}
