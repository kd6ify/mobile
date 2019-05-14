package com.futureconcepts.ax.rex.activities;

import java.util.UUID;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.AddressType;
import com.futureconcepts.ax.model.data.IncidentRequest;
import com.futureconcepts.ax.rex.R;
import com.futureconcepts.ax.rex.config.Config;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

public class VerifyLocationActivity extends Activity implements LocationListener
{
	private Uri _data;
	private LocationManager _locMgr;
	private Location _location;
	private ProgressDialog _locatingDialog;
	private AlertDialog _locationDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_location_activity);
        _data = getIntent().getData();
        _locMgr = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	_locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	_locatingDialog = new ProgressDialog(this);
    	_locatingDialog.setTitle("Locating");
    	_locatingDialog.setMessage("Locating your current location...");
    	_locatingDialog.setCancelable(true);
    	_locatingDialog.setButton("Skip", new OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				skipLocationAndProceed();
			}
    	});
    	_locatingDialog.show();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	_locMgr.removeUpdates(this);
    	if (_locatingDialog != null)
    	{
    		_locatingDialog.dismiss();
    		_locatingDialog = null;
    	}
    }

	@Override
	public void onLocationChanged(Location location)
	{
		_location = new Location(location);
		if (_locatingDialog != null)
		{
			_locatingDialog.dismiss();
			_locatingDialog = null;
		}
		if (_locationDialog == null)
		{
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Location obtained");
			b.setMessage("Your location is " + _location.toString());
			b.setNegativeButton("Use", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					recordLocationAndProceed();
					dialog.dismiss();
				}
			});
			b.setNeutralButton("Try Again", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					dialog.dismiss();
					_locationDialog = null;
				}
			});
			b.setPositiveButton("Skip Location", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					skipLocationAndProceed();
					dialog.dismiss();
				}
			});
			_locationDialog = b.show();
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}
	
	private void recordLocationAndProceed()
	{
		String addressID = createAddress();
		ContentValues values = new ContentValues();
		values.put(IncidentRequest.ADDRESS, addressID);
		getContentResolver().update(_data, values, null, null);
    	Intent intent = new Intent(this, SpecifyVictimsActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
	}
	
	private String createAddress()
	{
		ContentValues values = new ContentValues();
		String addressID = UUID.randomUUID().toString();
		values.put(Address.ID, addressID);
		values.put(Address.TYPE, AddressType.MOBILE);
		Address.setWKTFromLocation(values, _location);
		getContentResolver().insert(Address.CONTENT_URI, values);
		return addressID;
	}
	
	private void skipLocationAndProceed()
	{
    	Intent intent = new Intent(this, SpecifyVictimsActivity.class);
    	intent.setData(_data);
    	startActivity(intent);
    	finish();
	}
}
