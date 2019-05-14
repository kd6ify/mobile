package com.futureconcepts.ax.trinity.main;

import com.futureconcepts.ax.trinity.R;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

//import com.futureconcepts.trinity.R;

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
        setTheme(R.style.WhiteText);
        if (pi != null)
        {
        	boolean isLiteVersion = false;
        	if(isLiteVersion)
        	{
        		findPreference("version_name").setSummary("1.0.0.0");
        		findPreference("version_code").setSummary("1.0.0.0");
        	}else{
        		findPreference("version_name").setSummary(pi.versionName);
        		findPreference("version_code").setSummary(Integer.toString(pi.versionCode));
        	}
        }
    }
			
	public boolean onPreferenceChange(Preference preference, Object newValue)
    {
	    return true;
    }
}
