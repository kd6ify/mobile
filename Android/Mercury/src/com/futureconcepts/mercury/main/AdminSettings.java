package com.futureconcepts.mercury.main;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class AdminSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private static final String TAG = AdminSettings.class.getSimpleName();	
	private static final String KEY_WEB_SERVICE_ADDRESS = "web_service_address";
	private static final String KEY_CAMERA_HORIZONTAL_ANGLE = "camera_horizontal_angle";
	private static final String KEY_CAMERA_VERTICAL_ANGLE = "camera_vertical_angle";
	private static final String KEY_COMPASS_OFFSET = "compass_offset";

	private Config _config;
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try
        {
	        PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
	        if (pi != null)
	        {
		        setTitle("AntaresX " + pi.versionCode + " Admin Settings");
	        }
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.admin_settings);
    	_config = Config.getInstance(this);
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
        _config.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		SharedPreferences sharedPreferences = _config.getSharedPreferences();
		onSharedPreferenceChanged(sharedPreferences, KEY_CAMERA_HORIZONTAL_ANGLE);
		onSharedPreferenceChanged(sharedPreferences, KEY_CAMERA_VERTICAL_ANGLE);
		onSharedPreferenceChanged(sharedPreferences, KEY_COMPASS_OFFSET);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		_config.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
			
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
		if (key != null)
		{
			if (key.equals(KEY_WEB_SERVICE_ADDRESS))
			{
				startActivity(new Intent(this, SetPasswordActivity.class));
			}
			else if (key.equals(KEY_CAMERA_HORIZONTAL_ANGLE))
			{
		    	findPreference(KEY_CAMERA_HORIZONTAL_ANGLE).setSummary(sharedPreferences.getString(KEY_CAMERA_HORIZONTAL_ANGLE, "62.1"));
			}
			else if (key.equals(KEY_CAMERA_VERTICAL_ANGLE))
			{
		    	findPreference(KEY_CAMERA_VERTICAL_ANGLE).setSummary(sharedPreferences.getString(KEY_CAMERA_VERTICAL_ANGLE, "48.2"));
			}
			else if (key.equals(KEY_COMPASS_OFFSET))
			{
		    	findPreference(KEY_COMPASS_OFFSET).setSummary(sharedPreferences.getString(KEY_COMPASS_OFFSET, "0"));
			}
		}
		getListView().postInvalidate();
    }
}
