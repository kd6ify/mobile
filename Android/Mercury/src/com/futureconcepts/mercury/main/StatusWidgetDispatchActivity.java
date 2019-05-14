package com.futureconcepts.mercury.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StatusWidgetDispatchActivity extends Activity
{
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		Intent launcherIntent = null;
		boolean trinityInstalled = false;
		try
		{
			getPackageManager().getApplicationIcon("com.futureconcepts.trinity");
			trinityInstalled = true;
		}
		catch (Exception e)
		{
		}
		if (trinityInstalled)
		{
			
			launcherIntent = new Intent();
			launcherIntent.setClassName("com.futureconcepts.trinity", "com.futureconcepts.trinity.main.Launcher");
		}
		else
		{
			launcherIntent = new Intent(this, SettingsActivity.class);
		}
		startActivity(launcherIntent);
		finish();
    }
}
