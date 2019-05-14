package com.futureconcepts.jupiter.map;

import com.futureconcepts.awt.geom.Point2D;

public class LineFeature extends Feature
{
	com.osa.android.droyd.map.LineFeature _droydLineFeature;
	
	public LineFeature()
	{
		_droydLineFeature = new com.osa.android.droyd.map.LineFeature();
		_droydFeature = _droydLineFeature;
	}
	
	public void addPoint(Point2D.Double point)
	{
		_droydLineFeature.addPoint(point.x, point.y);
	}
}
