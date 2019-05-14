package com.futureconcepts.ax.trinity.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.futureconcepts.ax.trinity.R;
import com.futureconcepts.gqueue.MercurySettings;

public class Prefs extends PreferenceActivity implements OnPreferenceChangeListener
{
	private static final String TAG = "trinity.main.Prefs";
	private static final int SUBACTIVITY_TONE_PICKER = 1;

	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
			
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.prefs_options_menu, menu);
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
		Log.d(TAG, "onOptionsItemSelected");
		switch (item.getItemId())
		{
		case R.id.menu_select_alert_tone:
			startRingtonePicker();
			break;
		case R.id.menu_restart:
			Intent intent = new Intent("com.futureconcepts.action.START_SERVICES");
			sendBroadcast(intent, null);
			break;
		case R.id.menu_check_update:
			checkUpdate();
			break;
		}
		return false;
	}
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == SUBACTIVITY_TONE_PICKER)
			{
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String toneUriString = uri.toString();
//				MercurySettings.setAlertToneUri(this, toneUriString);
			}
		}
	}

	private void startRingtonePicker()
	{
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER); 
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
		startActivityForResult(intent, SUBACTIVITY_TONE_PICKER);
	}
	
	private void checkUpdate()
	{
		Log.d(TAG, "checkUpdate");
		try
		{
			PackageManager packageManager = (PackageManager)getPackageManager();
			PackageInfo currentInfo = packageManager.getPackageInfo(getPackageName(), 0);
			Intent intent = new Intent("com.futureconcepts.mercury.intent.action.CHECK_UPDATE");
			intent.putExtra("com.futureconcepts.mercury.intent.extra.PACKAGE_NAME", getPackageName());
			intent.putExtra("com.futureconcepts.mercury.intent.extra.VERSION_CODE", currentInfo.versionCode);
			sendBroadcast(intent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}
}
