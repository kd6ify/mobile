package com.futureconcepts.ax.trinity.geo;

import java.io.Closeable;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Triage;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class TriageOverlay extends ItemizedOverlay<TriageOverlayItem> implements Closeable
{
	private MapView _mapView;
	private Triage _triage;
	private int[] _positionMap;
	private int _itemCount = -1;
	
	public TriageOverlay(MapView mapView, Triage triage)
	{
		super(mapView.getContext().getResources().getDrawable(R.drawable.triage_icon));
		_mapView = mapView;
		_triage = triage;
		populate();
	}

	@Override
	public void close() throws IOException
	{
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
	protected TriageOverlayItem createItem(int i)
	{
		int position = _positionMap[i];
		_triage.moveToPosition(position);
		TriageOverlayItem item = new TriageOverlayItem(getContext(), _triage);
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
				int count = _triage.getCount();
				_positionMap = new int[count];
				for (int i = 0; i < count; i++)
				{
					_triage.moveToPosition(i);
					Address address = _triage.getAddress(getContext());
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

	@Override
	public boolean onTap(int i)
	{
		_triage.moveToPosition(_positionMap[i]);
		getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(Triage.CONTENT_URI, _triage.getID())));
		return true;
	}
}
