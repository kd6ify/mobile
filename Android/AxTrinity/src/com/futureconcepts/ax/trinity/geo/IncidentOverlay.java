package com.futureconcepts.ax.trinity.geo;

import android.content.Context;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Incident;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class IncidentOverlay extends ItemizedOverlay<IncidentOverlayItem>
{
	private static final String TAG = IncidentOverlay.class.getSimpleName();
	private MapView _mapView;
	private Incident _incident;
	private int[] _positionMap;
	private int _itemCount = -1;
	
	public IncidentOverlay(MapView mapView, Incident incident)
	{
		super(mapView.getContext().getResources().getDrawable(R.drawable.placemark));
		_incident = incident;
		_mapView = mapView;
		populate();
	}

	public Context getContext()
	{
		return _mapView.getContext();
	}
	
	public void repopulate()
	{
		_itemCount = -1;
		populate();
	}
	
	@Override
	protected IncidentOverlayItem createItem(int i)
	{
		int position = _positionMap[i];
		_incident.moveToPosition(position);
		IncidentOverlayItem item = new IncidentOverlayItem(getContext(), _incident);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		try
		{
			if (_itemCount == -1)
			{
				_itemCount = 0;
				int count = _incident.getCount();
				_positionMap = new int[count];
				for (int i = 0; i < count; i++)
				{
					_incident.moveToPosition(i);
					Address address = _incident.getAddress(getContext());
					if (address != null)
					{
						String wkt = address.getWKT();
						{
							if (wkt != null)
							{
								if (wkt.contains("POINT") || wkt.contains("POLYGON"))
								{
									_positionMap[_itemCount++] = i;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_itemCount = 0;
		}
		return _itemCount; 
	}
}
