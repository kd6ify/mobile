package com.futureconcepts.jupiter.map;

import android.net.Uri;

import com.futureconcepts.awt.geom.Point2D;

public class PointFeature extends Feature
{
	com.osa.android.droyd.map.PointFeature _droydPointFeature;
	private Uri _uri; 
	
	public PointFeature()
	{
		_droydPointFeature = new com.osa.android.droyd.map.PointFeature();
		_droydFeature = _droydPointFeature;
	}
	
	public void setPoint(Point2D.Double point)
	{
		_droydPointFeature.setPoint(point.x, point.y);
	}
	
	public Point2D.Double getPoint()
	{
		Point2D.Double value = new Point2D.Double();
		value.x = _droydPointFeature.getX();
		value.y = _droydPointFeature.getY();
		return value;
	}
	
	public void setUri(Uri uri)
	{
		_uri = uri;
	}
	
	public Uri getUri()
	{
		return _uri;
	}
}
