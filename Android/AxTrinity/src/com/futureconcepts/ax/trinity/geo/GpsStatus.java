package com.futureconcepts.ax.trinity.geo;

import com.futureconcepts.ax.trinity.R;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.widget.TextView;

public class GpsStatus extends Activity implements LocationListener
{
	private LocationManager mLocMgr;
	
	private TextView mTvStatus;
	
	private Location mCurrentLocation;
	
	private int mStatusChanges = 0;
	
	private int mLocationChanges = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_location);
		mLocMgr = (LocationManager)getSystemService(LOCATION_SERVICE);
		mTvStatus = (TextView)findViewById(R.id.status);
		Location lastKnownLocation = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (lastKnownLocation != null)
		{
			onLocationChanged(lastKnownLocation);
		}
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	if (mLocMgr != null)
    	{
    		mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    	}
    }

    @Override
    public void onStop()
    {
    	super.onStop();
    	if (mLocMgr != null)
    	{
    		mLocMgr.removeUpdates(this);
    	}
    }

	public void onLocationChanged(Location location)
	{
		mLocationChanges++;
		TextView tvLocationChanges = (TextView)findViewById(R.id.location_changes);
		tvLocationChanges.setText(Integer.toString(mLocationChanges));
		if (mCurrentLocation == null)
		{
			mCurrentLocation = new Location(location);
		}
		else
		{
			mCurrentLocation.set(location);
		}
		TextView tvLatitude = (TextView)findViewById(R.id.latitude);
		tvLatitude.setText(Double.toString(location.getLatitude()));
		TextView tvLongitude = (TextView)findViewById(R.id.longitude);
		tvLongitude.setText(Double.toString(location.getLongitude()));
		TextView tvProvider = (TextView)findViewById(R.id.provider);
		if (location.getProvider() != null)
		{
			tvProvider.setText(location.getProvider());
		}
		else
		{
			tvProvider.setText("(null)");
		}
		TextView tvAccuracy = (TextView)findViewById(R.id.accuracy);
		if (location.hasAccuracy())
		{
			tvAccuracy.setText(String.format("%f meters", location.getAccuracy()));
		}
		else
		{
			tvAccuracy.setText("not specified");
		}
		TextView tvAltitude = (TextView)findViewById(R.id.altitude);
		if (location.hasAltitude())
		{
			tvAltitude.setText(String.format("%f meters", location.getAltitude()));
		}
		else
		{
			tvAltitude.setText("not specified");
		}
		TextView tvBearing = (TextView)findViewById(R.id.bearing);
		if (location.hasBearing())
		{
			tvBearing.setText(String.format("%f degrees", location.getBearing()));
		}
		else
		{
			tvBearing.setText("not specified");
		}
		TextView tvSpeed = (TextView)findViewById(R.id.speed);
		if (location.hasSpeed())
		{
			tvSpeed.setText(String.format("%f meters/sec", location.getSpeed()));
		}
		else
		{
			tvSpeed.setText("not specified");
		}
	}

	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		mStatusChanges++;
		TextView tvStatusChanges = (TextView)findViewById(R.id.status_changes);
		tvStatusChanges.setText(Integer.toString(mStatusChanges));
		String statusString = "UNKNOWN";
		switch (status)
		{
		case LocationProvider.AVAILABLE:
			statusString += "AVAILABLE ";
			if (extras.containsKey("satellites"))
			{
				int numSats = extras.getInt("satellites");
				statusString += "(" + Integer.toString(numSats) + " sats)";
				Location locationNow = this.mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (locationNow != null)
				{
					if (mCurrentLocation != null)
					{
						if ((mCurrentLocation.getLatitude() != locationNow.getLatitude()) || mCurrentLocation.getLongitude() != locationNow.getLongitude())
						{
							onLocationChanged(locationNow);
						}
					}
					else
					{
						onLocationChanged(locationNow);
					}
				}
			}
			break;
		case LocationProvider.OUT_OF_SERVICE:
			statusString = "OUT_OF_SERVICE";
			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			statusString = "TEMPORARILY_UNAVAILABLE";
			break;
		}
		mTvStatus.setText(statusString);
	}
}
