package com.futureconcepts.mercury.main;

import com.futureconcepts.mercury.R;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class AboutActivity extends PreferenceActivity implements OnPreferenceChangeListener
{
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
			
	public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	    return true;
    }
}
