package com.futureconcepts.jupiter.layers;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.futureconcepts.awt.geom.Point2D;
import com.futureconcepts.jupiter.map.MapComponent;

import com.futureconcepts.jupiter.map.AreaFeature;
import com.futureconcepts.jupiter.map.PointFeature;

public class MyLocationLayer extends MapComponent.Layer implements LocationListener
{
	public static final String ID = "f855a2de-0075-490a-a133-17464bc93037";
	private static final String ACCURACY_ID = "2e2a8343-183e-4180-9e3f-5815808e8866";
    private Context _context;
	private PointFeature _pointFeature;
	private ThreadPoolExecutor _threadPool;
	private AreaFeature _accuracyCircleFeature;
	
	public MyLocationLayer(Context context, MapComponent component)
	{
		component.super();
		_id = ID;
		_context = context;
		_threadPool = new ThreadPoolExecutor(3, 5, 2000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public String getName()
	{
		return "My Location"; 
	}
	
	@Override
	public int getIconId()
	{
		return android.R.drawable.ic_menu_mylocation;
	}
	
	@Override
	public void load()
	{
		super.load();
		StyleManager styleManager = StyleManager.instance(_context);
	    postAddLayer(styleManager.getStyle(StyleManager.STYLE_MY_LOCATION));
	    getMapComponent().addLayerById(ACCURACY_ID, null, styleManager.getStyle(StyleManager.STYLE_ACCURACY_CIRCLE));
	}
	
	@Override
	public void close() throws IOException
	{
		if (_pointFeature != null)
		{
			getMapComponent().removeFeature(_pointFeature);
			_pointFeature = null;
		}
		if (_accuracyCircleFeature != null)
		{
			getMapComponent().removeFeature(_accuracyCircleFeature);
		}
		getMapComponent().removeLayerById(ACCURACY_ID);
		super.close();
	}

	@Override
	public void setEnabled(boolean value)
	{
		getMapComponent().enableLayer(ACCURACY_ID, value);
		super.setEnabled(value);
	}
	
	@Override
	public void onLocationChanged(final Location location)
	{
		_threadPool.execute(new Runnable() {
			@Override
			public void run() {
				try
				{
					postLocation(location);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
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

	private void postLocation(final Location location)
	{
		Point2D.Double point = new Point2D.Double(location.getLongitude(), location.getLatitude());
		if (location.hasAccuracy())
		{
			if (_accuracyCircleFeature == null)
			{
				_accuracyCircleFeature = new AreaFeature();
				postAddFeature(ACCURACY_ID, _accuracyCircleFeature);
			}
			_accuracyCircleFeature.drawCircle(point, location.getAccuracy() / 2.0d);
		}
		if (_pointFeature == null)
		{
		    _pointFeature = new PointFeature();
		    _pointFeature.setLabel(String.format("%f,%f", point.x, point.y));
		    _pointFeature.setPoint(point);
		    postAddFeature(_id, _pointFeature);
		}
		else
		{
			_pointFeature.setPoint(point);
		}
	}
}
