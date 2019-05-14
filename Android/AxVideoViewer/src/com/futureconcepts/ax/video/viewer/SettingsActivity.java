package com.futureconcepts.ax.video.viewer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private static final String TAG = SettingsActivity.class.getSimpleName();

	private SharedPreferences _sharedPreferences;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    	findPreference("device_id").setSummary(Settings.getDeviceId(this));
    	_sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	if (_sharedPreferences != null)
    	{
    		onSharedPreferenceChanged(_sharedPreferences, Settings.KEY_WEB_SERVICE_ADDRESS);
    	}
    	_sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		_sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
				
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Settings.KEY_WEB_SERVICE_ADDRESS))
		{
			findPreference(Settings.KEY_WEB_SERVICE_ADDRESS).setSummary(Settings.getWebServiceAddress(this));
		}
	}
}
