package com.futureconcepts.ax.trinity.geo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.futureconcepts.ax.model.data.Address;
import com.futureconcepts.ax.model.data.Tactic;
import com.futureconcepts.ax.trinity.R;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class TaskOverlay extends ItemizedOverlay<TaskOverlayItem>
{
	private static final String TAG = TaskOverlay.class.getSimpleName();
	private MapView _mapView;
	private Tactic _tactic;
	private int[] _positionMap;
	private int _itemCount = -1;
	
	public TaskOverlay(MapView mapView, Tactic tactic)
	{
		super(mapView.getContext().getResources().getDrawable(R.drawable.placemark));
		_mapView = mapView;
		_tactic = tactic;
		populate();
	}

	public Context getContext()
	{
		return _mapView.getContext();
	}
	
	public void repopulate()
	{
		populate();
	}
	
	@Override
	protected TaskOverlayItem createItem(int i)
	{
		int position = _positionMap[i];
		_tactic.moveToPosition(position);
		TaskOverlayItem item = new TaskOverlayItem(getContext(), _tactic);
		boundCenterBottom(item.getMarker(1));
		return item;
	}

	@Override
	public int size()
	{
		if (_itemCount == -1)
		{
			_itemCount = 0;
			int count = _tactic.getCount();
			_positionMap = new int[count];
			for (int i = 0; i < count; i++)
			{
				_tactic.moveToPosition(i);
				Address address = _tactic.getAddress(getContext());
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
		return _itemCount; 
	}
	
	@Override
	public boolean onTap(int i)
	{
		int position = _positionMap[i];
		_tactic.moveToPosition(position);
		if (getContext() != null)
		{
			Uri uri = Uri.withAppendedPath(Tactic.CONTENT_URI, _tactic.getID());
			Log.d(TAG, "tapped on " + uri.toString());
			getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
		return true;
	}
}
