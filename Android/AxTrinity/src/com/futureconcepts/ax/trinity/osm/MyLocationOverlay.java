package com.futureconcepts.ax.trinity.osm;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.overlay.Marker;
import org.mapsforge.map.model.MapViewPosition;

public class MyLocationOverlay extends Layer implements LocationListener,MyLocationNotifier
{
	  private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;
	  private  int UPDATE_DISTANCE = 50;
	  private  int UPDATE_INTERVAL = 60*1000;
	  private boolean centerAtNextFix;
	  private final Circle circle;
	  private Location lastLocation;
	  private final LocationManager locationManager;
	  private final MapViewPosition mapViewPosition;
	  private final Marker marker;
	  private boolean myLocationEnabled;
	  private boolean snapToLocationEnabled;
	  private List<MyLocationObserver> listeners;
	  
	  public static LatLong locationToLatLong(Location location)
	  {
	    return new LatLong(location.getLatitude(), location.getLongitude());
	  }
	  
	  private static Paint getDefaultCircleFill()
	  {
	    return getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
	  }
	  
	  private static Paint getDefaultCircleStroke()
	  {
	    return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
	  }
	  
	  private static Paint getPaint(int color, int strokeWidth, Style style)
	  {
	    Paint paint = GRAPHIC_FACTORY.createPaint();
	    paint.setColor(color);
	    paint.setStrokeWidth(strokeWidth);
	    paint.setStyle(style);
	    return paint;
	  }
	  
	  public MyLocationOverlay(Context context, MapViewPosition mapViewPosition, Bitmap bitmap)
	  {
	    this(context, mapViewPosition, bitmap, getDefaultCircleFill(), getDefaultCircleStroke());
	  }
	  
	  public MyLocationOverlay(Context context, MapViewPosition mapViewPosition, Bitmap bitmap,
			  Paint circleFill, Paint circleStroke)
	  {
		  listeners = new ArrayList<MyLocationObserver>();
	    this.mapViewPosition = mapViewPosition;
	    this.locationManager = ((LocationManager)context.getSystemService("location"));
	    this.marker = new Marker(null, bitmap, 0, 0);
	    this.circle = new Circle(null, 0.0F, circleFill, circleStroke);
	  }
	  
	  public synchronized void disableMyLocation()
	  {
	    if (this.myLocationEnabled)
	    {
	      this.myLocationEnabled = false;
	      this.locationManager.removeUpdates(this);
	    }
	  }
	  
	  public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint)
	  {
	    if (!this.myLocationEnabled) {
	      return;
	    }
	    this.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	    this.marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
	  }
	  
	  public synchronized boolean enableMyLocation(boolean centerAtFirstFix)
	  {
	    if (!enableBestAvailableProvider()) {
	      return false;
	    }
	    this.centerAtNextFix = centerAtFirstFix;
	    this.circle.setDisplayModel(this.displayModel);
	    this.marker.setDisplayModel(this.displayModel);
	    return true;
	  }
	  
	  public synchronized Location getLastLocation()
	  {
	    return this.lastLocation;
	  }
	  
	  public synchronized boolean isCenterAtNextFix()
	  {
	    return this.centerAtNextFix;
	  }
	  
	  public synchronized boolean isMyLocationEnabled()
	  {
	    return this.myLocationEnabled;
	  }
	  
	  public synchronized boolean isSnapToLocationEnabled()
	  {
	    return this.snapToLocationEnabled;
	  }
	  
	public  int getUpdateDistance() {
		return UPDATE_DISTANCE;
	}

	public  int getUpdateInterval() {
		return UPDATE_INTERVAL;
	}
	
	public  void setUpdateDistanceAndInterval(int distance, int interval)
	{
		this.UPDATE_DISTANCE = distance;
		this.UPDATE_INTERVAL = interval;
		//enableBestAvailableProvider();
	}

	public void onDestroy()
	  {
	    this.marker.onDestroy();
	  }
	  
	  public void onLocationChanged(Location location)
	  {
	    synchronized (this)
	    {
	    	this.lastLocation = location;
	    	notifyMyLocationHasChange();
	    	LatLong latLong = locationToLatLong(location);
	    	this.marker.setLatLong(latLong);
	    	this.circle.setLatLong(latLong);
	    	if (location.getAccuracy() != 0.0F) {
	    		this.circle.setRadius(location.getAccuracy());
	    	} else {
	    		this.circle.setRadius(40.0F);
	    	}
	    	if ((this.centerAtNextFix) || (this.snapToLocationEnabled))
	    	{
	    		this.centerAtNextFix = false;
	    		this.mapViewPosition.setCenter(latLong);
	    	}
	    	requestRedraw();
	    }
	  }
	  
	  public void onProviderDisabled(String provider)
	  {
	    enableBestAvailableProvider();
	  }
	  
	  public void onProviderEnabled(String provider)
	  {
	    enableBestAvailableProvider();
	  }
	  
	  public void onStatusChanged(String provider, int status, Bundle extras) {}
	  
	  public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled)
	  {
	    this.snapToLocationEnabled = snapToLocationEnabled;
	  }
	  
	  private synchronized boolean enableBestAvailableProvider()
	  {
	    disableMyLocation();
	    
//	    Criteria criteria = new Criteria();
//	    criteria.setAccuracy(1);
//	    String bestAvailableProvider = this.locationManager.getBestProvider(criteria, true);
//	    if (bestAvailableProvider == null) {
//	      return false;
//	    }
	    this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
	    this.myLocationEnabled = true;
	    return true;
	  }

	@Override
	public void registerListener(MyLocationObserver listener) {
		// Adds a new listener to the ArrayList
		listeners.add(listener);
	}

	@Override
	public void unRegisterListener(MyLocationObserver deleteListener) {
		if(deleteListener!=null){
			// Get the index of the observer to delete
			int listenerIndex = listeners.indexOf(deleteListener);
			// Print out message (Have to increment index to match)
			System.out.println("Observer " + (listenerIndex + 1) + " deleted");
			// Removes observer from the ArrayList		
			listeners.remove(listenerIndex);
		}
	}

	@Override
	public void notifyMyLocationHasChange() {
		// Cycle through all observers and notifies them of
		for (MyLocationObserver listener : listeners) {
			listener.locationHasChange(this.snapToLocationEnabled, this.lastLocation);
		}

	}
}
