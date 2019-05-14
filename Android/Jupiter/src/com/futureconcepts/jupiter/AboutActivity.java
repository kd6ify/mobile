package com.futureconcepts.jupiter;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
//	private static final String TAG = "AboutActivity";

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        PackageInfo pi = null;
        try
        {
	        pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (Exception e)
        {
        	pi = null;
        	e.printStackTrace();
        }
        addPreferencesFromResource(R.xml.about);
        if (pi != null)
        {
        	findPreference("version_name").setSummary(pi.versionName);
        	findPreference("version_code").setSummary(Integer.toString(pi.versionCode));
        }
    }
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.about_options_menu, menu);
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
		}
		return false;
	}

	public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	    return true;
    }
}
