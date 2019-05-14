package com.futureconcepts.ax.trinity.geo;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.view.View;

import com.google.android.maps.MapView;

public class Layer implements Closeable
{
	private MapView _mapView;
	private ArrayList<View> _childViews = new ArrayList<View>();
	
	public Layer(MapView mapView)
	{
		_mapView = mapView;
	}

	@Override
	public void close() throws IOException
	{
		for (View view : _childViews)
		{
			_mapView.removeView(view);
		}
		_childViews.clear();
	}

	public MapView getMapView()
	{
		return _mapView;
	}
	
	public Context getContext()
	{
		return _mapView.getContext();
	}
	
	protected void populate()
	{
	}

	protected boolean onTap(int i)
	{
		return true;
	}
	
	protected void addChildView(View view)
	{
		_mapView.addView(view);
		_childViews.add(view);
	}
}
