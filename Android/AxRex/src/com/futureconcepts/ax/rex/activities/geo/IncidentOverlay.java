package com.futureconcepts.ax.rex.activities.geo;

import android.location.Location;
import android.location.LocationManager;

import com.futureconcepts.ax.rex.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class IncidentOverlay extends ItemizedOverlay<IncidentOverlayItem>
{
	private static final String TAG = IncidentOverlay.class.getSimpleName();
	private MainMapActivity _mapActivity;
	private MapView _mapView;
	private GeoPoint _geoPoint;
	
	public IncidentOverlay(MainMapActivity mapActivity, MapView mapView)
	{
		super(mapActivity.getResources().getDrawable(R.drawable.placemark));
		_mapActivity = mapActivity;
		_mapView = mapView;
		populate();
	}

	public void repopulate()
	{
		populate();
	}
	
	@Override
	protected IncidentOverlayItem createItem(int i)
	{
		IncidentOverlayItem item = new IncidentOverlayItem(_mapActivity, _geoPoint);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		if (_geoPoint != null)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public boolean onTap(GeoPoint geoPoint, MapView mapView)
	{
		mark(geoPoint);
		return true;
	}
	
	public void mark(GeoPoint geoPoint)
	{
		_geoPoint = geoPoint;
		repopulate();
		_mapActivity.onMarkSet(geoPoint);
	}
	
	public Location getLocation()
	{
		Location result = new Location(LocationManager.GPS_PROVIDER);
		result.setLatitude(_geoPoint.getLatitudeE6() / 1000000);
		result.setLongitude(_geoPoint.getLongitudeE6() / 1000000);
		return result;
	}
}
