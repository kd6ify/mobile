package com.futureconcepts.ax.trinity.main;

import com.futureconcepts.ax.trinity.Config;
import com.futureconcepts.ax.trinity.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AdminSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private static final String TAG = "trinity.main.AdminSettings";

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
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
        Config.getPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Config.getPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.admin_settings_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_delete_queue:
//			GQueue.delete(this, SqlReplicationQueueService.SqlReplicationQueueService);
			break;
		case R.id.menu_reset_data:
			resetData();
			break;
		}
		return false;
	}
		
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
		if (key != null)
		{
			Log.d(TAG, "preference change: " + key);
		}
		getListView().postInvalidate();
    }
	
	private void resetData()
	{
	}
}
