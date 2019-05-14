package com.futureconcepts.mercury.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

import com.futureconcepts.mercury.Config;
import com.futureconcepts.mercury.R;

public class ToggleActivity extends Activity
{
	private static final String TAG = "tracker.ToggleActivity";
	private Config _config;
	private AlertDialog gpsAlert = null;
	
	@Override
    public void onCreate(Bundle icicle)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(icicle);
		_config = Config.getInstance(this);
    	setContentView(R.layout.tracker_toggle);
    	ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
    	toggleButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				toggleClick(v);
            }
    	});
    	Button cancelButton = (Button)findViewById(R.id.cancel_button);
    	cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
            {
				finish();
            }
    	});
    	if (_config.getTrackerEnabled())
    	{
    		toggleButton.setChecked(true);
    	}
    	else
    	{
    		toggleButton.setChecked(false);
    	}
	}
	
	@Override
    public void onResume()
	{
		super.onResume();
		if(!GPS.isGPSEnabled(getApplicationContext()))
		{
			ToggleButton toggleButton = (ToggleButton)findViewById(R.id.toggle_button);
			toggleButton.setChecked(false);
			buildAlertMessageNoGps();
	    	_config.setTrackerEnabled(toggleButton.isChecked());
		}
	}	
	
	private void toggleClick(View v)
    {
		if (!GPS.isGPSEnabled(getApplicationContext())) {
			buildAlertMessageNoGps();
	        ToggleButton toggleButton = (ToggleButton)v;
	        toggleButton.setChecked(false);
	    }else{
	    	ToggleButton toggleButton = (ToggleButton)v;
	    	_config.setTrackerEnabled(toggleButton.isChecked());
	    	finish();
	    }
    }
	
	 private void buildAlertMessageNoGps() {
		 if(gpsAlert==null){
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    builder.setMessage("Your GPS is disabled, do you want to enable it?")
		           .setCancelable(false)
		           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		            	   gpsAlert=null;
		            	   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
		               }
		           })
		           .setNegativeButton("No", new DialogInterface.OnClickListener() {
		               public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
		                    dialog.cancel();
		                    gpsAlert =null;
		               }
		           });
		     gpsAlert = builder.create();
		     gpsAlert.show();
		 }
		}
	

}
