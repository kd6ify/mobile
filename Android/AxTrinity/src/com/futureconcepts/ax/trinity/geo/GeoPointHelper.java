package com.futureconcepts.ax.trinity.geo;

import android.graphics.Point;

import com.futureconcepts.ax.model.data.Address;
import com.google.android.maps.GeoPoint;

public class GeoPointHelper
{
	public static GeoPoint getGeoPoint(Point point)
	{
		GeoPoint result = null;
		if (point != null)
		{
			result = new GeoPoint(point.y, point.x);
		}
		if (result == null)
		{
			result = new GeoPoint(0, 0);
		}
		return result;
	}
	public static GeoPoint getGeoPoint(Address address)
	{
		return getGeoPoint(address.getWKTAsPoint());
	}
}
