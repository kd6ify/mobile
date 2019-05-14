package com.futureconcepts.jupiter.map;

import java.util.ArrayList;
import java.util.Collection;

import com.futureconcepts.awt.geom.Point2D;
import com.osa.android.droyd.map.Point;

public class AreaFeature extends Feature
{
	com.osa.android.droyd.map.AreaFeature _droydAreaFeature;
	
	public AreaFeature()
	{
		_droydAreaFeature = new com.osa.android.droyd.map.AreaFeature();
		_droydFeature = _droydAreaFeature;
	}
	
	public void addPoint(Point2D.Double point)
	{
		if (_droydAreaFeature != null)
		{
			_droydAreaFeature.addPoint(point.x, point.y);
		}
	}
	
	public void setPoints(Collection<Point2D.Double> points)
	{
		ArrayList<Point> droydPoints = new ArrayList<Point>();
		for (Point2D.Double point : points)
		{
			droydPoints.add(new Point(point.x, point.y));
		}
		_droydAreaFeature.setPoints(droydPoints);
	}
	
	// drawCircle -- see http://stackoverflow.com/questions/3385117/how-to-change-1-meter-to-pixel-distance
	
	public void drawCircle(Point2D.Double point, double radiusMeters)
	{
		Collection<Point> points = new ArrayList<Point>();
		double d = radiusMeters / 6378800.0d;		
		double lat1 = Math.toRadians(point.y);
		double lon1 = Math.toRadians(point.x);
		// go around a circle from 0 to 360 degrees, every 10 degrees
		for (double a = 0; a < 361.0; a+= 10.0)
		{
			double tc = Math.toRadians(a);
			double y = Math.asin(Math.sin(lat1)*Math.cos(d)+Math.cos(lat1)*Math.sin(d)*Math.cos(tc));
			double dlon = Math.atan2(Math.sin(tc)*Math.sin(d)*Math.cos(lat1),Math.cos(d)-Math.sin(lat1)*Math.sin(y));
			double x = ((lon1-dlon+Math.PI) % (2*Math.PI)) - Math.PI;
			points.add(new Point(Math.toDegrees(x), Math.toDegrees(y)));
		}
		_droydAreaFeature.setPoints(points);
	}
}
